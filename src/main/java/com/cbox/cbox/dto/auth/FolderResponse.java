package com.cbox.cbox.dto.auth;

import java.util.List;

public class FolderResponse {
  private final List<String> items;
  private final boolean isFiles;

  public FolderResponse(List<String> items, boolean isFiles) {
    this.items = items;
    this.isFiles = isFiles;
  }

  public List<String> getItems() {
    return items;
  }

  public boolean isFiles() {
    return isFiles;
  }
}
