package org.companyos.dev.cos_common.object_tree;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.companyos.dev.cos_common.CCReflect;
import org.companyos.dev.cos_common.CCReturn;


public class OTNode {
  private static final ConcurrentHashMap<Class<?>, Object> $nastedClassObject = new ConcurrentHashMap<Class<?>, Object>();
  private static final ConcurrentHashMap<Class<?>, HashMap<String, Method>> $methodCache = new ConcurrentHashMap<Class<?>, HashMap<String, Method>>();
  private OTNode $parent;
  private String $name;
  private final ConcurrentHashMap<String, OTNode> $children = new ConcurrentHashMap<String, OTNode>();

  static boolean $isOTNodeName(String name) {
    return (name != null) && name.matches("^[_#$@A-Za-z0-9\u4E00-\u9FA5]+$");
  }

  static boolean $isOTMessageName(String message) {
    return (message != null) && message.matches("^[_A-Za-z][_A-Za-z0-9]*$");
  }

  private static void $cacheMessage(Class<?> klass) {
    if ($methodCache.containsKey(klass))
      return;

    HashMap<String, Method> map = new HashMap<String, Method>();
    Class<?> klassItr = klass;
  
    while (klassItr != null) {
      Method[] methods = klassItr.getDeclaredMethods();
      for (Method md : methods) {
        if (md.getName().startsWith("on")) {
          md.setAccessible(true);
          String methodName = md.getName().substring(2);
          if ($isOTMessageName(methodName)
              && map.putIfAbsent(methodName, md) != null) {
            OT.Log.log(
                OT.Log.Level.Error,
                "Import class " + klass.getName() + " Error !!! " + klassItr.getName() + "." + md.getName() + " has already been defined!");
          }
        }
      }
      klassItr = klassItr.getSuperclass();
    }

    $methodCache.putIfAbsent(klass, map);
  }

  protected void beforeAttach() {
  }

  protected void afterAttach() {
  }

  protected void beforeDetach() {
  }

  protected void afterDetack() {
  }

  final OTNode $getParent() {
    return this.$parent;
  }

  final public String $getName() {
    return this.$name;
  }

  final public String $getPath() {
    String ret = null;
    OTNode node = this;

    while (node != null) {
      ret = (ret == null) ? node.$getName() : node.$getName() + "." + ret;
      node = node.$parent;
    }

    if (ret != null && ret.startsWith(OTConfig.STRootName))
      return ret;
    else
      return null;
  }
  
  
  final CCReturn<?> $eval(OTThread currentThread, Object[] args) {
    Method method = null;
    try {
      method = $methodCache.get(this.getClass()).get(
          currentThread.currentMsg.msgName);

      if (method == null) {
        String errorMsg =   currentThread.currentMsg.target.$getPath() + ".on"
            + currentThread.currentMsg.msgName + " Syntax not found ";
        OT.Log.log(OT.Log.Level.Error, errorMsg);
        currentThread.lastEvalSuccess = false;
        return CCReturn.error(errorMsg);
      }
      
      currentThread.lastEvalSuccess = true;
      return (CCReturn<?>)method.invoke(this, args);
    }
    catch (InvocationTargetException e) {
      String errorMsg = currentThread.currentMsg.target.$getPath() + ".on"
          + currentThread.currentMsg.msgName + " Excute Error, Please catch exception in the Syntax";
      currentThread.lastEvalSuccess = false;    
      return CCReturn.error(errorMsg).setE(e);
    }
    catch (IllegalAccessException e) {
      String errorMsg = currentThread.currentMsg.target.$getPath() + ".on"
          + currentThread.currentMsg.msgName + " Access Deny ";
      currentThread.lastEvalSuccess = false;
      return CCReturn.error(errorMsg).setE(e);
    }
    catch (IllegalArgumentException e) {    
      String calledArgs = CCReflect.buildCallArgsString(args);
      String methodArgs = CCReflect.buildMethodArgsString(method);
      
      String errorMsg = currentThread.currentMsg.target.$getPath() + ".on"
          + currentThread.currentMsg.msgName + " Arguments Not Match!"
          + "\nCalledArgs: " + calledArgs
          + "\nMethodArgs: " + methodArgs;      
      
      currentThread.lastEvalSuccess = false;      
      return CCReturn.error(errorMsg).setE(e);
    }
  }

  final OTNode $getChild(String name) {
    return this.$children.get(name);
  }

  final OTNode $registerChild(String name, OTNode node) {
    return this.$children.put(name, node);
  }

  final boolean $unregisterChild(OTNode node) {
    return this.$children.remove(node.$getName()) == node;
  }


  final boolean $removeChildren() {
    boolean ret = true;

    for (OTNode child : this.$children.values()) {
      ret = ret && child.$remove();
    }

    return ret;
  }

  final static Object $createNestedClassParent(Class<?> parentClass)
      throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    if (!parentClass.isMemberClass()
        || Modifier.isStatic(parentClass.getModifiers())) {
      return parentClass.newInstance();
    }

    Object cache = $nastedClassObject.get(parentClass);
    if (cache != null)
      return cache;

    Constructor<?>[] constructors = parentClass.getDeclaredConstructors();
    Object[] args = new Object[1];
    for (Constructor<?> cs : constructors) {
      Class<?>[] paramTypesClass = cs.getParameterTypes();
      if (paramTypesClass.length == 1) {
        args[0] = $createNestedClassParent(paramTypesClass[0]);
        cs.setAccessible(true);
        Object ret = cs.newInstance(args);
        $nastedClassObject.put(parentClass, ret);
        return ret;
      }
    }

    return null;
  }

  final static OTNode $createNode(OTNode parent, String name, Class<?> klass,
      Object... args) {

    if (!OTNode.$isOTNodeName(name)) {
      return (OTNode) OT.Log.log(OT.Log.Level.Error, "Create object " + name + " error, object name is illegel");
    }

    try {
      Class<?>[] argsCls = null;
      Constructor<?> constructor = null;
      if (!klass.isMemberClass() || Modifier.isStatic(klass.getModifiers())) {
        argsCls = new Class[(args != null) ? args.length : 0];
        for (int i = 0; i < args.length; i++) {
          argsCls[i] = args[i].getClass();
        }
        constructor = klass.getDeclaredConstructor(argsCls);
      }
      else { // Nested none static class
        argsCls = new Class[(args != null) ? args.length + 1 : 1];
        for (int i = 1; i <= args.length; i++) {
          argsCls[i] = args[i].getClass();
        }
        Constructor<?>[] constructors = klass.getDeclaredConstructors();
        Object[] newArgs = new Object[args.length + 1];
        for (Constructor<?> cs : constructors) {
          Class<?>[] paramTypesClass = cs.getParameterTypes();
          if (paramTypesClass.length > 0) {
            argsCls[0] = paramTypesClass[0];
          }
          if (klass.getDeclaredConstructor(argsCls).equals(cs)) {
            constructor = cs;
            newArgs[0] = $createNestedClassParent(paramTypesClass[0]);
          }
        }
        System.arraycopy(args, 0, newArgs, 1, args.length);
        args = newArgs;
      }

      
      if (constructor != null) {
        constructor.setAccessible(true);
        OTNode.$cacheMessage(klass);
        OTNode node = (OTNode) constructor.newInstance(args);
        node.$name = name;
        return node;
      }
      else {
        return (OTNode) OT.Log.log(
            OT.Log.Level.Error,
            "Create object " + name + "error! class:" + klass.getName() + " constructor argument not match");
      }
    }
    catch (NoSuchMethodException e) {
      return (OTNode) OT.Log.log(
          OT.Log.Level.Error,
          "Create object " + name + "error! class:" + klass.getName() + " constructor not found");
    }
    catch (Exception e) {
      return (OTNode) OT.Log.log(
          OT.Log.Level.Error,
          "Create object " + name + "error, " + e);
    }
  }

  final static OTNode $createRoot(Class<?> klass) {
    OTNode node = OTNode.$createNode(null, OTConfig.STRootName, klass);
    node.$parent = null;
    return node;
  }

  final public OTNode $createChild(String name, Class<?> klass, Object... args) {
    OTNode node = OTNode.$createNode(this, name, klass, args);
    if (node != null) {
      node.$parent = this;
      node.beforeAttach();

      if (this.$registerChild(name, node) == null) {
        return (OTNode) OT.Log
            .log(OT.Log.Level.Error, "node name : " + name + " has exist");
      }

      node.afterAttach();

      return node;
    }
    else {
      return null;
    }
  }

  final boolean $remove() {
    if (this.$parent == null) {
      OT.Log.log(OT.Log.Level.SysError, "System error");
      return false;
    }

    this.beforeDetach();

    if (!this.$removeChildren()) {
      OT.Log.log(OT.Log.Level.SysError, "System error");
      return false;
    }

    if (!this.$parent.$unregisterChild(this)) {
      OT.Log.log(OT.Log.Level.SysError, "System error");
      return false;
    }

    this.afterDetack();

    return true;
  }
}
