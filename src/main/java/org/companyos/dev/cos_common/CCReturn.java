package org.companyos.dev.cos_common;

import org.json.JSONObject;

/**
 *
 * Created by tianshuo on 16/7/13.
 */
public class CCReturn<T> {
    private int code;
    private String message;
    private T value;
    private Exception exception;

    private CCReturn(int code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return this.code == 0;
    }

    public T getV() {
        return this.value;
    }

    public CCReturn<T> setV(T retValue) {
        this.value = retValue;
        return this;
    }

    public String getM() {
        return this.message;
    }

    public CCReturn<T> setM(String message) {
        this.message = message;
        return this;
    }


    public Exception getE() {
        return this.exception;
	}

    public CCReturn<T>  setE(Exception exception) {
        this.exception = exception;
        return this;
    }

    public static <E> CCReturn<E> success() {
        return new CCReturn<>(0);
    }

    public static <E> CCReturn<E> success(E ret) {
        return new CCReturn<E>(0).setV(ret);
    }


    public static <E> CCReturn<E> error(int code) {
        return new CCReturn<E>(code);
    }

    public static <E> CCReturn<E> error(CCError error) {
        return new CCReturn<E>(error.getCode());
    }
    
    public JSONObject toJSON() {
    	String e = this.exception == null ? null : this.exception.toString();
      return new JSONObject()
            .put("s", this.code)
            .put("m", this.message)
            .put("e", e)
            .put("v", this.getV());
    }
}
