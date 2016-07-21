package org.companyos.dev.cos_common.object_tree;

import java.util.LinkedList;
import java.util.Map;

import org.companyos.dev.cos_common.CCLightMap;
import org.companyos.dev.cos_common.CCReturn;

enum OTMessageStatus {
  None, Success, Error
}

class OTMessageBase {
  Object[] args;
	CCLightMap paramMap;
  String msgName;
  OTNode target;
  OTNode sender;
  int curDepth;
  String debug;

  private static volatile OTMessageBase RootMessage = null;

  public OTMessageBase(CCLightMap paramMap, String msgName, OTNode target, OTNode sender,
      int curDepth, String debug, Object[] args) {
    this.paramMap = paramMap;
    this.msgName = msgName;
    this.target = target;
    this.sender = sender;
    this.curDepth = curDepth;
    this.debug = debug;
    this.args = args;
  }

  final static OTMessageBase getRootMessage() {
    if (RootMessage == null) {
      RootMessage = new OTMessageBase(null, OTConfig.RootMessageName, new OTNode(), new OTNode(),
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
}

final public class OTMessage extends OTMessageBase {
  volatile private OTMessageStatus status;
  volatile private LinkedList<OTCallback> callbackPool;

  public OTMessage(CCLightMap paramMap, String msgName, OTNode target, OTNode sender, int curDepth,
      String debug, Object[] args) {
    super(paramMap, msgName, target, sender, curDepth, debug, args);
    this.args = args;
    this.status = OTMessageStatus.None;
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
