package fr.istic.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.MultivaluedMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

@Singleton
public class CacheStudentPdfFService {

    @ConfigProperty(name = "correctexam.uses3", defaultValue = "false")
    boolean uses3;

    private final Logger log = LoggerFactory.getLogger(CacheStudentPdfFService.class);

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
                            | InvalidResponseException | ServerException | XmlParserException e) {
                        e.printStackTrace();
                    }


                }
        this.fichierS3Service.putObject(name, bytes, contenttype);
    }

    public void uploadFile(MultipartFormDataInput input, long examId) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

        List<String> fileNames = new ArrayList<>();
        List<InputPart> inputParts = uploadForm.get("file");
        String fileName = null;
        for (InputPart inputPart : inputParts) {
            try {
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                fileName = getFileName(header);
                fileNames.add(fileName);

                InputStream inputStream = inputPart.getBody(InputStream.class, null);

                writeFile(inputStream, fileName, "application/pdf", examId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    protected void deleteFile(long examid) {

        if (this.uses3) {
            // String fileName = id + "indexdb.json";
            String fileName = "exportpdf/" + examid ;
            try {
                if (this.fichierS3Service.isObjectExist(fileName)) {
                    fichierS3Service.deleteObject(fileName);
                }
            } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | ErrorResponseException
                    | InsufficientDataException | InternalException | InvalidResponseException | ServerException
                    | XmlParserException | IOException e) {
                e.printStackTrace();
            }
        } else {
            File customDir = new File(UPLOAD_DIR);
            if (!customDir.exists()) {
                customDir.mkdirs();
            }
             File customDir1 = new File(UPLOAD_DIR + File.separator + "exportpdf"  +File.separator +  examid);
            if (!customDir1.exists()) {
                customDir1.mkdirs();
            }
            String fileName = customDir1.getAbsolutePath() ;

            if (Paths.get(fileName).toFile().exists()) {
                Paths.get(fileName).toFile().delete();
            }

            fileName = customDir.getAbsolutePath() +
                    File.separator + fileName;
            if (Paths.get(fileName).toFile().exists()) {
                try {
                    FileUtils.deleteDirectory(Paths.get(fileName).toFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void writeFile(InputStream inputStream, String fileName, String contenttype, long examId)
            throws IOException {
        byte[] bytes = IOUtils.toByteArray(inputStream);
        if (this.uses3) {
            fileName = "exportpdf/" +examId+ "/" + fileName;
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
             File customDir1 = new File(UPLOAD_DIR + File.separator  + examId);
            if (!customDir1.exists()) {
                customDir1.mkdirs();
            }
            fileName = customDir1.getAbsolutePath() +
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

    public InputStream getFile(long examId, String fileName) throws Exception {
        if (this.uses3) {
            String fileName1 = "exportpdf/" +examId + "/"+ fileName;
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
            File customDir1 = new File(UPLOAD_DIR + File.separator  +"exportpdf"+File.separator  + examId);
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
}
