package org.companyos.dev.cos_common.object_tree;

/**
 * Created by tianshuo on 16/7/19.
 */
public class OTNodeTest extends OTNode {
  public void onTest(String name) {
    OT.postMsg(this, "Test1", name);
  }

  public void onTest1(String name) {
    OT.postMsg("$.test", "Test2", name);
  }

  public void onTest2(String name) {
    OT.info("Hello " + name);
  }

}