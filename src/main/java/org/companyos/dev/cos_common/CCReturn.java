package org.companyos.dev.cos_common;

/**
 *
 * Created by tianshuo on 16/7/13.
 */
public class CCReturn<T> {
    private boolean ok;
    private String message;
    private String debug;
    private T ret;
    private Exception exception;

    private CCReturn(boolean ok) {
        this.ok = ok;
    }

    public boolean isOk() {
        return this.ok;
    }

    public T getR() {
        return this.ret;
    }

    public CCReturn<T> setR(T ret) {
        this.ret = ret;
        return this;
    }

    public String getM() {
        return this.message;
    }

    public CCReturn<T> setM(String message) {
        this.message = message;
        return this;
    }

    public String getD() {
        return this.debug;
    }
    
    public Exception getE() {
        return this.exception;
	}
    
    public CCReturn<T>  setE(Exception exception) {
    	exception.printStackTrace();
        this.exception = exception;
        return this;
    }
      
    public CCReturn<T> setD(String debug) {
        this.debug = debug;
        return this;
    }

    public static <E> CCReturn<E> success() {
        return new CCReturn<>(true);
    }

    public static <E> CCReturn<E> success(E ret) {
        return new CCReturn<E>(true).setR(ret);
    }

    public static <E> CCReturn<E> error() {
        return new CCReturn<>(false);
    }

    public static <E> CCReturn<E> error(String message) {
        return new CCReturn<E>(false).setM(message);
    }
}
