package org.companyos.dev.cos_common.object_tree;

import org.companyos.dev.cos_common.CCError;
import org.companyos.dev.cos_common.CCErrorManager;

/**
 * Created by tianshuo on 16/9/8.
 */
public class OTErrorDefine {
  /******* Error Define Start  *******/
  static final int CCErrorGroupId = 1;
  static CCError OTSyntaxNotFound = new CCError(1, "Syntax not found");
  static CCError OTSyntaxExecuteError = new CCError(2, "Syntax execute error");
  static CCError OTSyntaxAccessDeny = new CCError(3, "Syntax access deny");
  static CCError OTSyntaxArgumentsNotMatch = new CCError(4, "Syntax arguments not match");
  static CCError OTMessageDepthOverflow = new CCError(5, "Message depth overflow");
  static CCError OTMessageTargetNotFound = new CCError(6, "Message target not found");
  static CCError OTMessageEvalError = new CCError(7, "Message eval error");

  static {
    CCErrorManager.addClass(OTErrorDefine.class);
  }
  // End
}