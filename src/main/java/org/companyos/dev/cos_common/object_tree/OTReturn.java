// #

package org.companyos.dev.cos_common.object_tree;

import org.json.JSONObject;

public class OTReturn {  
  private static final int OTReturn_OK = 0;
  private static final int OTReturn_SysError = 1;
  private static final int OTReturn_SysWarn = 2;
  private static final int OTReturn_UserError = 3;
  private static final int OTReturn_UserWarn = 4;
 
  int s;
  String m;
  Exception e;
  Object v;
  String d;

  public OTReturn(int s, Object v) {
    this.s = s;
    this.v = v;
  }
  
  public boolean isSuccess() {
    return this.s == OTReturn_OK;
  }
  
  public int getS() {
    return this.s;
  }
  
  public String getM() {
    return this.m;
  }
  public OTReturn setM(String format, Object...args) {
    this.m = String.format(format, args);    
    return this;
  }
  
  public Exception getE() {
    return this.e;
  }
  public OTReturn setE(Exception e) {
    e.printStackTrace();
    this.e = e;
    return this;
  }
  
  public String getD() {
    return this.d;
  }
  private OTReturn setD(String d) {
    this.d = d;
    return this;
  }
  
  public Object getV() {
    return this.v;
  }
 
  public static OTReturn getSuccess() {
    return new OTReturn(OTReturn_OK, null);
  }
  
  public static OTReturn getSuccess(Object v) {
    return new OTReturn(OTReturn_OK, v);
  }
  
  public static OTReturn getUserError() {
    return new OTReturn(OTReturn_UserError, null).setD(getDebug());
  }
  
  public static OTReturn getUserError(Object v) {
    return new OTReturn(OTReturn_UserError, v).setD(getDebug());
  }
  
  public static OTReturn getUserWarn() {
    return new OTReturn(OTReturn_UserWarn, null).setD(getDebug());
  }
  
//  public static OTReturn getUserWarn(Object v) {
//    return new OTReturn(OTReturn_UserWarn, v).setD(getDebug());
//  }
  
  public static OTReturn getSysError() {
    return new OTReturn(OTReturn_SysError, null).setD(getDebug());
  }
  
//  public static OTReturn getSysError(Object v) {
//    return new OTReturn(OTReturn_SysError, v).setD(getDebug());
//  }
  
  public static OTReturn getSysWarn() {
    return new OTReturn(OTReturn_SysWarn, null).setD(getDebug());
  }
  
//  public static OTReturn getSysWarn(Object v) {
//    return new OTReturn(OTReturn_SysWarn, v).setD(getDebug());
//  }
  public static OTReturn getNotImplement() {
    return new OTReturn(OTReturn_SysError, null).setD(getDebug()).setM("Not Implement");
  }
  
  
  
  public JSONObject toJSONObject() {
    return new JSONObject()
        .put("s", this.getS())
        .put("m", this.getM())
        .put("v", this.getV())
        .put("e", this.getE())
        .put("d", this.getD());
  }
  
  private static String getDebug() {
    int upMethodDepth = 3;
    StackTraceElement callerStacks[] = Thread.currentThread().getStackTrace();
    return callerStacks[upMethodDepth].toString();
  }
}