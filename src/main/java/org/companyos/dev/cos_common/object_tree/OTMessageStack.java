package org.companyos.dev.cos_common.object_tree;

public class OTMessageStack {
  private int count = 0;
  private OTMessageBase[] msgPool = new OTMessageBase[0];

  final OTMessageBase push(OTSocketSlot param, String msgName, OTNode target, OTNode sender,
      int curDepth, String debug) {
    if (this.count % OTConfig.DefaultMessageMaxDepth == 0
        && this.count == this.msgPool.length) {
      OTMessageBase[] newPool = new OTMessageBase[this.count
          + OTConfig.DefaultMessageMaxDepth];

      System.arraycopy(this.msgPool, 0, newPool, 0, this.msgPool.length);

      for (int i = newPool.length - 1; i >= this.count; i--) {
        newPool[i] = new OTMessageBase(null, null, null, null, 0, null);
      }

      this.msgPool = newPool;
    }

    OTMessageBase ret = this.msgPool[count++];
    ret.param = param;
    ret.msgName = msgName;
    ret.target = target;
    ret.sender = sender;
    ret.curDepth = curDepth;
    ret.debug = debug;
    return ret;
  }

  final OTMessageBase popAndReturnHeader() {
    if (this.count % OTConfig.DefaultMessageMaxDepth == 0
        && this.msgPool.length - this.count >= 2 * OTConfig.DefaultMessageMaxDepth) {
      OTMessageBase[] newPool = new OTMessageBase[this.count
          - OTConfig.DefaultMessageMaxDepth];
      System.arraycopy(this.msgPool, 0, newPool, 0, newPool.length);
      this.msgPool = newPool;
    }
    OTMessageBase removeItem = msgPool[--this.count];
    removeItem.target = null;
    removeItem.sender = null;
    return this.count > 0 ? msgPool[this.count - 1] : null;
  }
}