package org.companyos.dev.cos_common.object_tree;

import org.companyos.dev.cos_common.CCLightMap;

public class OTMessageStack {
  private int count = 0;
  private OTMessage[] msgPool = new OTMessage[0];

  final OTMessage push(OTMessage msg) {
    if (this.count % OTConfig.DefaultMessageMaxDepth == 0
        && this.count == this.msgPool.length) {
      OTMessage[] newPool = new OTMessage[this.count
          + OTConfig.DefaultMessageMaxDepth];

      System.arraycopy(this.msgPool, 0, newPool, 0, this.msgPool.length);

      this.msgPool = newPool;
    }

    this.msgPool[count++] = msg;
    return msg;
  }

  final OTMessage popAndReturnHeader() {
    if (this.count % OTConfig.DefaultMessageMaxDepth == 0
        && this.msgPool.length - this.count >= 2 * OTConfig.DefaultMessageMaxDepth) {
      OTMessage[] newPool = new OTMessage[this.count
          - OTConfig.DefaultMessageMaxDepth];
      System.arraycopy(this.msgPool, 0, newPool, 0, newPool.length);
      this.msgPool = newPool;
    }

    msgPool[--this.count] = null;

    return this.count > 0 ? msgPool[this.count - 1] : null;
  }
}