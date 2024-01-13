package nl.tudelft.sem.template.submission.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.submission.models.RequestType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class HttpRequestService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String userMicroserviceUrl = "http://localhost:8085/";
    private final String reviewMicroserviceUrl = "http://localhost:8082/";
    private final String submissionMicroserviceUrl = "http://localhost:8084/";

    public class BadResponseException extends RuntimeException {
        // Custom exception class for representing bad responses
        public BadResponseException(String message) {
            super(message);
        }
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
     * get list request method.
     *
     * @param url where to send the get request to
     * @param responseType what type is the object that we are trying to receive
     * @param requestType where to send the request to
     * @return object which is the result of the get query
     */
    public <T> T get(String url, Class<T> responseType, RequestType requestType) {
        url = buildUrl(url, requestType);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            String response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
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
     * @param url where to send the get request to
     * @param responseType what type is the object that we are trying to receive
     * @param requestType where to send the request to
     * @return list of objects which is the result of the get query
     */
    public <T> List<T> getList(String url, Class<T> responseType, RequestType requestType) {
        url = buildUrl(url, requestType);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            String response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            try {
                List<T> objects;
                objects = objectMapper.readValue(response, new TypeReference<>() {
                });
                return objects;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error occurred while processing json");
            }

        } catch (IOException | InterruptedException exception) {
            throw new BadResponseException("Error occurred while making the HTTP request");
        }

    }
}
