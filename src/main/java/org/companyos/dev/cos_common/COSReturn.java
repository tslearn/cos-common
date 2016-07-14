package org.companyos.dev.cos_common;

/**
 * Created by tianshuo on 16/7/13.
 */
public class COSReturn<T> {
    private boolean ok;
    private String message;
    private String debug;
    private T ret;

    public COSReturn(boolean ok) {
        this.ok = ok;
    }

    public boolean isOk() {
        return this.ok;
    }

    public T getR() {
        return this.ret;
    }

    public COSReturn<T> setR(T ret) {
        this.ret = ret;
        return this;
    }

    public String getM() {
        return this.message;
    }

    public COSReturn<T> setM(String message) {
        this.message = message;
        return this;
    }

    public String getD() {
        return this.debug;
    }

    public COSReturn<T> setD(String debug) {
        this.debug = debug;
        return this;
    }

    public static <E> COSReturn<E> success() {
        return new COSReturn<E>(true);
    }

    public static <E> COSReturn<E> success(E ret) {
        return new COSReturn<E>(true).setR(ret);
    }

    public static <E> COSReturn<E> error() {
        return new COSReturn<E>(false);
    }

    public static <E> COSReturn<E> error(String message) {
        return new COSReturn<E>(false).setM(message);
    }
}
