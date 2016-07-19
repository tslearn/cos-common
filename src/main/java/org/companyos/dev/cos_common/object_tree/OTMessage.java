package org.companyos.dev.cos_common.object_tree;

import java.io.PrintStream;
import java.util.LinkedList;

enum OTMessageStatus {
  None, Success, Error
}

class OTMessageBase {
  OTSocketSlot param;
  String msgName;
  OTNode target;
  OTNode sender;
  int curDepth;
  String debug;

  private static volatile OTMessageBase RootMessage = null;

  public OTMessageBase(OTSocketSlot param, String msgName, OTNode target, OTNode sender,
      int curDepth, String debug) {
    this.param = param;
    this.msgName = msgName;
    this.target = target;
    this.sender = sender;
    this.curDepth = curDepth;
    this.debug = debug;
  }

  final static OTMessageBase getRootMessage() {
    if (RootMessage == null) {
      RootMessage = new OTMessageBase(null, "Root", new OTNode(), new OTNode(),
          OTConfig.DefaultMessageMaxDepth, "Root Message");
    }
    return RootMessage;
  }

  final void log(PrintStream ps) {
    synchronized (ps) {
      if (this.debug != null)
        ps.println(this.debug);
      else
        ps.println("OTRuntime debug mode is disabled !!!");
    }
  }
}

final public class OTMessage extends OTMessageBase {
  Object[] args;
  volatile private OTMessageStatus status;
  volatile private LinkedList<OTCallback> callbackPool;

  public OTMessage(OTSocketSlot param, String msgName, OTNode target, OTNode sender, int curDepth,
      String debug, Object[] args) {
    super(param, msgName, target, sender, curDepth, debug);
    this.args = args;
    this.status = OTMessageStatus.None;
  }

  OTReturn $eval(OTThread currentThread) {
    currentThread.pushMessage(this);
    try {
      OTReturn ret = this.target.$eval(currentThread, this.args);
      setFinish(currentThread);
      return ret;
    }
    finally {
      currentThread.popMessage();
    }
  }

  private synchronized void setFinish(OTThread currentThread) {
    if (currentThread.lastEvalSuccess) {
      this.status = OTMessageStatus.Success;
      if (this.callbackPool != null) {
        for (OTCallback cb : this.callbackPool) {
          cb.onSuccess();
        }
      }
    }
    else {
      this.status = OTMessageStatus.Error;
      if (this.callbackPool != null) {
        for (OTCallback cb : this.callbackPool) {
          cb.onError();
        }
      }
    }
    
    this.callbackPool = null;
  }
  
  private void evalCallBack(OTThread currentThread, OTCallback cb) {
    currentThread.pushMessage(this);
    try {
      if (this.status == OTMessageStatus.Success)
        cb.onSuccess();
      else {
        cb.onError();
      }
    }
    finally {
      currentThread.popMessage();
    }
  }

  public synchronized void setCallback(OTCallback cb) {
    if (this.status == OTMessageStatus.None) {
      if (this.callbackPool == null) {
        this.callbackPool = new LinkedList<OTCallback>();
      }
      this.callbackPool.add(cb);
    }
    else {
      OTThread currentThread = OTThread.currentThread();
      
      if (currentThread != null) {
        evalCallBack(currentThread, cb);
      }
      else {
        currentThread = OTThread.initExternal();
        try {
          evalCallBack(currentThread, cb);
        }
        finally {
          OTThread.stopExternal();
        }      
      }
    }
  }
}
