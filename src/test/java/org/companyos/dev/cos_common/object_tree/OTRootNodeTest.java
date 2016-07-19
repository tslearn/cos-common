package org.companyos.dev.cos_common.object_tree;

/**
 * Created by tianshuo on 16/7/19.
 */
public class OTRootNodeTest extends OTNode {
  public void afterAttach() {
    this.$createChild("test", OTNodeTest.class);
  }
}
