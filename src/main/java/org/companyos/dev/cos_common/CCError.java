package org.companyos.dev.cos_common;

public class CCError {
  private int code;
  private String message;

  public CCError(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return this.code;
  }

  public String getMessage() {
    return this.message;
  }
}
