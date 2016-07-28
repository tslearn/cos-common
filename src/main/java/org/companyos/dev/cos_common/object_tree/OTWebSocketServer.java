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
  private int port = 0; 
  private String hostname = null;

  public OTWebSocketServer(String hostname, int port) {
    if (port <= 0 || port > 65535) {
    	OT.$error("OTWebSocketServer port " + port + " is illegal!");
    	return;
    }
    
    this.hostname = hostname;
    this.port = port;
  }
  
  public boolean start() {
    try {
      QueuedThreadPool threadPool = new QueuedThreadPool();
      threadPool.setMaxThreads(OTConfig.JettyWebSocketThreadPoolSize);

      this.websocketServer = new Server(threadPool);

      // connector configuration
      SslContextFactory sslContextFactory = new SslContextFactory();
      sslContextFactory.setKeyStoreResource(Resource.newClassPathResource(OTConfig.KeyStorePath));
      sslContextFactory.setKeyStorePassword(OTConfig.KeyStorePassword);
      sslContextFactory.setKeyManagerPassword(OTConfig.KeyManagerPassword);
      SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString());
      HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(new HttpConfiguration());

      ServerConnector connector = new ServerConnector(this.websocketServer, sslConnectionFactory, httpConnectionFactory);
      if (this.hostname != null) {
        connector.setHost(this.hostname);
      }

      connector.setPort(this.port);
      this.websocketServer.addConnector(connector);

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