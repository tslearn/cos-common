package org.companyos.dev.cos_common;

import org.companyos.dev.cos_common.object_tree.OT;
import org.companyos.dev.cos_common.object_tree.OTCallback;
import org.companyos.dev.cos_common.object_tree.OTNode;

/**
 * Created by tianshuo on 16/7/22.
 */

class OTTest extends OTNode {
  public void onTest(String name) {
    OT.postMsg(this.$getPath(), "Test1", name);
  }

  public void onTest1(String name) {
    OT.info("hello " + name, true);
  }
}

class OTRoot extends OTNode {
  public void afterAttach() {
    this.$createChild("test", OTTest.class);
  }
}

public class App {
  public static void main(String[] args) {
    OT.start("0.0.0.0", 8999, OTRoot.class, true);
    OT.evalMsg("$.test", "Test",  "tianshuo");
  }
}
