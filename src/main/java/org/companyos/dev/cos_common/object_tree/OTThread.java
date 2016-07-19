package org.companyos.dev.cos_common.object_tree;

class OTThread extends Thread {
  private static ThreadLocal<OTThread> ThreadLocal = new ThreadLocal<OTThread>();
  private boolean runningStatus = false;
  boolean lastEvalSuccess = true;
  private final OTMessageStack msgStack = new OTMessageStack();
  OTMessageBase currentMsg = null;

  OTThread() {
    
  }
  
  final void goSystemPriority() {
    this.setPriority(Thread.MAX_PRIORITY);
  }

  final void goHighPriority() {
    this.setPriority(Thread.NORM_PRIORITY);
  }

  final void goLowPriority() {
    this.setPriority(Thread.MIN_PRIORITY);
  }

  public static OTThread currentThread() {
    try {
      return (OTThread) Thread.currentThread();
    }
    catch (Exception e) {
      return ThreadLocal.get();
    }
  }

  public static OTThread initExternal() {
    OTThread thObj = new OTThread();
    thObj.pushMessage(OTMessageBase.getRootMessage());
    ThreadLocal.set(thObj);
    return thObj;
  }

  public static void stopExternal() {
    ThreadLocal.remove();
  }

  final OTMessageBase pushEvalMessage(OTSocketSlot param, String msgName, OTNode target,
      OTNode sender, int curDepth, String debug) {
    return this.currentMsg = this.msgStack.push(param, msgName, target, sender,
        curDepth, debug);
  }

  final synchronized void pushMessage(OTMessageBase msg) {
    this.currentMsg = this.msgStack.push(msg.param, msg.msgName, msg.target, msg.sender,
        msg.curDepth, msg.debug);
  }

  final synchronized void popMessage() {
    this.currentMsg = this.msgStack.popAndReturnHeader();
  }

  protected synchronized boolean isRunning() {
    return this.runningStatus;
  }

  protected synchronized boolean turnOn() {
    if (this.runningStatus) {
      return false;
    }
    else {
      this.start();
      this.runningStatus = true;
      this.goSystemPriority();
      return true;
    }
  }

  final synchronized boolean shutDown() {
    this.goLowPriority();
    if (this.runningStatus) {
      this.runningStatus = false;
      return true;
    }
    else {
      return false;
    }
  }
}