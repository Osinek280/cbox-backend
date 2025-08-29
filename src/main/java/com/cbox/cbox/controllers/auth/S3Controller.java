package com.cbox.cbox.controllers.auth;

import com.cbox.cbox.services.S3Service;
import com.cbox.cbox.services.S3Service.FileNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class S3Controller {
  private final S3Service s3Service;

  @Autowired
  public S3Controller(S3Service s3Service) {
    this.s3Service = s3Service;
  }

  @GetMapping
  public FileNode getFiles(@RequestParam(required = false, defaultValue = "") String prefix) {
    return s3Service.listObjectsInTree(prefix);
  }
}