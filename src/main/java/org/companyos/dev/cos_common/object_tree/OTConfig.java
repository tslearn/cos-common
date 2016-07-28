package org.companyos.dev.cos_common.object_tree;

class OTConfig {
  static public final String STRootName = "$";
  static public final int DefaultMessageMaxDepth = 300;
  static public final int FirstBoostMS = 100;
  static public final int TerminaWaitTimeMS = 8000;
  static public final int ForceQuitWaitTimeMS = 7000;
  static public final String RootMessageName = "@@Root@@";
  static public final int JettyWebSocketThreadPoolSize = 4; // must greater than 4

  static public final String KeyStorePath = "keystore";
  static public final String KeyStorePassword = "123456";
  static public final String KeyManagerPassword = "654321";
}
