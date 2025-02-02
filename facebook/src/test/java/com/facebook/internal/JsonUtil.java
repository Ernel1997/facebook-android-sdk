/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.internal;

import android.annotation.SuppressLint;
import java.util.*;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * com.facebook.internal is solely for the use of other packages within the Facebook SDK for
 * Android. Use of any of the classes in this package is unsupported, and they may be modified or
 * removed without warning at any time.
 */
class JsonUtil {
  static void jsonObjectClear(JSONObject jsonObject) {
    @SuppressWarnings("unchecked")
    Iterator<String> keys = jsonObject.keys();
    while (keys.hasNext()) {
      keys.next();
      keys.remove();
    }
  }

  static boolean jsonObjectContainsValue(JSONObject jsonObject, Object value) {
    @SuppressWarnings("unchecked")
    Iterator<String> keys = jsonObject.keys();
    while (keys.hasNext()) {
      Object thisValue = jsonObject.opt(keys.next());
      if (thisValue != null && thisValue.equals(value)) {
        return true;
      }
    }
    return false;
  }

  private static final class JSONObjectEntry implements Map.Entry<String, Object> {
    private final String key;
    private final Object value;

    JSONObjectEntry(String key, Object value) {
      this.key = key;
      this.value = value;
    }

    @SuppressLint("FieldGetter")
    @Override
    public String getKey() {
      return this.key;
    }

    @Override
    public Object getValue() {
      return this.value;
    }

    @Override
    public Object setValue(Object object) {
      throw new UnsupportedOperationException("JSONObjectEntry is immutable");
    }
  }

  static Set<Map.Entry<String, Object>> jsonObjectEntrySet(JSONObject jsonObject) {
    HashSet<Map.Entry<String, Object>> result = new HashSet<Map.Entry<String, Object>>();

    @SuppressWarnings("unchecked")
    Iterator<String> keys = jsonObject.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      Object value = jsonObject.opt(key);
      result.add(new JSONObjectEntry(key, value));
    }

    return result;
  }

  static Set<String> jsonObjectKeySet(JSONObject jsonObject) {
    HashSet<String> result = new HashSet<String>();

    @SuppressWarnings("unchecked")
    Iterator<String> keys = jsonObject.keys();
    while (keys.hasNext()) {
      result.add(keys.next());
    }

    return result;
  }

  static void jsonObjectPutAll(JSONObject jsonObject, Map<String, Object> map) {
    Set<Map.Entry<String, Object>> entrySet = map.entrySet();
    for (Map.Entry<String, Object> entry : entrySet) {
      try {
        jsonObject.putOpt(entry.getKey(), entry.getValue());
      } catch (JSONException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }

  static Collection<Object> jsonObjectValues(JSONObject jsonObject) {
    ArrayList<Object> result = new ArrayList<Object>();

    @SuppressWarnings("unchecked")
    Iterator<String> keys = jsonObject.keys();
    while (keys.hasNext()) {
      result.add(jsonObject.opt(keys.next()));
    }

    return result;
  }
}
