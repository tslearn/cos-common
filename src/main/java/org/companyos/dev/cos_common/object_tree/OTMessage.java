package org.companyos.dev.cos_common.object_tree;

import java.util.LinkedList;

import org.companyos.dev.cos_common.CCLightMap;
import org.companyos.dev.cos_common.CCReturn;

enum OTMessageStatus {
  None, Success, Error
}


final public class OTMessage {
  Object[] args;
  CCLightMap paramMap;
  String msgName;
  OTNode target;
  OTNode sender;
  int curDepth;
  String debug;
  volatile private OTMessageStatus status;
  volatile private LinkedList<OTCallback> callbackPool;

  private static volatile OTMessage RootMessage = null;

  public OTMessage(CCLightMap paramMap, String msgName, OTNode target, OTNode sender, int curDepth,
      String debug, Object[] args) {
    this.paramMap = paramMap;
    this.msgName = msgName;
    this.target = target;
    this.sender = sender;
    this.curDepth = curDepth;
    this.debug = debug;
    this.args = args;
    this.status = OTMessageStatus.None;
  }


  final static OTMessage getRootMessage() {
    if (RootMessage == null) {
      RootMessage = new OTMessage(null, OTConfig.RootMessageName, new OTNode(), new OTNode(),
          OTConfig.DefaultMessageMaxDepth, "", new Object[0]);
    }
    return RootMessage;
  }

  final String getDebug() {
    if (this.debug != null)
      return this.debug;
    else
      return "OTRuntime debug mode is disabled !!!";
  }

  CCReturn<?> $eval(OTThread currentThread) {
    currentThread.pushMessage(this);
    try {
      CCReturn<?> ret = this.target.$eval(currentThread, this.args);
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
        currentThread = OTThread.startMessageService();
        try {
          evalCallBack(currentThread, cb);
        }
        finally {
          OTThread.stopMessageService();
        }      
      }
    }
  }
}
