package com.garage.cos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

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
import com.garage.crypt.BouncyCastleService;
import com.garage.vault.VaultTransitService;

import org.bouncycastle.openpgp.PGPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MinioS3Client {

    private AmazonS3 s3Client;

    @Autowired
    BouncyCastleService bouncyCastleService;
    @Autowired
    VaultTransitService vaultTransitService;

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

    public String uploadBytes(String bucketName, String keyName, byte[] bytes) throws IOException {
        System.out.println("Uploading bytes to bucket:" + bucketName + " key:" + keyName);
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        PutObjectRequest req = new PutObjectRequest(bucketName, keyName, stream, metadata);
        PutObjectResult result = s3Client.putObject(req);
        String msg = "Uploaded data with ETag " + result.getETag();
        System.out.println(msg);
        return msg;
    }

    public byte[] downloadBytes(String bucketName, String keyName) throws IOException {
        System.out.println("Downloading bytes from bucket:" + bucketName + " key:" + keyName);
        GetObjectRequest req = new GetObjectRequest(bucketName, keyName);
        S3Object object = s3Client.getObject(req);
        S3ObjectInputStream stream = object.getObjectContent();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4];
        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        System.out.println("Read " + buffer.size() + " bytes in returned buffer");
        buffer.flush();
        byte[] bytes = buffer.toByteArray();
        stream.close();
        System.out.println("Downloaded bytes size = " + bytes.length);
        return bytes;
    }

    public String uploadStringBcEncrypted(String bucket, String key, String data) throws NoSuchProviderException, SignatureException, NoSuchAlgorithmException, IOException, PGPException {
        String encryptedBlock = bouncyCastleService.encrypt(data.getBytes());
        return uploadBytes(bucket, key, encryptedBlock.getBytes());
    }

    public String downloadStringBcEncrypted(String bucket, String key) throws NoSuchProviderException, SignatureException, IOException, PGPException {
        String encryptedBlock = new String(downloadBytes(bucket, key));
        byte[] bytes = bouncyCastleService.decrypt(encryptedBlock);
        return new String(bytes);
    }

    public String uploadStringTransitEncrypted(String bucket, String key, String data, String path) throws URISyntaxException, IOException {
        String encryptedBlock = vaultTransitService.encrypt(path, data.getBytes());
        return uploadBytes(bucket, key, encryptedBlock.getBytes());
    }

    public String downloadStringTransitEncrypted(String bucket, String key, String path) throws IOException {
        String encryptedBlock = new String(downloadBytes(bucket, key));
        return new String(vaultTransitService.decrypt(path, encryptedBlock));
    }

    public String uploadFileBcEncrypted(String bucket, String key, byte[] bytes) throws NoSuchProviderException, SignatureException, NoSuchAlgorithmException, IOException, PGPException {
        String encryptedBlock = bouncyCastleService.encrypt(bytes);
        return uploadBytes(bucket, key, encryptedBlock.getBytes());
    }

    public byte[] downloadFileBcEncrypted(String bucket, String key) throws IOException, NoSuchProviderException, SignatureException, PGPException {
        String encryptedBlock = new String(downloadBytes(bucket, key));
        return bouncyCastleService.decrypt(encryptedBlock);
    }

    public String uploadFileTransitEncrypted(String bucket, String key, byte[] bytes, String path) throws URISyntaxException, IOException {
        String encryptedBlock = vaultTransitService.encrypt(path, bytes);
        return uploadBytes(bucket, key, encryptedBlock.getBytes());
    }

    public byte[] downloadFileTransitEncrypted(String bucket, String key, String path) throws IOException {
        String encryptedBlock = new String(downloadBytes(bucket, key));
        return vaultTransitService.decrypt(path, encryptedBlock);
    }


}
