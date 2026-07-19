package com.farmtofolk.farmtofolk_ledger.storage;

public final class ThumbnailKeys {

  private static final String THUMB_SUFFIX = "_400.jpg";

  private ThumbnailKeys() {}

  public static String forOriginal(String originalKey) {
    if (originalKey == null || originalKey.isBlank()) {
      return null;
    }
    int lastSlash = originalKey.lastIndexOf('/');
    if (lastSlash < 0) {
      return "thumbs/" + stripExtension(originalKey) + THUMB_SUFFIX;
    }
    String directory = originalKey.substring(0, lastSlash);
    String filename = originalKey.substring(lastSlash + 1);
    return directory + "/thumbs/" + stripExtension(filename) + THUMB_SUFFIX;
  }

  private static String stripExtension(String filename) {
    int dot = filename.lastIndexOf('.');
    if (dot <= 0) {
      return filename;
    }
    return filename.substring(0, dot);
  }
}
