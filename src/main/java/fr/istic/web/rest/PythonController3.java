package fr.istic.web.rest;

import jakarta.ws.rs.*;
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

@Path("/api")
public class PythonController3 {

    @POST
    @Path("/run-dan3")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response runDan(Map<String, Object> requestData) {
        try {
            // Decode the base64 image and save it to a temporary file
            System.out.println("I am in the pythonController3");
            String base64Data = requestData.getOrDefault("image", "").toString();
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1]; // Remove "data:image/png;base64," prefix
            }
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            String tempImagePath = "/tmp/uploaded_image.png";
            Files.write(Paths.get(tempImagePath), imageBytes);
            System.out.println("Image saved at: " + tempImagePath);

            // Define the Python script path
            String scriptPath = "src/main/resources/DAN_script/dan_script.py";

            // Check if the Python script exists
            File scriptFile = new File(scriptPath);
            if (!scriptFile.exists()) {
                System.err.println("Python script not found at: " + scriptPath);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Python script not found at: " + scriptPath))
                        .build();
            }

            // Check if the image file exists
            if (!Files.exists(Paths.get(tempImagePath))) {
                System.err.println("Image file not found: " + tempImagePath);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Image file not found: " + tempImagePath))
                        .build();
            }

            // Run the Python script with the image path as input
            ProcessBuilder pb = new ProcessBuilder("python3", scriptFile.getAbsolutePath(), tempImagePath);
            pb.directory(scriptFile.getParentFile()); // Set the working directory
            Process process = pb.start();

            // Read the Python script's output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.out.println("PythonController3 output: " + line);
            }

            // Read any errors from the Python script
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
                System.err.println("Python error: " + line);
            }

            // Wait for the Python script to finish
            int exitCode = process.waitFor();
            System.out.println("Python script exited with code: " + exitCode);

            // Handle script execution result
            if (exitCode == 0) {
                System.out.println("Python script executed successfully.");
                // Log the Python script output
                String rawOutput = output.toString();
                System.out.println("Raw Python output: " + rawOutput);

                // Extract the prediction part (JSON-like array in the output)
                /*String predictionText = rawOutput.lines()
                        .filter(l -> l.startsWith("[") && l.endsWith("]"))
                        .findFirst()
                        .orElse("Prediction not found");
                System.out.println("Sending prediction to frontend: " + predictionText); */// Add this debug log
                return Response.ok(Map.of("prediction", rawOutput.trim())).build();
            } else {
                System.err.println("Python script execution failed.");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(Map.of("error", "Python script failed with errors: " + errorOutput.toString().trim()))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error executing script: " + e.getMessage()))
                    .build();
        }
    }
}
