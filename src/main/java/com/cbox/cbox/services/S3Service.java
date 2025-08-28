package com.cbox.cbox.services;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@Service
public class S3Service {

  private final AmazonS3 s3Client;

  @Value("${aws.bucket.name}")
  private String bucketName;

  public S3Service(@Value("${cloud.aws.s3.endpoint}") String endpoint,
                   @Value("${cloud.aws.region.static}") String region,
                   @Value("${cloud.aws.credentials.access-key}") String accessKey,
                   @Value("${cloud.aws.credentials.secret-key}") String secretKey) {

    AwsClientBuilder.EndpointConfiguration endpointConfig = new AwsClientBuilder.EndpointConfiguration(endpoint, region);
    AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

    this.s3Client = AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(endpointConfig)
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withPathStyleAccessEnabled(true)
        .build();
  }

  public static class FileNode {
    public String name;
    public String type; // "folder" lub "file"
    public String path; // pełna ścieżka (tylko dla plików)
    public String previewUrl; // URL do .webp (tylko dla plików)
    public String downloadUrl; // URL do .skp (tylko dla plików)
    public List<FileNode> children;

    public FileNode() {
      this.children = new ArrayList<>();
    }
  }

  private FileNode getOrCreateChild(List<FileNode> children, String name) {
    for (FileNode child : children) {
      if (child.name.equals(name)) {
        return child; // już istnieje
      }
    }
    // jeśli nie znaleziono, tworzymy nowy
    FileNode newChild = new FileNode();
    newChild.name = name;
    children.add(newChild);
    return newChild;
  }


  public FileNode listObjectsInTree(String prefix) {
    FileNode root = new FileNode();
    root.name = prefix.isEmpty() ? "root" : prefix.substring(0, prefix.length() - 1).substring(prefix.lastIndexOf("/") + 1);
    root.type = "folder";

    ListObjectsV2Request request = new ListObjectsV2Request()
        .withBucketName(bucketName)
        .withPrefix(prefix.isEmpty() ? null : prefix);

    ListObjectsV2Result result;
    Map<String, FileNode> fileNodes = new HashMap<>();

    do {
      result = s3Client.listObjectsV2(request);
      for (S3ObjectSummary summary : result.getObjectSummaries()) {
        String key = summary.getKey();
        if (key.endsWith(".skp") || key.endsWith(".webp")) {
          addToTree(root, key.split("/"), key, summary, fileNodes);
        }
      }
      request.setContinuationToken(result.getNextContinuationToken());
    } while (result.isTruncated());

    assignPresignedUrls(fileNodes);

    return root;
  }

  private void addToTree(FileNode node, String[] parts, String fullKey, S3ObjectSummary summary, Map<String, FileNode> fileNodes) {
    if (parts.length == 0) return;

    System.out.println("node" + node);
    System.out.println("parts" + Arrays.toString(parts));
    System.out.println("  Pełny klucz: " + fullKey);
    System.out.println(summary);
    System.out.println(fileNodes);
    if (summary != null) {
      System.out.println("  Rozmiar: " + summary.getSize());
      System.out.println("  Data modyfikacji: " + summary.getLastModified());
    }

    String part = parts[0];
//    FileNode child = getOrCreateChild(node.children, part);

    if (parts.length == 1 && (part.endsWith(".skp") || part.endsWith(".webp"))) {
      String fileName = part.substring(0, part.lastIndexOf(".")); // bez rozszerzenia
      String parentPath = fullKey.substring(0, fullKey.lastIndexOf("/") + 1);

      FileNode fileNode = fileNodes.computeIfAbsent(parentPath + fileName, k -> {
        FileNode n = new FileNode();
        n.name = fileName;
        n.type = "file";
        n.path = parentPath + fileName + ".skp"; // domyślnie
        return n;
      });

      if (part.endsWith(".skp")) fileNode.downloadUrl = fullKey;
      else if (part.endsWith(".webp")) fileNode.previewUrl = fullKey;

      addChildIfNotExists(node, fileNode);
    } else {
      FileNode child = getOrCreateChild(node.children, part);
      child.type = "folder";
      addToTree(child, Arrays.copyOfRange(parts, 1, parts.length), fullKey, summary, fileNodes);
    }
  }

  private void addChildIfNotExists(FileNode parent, FileNode child) {
    for (FileNode c : parent.children) {
      if (c.name.equals(child.name)) return;
    }
    parent.children.add(child);
  }


  private void assignPresignedUrls(Map<String, FileNode> fileNodes) {
    for (FileNode node : fileNodes.values()) {
      if (node.downloadUrl != null) {
        node.downloadUrl = generatePresignedUrl(node.downloadUrl);
      }
      if (node.previewUrl != null) {
        node.previewUrl = generatePresignedUrl(node.previewUrl);
      }
    }
  }

  private String generatePresignedUrl(String key) {
    java.util.Date expiration = new java.util.Date();
    long expTimeMillis = expiration.getTime() + 1000 * 60 * 60; // 1 godzina
    expiration.setTime(expTimeMillis);

    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key)
        .withExpiration(expiration);
    URL url = s3Client.generatePresignedUrl(request);
    return url.toString();
  }

  public String uploadFile(MultipartFile file) throws IOException {
    String key = UUID.randomUUID() + "-" + file.getOriginalFilename();

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(file.getSize());
    metadata.setContentType(file.getContentType());

    s3Client.putObject(bucketName, key, file.getInputStream(), metadata);
    return s3Client.getUrl(bucketName, key).toString();
  }

  public S3Object downloadFile(String key) {
    return s3Client.getObject(bucketName, key);
  }
}