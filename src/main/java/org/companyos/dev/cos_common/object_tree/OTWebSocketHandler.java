package org.companyos.dev.cos_common.object_tree;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.json.JSONArray;
@WebSocket
public class OTWebSocketHandler extends WebSocketHandler {
  private OTSocketSlot param;
  
  @OnWebSocketConnect
  public void onConnect(Session session) {
    this.param = new OTSocketSlot(session);
    System.out.println("connected");
  }
  
  @OnWebSocketClose
  public void onClose(int statusCode, String reason) {
    OT.User.unregister(this.param.getUid()); 
    this.param = null; 
    System.out.println("disconnected");
  }

  @OnWebSocketError
  public void onError(Throwable t) {
    return;
  }
  
  @OnWebSocketMessage
  public void onMessage(String message) {
    String callback = ">";
    boolean needFreeThread = false;
    
    try {      
      int p1 = message.indexOf("@");
      int p2 = message.indexOf("<");
      int p3 = message.indexOf(">");
      
      if (p1 <= 0 || p1 > p2 || p2 > p3) { 
       this.param.sendString(callback, OTReturn.getSysError().setM("OT server receive data format error, received: " + message)); 
       return;
      }
      
      String target = message.substring(0, p1);
      String msg = message.substring(p1+1, p2);
      callback = message.substring(p2+1, p3+1);
      
      OTNode tNode = OT.Runtime.getNodeByPath(target);
      
      if (tNode == null) {
        this.param.sendString(callback, OTReturn.getSysError().setM("OT server eval error! target not found, target: " + target)); 
        return;
      }
      
      String args = message.substring(p3+1);  
      args = (args == null || args.length() == 0) ? "[]" : args;
      
      JSONArray json = new JSONArray(args);        
      Object[] passArgs = new Object[json.length()];
      
      for (int i = 0; i < json.length(); i++) {
        passArgs[i] = json.get(i);
      }
      
      OTThread th = OTThread.currentThread();

      if (th == null) {
        th = OTThread.initExternal();
        needFreeThread = true;
      }
      
      OT.User.setParam(this.param);
      OTReturn ret = OT.Message.evalMsg(tNode, msg, passArgs); 
      
      if (this.param != null) {
        this.param.sendString(callback , ret);
      }

      return;
    }
    catch (Exception e) {
      if (this.param != null) {
        this.param.sendString(callback, OTReturn.getSysError().setM("OT server eval exception").setE(e)); 
      }

      return;
    }
    finally {  
      if (needFreeThread) {
        OTThread.stopExternal();
      }
      OT.User.setParam(null);
    }
  }


  @Override
  public void configure(WebSocketServletFactory factory) {
    factory.register(this.getClass());
  }
}