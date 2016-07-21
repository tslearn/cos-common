package org.companyos.dev.cos_common.object_tree;

/**
 * Created by tianshuo on 16/7/19.
 */
public class OTNodeTest extends OTNode {

  public void beforeAttach() {
    System.out.println("$.test beforeAttach");
    // OT.Log.log(OT.Log.Level.Log, "beforeAttach");
  }

  public void afterAttach() {
    System.out.println("beforeAttach");
    // OT.Log.log(OT.Log.Level.Log, "afterAttach");
  }

  public void beforeDetach() {
    System.out.println("beforeDetach");
    // OT.Log.log(OT.Log.Level.Log, "beforeDetach");
  }

  public void afterDetach() {
    System.out.println("afterDetach");
    // OT.Log.log(OT.Log.Level.Log, "afterDetach");
  }

  public void onTest(String name) {
    OT.postMsg(this, "Test1", name);
  }

  public void onTest1(String name) {
    OT.postMsg("$.test", "Test2", name);
  }

  public void onTest2(String name) {
    OT.error("Hello " + name);
  }

}