package org.companyos.dev.cos_common;

import java.text.SimpleDateFormat;

/**
 * Created by tianshuo on 16/7/19.
 */
public class CCDate {
  private java.util.Date date;


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
    return new SimpleDateFormat(fmt).format(fmt);
  }
}
