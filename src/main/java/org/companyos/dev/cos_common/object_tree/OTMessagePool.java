package org.companyos.dev.cos_common.object_tree;

import org.companyos.dev.cos_common.CCReturn;

final class OTMessagePool {
  final private static int ReadyMsgMaxSize = 500000;
  final private static int DelayMsgMaxSize = 200000;


  final private OTMessageDelayPool delayPool;
  final private OTMessageReadyPool readyList;

  OTMessagePool() {
    this.readyList = new OTMessageReadyPool(ReadyMsgMaxSize);
    this.delayPool = new OTMessageDelayPool(this.readyList, DelayMsgMaxSize);
  }

  final OTMessageReadyPool getReadyList() {
    return this.readyList;
  }

  protected synchronized boolean turnOn() {
    return this.delayPool.turnOn();
  }

  final synchronized boolean shutDown() {
    return this.delayPool.shutDown();
  }

  final public OTMessage postMessage(OTMessage msg, long delay) {
    try {
      if (delay <= 0)
        this.readyList.put(msg);
      else
        this.delayPool.put(msg, delay);

      return msg;
    }
    catch (Exception e) {
      throw new Error();
    }
  }

  final CCReturn<?> evalMessage(OTThread currentThread, OTMessage msg) {
    CCReturn<?> ret = null;

    if (msg.curDepth < 0) {
      ret = CCReturn.error("message depth overflow");
    }
    else if (msg.target == null) {
      OT.$error("target not found");
      return null;
    }
    else {
      currentThread.pushEvalMessage(msg);

      try {
        ret = msg.target.$eval(currentThread, msg.args);
      }
      catch (Exception e) {
        ret = CCReturn.error("eval error").setE(e);
      }
      finally {
        currentThread.popMessage();
      }
    }

    return ret;
  }
}