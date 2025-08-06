package com.cbox.cbox.controllers.auth;

import com.cbox.cbox.services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class S3Controller {
  private final S3Service s3Service;

  @Autowired
  public S3Controller(S3Service s3Service) {
    this.s3Service = s3Service;
  }

  @GetMapping
  public List<String> getFolders() {
    return s3Service.listAllObjects();
  }

//  @GetMapping
//  public List<String> listAllFiles() {
//    return s3Service.listAllObjects();
//  }
}
