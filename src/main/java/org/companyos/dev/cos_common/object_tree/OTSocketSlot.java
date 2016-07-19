/**
 * Project Name:nimobile-server
 * File Name:OTParam.java
 * Package Name:org.companyos.dev.companyos_otree_server
 * Date:2015年7月23日上午11:38:40
 * Copyright (c) 2015, companyos.org All Rights Reserved.
 *
*/

package org.companyos.dev.cos_common.object_tree;

public class OTSocketSlot {
  private org.eclipse.jetty.websocket.api.Session session;
  private long uid = -1;
  
  public OTSocketSlot(org.eclipse.jetty.websocket.api.Session session) {
    this.session = session;
  }
  
  public long getUid() {
    return uid;
  }

  public void setUid(long uid) {
    this.uid = uid;
  }

 
  public boolean sendString(OTReturn ret) {
    return sendString(">", ret);
  }
  
  public boolean sendString(String callback, OTReturn ret) {
    try {      
      this.session.getRemote().sendString(callback + ret.toJSONObject().toString());
      return true;
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  public boolean send(String text) {
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
  
}

