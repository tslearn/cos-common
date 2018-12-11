package org.companyos.dev.cos_common.object_tree;

import org.companyos.dev.cos_common.CCThread;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Created by tianshuo on 16/7/19.
 */
public class OTTest extends OTNode {
  @Before
  public void onBefore() throws Exception {
    OT.start("0.0.0.0", 23159, 23160, OTRootNodeTest.class, true, 6, null, null, null);
  }

  @After
  public void onAfter() throws Exception {
	    OT.stop();
  }

  @Test
  public void tom_ot1() throws Exception {
    OT.evalMsg("$.test", "Test", "tian-001");

    OT.postMsg("$.test", "Test", "tian-001");

    OT.delayPostMsg(1000, "$.test", "Test", "tian-001");

    CCThread.trySleepMS(3000);
  }
}