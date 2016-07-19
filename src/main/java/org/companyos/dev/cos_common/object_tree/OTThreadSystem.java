package org.companyos.dev.cos_common.object_tree;

import java.util.LinkedList;
import java.util.ListIterator;

import org.companyos.dev.cos_common.CCThread;

final class OTThreadSystem extends OTThread {
  final private LinkedList<OTThreadMessage> timeoutThreads = new LinkedList<OTThreadMessage>();
  private OTThreadMessage[] msgThreadPool = new OTThreadMessage[0];
  private boolean isTerminal = false;

  OTThreadSystem() {
    this.goSystemPriority();
  }

  final synchronized boolean closeMsgThreadPool() {
    boolean ret = true;
    for (int i = 0; i < this.msgThreadPool.length; i++) {
      ret = ret && this.msgThreadPool[i].shutDown();
    }
    return ret;
  }

  final synchronized void clearMsgThreadPool() {
    for (int i = 0; i < this.msgThreadPool.length; i++) {
      OTThreadMessage shutDownThread = this.msgThreadPool[i];
      shutDownThread.shutDown();
      this.timeoutThreads.add(shutDownThread);
      this.msgThreadPool[i] = null;
    }
  }

  final synchronized boolean setCPUCores(int cores) {
    int newLength = (cores > 1) ? cores - 1 : 1;
    int oldLength = this.msgThreadPool.length;

    if (newLength > oldLength) {
      System.out.println("Msg Thread set to " + newLength);
      OTThreadMessage[] newPool = new OTThreadMessage[newLength];
      for (int i = 0; i < oldLength; i++) {
        newPool[i] = this.msgThreadPool[i];
      }
      for (int i = oldLength; i < newLength; i++) {
        newPool[i] = new OTThreadMessage();
        newPool[i].turnOn();
      }
      this.msgThreadPool = newPool;
      return true;
    }
    else if (newLength < oldLength) {
      System.out.println("Msg Thread set to " + newLength);
      OTThreadMessage[] newPool = new OTThreadMessage[newLength];
      for (int i = 0; i < newLength; i++) {
        newPool[i] = this.msgThreadPool[i];
      }
      for (int i = newLength; i < oldLength; i++) {
        OTThreadMessage shutDownThread = this.msgThreadPool[i];
        shutDownThread.shutDown();
        this.timeoutThreads.add(shutDownThread);
        this.msgThreadPool[i] = null;
      }
      this.msgThreadPool = newPool;
      return true;
    }
    else {
      return false;
    }
  }

  final void sweepThreads(long nowMS) {
    boolean canSweep = true;

    if (OT.Runtime.isDebug) {
      for (int i = 0; i < this.msgThreadPool.length; i++) {
        OTThreadMessage msgThread = this.msgThreadPool[i];
        Thread.State state = msgThread.getState();

        if (!msgThread.isWorking()
            || (state != Thread.State.BLOCKED && state != Thread.State.WAITING && state != Thread.State.TIMED_WAITING)) {
          canSweep = false;
          break;
        }
      }
    }

    if (canSweep) {
      for (int i = 0; i < this.msgThreadPool.length; i++) {
        OTThreadMessage msgThread = this.msgThreadPool[i];
        if (msgThread.isTimeout(nowMS)) {
          System.out.println("sweep");
          OTThreadMessage shutDownThread = this.msgThreadPool[i];
          shutDownThread.shutDown();
          timeoutThreads.add(shutDownThread);
          this.msgThreadPool[i] = new OTThreadMessage();
          this.msgThreadPool[i].turnOn();
        }
      }
    }
  }

  private void clearTimeoutThread() {
    ListIterator<OTThreadMessage> iter = this.timeoutThreads.listIterator();
    while (iter.hasNext()) {
      OTThreadMessage msgThread = iter.next();
      if (msgThread.isTerminal) {
        iter.remove();
      }
    }
  }

  private void terminalTimeoutThread() {
    ListIterator<OTThreadMessage> iter = this.timeoutThreads.listIterator();
    while (iter.hasNext()) {
      OTThreadMessage msgThread = iter.next();
      if (!msgThread.isTerminal && !msgThread.isWorking()) {
        msgThread.interrupt();
      }
    }
  }

  public void close() {
    this.shutDown();

    while (this.isTerminal == false) {
    	CCThread.trySleepMS(1000);
    }
  }

  public void run() {   
    while (this.isRunning()) {
      this.setCPUCores(Runtime.getRuntime().availableProcessors());
      this.sweepThreads(OT.Runtime.currentTimeMillis());
      this.clearTimeoutThread();
      if (!OT.Runtime.synchronizeTime()) {
        CCThread.trySleepNanoSeconds(1000);
      }
    }
    
    this.clearMsgThreadPool();

    System.out.println("System is Stopping");

    // 正常等待结束
    // synchronizeTime is shutdown, STRuntime.currentTimeMS is not work again !
    long now = System.currentTimeMillis();
    while (this.timeoutThreads.size() != 0
        && System.currentTimeMillis() - now < OTConfig.TerminaWaitTimeMS) {
      System.out.println("Waiting msgThread stopped "
          + (now + OTConfig.TerminaWaitTimeMS - System.currentTimeMillis())
          / 1000 + "s");
      this.clearTimeoutThread();
      CCThread.trySleepMS(1000);
    }

    now = System.currentTimeMillis();
    while (this.timeoutThreads.size() != 0
        && System.currentTimeMillis() - now < OTConfig.ForceQuitWaitTimeMS) {
      System.out.println("Terminal msgThread stopped "
          + (now + OTConfig.ForceQuitWaitTimeMS - System.currentTimeMillis())
          / 1000 + "s");
      this.terminalTimeoutThread();
      this.clearTimeoutThread();
      CCThread.trySleepMS(1000);
    }

    if (this.timeoutThreads.size() > 0) {
      ListIterator<OTThreadMessage> iter = this.timeoutThreads.listIterator();
      while (iter.hasNext()) {
        OTThreadMessage msgThread = iter.next();
        if (!msgThread.isTerminal) {
          System.err.println(msgThread.currentMsg.target.getClass().getName()
              + ".on" + msgThread.currentMsg.msgName + " is Not Stopping !!!");
        }
      }
    }
    else {
      System.out.println("System Thread Stopped !!!");
    }

    this.isTerminal = true;
  }
}