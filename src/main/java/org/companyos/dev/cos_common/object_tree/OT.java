package org.companyos.dev.cos_common.object_tree;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class OT {
  static class Config {
    static public final String STRootName = "$";
    static public final int DefaultMessageMaxDepth = 300;
    static public final int FirstBoostMS = 100;
    static public final int TerminaWaitTimeMS = 8000;
    static public final int ForceQuitWaitTimeMS = 7000;     
  }
  
  static public class Tools {
    
    public static final StringBuilder getStringBuilder() {
      // Single instance make multi thread bug
      return  new StringBuilder();
    }
    
    public final static boolean trySleepMS(long timems) {
      try {
        TimeUnit.MILLISECONDS.sleep(timems);
        return true;
      }
      catch (InterruptedException ex) {
        return false;
      }
    }

    public final static boolean trySleepNanoSeconds(long timens) {
      try {
        TimeUnit.NANOSECONDS.sleep(timens);
        return true;
      }
      catch (InterruptedException ex) {
        return false;
      }
    }
    
    final static String buildCallArgsString(Object[] args) {
      if (args == null)
        return null;
      
      StringBuilder sb = getStringBuilder();
      sb.append("(");
      
      for (int i = 0; i < args.length; i++) {
        if (i != 0)
            sb.append(", ");
        
        sb.append(args[i].getClass().getCanonicalName());
      }

      return sb.toString();
    }
    
    final static String buildMethodArgsString(Method method) {
      if (method == null)
        return null;
      
      Parameter[] args = method.getParameters();
      
      StringBuilder sb = getStringBuilder();
      sb.append("(");
      
      for (int i = 0; i < args.length; i++) {
        if (i != 0)
            sb.append(", ");
        
        sb.append(args[i].getType().getCanonicalName());
      }

      return sb.toString();
    }  
    
    final public static java.sql.Date parseSqlDate(String v) {
      try {
        java.util.Date date = java.util.Date.from(Instant.parse(v));
        return new java.sql.Date(date.getTime());
      }
      catch (Exception e) {
        return null;
      }
    }
  }
  

  
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

    public static void logSysError(String outString) {
      Log.log(Level.SysError, outString);
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
            ps.print("  (" + callerStacks[i].getFileName() + ":"
                + callerStacks[i].getLineNumber() + ")");
          ps.println("");
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
    
    public static boolean sendString(OTReturn ret) {
      OTSocketSlot p = User.getParam();
      if (p != null) {
        return p.sendString(ret);
      }
      else {
        return false;
      }
    }
  
    public static OTReturn evalMsg(OTNode target, String msgName, Object... args) {
      OTThread th = OTThread.currentThread();
      if (th != null) {
        return msgPool.evalMessage(th, target, msgName, args);
      }
      else {
        th = OTThread.initExternal();
        try {
          return msgPool.evalMessage(th, target, msgName, args);
        }
        finally {
          OTThread.stopExternal();
        }
      }
    }

    public static OTReturn evalMsg(String target, String msgName, Object... args) {
      return Message.evalMsg(Runtime.getNodeByPath(target), msgName, args);
    }

    public static OTMessage postMsg(OTNode target, String msgName, Object... args) {
      OTThread th = OTThread.currentThread();
      if (th != null) {
        return msgPool.postMessage(th, 0, target, msgName, args);
      }
      else {
        th = OTThread.initExternal();
        try {
          return msgPool.postMessage(th, 0, target, msgName, args);
        }
        finally {
          OTThread.stopExternal();
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
        th = OTThread.initExternal();
        try {
          return msgPool.postMessage(th, delay, target, msgName, args);
        }
        finally {
          OTThread.stopExternal();
        }
      }
    }

    public static OTMessage delayPostMsg(long delay, String target, String msgName,
        Object... args) {
      return Message.delayPostMsg(delay, Runtime.getNodeByPath(target), msgName, args);
    }
  }
  
  static public class User { 
    private static ConcurrentHashMap<Long, OTSocketSlot> userSockHash = new  ConcurrentHashMap<Long, OTSocketSlot>();

    static synchronized public OTReturn register(long id) {
      OTSocketSlot p = User.getParam();
        
      if (p == null) {
        return OTReturn.getSysError().setM("注册用户失败，用户未初始化");
      }
      
      if (id <= 0) {
        return OTReturn.getSysError().setM("注册用户失败，用户ID错误");
      }
      
      if (userSockHash.containsKey(id)) {
        userSockHash.get(id).send("@UserLoginOtherDevice>{}");
        userSockHash.get(id).close();
        userSockHash.remove(id);
      }
      
      p.setUid(id);  
      userSockHash.put(id, p);
      return OTReturn.getSuccess();
    }
    
    static synchronized public OTReturn lock(long id) { 
      if (userSockHash.get(id) != null) {
        userSockHash.get(id).send("@UserLocked>{}");
        userSockHash.get(id).close();
        userSockHash.remove(id);
      }

      return OTReturn.getSuccess();
    }
    
    static synchronized public OTReturn unregister(long id) {      
      userSockHash.remove(id) ;
      return OTReturn.getSuccess();
    }
    
    public static long getId() {
      OTSocketSlot p = User.getParam();
      if (p != null) {
        return p.getUid();
      }
      else {
        Log.log(Log.Level.Error, "User param not initilized");
        return -1;
      }
    }
    
    static OTSocketSlot getParam() {
      OTThread th = OTThread.currentThread();
      if (th != null) {
        return th.currentMsg.param;
      }
      else {
        return null;
      }
    }
    
    public static OTSocketSlot getSocketSlotByUid(long uid) {
      return userSockHash.get(uid);
    }
    
   static boolean setParam(OTSocketSlot param) {
      OTThread th = OTThread.currentThread();
      if (th != null) {
        th.currentMsg.param = param;
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
    static final private OTSystemThread sysThread = new OTSystemThread();
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