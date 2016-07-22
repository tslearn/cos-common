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
	  JSONObject r = ret.toJSON();
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
  
  private boolean send(String text) {
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
    boolean needFreeThread = false;
    
    try { 
        JSONObject client = new JSONObject(message);
 
        String target = client.getString("t");  
        String msg = client.getString("m");  
        long callback = client.getLong("c");
        
        if (callback <= 0) {
           	this.send(CCReturn.error("OT server message callback error: [" + callback + "]")); 
           	return;
        }
              
        OTNode tNode = OT.getNodeByPath(target);
      
        if (tNode == null) {
        	this.response(callback, CCReturn.error("OT server message target error: " + target)); 
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
	      
	    OTThread th = OTThread.currentThread();

		if (th == null) {
	        th = OTThread.startMessageService();
	        needFreeThread = true;
		}
      
		OT.putKeyIfAbsent("securty", this.security);
		
		CCReturn<?> ret = OT.evalMsg(tNode, msg, passArgs);
		this.response(callback, ret);
		return;
    }
    catch (Exception e) {
        this.send(CCReturn.error("OT server eval message exception").setE(e)); 
        return;
    }
    finally {  
      if (needFreeThread) {
        OTThread.stopMessageService();
      }
      OT.clearAllKeys();
    }
  }

  @Override
  public void configure(WebSocketServletFactory factory) {
    factory.register(this.getClass());
  }
}