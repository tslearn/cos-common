package org.companyos.dev.cos_common.object_tree;

import org.companyos.dev.cos_common.CCReflect;
import org.companyos.dev.cos_common.CCReturn;

final class OTMessagePool {
  final private static int ReadyMsgMaxSize = 30000;
  final private static int DelayMsgMaxSize = 20000;


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

  final private OTMessage putMessage(OTMessage msg, long delay) {
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

  final CCReturn<?> evalMessage(OTThread currentThread, OTNode target,
      String msgName, Object[] args) {
    OTMessageBase thisMsg = currentThread.currentMsg;

    if (thisMsg.curDepth <= 0) {
      return CCReturn.error("message depth overflow");
    }
    
    if (target == null) {
      return CCReturn.error("target not found");
    }

    currentThread.pushEvalMessage(thisMsg.paramMap, msgName, target, thisMsg.target,
        thisMsg.curDepth - 1, OT.$getDebugInfo(thisMsg, 5, args), args);

    try {
      return target.$eval(currentThread, args);
    }
    catch (Exception e) {
      OT.$error(e.toString());
      return CCReturn.error().setE(e);
    }
    finally {
      currentThread.popMessage();
    }
  }

  final OTMessage postMessage(OTThread currentThread, long delayms,
      OTNode target, String msgName, Object[] args) {
    OTMessageBase thisMsg = currentThread.currentMsg;

    if (thisMsg.curDepth <= 0) {
      OT.$error("message depth overflow");
      return null;
    }

    return this.putMessage(new OTMessage(thisMsg.paramMap, msgName, target, thisMsg.target,
        thisMsg.curDepth - 1, OT.$getDebugInfo(thisMsg, 5, args), args), delayms);
  }
}