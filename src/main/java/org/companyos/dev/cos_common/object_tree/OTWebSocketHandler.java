package org.companyos.dev.cos_common.object_tree;

import java.util.UUID;

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
  private String security = UUID.randomUUID().toString();

  public boolean response(long callback, CCReturn<?> ret) {
    JSONObject r = null;

    if (ret == null) {
      r = new JSONObject();
    }
    else {
      r = ret.toJSON();
    }

    r.put("c", callback);
    r.put("t", ClientBack);
    return send(r.toString());
  }

  public boolean send(CCReturn<?> ret) {
    JSONObject r = ret.toJSON();
    r.put("c", 0);
    r.put("t", ServerBack);
    return send(r.toString());
  }

  private synchronized boolean send(String text) {
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
    this.session = session;
    OT.$registerWebSocketHandler(security, this);
    OT.info("websock connected! security: " + this.security, true);
  }

  @OnWebSocketClose
  public void onClose(int statusCode, String reason) {
    OT.$unregisterWebSocketHandler(security);
    this.session = null;
    OT.info("disconnected! security: " + this.security);
  }

  @OnWebSocketError
  public void onError(Throwable t) {

  }

  @OnWebSocketMessage
  public void onMessage(String message) {
    JSONObject client = new JSONObject(message);

    String target = client.getString("t");
    String msg = client.getString("m");
    long callback = client.getLong("c");

    if (callback <= 0) {
      this.send(CCReturn.error("OT server message callback error: [" + callback + "]"));
      return;
    }

    JSONArray args = client.getJSONArray("a");
    if (args == null) {
      args = new JSONArray();
    }

    Object[] passArgs = new Object[args.length()];

    for (int i = 0; i < args.length(); i++) {
      passArgs[i] = args.get(i);
    }

    OT.postMsgWithWebSocket(callback, security, target, msg, passArgs);

    return;
  }

  @Override
  public void configure(WebSocketServletFactory factory) {
    factory.register(this.getClass());
  }
}