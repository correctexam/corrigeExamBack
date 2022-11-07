package fr.istic.service;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
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


    private final Logger log = LoggerFactory.getLogger(FichierS3Service.class);


    public boolean isObjectExist(String name) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(name).build());
            return true;
        } catch (ErrorResponseException e) {
          //  e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }



    public InputStream getObject(String name) throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        try {
            return  minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(name)
                        .build());
        }  catch (MinioException e) {
            throw new IllegalStateException(e);
        }
    }

    public void putObject(String name, byte[] bytes, String contenttype) throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        try {
            long size = bytes.length;
             minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(name).stream(
                            new ByteArrayInputStream(bytes), size,-1)
                        .contentType(contenttype)
                        .build());
        }  catch (MinioException e) {
            throw new IllegalStateException(e);
        }
    }

    public void deleteObject(String name) throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        this.minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(name).build());
    }

}
