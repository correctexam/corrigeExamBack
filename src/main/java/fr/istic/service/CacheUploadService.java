package fr.istic.service;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import javax.inject.Singleton;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;

import com.google.gson.stream.JsonReader;

@Singleton
public class CacheUploadService {


    private final Logger log = LoggerFactory.getLogger(CacheUploadService.class);

    @ConfigProperty(name = "upload.directory")
    String UPLOAD_DIR;

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
                writeFile(inputStream,fileName);
                 } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteFile(long id) {
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


    private void writeFile(InputStream inputStream,String fileName)
            throws IOException {
        byte[] bytes = IOUtils.toByteArray(inputStream);
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

    private String getFileName(MultivaluedMap<String, String> header) {
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

    public File getFile(String fileName) throws Exception{
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
        return Paths.get(fileName).toFile();
    }

    public String getAlignPage(long id, int pagefileter, boolean nonalign) throws IOException {

        String fileName = id + "indexdb.json";
        File customDir = new File(UPLOAD_DIR);
        fileName = customDir.getAbsolutePath() +
                File.separator + fileName;
        if (Paths.get(fileName).toFile().exists()){

        InputStream inputStream = Files.newInputStream(Paths.get(fileName));


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
