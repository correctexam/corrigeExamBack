package fr.istic.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

@Singleton
public class FichierS3Service {

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "correctexam.bucketname")
    String bucketName;

    @ConfigProperty(name = "correctexam.saveasfile", defaultValue = "false")
    boolean saveasfile = false;

    private final Logger log = LoggerFactory.getLogger(FichierS3Service.class);

    private void createBucketifNotExist() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                // Make a new bucket called 'asiatrip'.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isObjectExist(String name) {
        if (saveasfile) {
            Path path = Paths.get(name);
            return Files.exists(path);

        } else {

            try {
                this.createBucketifNotExist();
                minioClient.statObject(StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(name).build());
                return true;
            } catch (ErrorResponseException e) {
                // e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public InputStream getObject(String name)
            throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        if (saveasfile) {
            Path path = Paths.get(name);
            return Files.newInputStream(path);
        } else {

            try {
                return minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucketName)
                                .object(name)
                                .build());
            } catch (MinioException e) {
                throw new IllegalStateException(e);
            }
        }
    }
    public void uploadObject(String name, String filename, String contenttype) throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException{
         if (!saveasfile) {
        try {

                this.createBucketifNotExist();

                minioClient.uploadObject(
                        UploadObjectArgs.builder()
                                .bucket(bucketName)
                                .object(name).filename(
                                        filename)
                                .contentType(contenttype)
                                .build());
            } catch (MinioException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);

        }}
        else{
            Path patht = Paths.get("template");
            if (!Files.exists(patht)){
                Files.createDirectory(patht);
            }
            Path paths = Paths.get("scan");
            if (!Files.exists(paths)){
                Files.createDirectory(paths);
            }
             Path pathc = Paths.get("cache");
            if (!Files.exists(pathc)){
                Files.createDirectory(pathc);
            }
            Path path = Paths.get(name);
            OutputStream outputStream = Files.newOutputStream(path);
            outputStream.write(Files.readAllBytes(Paths.get(filename)));
            outputStream.close();
        }

    }
    public void putObject(String name, byte[] bytes, String contenttype)
            throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        if (saveasfile) {
            Path patht = Paths.get("template");
            if (!Files.exists(patht)){
                Files.createDirectory(patht);
            }
            Path paths = Paths.get("scan");
            if (!Files.exists(paths)){
                Files.createDirectory(paths);
            }
             Path pathc = Paths.get("cache");
            if (!Files.exists(pathc)){
                Files.createDirectory(pathc);
            }


            Path path = Paths.get(name);
            OutputStream outputStream = Files.newOutputStream(path);
            outputStream.write(bytes);
            outputStream.close();

        } else {

            try {

                this.createBucketifNotExist();
                long size = bytes.length;

                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(name).stream(
                                        new ByteArrayInputStream(bytes), size, -1)
                                .contentType(contenttype)
                                .build());
            } catch (MinioException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }
    }

    public void deleteObject(String name) throws InvalidKeyException, ErrorResponseException, InsufficientDataException,
            InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException,
            IllegalArgumentException, IOException {
        if (saveasfile) {

        } else {

            this.minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(name).build());
        }

    }
}
