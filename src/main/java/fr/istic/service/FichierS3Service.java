package fr.istic.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Item;

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
        } catch (MinioException
                | IllegalArgumentException  e) {
            e.printStackTrace();
        }
    }

    public long getObjectTimestamp(String name) {
        if (saveasfile) {
            Path path = Paths.get(name);
            return Files.exists(path) ? path.toFile().lastModified() : -1;

        } else {

            try {
                this.createBucketifNotExist();
                var stat = minioClient.statObject(StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(name).build());
                return stat.lastModified().toEpochSecond();
            } catch (ErrorResponseException e) {
                // e.printStackTrace();
                return -1;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
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
                                        new ByteArrayInputStream(bytes), size, Long.valueOf(-1))
                                .contentType(contenttype)
                                .build());
            } catch (MinioException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Lists the objects whose key starts with the given prefix (recursively).
     * In {@code saveasfile} mode, the prefix must be a top-level folder (e.g. {@code "scan/"})
     * and the local files below it are returned with '/'-separated keys.
     *
     * @throws StorageException when the storage cannot be listed
     */
    public List<StorageObject> listObjects(String prefix) {
        List<StorageObject> objects = new ArrayList<>();
        if (saveasfile) {
            Path root = Paths.get(prefix);
            if (!Files.isDirectory(root)) {
                return objects;
            }
            Path base = Paths.get("").toAbsolutePath();
            try (Stream<Path> files = Files.walk(root)) {
                for (Path file : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
                    String key = base.relativize(file.toAbsolutePath()).toString().replace('\\', '/');
                    objects.add(new StorageObject(key, Files.getLastModifiedTime(file).toInstant(), Files.size(file)));
                }
            } catch (IOException e) {
                throw new StorageException(prefix, "Could not list local files", e);
            }
            return objects;
        }
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(true).build());
            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir()) {
                    continue;
                }
                objects.add(new StorageObject(item.objectName(),
                        item.lastModified() != null ? item.lastModified().toInstant() : null, item.size()));
            }
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException
                | IOException e) {
            throw new StorageException(prefix, "Could not list objects of bucket " + bucketName, e);
        }
        return objects;
    }

    public void deleteObject(String name) throws InvalidKeyException, ErrorResponseException, InsufficientDataException,
            InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException,
            IllegalArgumentException, IOException {
        if (saveasfile) {
            // Local file storage: the object key is the relative file path
            try {
                Files.deleteIfExists(Paths.get(name));
            } catch (IOException e) {
                throw new StorageException(name, "Could not delete local file", e);
            }
        } else {
            // Removing a missing object is not an error on S3/MinIO: this call is idempotent
            try {
                this.minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(name).build());
            } catch (MinioException e) {
                throw new StorageException(name, "Could not delete object from bucket " + bucketName, e);
            }
        }
    }
}
