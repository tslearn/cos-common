package org.companyos.dev.cos_common.object_tree;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.companyos.dev.cos_common.CCThread;

class OTMessageDelayPool extends OTThread { 
  final static private int DelayPoolSize = 65535;

  private final int capacity;
  private final AtomicInteger count = new AtomicInteger();
  private final ReentrantLock putLock = new ReentrantLock();
  private final Condition notFull = putLock.newCondition();

  final private OTMessageDelayList[] delayPool;
  final private OTMessageReadyPool readyList;
  final private AtomicLong currentTimeMS = new AtomicLong(
      System.currentTimeMillis());

  public OTMessageDelayPool(OTMessageReadyPool readyList,
      int capacity) {
    this.readyList = readyList;
    this.capacity = capacity;

    this.delayPool = new OTMessageDelayList[DelayPoolSize];
    for (int i = 0; i < DelayPoolSize; i++) {
      this.delayPool[i] = new OTMessageDelayList();
    }
  }

  void put(OTMessage msg, long delayMS) throws InterruptedException {
    if (msg == null)
      throw new NullPointerException();

    int c = -1;
    final ReentrantLock putLock = this.putLock;
    final AtomicInteger count = this.count;
    putLock.lockInterruptibly();
    try {
      while (count.get() == capacity) {
        this.notFull.await();
      }

      for (long evalTimeMS = currentTimeMS.get() + delayMS;; evalTimeMS++) {
        if (this.delayPool[(int) (evalTimeMS % DelayPoolSize)].add(msg,
            (int) (delayMS / DelayPoolSize), evalTimeMS) == true)
          break;
      }

      c = count.getAndIncrement();
      if (c + 1 < capacity)
        notFull.signal();
    }
    finally {
      putLock.unlock();
    }
  }

  final public void run() {
    this.goSystemPriority();

    while (this.isRunning()) {
      if (this.currentTimeMS.get() < OT.currentTimeMillis()) {
        long currTime = this.currentTimeMS.getAndIncrement();

        if (this.count.get() > 0) {
          for (OTMessage msg :  this.delayPool[(int) (currTime % DelayPoolSize)]
              .polls()) {
            try {
              this.readyList.put(msg);

              if (this.count.getAndDecrement() == capacity) {
                this.putLock.lock();
                try {
                  this.notFull.signal();
                }
                finally {
                  this.putLock.unlock();
                }
              }
            }
            catch (InterruptedException e) {
              throw new Error();
            }
          }
        }
        else {
          CCThread.trySleepNanoSeconds(1000);
        }
      }
      else {
    	  CCThread.trySleepNanoSeconds(1000);
      }
    }
    System.out.println("System thread for delay pool terminal");
  }
}
