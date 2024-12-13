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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for running the Python script to refine and extract lines from an image.
 */
@Path("/api")
public class CoupageDimageController {

    private final Logger log = LoggerFactory.getLogger(CoupageDimageController.class);

    @POST
    @Path("/coupage-dimage")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response runPythonScript(Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();

        try {
            log.info("Launching Python script...");

            // Retrieve the base64-encoded image data
            String base64Data = requestData.getOrDefault("image", "").toString();
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1]; // Remove "data:image/png;base64," prefix
            }
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            String tempImagePath = "/tmp/uploaded_image.png";
            Files.write(Paths.get(tempImagePath), imageBytes);
            log.info("Image successfully written to: {}", tempImagePath);

            // Define the path to the Python script
            String scriptPath = "src/main/mlt/coupage_dimage.py";

            // Check if the script file exists
            File scriptFile = new File(scriptPath);
            if (!scriptFile.exists()) {
                log.error("Python script file not found: {}", scriptPath);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Python script file not found: " + scriptPath))
                        .build();
            }

            // Run the Python script
            ProcessBuilder pb = new ProcessBuilder("python3", scriptFile.getAbsolutePath(), tempImagePath);
            pb.directory(scriptFile.getParentFile());
            Process process = pb.start();

            // Capture the Python script's standard output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Capture any errors from the Python script
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorOutput.append(errorLine).append("\n");
                log.error("Python Error/Warning: {}", errorLine);
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();
            log.info("Process finished with exit code: {}", exitCode);

            // Build the response JSON
            response.put("exitCode", exitCode);
            response.put("output", output.toString());

            if (exitCode == 0) {
                // Parse the Python script's output
                String scriptOutput = output.toString().trim();
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> parsedOutput = mapper.readValue(scriptOutput, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    response.putAll(parsedOutput);

                    // Include warnings if present
                    if (errorOutput.length() > 0) {
                        response.put("warnings", errorOutput.toString());
                    }

                    return Response.ok(response).build();
                } catch (Exception e) {
                    log.error("Error parsing Python script output", e);
                    response.put("error", "Error parsing Python script output: " + e.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(response)
                            .build();
                }
            } else {
                // Handle cases where the script fails
                response.put("error", "Python script failed with exit code: " + exitCode);
                response.put("errorOutput", errorOutput.toString());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(response)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error executing Python script", e);
            response.put("error", "Error executing Python script: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(response)
                    .build();
        }
    }
}
