package org.companyos.dev.cos_common.object_tree;

final class OTThreadMessage extends OTThread {
  private final OTMessageReadyPool readyList;
  private volatile long lastStartMS = Long.MAX_VALUE;
  boolean isTerminal = false;

  OTThreadMessage() {
    this.goSystemPriority();
    this.readyList = OT.Message.msgPool.getReadyList();
  }

  final public void run() {
    // System.out.println("Message Thread Start");
    while (this.isRunning()) {
      try {
        OTMessage msg = readyList.take();
        synchronized (this) {
          this.lastStartMS = OT.Runtime.currentTimeMillis();
        }
        msg.$eval(this);
      }
      catch (InterruptedException e) {
      }
      finally {
        synchronized (this) {
          this.lastStartMS = Long.MAX_VALUE;
        }
      }
    }
    this.isTerminal = true;
    // System.out.println("Message Thread Shutdown");
  }

  final synchronized boolean isTimeout(long nowMS) {
    return (nowMS - this.lastStartMS) > OTConfig.FirstBoostMS;
  }

  final synchronized boolean isWorking() {
    return this.lastStartMS != Long.MAX_VALUE;
  }
}
