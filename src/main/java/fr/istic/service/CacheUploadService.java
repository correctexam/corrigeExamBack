package fr.istic.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.MultivaluedMap;

@Singleton
public class CacheUploadService {

    @ConfigProperty(name = "correctexam.uses3", defaultValue = "false")
    boolean uses3;

    private final Logger log = LoggerFactory.getLogger(CacheUploadService.class);

    @ConfigProperty(name = "upload.directory")
    String UPLOAD_DIR;

    @Inject
    FichierS3Service fichierS3Service;

    protected InputStream getObject(String name)
            throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        if (this.fichierS3Service.isObjectExist(name)) {
            var in = fichierS3Service.getObject(name);
            return in;
        } else {
            return NullInputStream.nullInputStream();
        }
    }

    protected void putObject(String name, byte[] bytes, String contenttype)
            throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        if (this.fichierS3Service.isObjectExist(name)) {
            try {
                this.fichierS3Service.deleteObject(name);
            } catch (ErrorResponseException | InsufficientDataException | InternalException
                    | InvalidResponseException | ServerException | XmlParserException | StorageException e) {
                // putObject overwrites the object anyway: a failed delete must not abort the upload
                log.warn("Could not delete {} before overwriting it", name, e);
            }

        }
        this.fichierS3Service.putObject(name, bytes, contenttype);
    }

    public void uploadFile(@RestForm("file") org.jboss.resteasy.reactive.multipart.FileUpload file) {
        try {
            FileInputStream inputStream = new FileInputStream(file.uploadedFile().toFile());

            String fileName = file.fileName();

            writeFile(inputStream, fileName, "application/json");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File writeStreamToTempFile(InputStream inputStream, String tempFileSuffix) throws IOException {
        FileOutputStream outputStream = null;

        try {
            File file = File.createTempFile("cacheDb", tempFileSuffix);
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            while (true) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                outputStream.write(buffer, 0, bytesRead);
            }
            return file;
        }

        finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    /**
     * Returns the keys of the existing S3 cache objects of an exam (JSON index caches,
     * their parts and the SQLite cache). Read-only: nothing is deleted here, so that the
     * caller can delete the objects only once the database transaction has committed.
     *
     * @return the keys, or an empty list when S3 is not used
     */
    public List<String> listCacheObjectKeys(long examId) {
        List<String> keys = new ArrayList<>();
        if (!this.uses3) {
            return keys;
        }
        for (String candidate : List.of("cache/" + examId + "indexdb.json",
                "cache/" + examId + "_exam_template_indexdb.json",
                "cache/" + examId + ".sqlite3")) {
            if (this.fichierS3Service.isObjectExist(candidate)) {
                keys.add(candidate);
            }
        }
        long k = 1;
        String part = "cache/" + examId + "_part_" + k + "_indexdb.json";
        while (this.fichierS3Service.isObjectExist(part)) {
            keys.add(part);
            k = k + 1;
            part = "cache/" + examId + "_part_" + k + "_indexdb.json";
        }
        return keys;
    }

    /**
     * Deletes the cache files of an exam stored on the local file system (when S3 is not used).
     */
    protected void deleteLocalCacheFiles(long id) {
        if (this.uses3) {
            return;
        }
        File customDir = new File(UPLOAD_DIR);
        if (!customDir.exists()) {
            customDir.mkdirs();
        }
        String fileName = "cache" + File.separator + id + "indexdb.json";

        fileName = customDir.getAbsolutePath() +
                File.separator + fileName;
        if (Paths.get(fileName).toFile().exists()) {
            Paths.get(fileName).toFile().delete();
        } else {
            long k = 1;
            fileName = customDir.getAbsolutePath() +
                    File.separator + "cache" + File.separator + id + "_part_" + k + "_indexdb.json";
            while (Paths.get(fileName).toFile().exists()) {
                Paths.get(fileName).toFile().delete();
                k = k + 1;
                fileName = customDir.getAbsolutePath() +
                        File.separator + "cache" + File.separator + id + "_part_" + k + "_indexdb.json";
            }

        }
    }

    protected void writeFile(InputStream inputStream, String fileName, String contenttype)
            throws IOException {
        byte[] bytes = IOUtils.toByteArray(inputStream);
        if (this.uses3) {
            fileName = "cache/" + fileName;
            try {
                this.putObject(fileName, bytes, contenttype);
            } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        } else {

            File customDir = new File(UPLOAD_DIR);
            if (!customDir.exists()) {
                customDir.mkdirs();
            }
            fileName = customDir.getAbsolutePath() +
                    File.separator + fileName;
            if (Paths.get(fileName).toFile().exists()) {
                Paths.get(fileName).toFile().delete();
            }

            Files.write(Paths.get(fileName), bytes,
                    StandardOpenOption.CREATE_NEW);

        }

    }

    protected String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "";
    }

    public InputStream getFile(String fileName) throws Exception {
        if (this.uses3) {
            String fileName1 = "cache/" + fileName;
            try {
                return this.getObject(fileName1);
            } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                e.printStackTrace();
                return NullInputStream.nullInputStream();
            }
        } else {
            File customDir = new File(UPLOAD_DIR);
            if (!customDir.exists()) {
                customDir.mkdirs();
            }
            File customDir1 = new File(UPLOAD_DIR + File.separator + "cache");
            if (!customDir1.exists()) {
                customDir1.mkdirs();
            }

            fileName = customDir1.getAbsolutePath() +
                    File.separator + fileName;
            if (!Paths.get(fileName).toFile().exists()) {
                log.error("pas de fichier " + fileName);
                throw new Exception("No such file");
            }
            return Files.newInputStream(Paths.get(fileName));

        }

    }

    Map<Long, String> pathsqlite = new HashMap<Long, String>();
    Map<Long, Long> timestampsqlite = new HashMap<Long, Long>();

    public String getAlignPageSqlite(long id, int pagefileter, boolean nonalign) throws IOException {
        InputStream inputStream = null;
        String dbpath = "";
            if (this.uses3) {
                String fileName = "cache/" + id + ".sqlite3";
                var s  =this.fichierS3Service.getObjectTimestamp(fileName);

                if (!pathsqlite.containsKey(id) || !Paths.get(pathsqlite.get(id)).toFile().exists()) {
                    try {
                        if (this.fichierS3Service.isObjectExist(fileName)) {
                            inputStream = this.getObject(fileName);
                            dbpath = this.writeStreamToTempFile(inputStream, id + ".sqlite3").getAbsolutePath();
                            pathsqlite.put(id, dbpath);
                            timestampsqlite.put(id, s);
                        }
                    } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                        e.printStackTrace();
                        return "";
                    }
                } else{
                  dbpath = pathsqlite.get(id);                  
                  var s1 = timestampsqlite.get(id);
                  Path path = Paths.get(dbpath);
                  if (s1 == null || s1 < s) {
                    dbpath = "";
                    Files.delete(path);
                    try {
                        if (this.fichierS3Service.isObjectExist(fileName)) {
                            inputStream = this.getObject(fileName);
                            dbpath = this.writeStreamToTempFile(inputStream, id + ".sqlite3").getAbsolutePath();
                            pathsqlite.put(id, dbpath);
                            timestampsqlite.put(id, s);
                        }
                    } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                        e.printStackTrace();
                        return "";
                    }
                }

                } 
            }
                
            else {
                String fileName = id + ".sqlite3";
                File customDir = new File(UPLOAD_DIR);
                fileName = customDir.getAbsolutePath() +
                        File.separator + fileName;
                if (Paths.get(fileName).toFile().exists()) {
                    dbpath = Paths.get(fileName).toFile().getAbsolutePath();
//                    pathsqlite.put(id, dbpath);

                }
            }
        
        if ("".equals(dbpath) || dbpath == null) {
            return "";
        }

        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:" + dbpath;

            // create a connection to the database
            conn = DriverManager.getConnection(url);

            String query = "select imageData from align where page=" + pagefileter;
            if (nonalign) {
                query = "select imageData from nonalign where page=" + pagefileter;
            }
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                boolean hasAline = rs.next();
                if (hasAline) {
                    String imageData = rs.getString("imageData");

                    return imageData;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {

            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                log.error(ex.getMessage());
            }
        }
        return "";
    }

    public long getCachePageInTemplateSqlite(long id) throws IOException {
        InputStream inputStream = null;
        String dbpath = "";
        if (pathsqlite.containsKey(id) && Paths.get(pathsqlite.get(id)).toFile().exists()) {
            dbpath = pathsqlite.get(id);
        } else {
            if (this.uses3) {
                String fileName = "cache/" + id + ".sqlite3";
                try {
                    if (this.fichierS3Service.isObjectExist(fileName)) {
                        inputStream = this.getObject(fileName);
                        dbpath = this.writeStreamToTempFile(inputStream, id + ".sqlite3").getAbsolutePath();
                        pathsqlite.put(id, dbpath);
                    }
                } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                    e.printStackTrace();
                    return 0;
                }
            } else {
                String fileName = id + ".sqlite3";
                File customDir = new File(UPLOAD_DIR);
                fileName = customDir.getAbsolutePath() +
                        File.separator + fileName;
                if (Paths.get(fileName).toFile().exists()) {
                    dbpath = Paths.get(fileName).toFile().getAbsolutePath();
                    pathsqlite.put(id, dbpath);

                }
            }
        }
        if ("".equals(dbpath) || dbpath == null) {
            return 0;
        }

        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:" + dbpath;

            // create a connection to the database
            conn = DriverManager.getConnection(url);
            // TODO
            String query = "select count(*) from template";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                boolean hasAline = rs.next();
                if (hasAline) {
                    Long page = rs.getLong(1);

                    return page;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {

            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                log.error(ex.getMessage());
            }
        }
        return 0;
    }

    public long getCacheTimeStampSqlite(long id) throws IOException {

        InputStream inputStream = null;
        String dbpath = "";

        if (this.uses3) {
            String fileName = "cache/" + id + ".sqlite3";
            long s = this.fichierS3Service.getObjectTimestamp(fileName);
            if (!pathsqlite.containsKey(id) || !Paths.get(pathsqlite.get(id)).toFile().exists()) {
                try {
                    if (this.fichierS3Service.isObjectExist(fileName)) {
                        inputStream = this.getObject(fileName);
                        dbpath = this.writeStreamToTempFile(inputStream, id + ".sqlite3").getAbsolutePath();
                        pathsqlite.put(id, dbpath);
                        timestampsqlite.put(id, s);
                    }
                } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                    e.printStackTrace();
                    return 0;
                }

            } else {

                dbpath = pathsqlite.get(id);
                var s1 = timestampsqlite.get(id);
                Path path = Paths.get(dbpath);
                if (s1 == null || s1 < s) {
                    dbpath = "";
                    Files.delete(path);
                    try {
                        if (this.fichierS3Service.isObjectExist(fileName)) {
                            inputStream = this.getObject(fileName);
                            dbpath = this.writeStreamToTempFile(inputStream, id + ".sqlite3").getAbsolutePath();
                            pathsqlite.put(id, dbpath);
                            timestampsqlite.put(id, s);
                        }
                    } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                        e.printStackTrace();
                        return 0;
                    }
                }

            }

        } else {
            String fileName = id + ".sqlite3";
            File customDir = new File(UPLOAD_DIR);
            fileName = customDir.getAbsolutePath() +
                    File.separator + fileName;
            if (Paths.get(fileName).toFile().exists()) {
                dbpath = Paths.get(fileName).toFile().getAbsolutePath();
            }

        }
        if ("".equals(dbpath) || dbpath == null) {
            return 0;
        }

        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:" + dbpath;

            // create a connection to the database
            conn = DriverManager.getConnection(url);
            String query = "select DT from exam";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                boolean hasAline = rs.next();
                if (hasAline) {
                    Date time = rs.getTime(1);
                    return time.getTime();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {

            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                log.error(ex.getMessage());
            }
        }
        return 0;
    }

    public long getCacheTimeStamp(long id, String cacheType) throws IOException {
        if (!"sqlite".equals(cacheType)) {
            InputStream inputStream = null;
            long timestamp = 0;
            if (this.uses3) {
                String fileName = "cache/" + id + "indexdb.json";

                try {
                    if (this.fichierS3Service.isObjectExist(fileName)) {
                        inputStream = this.getObject(fileName);
                    } else {
                        fileName = "cache/" + id + "_exam_template_indexdb.json";

                        if (this.fichierS3Service.isObjectExist(fileName)) {

                            inputStream = this.getObject(fileName);

                        }
                    }
                } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                    e.printStackTrace();
                    return 0;
                }
                if (inputStream == null) {
                    return -1;

                }

            } else {
                String fileName = id + "indexdb.json";
                File customDir = new File(UPLOAD_DIR);
                fileName = customDir.getAbsolutePath() +
                        File.separator + fileName;
                if (Paths.get(fileName).toFile().exists()) {

                    inputStream = Files.newInputStream(Paths.get(fileName));

                } else {
                    fileName = "cache/" + id + "_exam_template_indexdb.json";
                    fileName = customDir.getAbsolutePath() +
                            File.separator + fileName;
                    if (Paths.get(fileName).toFile().exists()) {
                        inputStream = Files.newInputStream(Paths.get(fileName));

                    }

                }
                if (inputStream == null) {
                    return -1;

                }

            }

            JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
            reader.beginObject();
            reader.nextName();
            reader.skipValue();
            reader.nextName();
            reader.skipValue();
            reader.nextName();
            reader.beginObject();
            reader.nextName();
            reader.skipValue();
            reader.nextName();
            reader.skipValue();
            reader.nextName();
            reader.skipValue();
            reader.nextName();
            reader.beginArray();

            // exams

            reader.beginObject();
            reader.nextName();
            reader.nextString();
            reader.nextName();
            reader.skipValue();
            reader.nextName();
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                reader.nextName();
                reader.nextLong();
                if (reader.hasNext()) {
                    reader.nextName();
                    timestamp = reader.nextLong();
                }
                reader.endObject();
            }
            reader.endArray();

            reader.endObject();

            // reader.endObject();

            reader.close();
            inputStream.close();
            return timestamp;
        } else {
            return this.getCacheTimeStampSqlite(id);
        }

    }

    public long getCachePageInTemplate(long id) throws IOException {
        InputStream inputStream = null;
        long pageNumber = 0;
        if (this.uses3) {
            String fileName = "cache/" + id + "indexdb.json";

            try {
                if (this.fichierS3Service.isObjectExist(fileName)) {
                    inputStream = this.getObject(fileName);
                } else {
                    fileName = "cache/" + id + "_exam_template_indexdb.json";

                    if (this.fichierS3Service.isObjectExist(fileName)) {

                        inputStream = this.getObject(fileName);

                    }
                }
            } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                e.printStackTrace();
                return 0;
            }
            if (inputStream == null) {
                return this.getCachePageInTemplateSqlite(id);

            }

        } else {
            String fileName = id + "indexdb.json";
            File customDir = new File(UPLOAD_DIR);
            fileName = customDir.getAbsolutePath() +
                    File.separator + fileName;
            if (Paths.get(fileName).toFile().exists()) {

                inputStream = Files.newInputStream(Paths.get(fileName));

            } else {
                fileName = "cache/" + id + "_exam_template_indexdb.json";
                fileName = customDir.getAbsolutePath() +
                        File.separator + fileName;
                if (Paths.get(fileName).toFile().exists()) {
                    inputStream = Files.newInputStream(Paths.get(fileName));

                }

            }
            if (inputStream == null) {
                return this.getCachePageInTemplateSqlite(id);

            }

        }

        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
        reader.beginObject();
        reader.nextName();
        reader.skipValue();
        reader.nextName();
        reader.skipValue();
        reader.nextName();
        reader.beginObject();
        reader.nextName();
        reader.skipValue();
        reader.nextName();
        reader.skipValue();
        reader.nextName();
        reader.skipValue();
        reader.nextName();
        reader.beginArray();

        // exams

        reader.beginObject();
        reader.nextName();
        reader.nextString();
        reader.nextName();
        reader.skipValue();
        reader.nextName();
        reader.skipValue();
        reader.endObject();

        // templates

        reader.beginObject();
        reader.nextName();
        reader.nextString();
        reader.nextName();
        reader.skipValue();
        reader.nextName();
        // reader.skipValue();
        reader.beginArray();
        while (reader.hasNext()) {

            reader.beginObject();
            reader.nextName();
            reader.nextString();
            reader.nextName();
            long l = reader.nextLong();
            if (l > pageNumber) {
                pageNumber = l;
            }
            reader.nextName();
            reader.skipValue();
            reader.nextName();
            reader.skipValue();

            reader.endObject();
        }

        // reader.endObject();

        reader.close();
        inputStream.close();
        return pageNumber;

    }

    public String getAlignPage(long id, int pagefileter, boolean nonalign) throws IOException {
        InputStream inputStream = null;
        if (this.uses3) {
            String fileName = "cache/" + id + "indexdb.json";
            try {
                if (this.fichierS3Service.isObjectExist(fileName)) {
                    inputStream = this.getObject(fileName);
                } else {
                    int part = (pagefileter - 1) / 50;
                    fileName = "cache/" + id + "_part_" + (part + 1) + "_indexdb.json";
                    if (this.fichierS3Service.isObjectExist(fileName)) {
                        inputStream = this.getObject(fileName);
                    }
                }
            } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                e.printStackTrace();
                return "";
            }
            if (inputStream == null) {
                return this.getAlignPageSqlite(id, pagefileter, nonalign);

            }

        } else {
            String fileName = id + "indexdb.json";
            File customDir = new File(UPLOAD_DIR);
            fileName = customDir.getAbsolutePath() +
                    File.separator + fileName;
            if (Paths.get(fileName).toFile().exists()) {

                inputStream = Files.newInputStream(Paths.get(fileName));

            } else {
                int part = (pagefileter - 1) / 50;
                fileName = "cache/" + id + "_part_" + (part + 1) + "_indexdb.json";
                fileName = customDir.getAbsolutePath() +
                        File.separator + fileName;
                if (Paths.get(fileName).toFile().exists()) {

                    inputStream = Files.newInputStream(Paths.get(fileName));

                }

            }
            if (inputStream == null) {
                return this.getAlignPageSqlite(id, pagefileter, nonalign);

            }

        }

        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
        reader.beginObject();
        reader.nextName();
        reader.skipValue();
        reader.nextName();
        reader.skipValue();
        reader.nextName();

        reader.beginObject();
        reader.nextName();
        reader.skipValue();
        reader.nextName();
        reader.skipValue();
        reader.nextName();
        reader.skipValue();
        reader.nextName();
        reader.beginArray();

        // exams

        reader.beginObject();
        reader.nextName();
        reader.nextString();
        reader.nextName();
        reader.skipValue();
        reader.nextName();
        reader.skipValue();
        reader.endObject();

        // templates

        reader.beginObject();
        reader.nextName();
        reader.nextString();
        reader.nextName();
        reader.skipValue();
        reader.nextName();
        reader.skipValue();
        reader.endObject();

        if (nonalign) {
            reader.beginObject();
            reader.nextName();
            reader.nextString();
            reader.nextName();
            reader.skipValue();
            reader.nextName();
            reader.skipValue();
            reader.endObject();
        }

        // alignImages

        reader.beginObject();
        reader.nextName();
        reader.nextString();
        reader.nextName();
        reader.skipValue();
        reader.nextName();
        reader.beginArray();
        boolean found = false;
        boolean end = false;
        while (!found && !end) {
            reader.beginObject();
            // examId
            reader.nextName();
            reader.skipValue();
            // pageNumber
            reader.nextName();
            int page = reader.nextInt();
            if (page == pagefileter) {
                found = true;
                reader.nextName();
                String value = reader.nextString();
                reader.close();
                inputStream.close();
                return value;
            } else {
                // value
                reader.nextName();
                reader.skipValue();
                // id
                reader.nextName();
                reader.skipValue();
                reader.endObject();
            }
            if (!reader.hasNext()) {
                reader.endArray();
                end = true;
            }
        }
        reader.close();
        inputStream.close();

        return null;

    }
}
