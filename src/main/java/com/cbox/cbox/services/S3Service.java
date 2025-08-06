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

import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.UUID;

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

  public List<String> listAllObjects() {
    ListObjectsV2Request request = new ListObjectsV2Request()
        .withBucketName(bucketName)
        .withDelimiter("/");

    ListObjectsV2Result result = s3Client.listObjectsV2(request);

    return result.getCommonPrefixes().stream()
        .map(prefix -> prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix)
        .collect(Collectors.toList());
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