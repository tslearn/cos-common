package org.companyos.dev.cos_common;

import org.json.JSONObject;

/**
 *
 * Created by tianshuo on 16/7/13.
 */
public class CCReturn<T> {
    private boolean success;
    private String message;
    private T value;
    private Exception exception;

    private CCReturn(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return this.success;
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
        return new CCReturn<>(true);
    }

    public static <E> CCReturn<E> success(E ret) {
        return new CCReturn<E>(true).setV(ret);
    }

    public static <E> CCReturn<E> error() {
        return new CCReturn<>(false);
    }

    public static <E> CCReturn<E> error(String message) {
        return new CCReturn<E>(false).setM(message);
    }
    
    public JSONObject toJSON() {
    	String e = this.exception == null ? null : this.exception.toString();
      return new JSONObject()
            .put("s", this.success)
            .put("m", this.message)
            .put("e", e)
            .put("v", this.getV());
    }
}
