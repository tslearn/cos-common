package org.companyos.dev.cos_common.object_tree;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.companyos.dev.cos_common.CCLightMap;
import org.companyos.dev.cos_common.CCReflect;
import org.companyos.dev.cos_common.CCReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OT {
  private static final Logger log
      = LoggerFactory.getLogger(OT.class);

  static boolean isDebug;
  static boolean isStart = false;
  static private AtomicLong currTimeMS;
  static private OTNode rootNode;
  static private OTThreadSystem sysThread;
  static private OTWebSocketServer websocketServer;
  static OTMessagePool msgPool;
  private static ConcurrentHashMap<String, OTWebSocketHandler> wsHandlerHash;


  public final static boolean sendWebSocketMessage(String security, String msg) {
    OTWebSocketHandler wsHandler = wsHandlerHash.get(security);
    if (wsHandler != null)
      return wsHandler.send(msg);
    else
      return false;
  }

  final public static OTNode getNodeByPath(String path) {
    if (path != null && path.startsWith(OTConfig.STRootName)) {
      OTNode ret = OT.rootNode;

      if (path.length() == 1) {
        return ret;
      }
      else {
        String[] nodes = path.trim().split("\\.");

        for (int i = 1; ret != null && i < nodes.length; i++) {
          ret = ret.$getChild(nodes[i]);
        }
        return ret;
      }
    }
    else {
      return null;
    }
  }

  public static CCReturn<?> evalMsg(OTNode target, String msgName, Object... args) {
    return OT.$evalMsg(target, msgName, args);
  }

  public static CCReturn<?> evalMsg(String target, String msgName, Object... args) {
    return OT.$evalMsg(OT.getNodeByPath(target), msgName, args);
  }

  public static OTMessage postMsg(OTNode target, String msgName, Object... args) {
    return OT.$postMsg(target, msgName, args);
  }

  public static OTMessage postMsg(String target, String msgName, Object... args) {
    return OT.$postMsg(OT.getNodeByPath(target), msgName, args);
  }


  public static OTMessage delayPostMsg(long delay, OTNode target, String msgName, Object... args) {
    return OT.$delayPostMsg(delay, target, msgName, args);
  }

  public static OTMessage delayPostMsg(long delay, String target, String msgName, Object... args) {
    return OT.$delayPostMsg(delay, OT.getNodeByPath(target), msgName, args);
  }


  /**
   * trace ot message
   * @param msg
   */
  public final static void trace(String msg) {
    log.trace($getCallStackLog(msg, false, false));
  }

  public final static void debug(String msg) {
    log.debug($getCallStackLog(msg, false, false));
  }

  public final static void info(String msg) {
    log.info($getCallStackLog(msg, false, false));
  }

  public final static void warn(String msg) {
    log.warn($getCallStackLog(msg, false, false));
  }

  public final static void error(String msg) {
    log.error($getCallStackLog(msg, true, true));
  }

  public final static void trace(String msg, boolean isLogMessageStack) {
    log.trace($getCallStackLog(msg, isLogMessageStack, false));
  }

  public final static void debug(String msg, boolean isLogMessageStack) {
    log.debug($getCallStackLog(msg, isLogMessageStack, false));
  }

  public final static void info(String msg, boolean isLogMessageStack) {
    log.info($getCallStackLog(msg, isLogMessageStack, false));
  }

  public final static void warn(String msg, boolean isLogMessageStack) {
    log.warn($getCallStackLog(msg, isLogMessageStack, false));
  }

  public final static void error(String msg, boolean isLogMessageStack) {
    log.error($getCallStackLog(msg, isLogMessageStack, true));
  }

  synchronized public static OTNode start(String host, int port, Class<?> rootNodeCls, boolean isDebug) {
    boolean isStartMessageService  = false;
    try {
      if (!OT.isStart) {

        OT.msgPool = new OTMessagePool();
        OTThread.startMessageService();
        isStartMessageService = true;
        OT.isDebug = isDebug;
        OT.websocketServer = new OTWebSocketServer(host, port);
        OT.currTimeMS = new AtomicLong(System.currentTimeMillis());
        OT.wsHandlerHash = new ConcurrentHashMap<String, OTWebSocketHandler> ();

        OT.msgPool.turnOn();

        OT.rootNode = OTNode.$createRoot(rootNodeCls);
        OT.rootNode.beforeAttach();
        OT.rootNode.afterAttach();

        OT.sysThread = new OTThreadSystem();
        OT.sysThread.turnOn();

        if (OT.websocketServer.start()) {
          OT.info("OT system is start successful!");
          OT.isStart = true;
          return OT.rootNode;
        }
        else {
          OT.$error("OT system is start failed!", false);
          OT.isStart = true;
          OT.stop();
          return null;
        }
      }
      else {
        OT.$error("OT system has already been started", false);
        return null;
      }
    }
    finally {
      if (isStartMessageService) {
        OTThread.stopMessageService();
      }
    }
  }

  synchronized public static boolean stop() {
    if (OT.isStart) {
      boolean ret = true;
      OT.isStart = false;

      OT.websocketServer.stop();
      OT.websocketServer = null;

      OT.sysThread.close();
      OT.sysThread = null;

      OT.msgPool.shutDown();
      OT.msgPool = null;


      OT.currTimeMS = new AtomicLong(0);

      OT.wsHandlerHash = null;

      if (OT.rootNode != null) {
        ret = ret && OT.rootNode.$remove(true);
        OT.rootNode = null;
      }

      if (OT.msgPool != null) {
        ret = ret && OT.msgPool.shutDown();
        OT.msgPool = null;
      }

      return ret;
    }
    else {
      return false;
    }
  }


  public static String getKey(String key) {
    OTThread th = OTThread.currentThread();
    if (th != null && th.currentMsg.paramMap != null) {
      return (String) th.currentMsg.paramMap.get(key);
    }
    else {
      return null;
    }
  }

  public static boolean containsKey(String key) {
    OTThread th = OTThread.currentThread();
    if (th != null && th.currentMsg.paramMap != null) {
      return OT.getKey(key) != null;
    }
    else {
      return false;
    }
  }

  public static boolean putKeyIfAbsent(String key, String value) {
    OTThread th = OTThread.currentThread();
    if (th != null) {
      if (th.currentMsg.paramMap == null) {
        th.currentMsg.paramMap = new CCLightMap();
      }

      return th.currentMsg.paramMap.put(key, value);
    }
    else {
      return false;
    }
  }

  static boolean clearAllKeys() {
    OTThread th = OTThread.currentThread();
    if (th != null && th.currentMsg.paramMap != null) {
      th.currentMsg.paramMap = null;
      return true;
    }
    else {
      return false;
    }
  }

  static boolean $registerWebSocketHandler(String security, OTWebSocketHandler wsHandler) {
    return wsHandlerHash.putIfAbsent(security, wsHandler) == null;
  }

  static boolean $unregisterWebSocketHandler(String security) {
    return wsHandlerHash.remove(security) != null;
  }

  final static void $error(String msg) {
    log.error($getCallStackLog(msg, true, true));
  }

  final static void $error(String msg, boolean isLogMessageStack) {
    log.error($getCallStackLog(msg, isLogMessageStack, true));
  }

  private static String $getCallStackLog(String outString, boolean isLogMessageStack, boolean isLogInternal) {
    OTThread otThread =  OTThread.currentThread();

    if (otThread == null)
      return outString;

    OTMessageBase msg = otThread.currentMsg;
    OTNode target = (msg != null) ? msg.target : null;
    String path = "System";
    if (target != null) {
      path = target.$getPath();
    }

    StringBuilder sb = new StringBuilder();

    if (path == null || path.length() == 0) {
      sb.append(outString);
    }
    else {
      sb.append(path + ": " + outString);
    }

    if (!isLogMessageStack)
      return sb.toString();

    // Log Stack
    StackTraceElement[]  callerStacks = Thread.currentThread()
        .getStackTrace();

    sb.append("\r\n");

    sb.append("  @ ")
        .append(path).append(".")
        .append(msg.msgName)
        .append(CCReflect.buildCallArgsString(msg.args)).append("  ")
        .append(callerStacks[3].getClassName()).append("#")
        .append(callerStacks[3].getMethodName()).append(": (")
        .append(callerStacks[3].getFileName()).append(":")
        .append(callerStacks[3].getLineNumber()).append(")\r\n");


    if (isLogInternal) {
      for (int i = 4; i < callerStacks.length; i++) {
        sb.append("    #  ")
            .append(callerStacks[i].getClassName()).append("#")
            .append(callerStacks[i].getMethodName()).append(": (")
            .append(callerStacks[i].getFileName()).append(":")
            .append(callerStacks[i].getLineNumber()).append(")\r\n");
      }
    }

    if (msg != null) {
      sb.append(msg.getDebug());
    }
    return sb.toString();
  }

  private static CCReturn<?> $evalMsg(OTNode target, String msgName, Object... args) {
    if (!OT.isStart) {
      OT.$error("OT system is not start", false);
      return null;
    }


    OTThread th = OTThread.currentThread();
    if (th != null) {
      return msgPool.evalMessage(th, target, msgName, args);
    }
    else {
      th = OTThread.startMessageService();
      try {
        return msgPool.evalMessage(th, target, msgName, args);
      }
      finally {
        OTThread.stopMessageService();
      }
    }
  }

  private static OTMessage $postMsg(OTNode target, String msgName, Object... args) {
    if (!OT.isStart) {
      OT.$error("OT system is not start", false);
      return null;
    }

    OTThread th = OTThread.currentThread();
    if (th != null) {
      return msgPool.postMessage(th, 0, target, msgName, args);
    }
    else {
      th = OTThread.startMessageService();
      try {
        return msgPool.postMessage(th, 0, target, msgName, args);
      }
      finally {
        OTThread.stopMessageService();
      }
    }
  }

  public static OTMessage $delayPostMsg(long delay, OTNode target, String msgName,
                                        Object... args) {
    if (!OT.isStart) {
      OT.$error("OT system is not start", false);
      return null;
    }

    OTThread th = OTThread.currentThread();
    if (th != null) {
      return msgPool.postMessage(th, delay, target, msgName, args);
    }
    else {
      th = OTThread.startMessageService();
      try {
        return msgPool.postMessage(th, delay, target, msgName, args);
      }
      finally {
        OTThread.stopMessageService();
      }
    }
  }

  final static boolean $synchronizeTime() {
    while (true) {
      long now = System.currentTimeMillis();
      long old = OT.currTimeMS.get();

      if (now - old <= 0)
        return false;

      if (now - old > 100)
        System.out.println("synchronizeTime " + (now - old) + " over 50ms");

      if (OT.currTimeMS.compareAndSet(old, now))
        return true;
    }
  }

  final static long $currentTimeMillis() {
    return OT.currTimeMS.get();
  }

  final static String $getDebugInfo(OTMessageBase thisMsg, int stackDepth,  Object[] args) {
    if (OT.isDebug) {
      StackTraceElement callerStacks[] = Thread.currentThread().getStackTrace();
      StringBuilder sb = new StringBuilder();


      if (OTConfig.RootMessageName.equals(thisMsg.msgName)) {
        sb.append("  @ ")
            .append("Root Message").append("  ")
            .append(callerStacks[stackDepth].getClassName()).append("#")
            .append(callerStacks[stackDepth].getMethodName()).append(": (")
            .append(callerStacks[stackDepth].getFileName()).append(":")
            .append(callerStacks[stackDepth].getLineNumber()).append(")");
      }
      else {
        sb.append("  @ ")
            .append(thisMsg.target.$getPath()).append(".")
            .append(thisMsg.msgName)
            .append(CCReflect.buildCallArgsString(args)).append("  ")
            .append(callerStacks[stackDepth].getClassName()).append("#")
            .append(callerStacks[stackDepth].getMethodName()).append(": (")
            .append(callerStacks[stackDepth].getFileName()).append(":")
            .append(callerStacks[stackDepth].getLineNumber()).append(")\r\n")
            .append(thisMsg.debug);
      }

      return sb.toString();
    }
    else {
      return null;
    }
  }
}