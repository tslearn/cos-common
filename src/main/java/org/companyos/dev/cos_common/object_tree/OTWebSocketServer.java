package org.companyos.dev.cos_common.object_tree;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.servlet.ServletHolder;

public class OTWebSocketServer {
  private Server websocketServer = null;
  private int wsPort = 0;
  private int wssPort = 0;
  private String hostname = null;

  public OTWebSocketServer(String hostname, int wsPort, int wssPort) {
    if (wsPort <= 0 || wsPort > 65535) {
    	OT.$error("OTWebSocketServer ws Port " + wsPort + " is illegal!");
    	return;
    }

    if (wssPort <= 0 || wssPort > 65535) {
      OT.$error("OTWebSocketServer wss Port " + wssPort + " is illegal!");
      return;
    }

    this.hostname = hostname;
    this.wsPort = wsPort;
    this.wssPort = wssPort;
  }
  
  public boolean start(
      int webSocketThreadPoolSize,
      String keystorePath,
      String keystorePassword,
      String keyManagerPassword) {
    try {
      QueuedThreadPool threadPool = new QueuedThreadPool();
      threadPool.setMaxThreads(webSocketThreadPoolSize);
      this.websocketServer = new Server(threadPool);

      // add ws connect
      ServerConnector wsConnector = new ServerConnector(this.websocketServer);
      if (this.hostname != null) {
        wsConnector.setHost(this.hostname);
      }
      wsConnector.setPort(this.wsPort);
      this.websocketServer.addConnector(wsConnector);


      if (this.wssPort > 0 && keystorePath != null && keystorePath.length() > 0) {
        // connector configuration
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStoreResource(Resource.newClassPathResource(keystorePath));
        sslContextFactory.setKeyStorePassword(keystorePassword);
        sslContextFactory.setKeyManagerPassword(keyManagerPassword);
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString());
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(new HttpConfiguration());
        ServerConnector wssConnector = new ServerConnector(this.websocketServer, sslConnectionFactory, httpConnectionFactory);
        if (this.hostname != null) {
          wssConnector.setHost(this.hostname);
        }

        wssConnector.setPort(this.wssPort);
        this.websocketServer.addConnector(wssConnector);
      }

      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
      context.setContextPath("/");
      this.websocketServer.setHandler(context);

      ServletHolder holderEvents = new ServletHolder("ws-ot", OTWebSocketServlet.class);
      context.addServlet(holderEvents, "/ot/*");

      this.websocketServer.setStopTimeout(0);
      this.websocketServer.start();
      return true;
    }
    catch (Exception e) {
      e.printStackTrace();
      OT.$error("Websocket server start error!  " + e, false);
      this.websocketServer = null;
      return false;
    }
  }

  public boolean stop() {
    try {
      if (this.websocketServer != null) {
        this.websocketServer.stop();
        return true;
      }
      else {
        return false;
      }
    }
    catch (Exception e) {
      OT.$error("Websocket server stop error! " + e, false);
      return false;
    }
  }
}