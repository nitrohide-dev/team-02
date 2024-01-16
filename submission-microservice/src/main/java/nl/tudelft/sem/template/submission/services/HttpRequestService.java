package nl.tudelft.sem.template.submission.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.submission.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.submission.models.RequestType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

@Service
public class HttpRequestService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtTokenVerifier jwtTokenVerifier;

    private final String userMicroserviceUrl = "http://localhost:8085/";
    private final String reviewMicroserviceUrl = "http://localhost:8082/";

    public HttpRequestService(JwtTokenVerifier jwtTokenVerifier) {
        this.jwtTokenVerifier = jwtTokenVerifier;
    }

    public class BadResponseException extends RuntimeException {
        // Custom exception class for representing bad responses
        public BadResponseException(String message) {
            super(message);
        }
    }

    /**
     * Returns response as a string.
     *
     * @param url url
     * @return response
     * @throws IOException          if http request was not successful
     * @throws InterruptedException if request was interrupted
     */
    private String getResponse(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + jwtTokenVerifier.getLastEnteredToken())
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    /**
     * Returns full url depending on the requested microservice.
     *
     * @param apiUrl      second part of request
     * @param requestType requested microservice
     * @return full url
     */
    private String buildUrl(String apiUrl, RequestType requestType) {
        try {
            if (requestType.equals(RequestType.REVIEW)) {
                return reviewMicroserviceUrl + apiUrl.replace(" ", "%20");
            }
            return userMicroserviceUrl + apiUrl.replace(" ", "%20");
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong when creating the url");
        }
    }

    /**
     * Makes request and returns a single attribute from json.
     *
     * @param url         url
     * @param requestType type of request
     * @param attribute   attribute name
     * @return attribute as a string
     */
    public String getAttribute(String url, RequestType requestType, String attribute) {
        url = buildUrl(url, requestType);
        try {
            String response = getResponse(url);
            try {
                JsonNode jsonNode = objectMapper.readTree(response);
                return jsonNode.get(attribute).asText();
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error occurred while processing json");
            }
        } catch (IOException | InterruptedException exception) {
            throw new BadResponseException("Error occurred while making the HTTP request");
        }
    }

    /**
     * get list request method.
     *
     * @param url          where to send the get request to
     * @param responseType what type is the object that we are trying to receive
     * @param requestType  where to send the request to
     * @return object which is the result of the get query
     */
    public <T> T get(String url, Class<T> responseType, RequestType requestType) {
        url = buildUrl(url, requestType);
        try {
            String response = getResponse(url);
            try {
                T object;
                object = objectMapper.readValue(response, responseType);
                return object;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error occurred while processing json");
            }

        } catch (IOException | InterruptedException exception) {
            throw new BadResponseException("Error occurred while making the HTTP request");
        }
    }

    /**
     * get list request method.
     *
     * @param url          where to send the get request to
     * @param responseType what type is the object that we are trying to receive
     * @param requestType  where to send the request to
     * @return list of objects which is the result of the get query
     */
    public <T> List<T> getList(String url, Class<T[]> responseType, RequestType requestType) {
        url = buildUrl(url, requestType);

        try {
            String response = getResponse(url);
            try {
                List<T> objects;
                objects = Arrays.asList(objectMapper.readValue(response, responseType));
                return objects;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e.getMessage());
            }

        } catch (IOException | InterruptedException exception) {
            throw new BadResponseException("Error occurred while making the HTTP request");
        }
    }
}
