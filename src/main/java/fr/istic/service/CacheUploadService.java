package fr.istic.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MultivaluedMap;

import java.io.File;
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

import java.io.InputStreamReader;

import com.google.gson.stream.JsonReader;

@Singleton
public class CacheUploadService {




    @ConfigProperty(name = "correctexam.uses3", defaultValue = "false")
    boolean uses3;


    private final Logger log = LoggerFactory.getLogger(CacheUploadService.class);

    @ConfigProperty(name = "upload.directory")
    String UPLOAD_DIR;

    @Inject
    FichierS3Service fichierS3Service;

    protected InputStream getObject(String name) throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
            if (this.fichierS3Service.isObjectExist(name)){
                return  fichierS3Service.getObject(name);
            }
            else {
                return NullInputStream.nullInputStream();
            }
    }

    protected void putObject(String name, byte[] bytes, String contenttype) throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
            this.fichierS3Service.putObject(name, bytes, contenttype);
    }


    public void uploadFile(MultipartFormDataInput input) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

        List<String> fileNames = new ArrayList<>();
        List<InputPart> inputParts = uploadForm.get("file");
        String fileName = null;
        for (InputPart inputPart : inputParts) {
            try {
                MultivaluedMap<String, String> header =
                                                inputPart.getHeaders();
                fileName = getFileName(header);
                fileNames.add(fileName);

                InputStream inputStream = inputPart.getBody(InputStream.class, null);

                writeFile(inputStream,fileName,"application/json");
                 } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    protected void deleteFile(long id) {
        String fileName = id + "indexdb.json";
        File customDir = new File(UPLOAD_DIR);
        if (!customDir.exists()) {
            customDir.mkdirs();
        }
        fileName = customDir.getAbsolutePath() +
                File.separator + fileName;
        if (Paths.get(fileName).toFile().exists()){
            Paths.get(fileName).toFile().delete();
        }

    }


    protected void writeFile(InputStream inputStream,String fileName, String contenttype)
            throws IOException {
        byte[] bytes = IOUtils.toByteArray(inputStream);
        if (this.uses3){
            fileName = "cache/" + fileName;
            try {
                this.putObject(fileName, bytes,contenttype);
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
            if (Paths.get(fileName).toFile().exists()){
                Paths.get(fileName).toFile().delete();
            }

            Files.write(Paths.get(fileName), bytes,
                    StandardOpenOption.CREATE_NEW);

        }

    }

    protected String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.
                getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "";
    }

    public InputStream getFile(String fileName) throws Exception{
        if (this.uses3){
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
            fileName = customDir.getAbsolutePath() +
                    File.separator + fileName;
            if (!Paths.get(fileName).toFile().exists()){
                log.error("pas de fichier " +  fileName);
                        throw new Exception("No such file");
            }
            return  Files.newInputStream(Paths.get(fileName));

        }

    }

    public String getAlignPage(long id, int pagefileter, boolean nonalign) throws IOException {
        InputStream inputStream = null;
        if (this.uses3){
            String fileName = "cache/" + id + "indexdb.json";
            try {
                inputStream = this.getObject(fileName);
            } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                e.printStackTrace();
                return "";
            }
        }else {
            String fileName = id + "indexdb.json";
            File customDir = new File(UPLOAD_DIR);
            fileName = customDir.getAbsolutePath() +
                    File.separator + fileName;
            if (Paths.get(fileName).toFile().exists()){

            inputStream = Files.newInputStream(Paths.get(fileName));

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

//            templates

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

//            alignImages

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
           //examId
           reader.nextName();
           reader.skipValue();
           //pageNumber
           reader.nextName();
           int page = reader.nextInt();
           if (page == pagefileter) {
               found = true;
               reader.nextName();
               String value =reader.nextString();
               reader.close();
               inputStream.close();
               return value;
           }
           else {
               //value
               reader.nextName();
               reader.skipValue();
               //id
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

    }
       return null;


}
}
