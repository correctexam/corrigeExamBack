package fr.istic.web.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/api")
public class PythonController2 {

    @POST
@Path("/run-dan2")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response runDan(Map<String, Object> requestData) {
    try {
        // Log the incoming request
        //System.out.println("Incoming requestData: " + requestData);

        // Retrieve the base64-encoded image data
        String base64Data = requestData.containsKey("image") ? requestData.get("image").toString() : "";
        if (base64Data.contains(",")) {
            base64Data = base64Data.split(",")[1]; // Remove "data:image/png;base64," prefix
        }

        // Decode base64 and save it as an image file
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        String tempImagePath = "/tmp/uploaded_image.png";
        Files.write(Paths.get(tempImagePath), imageBytes);
        System.out.println("Image saved at: " + tempImagePath);

        // Prepare payload for the Python server
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> pythonPayload = Map.of("imagePath", tempImagePath);
        String jsonRequestData = objectMapper.writeValueAsString(pythonPayload);

        // Log the serialized JSON payload
        System.out.println("Payload to Python server: " + jsonRequestData);

        // Create the HTTP client
        HttpClient client = HttpClient.newHttpClient();

        // Build the HTTP POST request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:5000/predict")) // Python server endpoint
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestData))
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Log the Python server response
        System.out.println("Python server response: " + response.body());

        // Return the response from the Python server
        return Response.ok(response.body()).build();
    } catch (Exception e) {
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error calling Python server: " + e.getMessage()))
                .build();
    }
}



}
