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

    currentThread.pushEvalMessage(msg);

    try {
      if (msg.curDepth < 0) {
        ret = CCReturn.error(OTErrorDefine.OTMessageDepthOverflow).setM("message depth overflow");
      }
      else if (msg.target == null) {
        ret = CCReturn.error(OTErrorDefine.OTMessageTargetNotFound).setM("message target not found");
      }
      else {
        ret = msg.target.$eval(currentThread, msg.args);
      }
    }
    catch (Exception e) {
      ret = CCReturn.error(OTErrorDefine.OTMessageEvalError).setM("message eval error").setE(e);
    }
    finally {
      currentThread.popMessage();
    }

    switch (msg.type) {
      case WebSocket:
        if (msg.callback > 0 && msg.otWebSocketHandler != null && !msg.otWebSocketHandler.isClosed()) {
          msg.otWebSocketHandler.response(msg.callback, ret);
        }
        break;
      case Http:
        break;
      default:
        break;
    }

    return ret;
  }
}