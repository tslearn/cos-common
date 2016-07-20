package org.companyos.dev.cos_common.object_tree;

import java.util.LinkedList;

class OTMessageDelayList {
  final private class Item {
    final private OTMessage value;
    private long delayCycle;
    private Item next = null;

    Item(OTMessage msg, int delayCycle) {
      this.value = msg;
      this.delayCycle = delayCycle;
    }
  }

  final private Item head;
  private Item tail;

  OTMessageDelayList() {
    this.head = this.tail = new Item(null, 0);
  }

  final synchronized boolean add(OTMessage msg, int delayCycle, long evalTime) {
    if (OT.currentTimeMillis() < evalTime) {
      this.tail = this.tail.next = new Item(msg, delayCycle);
      return true;
    }
    else {
      return false;
    }
  }

  final synchronized LinkedList<OTMessage> polls() {
    LinkedList<OTMessage> ret = new LinkedList<OTMessage>();
    Item iter = this.head.next;
    Item prev = this.head;
    while (iter != null) {
      if (iter.delayCycle == 0) {
        prev.next = iter.next;
        ret.add(iter.value);
        iter.next = null;
      }
      else {
        iter.delayCycle--;
        prev = iter;
      }

      iter = prev.next;
    }
    this.tail = prev;
    return ret;
  }
}
