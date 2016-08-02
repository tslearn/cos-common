package org.companyos.dev.cos_common;

import java.security.MessageDigest;

public class CCMD5 {
  // 十六进制下数字到字符的映射
  private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6",
      "7", "8", "9", "a", "b", "c", "d", "e", "f" };

  public static final boolean verify(String value, String md5Value) {
    if (value == null || md5Value == null)
      return false;

    return md5Value.equals(encode(value));
  }

  public static final String encode(String originString) {
    if (originString != null) {
      try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] results = md.digest(originString.getBytes());
        return byteArrayToHexString(results);
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    return null;
  }

  private static final String byteArrayToHexString(byte[] b) {
    StringBuffer resultSb = new StringBuffer();
    for (int i = 0; i < b.length; i++) {
      resultSb.append(byteToHexString(b[i]));
    }
    return resultSb.toString();
  }

  private static final String byteToHexString(byte b) {
    int n = b;
    if (n < 0)
      n = 256 + n;
    int d1 = n / 16;
    int d2 = n % 16;
    return hexDigits[d1] + hexDigits[d2];
  }
}
