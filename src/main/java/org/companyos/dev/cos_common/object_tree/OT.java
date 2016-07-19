package org.companyos.dev.cos_common.object_tree;

import java.io.PrintStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.companyos.dev.cos_common.CCReturn;

public class OT {
  static public class Log {
    enum Level {
      Log, Warning, Error, SysLog, SysWarning, SysError, DebugLog, DebugWarning, DebugError
    }
    static class Info {
      final String type;
      final boolean isLog;

      // final boolean isWarning;
      // final boolean isError;

      public Info(String type, boolean isLog, boolean isWarning, boolean isError) {
        this.type = type;
        this.isLog = isLog;
        // this.isWarning = isWarning;
        // this.isError = isError;
      }
    }

    private final static Info getInfo(Level level) {
      switch (level) {
      case Log:
        return new Info("Log", true, false, false);
      case Warning:
        return new Info("Warning", false, true, false);
      case Error:
        return new Info("Error", false, false, true);
      case DebugLog:
        return new Info("DebugLog", true, false, false);
      case DebugWarning:
        return new Info("DebugWarning", false, true, false);
      case DebugError:
        return new Info("DebugError", false, false, true);
      case SysLog:
        return new Info("SysLog", true, false, false);
      case SysWarning:
        return new Info("SysWarning", false, true, false);
      case SysError:
        return new Info("SysError", false, false, true);
      default:
        return null;
      }
    }

    public static Object log(Level logLevel, String outString) {
      OTMessageBase msg = OTThread.currentThread().currentMsg;
      OTNode target = (msg != null) ? msg.target : null;
      String path = "System";
      if (target != null) {
        path = target.$getPath();
      }

      Info info = getInfo(logLevel);

      if (info != null) {
        PrintStream ps = info.isLog ? System.out : System.err;
        synchronized (ps) {
          StackTraceElement callerStacks[] = Thread.currentThread()
              .getStackTrace();
          ps.println(info.type + " : " + path + " : " + outString);
          for (int i = 2; i < callerStacks.length; i++)
            ps.println("  (" + callerStacks[i].getFileName() + ":"
                + callerStacks[i].getLineNumber() + ")");
        }
        if (msg != null) {
          msg.log(ps);
        }
        ps.close();
      }
      return null;
    }
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
        Log.log(Log.Level.Error, "User param not initilized");
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
    static OTNode rootNode;
    static final private OTThreadSystem sysThread = new OTThreadSystem();
    static private OTWebSocketServer websocketServer;

    synchronized public static OTNode start(String host, int port, Class<?> rootNodeCls, boolean isDebug) {
      if (!Runtime.isStart) {
        Runtime.isDebug = isDebug;
        Runtime.websocketServer = new OTWebSocketServer(host, port);
        Runtime.currTimeMS = new AtomicLong(System.currentTimeMillis());
        Runtime.rootNode = OTNode.$createRoot(rootNodeCls);
        Runtime.rootNode.beforeAttach();
        Runtime.rootNode.afterAttach();
        

        Runtime.sysThread.turnOn();
        //OT.Runtime.msgPool.turnOn();
        Runtime.websocketServer.start();
        Runtime.isStart = true;
        return Runtime.rootNode;
      }
      else {
        return null;
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