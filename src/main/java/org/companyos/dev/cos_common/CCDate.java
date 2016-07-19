package org.companyos.dev.cos_common;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by tianshuo on 16/7/19.
 */
public class CCDate {
  private java.util.Date date;

  private static Map<String, DateFormat> FormatCache =  new HashMap<String, DateFormat>() {
    {
      put("yyyyMMdd", new SimpleDateFormat("yyyyMMdd"));
      put("B", null);
    }
  };

  public CCDate() {
    this.date = new java.util.Date();
  }

  public CCDate(java.util.Date date) {
    this.date = date;
  }

  public CCDate(java.sql.Date date) {
    this.date = new java.util.Date(date.getTime());
  }

  public String format(String fmt) {
    // Cached fmt
    if (FormatCache.containsKey(fmt)) {
      return FormatCache.get(fmt).format(this.date);
    }
    // new fmt
    else {
      return new SimpleDateFormat(fmt).format(this.date);
    }
  }
}
