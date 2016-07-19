package org.companyos.dev.cos_common;

import java.util.concurrent.TimeUnit;

public class CCThread {
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
}
