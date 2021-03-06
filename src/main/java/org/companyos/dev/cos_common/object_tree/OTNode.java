package org.companyos.dev.cos_common.object_tree;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.companyos.dev.cos_common.CCLightMap;
import org.companyos.dev.cos_common.CCReflect;
import org.companyos.dev.cos_common.CCReturn;


public class OTNode {
  private static final ConcurrentHashMap<Class<?>, Object> $nastedClassObject = new ConcurrentHashMap<Class<?>, Object>();
  private static final ConcurrentHashMap<Class<?>, HashMap<String, Method>> $methodCache = new ConcurrentHashMap<Class<?>, HashMap<String, Method>>();
  private OTNode $parent;
  private String $name;
  private final CCLightMap $children = new CCLightMap();

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
        	  OT.$error("Import class " + klass.getName() + " Error !!! " + klassItr.getName() + "." + md.getName() + " has already been defined!");
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

  protected void afterDetach() {
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
        OT.$error(errorMsg);
        currentThread.lastEvalSuccess = false;
        return CCReturn.error(OTErrorDefine.OTSyntaxNotFound).setM(errorMsg);
      }
      
      currentThread.lastEvalSuccess = true;
      Object ret = method.invoke(this, args);

      if (ret == null)
        return CCReturn.success(null);

      try {
        return (CCReturn<?>) ret;
      }
      catch (Exception e) {
        if (ret instanceof Boolean)
          return CCReturn.<Boolean>success((Boolean)ret);
        else if (ret instanceof String)
          return CCReturn.<String>success((String)ret);
        else if (ret instanceof Integer)
          return CCReturn.<Integer>success((Integer)ret);
        else if (ret instanceof Long)
          return CCReturn.<Long>success((Long)ret);
        else if (ret instanceof Character)
          return CCReturn.<Character>success((Character)ret);
        else if (ret instanceof Byte)
          return CCReturn.<Byte>success((Byte)ret);
        else if (ret instanceof Short)
          return CCReturn.<Short>success((Short)ret);
        else if (ret instanceof Float)
          return CCReturn.<Float>success((Float)ret);
        else if (ret instanceof Double)
          return CCReturn.<Double>success((Double)ret);
        else
          return CCReturn.<Object>success(ret);
      }
    }
    catch (InvocationTargetException e) {
      String errorMsg = currentThread.currentMsg.target.$getPath() + ".on"
          + currentThread.currentMsg.msgName + " Excute Error, Please catch exception in the Syntax";
      currentThread.lastEvalSuccess = false;    
      return CCReturn.error(OTErrorDefine.OTSyntaxExecuteError).setM(errorMsg).setE(e);
    }
    catch (IllegalAccessException e) {
      String errorMsg = currentThread.currentMsg.target.$getPath() + ".on"
          + currentThread.currentMsg.msgName + " Access Deny ";
      currentThread.lastEvalSuccess = false;
      return CCReturn.error(OTErrorDefine.OTSyntaxAccessDeny).setM(errorMsg).setE(e);
    }
    catch (IllegalArgumentException e) {    
      String calledArgs = CCReflect.buildCallArgsString(args);
      String methodArgs = CCReflect.buildMethodArgsString(method);
      
      String errorMsg = currentThread.currentMsg.target.$getPath() + ".on"
          + currentThread.currentMsg.msgName + " Arguments Not Match!"
          + "\nCalledArgs: " + calledArgs
          + "\nMethodArgs: " + methodArgs;      
      
      currentThread.lastEvalSuccess = false;      
      return CCReturn.error(OTErrorDefine.OTSyntaxArgumentsNotMatch).setM(errorMsg).setE(e);
    }
  }

  final OTNode $getChild(String name) {
    return (OTNode) this.$children.get(name);
  }

  final boolean $registerChild(String name, OTNode node) {
    return this.$children.put(name, node);
  }

  final boolean $unregisterChild(OTNode node) {
    return this.$children.remove(node.$getName()) == node;
  }


  final boolean $removeChildren() {
    boolean ret = true;

    for (Object child : this.$children.values()) {
      ret = ret && ((OTNode)child).$remove(false);
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
    	OT.$error("Create object " + name + " error, object name is illegel");
    	return null;
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
    	OT.$error("Create object " + name + "error! class:" + klass.getName() + " constructor argument not match");
        return null;
      }
    }
    catch (NoSuchMethodException e) {
    	OT.$error("Create object " + name + "error! class:" + klass.getName() + " constructor not found");
    	return null;
    }
    catch (Exception e) {
    	OT.$error("Create object " + name + "error, " + e);
    	return null;
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

      if (!this.$registerChild(name, node)) {
    	OT.$error("node name : " + name + " has exist");
        return null;
      }

      node.afterAttach();

      return node;
    }
    else {
      return null;
    }
  }

  final boolean $remove(boolean isRoot) {
    if (isRoot) {
      this.beforeDetach();

      if (!this.$removeChildren()) {
        OT.$error("remove " + this.$getPath() + " children error");
        return false;
      }

      this.afterDetach();
    }
    else {
      if (this.$parent == null) {
        OT.$error("remove " + this.$getPath() + " error, node has not a parent");
        return false;
      }

      this.beforeDetach();

      if (!this.$removeChildren()) {
        OT.$error("remove " + this.$getPath() + " children error");
        return false;
      }

      if (!this.$parent.$unregisterChild(this)) {
        OT.$error("remove " + this.$getPath() + " error, his parent unregister it");
        return false;
      }

      this.afterDetach();
    }

    return true;
  }
}
