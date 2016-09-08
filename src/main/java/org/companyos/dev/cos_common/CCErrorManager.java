package org.companyos.dev.cos_common;

import org.companyos.dev.cos_common.object_tree.OT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Hashtable;


/**
 * Created by tianshuo on 16/9/8.
 */
public class CCErrorManager {
  private static final Logger log
    = LoggerFactory.getLogger(CCErrorManager.class);

  private final static Hashtable<Integer, CCError> CodeHash = new Hashtable();
  private final static Hashtable<String, CCError> MessageHash = new Hashtable();
  private final static Hashtable<Integer, Class<?>> ClassGroupIdHash = new Hashtable();

  public final static CCError getErrorByMessage(String message) {
    return MessageHash.get(message);
  }

  public final static CCError getErrorByCode(Integer code) {
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
      log.error("Error: " + klass.getName() +
        " add to CCErrorManager must define static int CCErrorGroupId");
      return;
    }
    catch (IllegalAccessException e) {
      log.error("Error: " + klass.getName() +
        " add to CCErrorManager must define static int CCErrorGroupId");
      return;
    }

    if (classErrorGroupId <= 0 || classErrorGroupId >= 32768) {
      log.error("Error: " + klass.getName() +
        " static attribute CCErrorGroupId must be large than 0 and small than 32768");
      return;
    }

    // Deal the CCErrorGroupId conflict
    if (ClassGroupIdHash.containsKey(classErrorGroupId)) {
      Class<?> registerClass = ClassGroupIdHash.get(classErrorGroupId);

      if (registerClass.getCanonicalName().equals(klass.getCanonicalName())) {
        log.error("Error: " + klass.getName() +
          " add to CCErrorManager twice");
      }
      else {
        log.error("Error: " + klass.getName() +
          " CCErrorGroupId is been registered by " + registerClass.getCanonicalName());
      }
      return;
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
            if (errorObj instanceof  CCError) {
              Field innerCodeField = errorObj.getClass().getDeclaredField("innerCode");
              innerCodeField.setAccessible(true);
              int innerCode = innerCodeField.getInt(errorObj);

              if (innerCode <= 0 || innerCode >= 32768) {
                log.error("Error: " + klass.getName() + error.getName() +
                  " inner code must be large than 0 and small than 32768");
                continue;
              }


              Field codeField = errorObj.getClass().getDeclaredField("code");
              codeField.setAccessible(true);
              codeField.setInt(errorObj, classErrorGroupId << 16 | innerCode);


              Field message = errorObj.getClass().getDeclaredField("message");
              message.setAccessible(true);

              int iCode = codeField.getInt(errorObj);
              String sMessage = message.get(errorObj).toString();

              if (CodeHash.containsKey(iCode)) {
                log.error(klass.getCanonicalName() + "CCError code confilct, code: " + iCode);
                continue;
              }

              if (MessageHash.containsKey(sMessage)) {
                log.error(klass.getCanonicalName() + "CCError message confilct, message: " + sMessage);
                continue;
              }


              CodeHash.put(iCode, (CCError)errorObj);
              MessageHash.put(sMessage, (CCError)errorObj);
            }
          }
          catch (Throwable t) {
            log.error(t.toString());
          }
        }

      }
    }
  }
}


