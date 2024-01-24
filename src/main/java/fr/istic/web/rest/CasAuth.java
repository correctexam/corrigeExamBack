package fr.istic.web.rest;

import fr.istic.security.jwt.TokenProvider;
import fr.istic.service.AuthenticationService;
import fr.istic.service.UserService;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Verify service ticket sent from web app
 */
@Path("/api/cas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class CasAuth {
    private final Logger log = LoggerFactory.getLogger(CasAuth.class);
    final AuthenticationService authenticationService;
    final TokenProvider tokenProvider;
    final UserService userService;
    @ConfigProperty(name = "configcas.server_cas")
    String server_cas;
    @ConfigProperty(name = "configcas.domain_service")
    String domain_service;

    @Inject
    public CasAuth(AuthenticationService authenticationService, TokenProvider tokenProvider, UserService userService) {
        this.authenticationService = authenticationService;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @GET
    @Path("/authenticate/{st}")
    @PermitAll
    public Response authorize(@PathParam("st") String serviceTicket) {
        log.debug("AUTHORIZE CAS ENDPOINT REACHED");
        String responseCAS;
        String constructedURL = String.format("%s/serviceValidate?ticket=%s&service=%s", server_cas, serviceTicket, domain_service);
        try {
            responseCAS = sendGET(constructedURL);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(responseCAS.getBytes(StandardCharsets.UTF_8)));
            doc.getDocumentElement().normalize();
            String login = doc.getElementsByTagName("cas:user").item(0).getTextContent();
            String email = doc.getElementsByTagName("cas:mail").item(0).getTextContent();
            String lastName = doc.getElementsByTagName("cas:sn").item(0).getTextContent();
            String firstName = doc.getElementsByTagName("cas:givenName").item(0).getTextContent();
            // if the user do not exist yet create an account
            if (userService.getUserWithAuthoritiesByLogin(login).isEmpty()) {
                userService.createUserOnlyLogin(login, email, lastName, firstName);
            }
            QuarkusSecurityIdentity identity = authenticationService.authenticateNoPwd(login);
            String jwt = tokenProvider.createToken(identity, true);
            return Response.ok().entity(new UserJWTController.JWTToken(jwt)).header("Authorization", "Bearer " + jwt).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String sendGET(String https_url) throws IOException {
        URL url = new URL(https_url);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();
            return response.toString();
        }
        return responseCode+"";
    }
}
