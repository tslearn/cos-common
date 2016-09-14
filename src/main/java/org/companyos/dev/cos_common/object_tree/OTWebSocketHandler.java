package org.companyos.dev.cos_common.object_tree;

import java.util.UUID;

import org.companyos.dev.cos_common.CCErrorManager;
import org.companyos.dev.cos_common.CCReturn;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.json.JSONArray;
import org.json.JSONObject;


@WebSocket
public class OTWebSocketHandler extends WebSocketHandler {
  private static final long ClientBack = 1;
  private static final long ServerBack = 2;

  private Session session;
  private String wsSession = UUID.randomUUID().toString();
  private long uid = 0;
  private String ip = null;

  String getIp() {
    return this.ip;
  }

  long getUid() {
    return this.uid;
  }

  boolean setUid(long uid) {
    if (this.uid == 0 || uid == 0) {
      this.uid = uid;
      return true;
    }
    else {
      return false;
    }
  }


  boolean response(long callback, CCReturn<?> ret) {
    JSONObject r = null;

    if (ret == null) {
      r = new JSONObject();
    }
    else {
      r = ret.toJSON();
    }

    r.put("c", callback);
    r.put("t", ClientBack);
    if (!ret.isSuccess()) {
      if (ret.getM() == null) {
        r.put("m", CCErrorManager.getErrorByCode(ret.getStatus()).getMessage());
      }
    }
    return _send(r.toString());
  }

  boolean send(String message, Object value) {
    JSONObject r = new JSONObject();
    r.put("c", 0);
    r.put("t", ServerBack);
    r.put("m", message);
    r.put("v", value);
    return _send(r.toString());
  }

  private  boolean _send(String text) {
    OT.info("Send back to client: " + text);
    try {
      this.session.getRemote().sendString(text);
      return true;
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public void close() {
    this.session.close();
  }

  @OnWebSocketConnect
  public void onConnect(Session session) {
    this.ip = session.getRemoteAddress().getHostName().toString();
    this.session = session;
    OT.$registerWebSocketSession(this.wsSession, this);
    this.send("WebSocket:open", this.wsSession);
    OT.info("WebSocket connected! session id: " + this.wsSession, true);
  }

  @OnWebSocketClose
  public void onClose(int statusCode, String reason) {
    OT.$unregisterWebSocketSession(this.wsSession);
    this.session = null;
    OT.info("disconnected! session id: " + this.wsSession);
  }

  @OnWebSocketError
  public void onError(Throwable t) {
    OT.warn(t.toString());
  }

  @OnWebSocketMessage
  public void onMessage(String message) {
    JSONObject client = new JSONObject(message);

    String target = client.getString("t");
    String msg = client.getString("m");
    long callback = client.getLong("c");

    if (callback <= 0) {
      this.send("OTServer:error", "OT server message callback error: [" + callback + "]");
      return;
    }

    JSONArray args = client.getJSONArray("a");
    if (args == null) {
      args = new JSONArray();
    }

    Object[] passArgs = new Object[args.length()];

    for (int i = 0; i < args.length(); i++) {
      Object v = args.get(i);
      if (v instanceof Integer)
        passArgs[i] = Long.parseLong(v.toString());
      else
        passArgs[i] = v;
    }

    OT.postMsgWithWebSocket(callback, this.wsSession, uid, target, msg, passArgs);
  }

  @Override
  public void configure(WebSocketServletFactory factory) {
    factory.register(this.getClass());
  }
}