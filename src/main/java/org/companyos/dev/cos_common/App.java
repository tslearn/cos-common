package org.companyos.dev.cos_common;

import org.companyos.dev.cos_common.object_tree.OT;
import org.companyos.dev.cos_common.object_tree.OTNode;


class OTTest extends OTNode {
  public String onTest(String name) {
    return "hello " + name ;
  }
}

class OTRoot extends OTNode {
  public void afterAttach() {
    this.$createChild("test", OTTest.class);
  }
}

public class App {
  static public final String KeyStorePath = "sample.jks";
  static public final String KeyStorePassword = "secret";
  static public final String KeyManagerPassword = "password";

  public static void main(String[] args) {
    OT.start("0.0.0.0", 20001, 20002,  OTRoot.class, true, 8, KeyStorePath, KeyStorePassword, KeyManagerPassword);
  }
}


