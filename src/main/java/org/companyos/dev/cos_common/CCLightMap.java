package org.companyos.dev.cos_common;

import java.util.HashMap;

public class CCLightMap {
  interface IOTLightMap {
    boolean put(String key, Object value);

    Object remove(String key);

    Object get(String key);

    Object[] values();

    int size();
  }

  private final class OTLinarLightMap implements IOTLightMap {
    private String[] keys = new String[0];
    private Object[] vals = new Object[0];

    OTLinarLightMap(OTHashLightMap map) {
      if (map != null) {
        for (java.util.Map.Entry<String, Object> entry : map.hash.entrySet()) {
          this.put(entry.getKey(), entry.getValue());
        }
      }
    }

    public boolean put(String key, Object value) {
      int mid, oldLength = this.keys.length, first = 0, last = oldLength;

      while (first < last) {
        mid = (first + last) / 2;
        int cmp = this.keys[mid].compareTo(key);
        if (cmp > 0) {
          last = mid;
        }
        else if (cmp < 0) {
          first = mid + 1;
        }
        else {
          return false;
        }
      }

      String[] newKeys = new String[oldLength + 1];
      Object[] newVals = new Object[oldLength + 1];

      if (first > 0) {
        System.arraycopy(this.keys, 0, newKeys, 0, first);
        System.arraycopy(this.vals, 0, newVals, 0, first);
      }

      newKeys[first] = key;
      newVals[first] = value;

      if (oldLength - first > 0) {
        System.arraycopy(this.keys, first, newKeys, first + 1, oldLength
            - first);
        System.arraycopy(this.vals, first, newVals, first + 1, oldLength
            - first);
      }

      this.keys = newKeys;
      this.vals = newVals;

      return true;
    }

    final public Object remove(String key) {
      int low = 0, high = this.keys.length - 1;

      while (low <= high) {
        int mid = (low + high) / 2;
        int cmp = this.keys[mid].compareTo(key);
        if (cmp == 0) { // find it
          Object ret = this.vals[mid];
          int newLength = this.keys.length - 1;
          String[] newKeys = new String[newLength];
          Object[] newVals = new Object[newLength];

          if (mid > 0) {
            System.arraycopy(this.keys, 0, newKeys, 0, mid);
            System.arraycopy(this.vals, 0, newVals, 0, mid);
          }

          if (newLength - mid > 0) {
            System.arraycopy(this.keys, mid + 1, newKeys, mid, newLength - mid);
            System.arraycopy(this.vals, mid + 1, newVals, mid, newLength - mid);
          }

          this.keys = newKeys;
          this.vals = newVals;

          return ret;
        }
        else if (cmp > 0)
          high = mid - 1;
        else
          low = mid + 1;
      }
      
      return null;
    }

    final public Object get(String key) {
      int low = 0, high = this.keys.length - 1;

      while (low <= high) {
        int mid = (low + high) / 2;
        int cmp = this.keys[mid].compareTo(key);
        if (cmp == 0)
          return this.vals[mid];
        else if (cmp > 0)
          high = mid - 1;
        else
          low = mid + 1;
      }
      return null;
    }

    public Object[] values() {
      return this.vals.clone();
    }

    public int size() {
      return this.keys.length;
    }
  }

  private final class OTHashLightMap implements IOTLightMap {
    final HashMap<String, Object> hash = new HashMap<String, Object>();

    public OTHashLightMap(OTLinarLightMap map) {
      for (int i = map.keys.length - 1; i >= 0; i--) {
        this.hash.put(map.keys[i], map.vals[i]);
      }
    }

    final public boolean put(String key, Object value) {
      return this.hash.putIfAbsent(key, value) == null;
    }

    final public Object remove(String key) {
      return this.hash.remove(key);
    }

    final public Object get(String key) {
      return this.hash.get(key);
    }

    public Object[] values() {
      Object[] ret = new Object[this.hash.size()];
      int pos = 0;
      for (java.util.Map.Entry<String, Object> entry : this.hash.entrySet()) {
        ret[pos++] = entry.getValue();
      }
      return ret;
    }

    public int size() {
      return this.hash.size();
    }
  }

  static final long CONVERTSIZE = 32L;
  private IOTLightMap container = null;

  public final synchronized boolean put(String key, Object value) {
    if (key == null || value == null)
      return false;

    if (this.container == null) {
      this.container = new OTLinarLightMap(null);
    }

    boolean ret = this.container.put(key, value);
    
    if (ret && this.container.size() == CONVERTSIZE) {
      this.container = new OTHashLightMap((OTLinarLightMap) this.container);
    }
    
    return ret;
  }

  public final synchronized Object remove(String key) {
    if (this.container == null)
      return null;

    Object ret = this.container.remove(key);

    if (ret == null)
      return null;

    int size = this.container.size();

    if (size == 0) {
      this.container = null;
    }
    else if (size == CONVERTSIZE) {
      this.container = new OTLinarLightMap((OTHashLightMap) this.container);
    }

    return ret;
  }

  public final synchronized Object get(String key) {
    return (this.container != null) ? this.container.get(key) : null;
  }

  public final synchronized int size() {
    return (this.container != null) ? this.container.size() : 0;
  }

  public final synchronized Object[] values() {
    return (this.container != null) ? this.container.values() : new Object[0];
  }
}
