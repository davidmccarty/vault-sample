package com.garage.cos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MinioS3Client {

    private AmazonS3 s3Client;

    @Value("${cos.endpoint}")
    private String cosEndpoint;
    @Value("${cos.user}")
    private String cosUser;
    @Value("${cos.password}")
    private String cosPassword;
    private String cosRegion=Regions.US_EAST_1.name();

    @PostConstruct
    private void connect(){
        AWSCredentials credentials = new BasicAWSCredentials(cosUser, cosPassword);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSignerOverride("AWSS3V4SignerType");

        s3Client = AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(cosEndpoint, cosRegion))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    public String uploadString(String bucketName, String keyName, String data) throws IOException {
        System.out.println("Uploading bytes to bucket:" + bucketName + " key:" + keyName);
        ByteArrayInputStream stream = new ByteArrayInputStream(data.getBytes());
        PutObjectRequest req = new PutObjectRequest(bucketName, keyName, stream, null);
        PutObjectResult result = s3Client.putObject(req);
        String msg = "Uploaded string with ETag " + result.getETag();
        System.out.println(msg);
        return msg;
    }

    public String downloadString(String bucketName, String keyName) throws IOException {
        System.out.println("Downloading bytes from bucket:" + bucketName + " key:" + keyName);
        GetObjectRequest req = new GetObjectRequest(bucketName, keyName);
        S3Object object = s3Client.getObject(req);
        ObjectMetadata metadata = object.getObjectMetadata();
        System.out.println("Metadata" + "\n"
                            + "   type: " + metadata.getContentType() + "\n"
                            + "   user: " + metadata.getUserMetadata());
        S3ObjectInputStream stream = object.getObjectContent();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4];
        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        System.out.println("Read " + nRead + " bytes in returned buffer");
        buffer.flush();
        byte[] bytes = buffer.toByteArray();
        stream.close();
        return new String(bytes);
    }

    public void uploadEncrypted(String bucketName, String keyName, byte[] bytes, String type) throws IOException {
        System.out.println("Uploading encrypted bytes to bucket:" + bucketName + " key:" + keyName);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(type);
        Map<String,String> userMetadata = new HashMap<String, String>();
        userMetadata.put("hello","world");
        metadata.setUserMetadata(userMetadata);
        PutObjectRequest req = new PutObjectRequest(bucketName, keyName, new ByteArrayInputStream(bytes), metadata);
        s3Client.putObject(req);
    }




}
