package org.companyos.dev.cos_common;

import org.companyos.dev.cos_common.object_tree.OT;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Hashtable;


/**
 * Created by tianshuo on 16/9/8.
 */
public class CCErrorManager {
  private final static Hashtable<Long, CCError> CodeHash = new Hashtable();
  private final static Hashtable<String, CCError> MessageHash = new Hashtable();
  private final static Hashtable<Integer, Class<?>> ClassGroupIdHash = new Hashtable();

  public final static CCError getErrorByMessage(String message) {
    return MessageHash.get(message);
  }

  public final static CCError getErrorByCode(int code) {
    return CodeHash.get(code);
  }

  public final static void addClass(Class<?> klass)  {
    // Check the class CCErrorGroupId
    int classErrorGroupId;
    try {
      Field classErrorGroupIdField = klass.getDeclaredField("CCErrorGroupId");
      classErrorGroupIdField.setAccessible(true);
      classErrorGroupId = classErrorGroupIdField.getInt(null);
    }
    catch (NoSuchFieldException e) {
      new Error("Error: " + klass.getName() +
        " add to CCErrorManager must define static int CCErrorGroupId");
      return;
    }
    catch (IllegalAccessException e) {
      new Error("Error: " + klass.getName() +
        " add to CCErrorManager must define static int CCErrorGroupId");
      return;
    }

    if (classErrorGroupId <= 0) {
      new Error("Error: " + klass.getName() +
        " static attribute CCErrorGroupId must be large than 0");
    }

    // Deal the CCErrorGroupId conflict
    if (ClassGroupIdHash.containsKey(classErrorGroupId)) {
      Class<?> registerClass = ClassGroupIdHash.get(classErrorGroupId);

      if (registerClass.getCanonicalName().equals(klass.getCanonicalName())) {
        new Error("Error: " + klass.getName() +
          " add to CCErrorManager twice");
      }
      else {
        new Error("Error: " + klass.getName() +
          " CCErrorGroupId is been registered by " + registerClass.getCanonicalName());
      }
    }
    else {
      ClassGroupIdHash.put(classErrorGroupId, klass);
    }

    // Iterator class Fields
    Field[] errors = klass.getDeclaredFields();
    for(int i = 0; i < errors.length; i++) {
      Field error = errors[i];

      // only deal static error define
      if (Modifier.isStatic(error.getModifiers())) {
        error.setAccessible(true);

        if (!"CodeHash".equals(error.getName()) && !"MessageHash".equals(error.getName())) {
          try {
            Object errorObj = error.get(null);
            Field code = errorObj.getClass().getDeclaredField("code");
            code.setAccessible(true);
            Field message = errorObj.getClass().getDeclaredField("message");
            message.setAccessible(true);

            Long iCode = code.getLong(errorObj);
            String sMessage = message.get(errorObj).toString();

            if (CodeHash.containsKey(iCode)) {
              new Error(klass.getCanonicalName() + "CCError code confilct, code: " + iCode);
              continue;
            }

            if (MessageHash.containsKey(sMessage)) {
              new Error(klass.getCanonicalName() + "CCError message confilct, message: " + sMessage);
              continue;
            }


            CodeHash.put(iCode, (CCError)errorObj);
            MessageHash.put(sMessage, (CCError)errorObj);

          }
          catch (Throwable t) {
            new Error(t.toString());
          }
        }

      }
    }
  }
}


