package org.companyos.dev.cos_common.object_tree;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.companyos.dev.cos_common.CCReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OT {
  private static final Logger log
      = LoggerFactory.getLogger(OT.class);

  public final static void trace(String msg) {
    log.trace(toOTMessage(msg));
  }

  public final static void debug(String msg) {
    log.debug(toOTMessage(msg));
  }

  public final static void info(String msg) {
    log.info(toOTMessage(msg));
  }

  public final static void warn(String msg) {
    log.warn(toOTMessage(msg));
  }

  public final static void error(String msg) {
    log.error(toOTMessage(msg));
  }

  final static void fatal(String msg) {
    log.error(toOTMessage(msg));
  }

  final static void ot_error(String msg) {
    log.error(toOTMessage(msg));
  }


  private static String toOTMessage(String outString) {
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

    for (int i = 1; i < callerStacks.length; i++) {
      sb.append(i == 1 ? "  @ " : "    #  ")
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

  static public class Message {
    static final OTMessagePool msgPool = new OTMessagePool();
    
    public static boolean sendString(CCReturn<?> ret) {
      OTWebSocketHandler p = User.getCurrentHandler();
      if (p != null) {
        return p.send(ret);
      }
      else {
        return false;
      }
    }
  
    public static CCReturn<?> evalMsg(OTNode target, String msgName, Object... args) {
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

    public static CCReturn<?> evalMsg(String target, String msgName, Object... args) {
      return Message.evalMsg(Runtime.getNodeByPath(target), msgName, args);
    }

    public static OTMessage postMsg(OTNode target, String msgName, Object... args) {
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

    public static OTMessage postMsg(String target, String msgName, Object... args) {
      return Message.postMsg(Runtime.getNodeByPath(target), msgName, args);
    }

    public static OTMessage delayPostMsg(long delay, OTNode target, String msgName,
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

    public static OTMessage delayPostMsg(long delay, String target, String msgName,
        Object... args) {
      return Message.delayPostMsg(delay, Runtime.getNodeByPath(target), msgName, args);
    }
  }
  
  static public class User { 
    private static ConcurrentHashMap<Long, OTWebSocketHandler> userSockHash 
    	= new  ConcurrentHashMap<Long, OTWebSocketHandler>();

    static synchronized public CCReturn<?> register(long uid) {
      OTWebSocketHandler p = User.getCurrentHandler();
        
      if (p == null) {
        return CCReturn.error("注册用户失败，用户未初始化");
      }
      
      if (uid <= 0) {
        return CCReturn.error("注册用户失败，用户ID错误");
      }

      if (userSockHash.contains(uid)) {
    	  return CCReturn.error("USER_ALREADY_LOGIN");
      }
      else {
          userSockHash.put(uid, p);
          return CCReturn.success();
      }
    }
    
    static synchronized public CCReturn<?> lock(long id) { 
      if (userSockHash.get(id) != null) {
        userSockHash.get(id).send("@UserLocked>{}");
        userSockHash.get(id).close();
        userSockHash.remove(id);
      }

      return CCReturn.success();
    }
    
    static synchronized public CCReturn<?> unregister(long id) {      
      userSockHash.remove(id) ;
      return CCReturn.success();
    }
    
    public static long getId() {
      OTWebSocketHandler p = User.getCurrentHandler();
      if (p != null) {
        return p.getUid();
      }
      else {
        OT.ot_error("User param not initilized");
        return -1;
      }
    }
    
    static OTWebSocketHandler getCurrentHandler() {
      OTThread th = OTThread.currentThread();
      if (th != null) {
        return th.currentMsg.handler;
      }
      else {
        return null;
      }
    }
    
    public static OTWebSocketHandler getHandlerByUid(long uid) {
      return userSockHash.get(uid);
    }
    
   static boolean setHandler(OTWebSocketHandler handler) {
      OTThread th = OTThread.currentThread();
      if (th != null) {
        th.currentMsg.handler = handler;
        return true;
      }
      else {
        return false;
      }
    }
  }
  
  static public class Runtime {
    static boolean isDebug;
    static boolean isStart = false;
    static private AtomicLong currTimeMS;
    static private OTNode rootNode;
    static private OTThreadSystem sysThread;
    static private OTWebSocketServer websocketServer;

    synchronized public static OTNode start(String host, int port, Class<?> rootNodeCls, boolean isDebug) {
      boolean isStartMessageService  = false;
      try {
        if (!Runtime.isStart) {
          OTThread.startMessageService();
          isStartMessageService = true;
          Runtime.isDebug = isDebug;
          Runtime.websocketServer = new OTWebSocketServer(host, port);
          Runtime.currTimeMS = new AtomicLong(System.currentTimeMillis());

          Runtime.rootNode = OTNode.$createRoot(rootNodeCls);
          Runtime.rootNode.beforeAttach();
          Runtime.rootNode.afterAttach();

          Runtime.sysThread = new OTThreadSystem();
          Runtime.sysThread.turnOn();

          Runtime.websocketServer.start();
          Runtime.isStart = true;

          return Runtime.rootNode;
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
      if (Runtime.isStart) {
        Runtime.isStart = false;
        Runtime.websocketServer.stop();  
        Runtime.sysThread.close();
        boolean ret = Runtime.rootNode.$removeChildren();
        //ret = ret && OT.Runtime.msgPool.shutDown();
        return ret;
      }
      else {
        return false;
      }
    }
   
    final static boolean synchronizeTime() {
      while (true) {
        long now = System.currentTimeMillis();
        long old = Runtime.currTimeMS.get();

        if (now - old <= 0)
          return false;

        if (now - old > 100)
          System.out.println("synchronizeTime " + (now - old) + " over 50ms");

        if (Runtime.currTimeMS.compareAndSet(old, now))
          return true;
      }
    }

    final static long currentTimeMillis() {
      return Runtime.currTimeMS.get();
    }

    final public static OTNode getNodeByPath(String path) {
      if (path != null && path.startsWith(OTConfig.STRootName)) {
        OTNode ret = Runtime.rootNode;

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
   


  }
}