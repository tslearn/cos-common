package org.companyos.dev.cos_common.object_tree;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

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
    log.trace($getCallStackLog(msg, false));
  }

  public final static void debug(String msg) {
    log.debug($getCallStackLog(msg, false));
  }

  public final static void info(String msg) {
    log.info($getCallStackLog(msg, false));
  }

  public final static void warn(String msg) {
    log.warn($getCallStackLog(msg, false));
  }

  public final static void error(String msg) {
    log.error($getCallStackLog(msg, false));
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
        OT.rootNode = OTNode.$createRoot(rootNodeCls);
        OT.rootNode.beforeAttach();
        OT.rootNode.afterAttach();

        OT.sysThread = new OTThreadSystem();
        OT.sysThread.turnOn();

        OT.websocketServer.start();
        OT.isStart = true;

        return OT.rootNode;
      }
      else {
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
      OT.isStart = false;
      OT.websocketServer.stop();
      OT.sysThread.close();
      boolean ret = OT.rootNode.$removeChildren();
      //ret = ret && OT.Runtime.msgPool.shutDown();
      return ret;
    }
    else {
      return false;
    }
  }


  public static String getKey(String key) {
    OTThread th = OTThread.currentThread();
    if (th != null && th.currentMsg.paramMap != null) {
      return th.currentMsg.paramMap.get(key);
    }
    else {
      return null;
    }
  }

  public static boolean containsKey(String key) {
    OTThread th = OTThread.currentThread();
    if (th != null && th.currentMsg.paramMap != null) {
      return th.currentMsg.paramMap.containsKey(key);
    }
    else {
      return false;
    }
  }

  public static String putKeyIfAbsent(String key, String value) {
    OTThread th = OTThread.currentThread();
    if (th != null) {
      if (th.currentMsg.paramMap == null) {
        th.currentMsg.paramMap = new HashMap<String, String>();
      }

      return th.currentMsg.paramMap.putIfAbsent(key, value);
    }
    else {
      return null;
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
    log.error($getCallStackLog(msg, true));
  }

  private static String $getCallStackLog(String outString, boolean isLogInternal) {
    OTMessageBase msg = OTThread.currentThread().currentMsg;
    OTNode target = (msg != null) ? msg.target : null;
    String path = "System";
    if (target != null) {
      path = target.$getPath();
    }

    StringBuilder sb = new StringBuilder();
    sb.append("\r\n  " + path + ": " + outString + "\r\n");

    StackTraceElement callerStacks[] = Thread.currentThread()
        .getStackTrace();

    for (int i = 3; i < callerStacks.length; i++) {
      sb.append(i == 3 ? "  @ " : "    #  ")
          .append(callerStacks[i].getClassName()).append(".")
          .append(callerStacks[i].getMethodName()).append(": (")
          .append(callerStacks[i].getFileName()).append(":")
          .append(callerStacks[i].getLineNumber()).append(")\r\n");
    }

    if (msg != null) {
      sb.append(msg.getDebug());
    }
    return sb.toString();
  }

  private static CCReturn<?> $evalMsg(OTNode target, String msgName, Object... args) {
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

  final static boolean synchronizeTime() {
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

  final static long currentTimeMillis() {
    return OT.currTimeMS.get();
  }
}