package org.companyos.dev.cos_common.object_tree;

import java.net.InetSocketAddress;

import org.companyos.dev.cos_common.CCThread;
import org.eclipse.jetty.server.Server;

public class OTWebSocketServer {
  private Server websocketServer = null;
  private int port = 0; 
  private String hostname = null;

  public OTWebSocketServer(String hostname, int port) {
    if (port <= 0 || port > 65535) {
      OT.Log.logSysError("OTWebSocketServer port " + port + " is illegal!");
    }
    
    this.hostname = hostname;
    this.port = port;
  }
  
  public boolean start() {
    try {
      if (this.hostname != null) {
        this.websocketServer = new Server(new InetSocketAddress(this.hostname, this.port));
      }
      else {
        this.websocketServer = new Server(this.port);
      }
      this.websocketServer.setHandler(new OTWebSocketHandler());
      this.websocketServer.setStopTimeout(0);
      this.websocketServer.start();
      return true;
    }
    catch (Exception e) {
      OT.Log.logSysError("Websocket server start error!  " + e);
      this.websocketServer = null;
      return false;
    }
  }

  public boolean stop() {
    try {
      this.websocketServer.stop();
      CCThread.trySleepMS(2000);
      return true;
    }
    catch (Exception e) {
      OT.Log.logSysError("Websocket server stop error! " + e);
      return false;
    }
  }
}