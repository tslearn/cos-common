package org.companyos.dev.cos_common.object_tree;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Created by tianshuo on 16/7/19.
 */
public class OTTest extends OTNode {
  @Before
  public void onBefore() throws Exception {
    OT.start("0.0.0.0", 23159, OTRootNodeTest.class, true);
  }

  @After
  public void onAfter() throws Exception {
	    OT.stop();
  }

  @Test
  public void tom_ot1() throws Exception {
    OT.evalMsg("$.test", "Test", "tianshuo");
  }

//  @Test
//  public void tom_ot2() throws Exception {
//    OT.Message.evalMsg("$.test", "Test", "tianshuo");
//  }
}