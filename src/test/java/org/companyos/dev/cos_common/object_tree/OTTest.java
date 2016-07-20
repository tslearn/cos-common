package org.companyos.dev.cos_common.object_tree;

import org.companyos.dev.cos_common.CCReturn;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by tianshuo on 16/7/19.
 */
public class OTTest extends OTNode {
  @Before
  public void onBefore() throws Exception {
    OT.Runtime.start("0.0.0.0", 23159, OTRootNodeTest.class, true);
  }

  @After
  public void onAfter() throws Exception {
	    OT.Runtime.stop();
  }

  @Test
  public void tom_ot() throws Exception {
    OT.Message.evalMsg("$.test", "Test", "tianshuo");
    // System.out.println("eval finish");
  }
}