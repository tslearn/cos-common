package org.companyos.dev.cos_common.object_tree;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

@SuppressWarnings("serial")
public class OTWebSocketServlet extends WebSocketServlet
{
  @Override
  public void configure(WebSocketServletFactory factory)
  {
    factory.register(OTWebSocketHandler.class);
  }
}