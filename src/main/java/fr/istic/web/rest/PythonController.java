package fr.istic.web.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * REST controller to run a Python script
 */
@Path("/api")
public class PythonController {

    private final Logger log = LoggerFactory.getLogger(PythonController.class);

    @POST
    @Path("/run-dan")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response runPythonScript(Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();
    
        try {
            log.info("Lancement du script Python...");
    
            // Retrieve the base64-encoded image data
            String base64Data = requestData.containsKey("imagePath") ? requestData.get("imagePath").toString() : "";
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1]; // Remove "data:image/png;base64," prefix
            }
    
            // Decode base64 and save it as an image file
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            String tempImagePath = "/tmp/uploaded_image.png";
            Files.write(Paths.get(tempImagePath), imageBytes);
            
            // Define the path to the Python script
            String scriptPath = "/home/xpinar/correctExamDAN/corrigeExamBackDAN/src/main/resources/DAN_script/run_dan.py";
    
            // Check if the script file exists
            File scriptFile = new File(scriptPath);
            if (!scriptFile.exists()) {
                log.error("Le fichier script Python est introuvable: " + scriptPath);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Le fichier script Python est introuvable: " + scriptPath))
                        .build();
            }
    
            // Command to run the Python script with the image path as argument
            ProcessBuilder pb = new ProcessBuilder("python3", scriptFile.getAbsolutePath(), tempImagePath);
            pb.directory(scriptFile.getParentFile());  // Set working directory
            Process process = pb.start();
    
            // Read the Python script's standard output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.info("Sortie Python: " + line);
            }
    
            // Read any errors from the Python script
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorOutput.append(errorLine).append("\n");
                log.error("Erreur/Avertissement Python: " + errorLine);
            }
    
            // Wait for the process to finish
            int exitCode = process.waitFor();
            log.info("Processus terminé avec code : " + exitCode);
    
            // Build the response JSON
            response.put("exitCode", exitCode);
            response.put("output", output.toString());
    
            if (exitCode == 0) {
                if (errorOutput.length() > 0) {
                    // If there are warnings
                    response.put("warnings", errorOutput.toString());
                }
                return Response.ok(response).build();
            } else {
                // If there are errors
                response.put("error", errorOutput.toString());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(response)
                        .build();
            }
    
        } catch (Exception e) {
            log.error("Erreur lors de l'exécution du script Python", e);
            response.put("error", "Erreur lors de l'exécution du script Python: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(response)
                    .build();
        }
    }
}    