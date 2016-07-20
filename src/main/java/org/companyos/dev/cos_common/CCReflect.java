package org.companyos.dev.cos_common;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class CCReflect {
  public final static String buildCallArgsString(Object[] args) {
    if (args == null)
      return null;

    StringBuilder sb = new StringBuilder();
    sb.append("(");

    for (int i = 0; i < args.length; i++) {
      if (i != 0)
        sb.append(", ");

      sb.append(args[i].toString())
          .append(":")
          .append(args[i].getClass().getCanonicalName());
    }

    sb.append(")");

    return sb.toString();
  }

  public final static String buildMethodArgsString(Method method) {
    if (method == null)
      return null;

    Parameter[] args = method.getParameters();

    StringBuilder sb = new StringBuilder();
    sb.append("(");

    for (int i = 0; i < args.length; i++) {
      if (i != 0)
        sb.append(", ");

      sb.append(args[i].getType().getCanonicalName());
    }

    sb.append(")");

    return sb.toString();
  }
}
