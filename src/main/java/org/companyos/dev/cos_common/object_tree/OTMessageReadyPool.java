package org.companyos.dev.cos_common.object_tree;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class OTMessageReadyPool {
  final private AtomicInteger putIndex = new AtomicInteger(0);
  final private AtomicInteger takeIndex = new AtomicInteger(0);
  final private ArrayBlockingQueue<OTMessage>[] pool;

  @SuppressWarnings("unchecked")
  public OTMessageReadyPool(int capacity) {
    int perQueueSize = capacity / 64;
    this.pool = new ArrayBlockingQueue[64];
    for (int i = 0; i < 64; i++) {
      this.pool[i] = new ArrayBlockingQueue<OTMessage>(
          Math.max(perQueueSize, 1));
    }
  }

  public void put(OTMessage msg) throws InterruptedException {
    this.pool[putIndex.getAndIncrement() & 0x3F].put(msg);
  }

  public OTMessage take() throws InterruptedException {
    return this.pool[takeIndex.getAndIncrement() & 0x3F].take();
  }
}