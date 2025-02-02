/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.facebook.internal.Utility;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import junit.framework.Assert;
import org.json.JSONArray;
import org.json.JSONObject;

public class TestUtils {
  public static final double DOUBLE_EQUALS_DELTA = 0.00001;

  public static <T extends Serializable> T serializeAndUnserialize(final T t) {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      new ObjectOutputStream(os).writeObject(t);
      ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

      @SuppressWarnings("unchecked")
      T ret = (T) (new ObjectInputStream(is)).readObject();

      return ret;
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static <E extends Parcelable> E parcelAndUnparcel(final E object) {
    final Parcel writeParcel = Parcel.obtain();
    final Parcel readParcel = Parcel.obtain();
    try {
      writeParcel.writeParcelable(object, 0);
      final byte[] bytes = writeParcel.marshall();
      readParcel.unmarshall(bytes, 0, bytes.length);
      readParcel.setDataPosition(0);
      return readParcel.readParcelable(object.getClass().getClassLoader());
    } finally {
      writeParcel.recycle();
      readParcel.recycle();
    }
  }

  public static void assertSamePermissions(
      final Collection<String> expected, final AccessToken actual) {
    if (expected == null) {
      Assert.assertEquals(null, actual.getPermissions());
    } else {
      for (String p : expected) {
        Assert.assertTrue(actual.getPermissions().contains(p));
      }
      for (String p : actual.getPermissions()) {
        Assert.assertTrue(expected.contains(p));
      }
    }
  }

  public static void assertSameCollectionContents(
      final Collection expected, final Collection actual) {
    if (expected == null) {
      Assert.assertEquals(null, actual);
    } else {
      for (Object p : expected) {
        Assert.assertTrue(actual.contains(p));
      }
      for (Object p : actual) {
        Assert.assertTrue(expected.contains(p));
      }
    }
  }

  public static void assertSamePermissions(
      final Collection<String> expected, final Collection<String> actual) {
    assertSameCollectionContents(expected, actual);
  }

  public static void assertAtLeastExpectedPermissions(
      final Collection<String> expected, final Collection<String> actual) {
    if (expected != null) {
      for (String p : expected) {
        Assert.assertTrue(actual.contains(p));
      }
    }
  }

  private static void assertEqualContents(
      final Bundle a, final Bundle b, boolean collectionOrderMatters) {
    for (String key : a.keySet()) {
      if (!b.containsKey(key)) {
        Assert.fail("bundle does not include key " + key);
      }
      Object aValue = a.get(key);
      Object bValue = b.get(key);
      if (!collectionOrderMatters && aValue instanceof Collection && bValue instanceof Collection) {
        assertSameCollectionContents((Collection) aValue, (Collection) bValue);
      } else {
        Assert.assertEquals(a.get(key), b.get(key));
      }
    }
    for (String key : b.keySet()) {
      if (!a.containsKey(key)) {
        Assert.fail("bundle does not include key " + key);
      }
    }
  }

  public static void assertEqualContentsWithoutOrder(final Bundle a, final Bundle b) {
    assertEqualContents(a, b, false);
  }

  public static void assertEqualContents(final Bundle a, final Bundle b) {
    assertEqualContents(a, b, true);
  }

  @TargetApi(16)
  public static void assertEquals(final JSONObject expected, final JSONObject actual) {
    // JSONObject.equals does not do an order-independent comparison, so let's roll our own  :(
    if (areEqual(expected, actual)) {
      return;
    }
    Assert.failNotEquals("", expected, actual);
  }

  @TargetApi(16)
  public static void assertEquals(final JSONArray expected, final JSONArray actual) {
    // JSONObject.equals does not do an order-independent comparison, so let's roll our own  :(
    if (areEqual(expected, actual)) {
      return;
    }
    Assert.failNotEquals("", expected, actual);
  }

  private static boolean areEqual(final JSONObject expected, final JSONObject actual) {
    // JSONObject.equals does not do an order-independent comparison, so let's roll our own  :(
    if (expected == actual) {
      return true;
    }
    if ((expected == null) || (actual == null)) {
      return false;
    }

    final Iterator<String> expectedKeysIterator = expected.keys();
    final HashSet<String> expectedKeys = new HashSet<String>();
    while (expectedKeysIterator.hasNext()) {
      expectedKeys.add(expectedKeysIterator.next());
    }

    final Iterator<String> actualKeysIterator = actual.keys();
    while (actualKeysIterator.hasNext()) {
      final String key = actualKeysIterator.next();
      if (!areEqual(expected.opt(key), actual.opt(key))) {
        return false;
      }
      expectedKeys.remove(key);
    }
    return expectedKeys.size() == 0;
  }

  private static boolean areEqual(final JSONArray expected, final JSONArray actual) {
    // JSONObject.equals does not do an order-independent comparison, so we need to check values
    // that are JSONObject
    // manually
    if (expected == actual) {
      return true;
    }
    if ((expected == null) || (actual == null)) {
      return false;
    }
    if (expected.length() != actual.length()) {
      return false;
    }

    final int length = expected.length();
    for (int i = 0; i < length; ++i) {
      if (!areEqual(expected.opt(i), actual.opt(i))) {
        return false;
      }
    }
    return true;
  }

  private static boolean areEqual(final Object expected, final Object actual) {
    if (expected == actual) {
      return true;
    }
    if ((expected == null) || (actual == null)) {
      return false;
    }
    if ((expected instanceof JSONObject) && (actual instanceof JSONObject)) {
      return areEqual((JSONObject) expected, (JSONObject) actual);
    }
    if ((expected instanceof JSONArray) && (actual instanceof JSONArray)) {
      return areEqual((JSONArray) expected, (JSONArray) actual);
    }
    return expected.equals(actual);
  }

  public static String getAssetFileStringContents(final Context context, final String assetPath)
      throws IOException {
    InputStream inputStream = null;
    BufferedReader reader = null;
    try {
      final AssetManager assets = context.getResources().getAssets();
      inputStream = assets.open(assetPath);
      reader = new BufferedReader(new InputStreamReader(inputStream));
      final StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
      return sb.toString();
    } finally {
      Utility.closeQuietly(inputStream);
      Utility.closeQuietly(reader);
    }
  }
}
