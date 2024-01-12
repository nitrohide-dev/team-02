package nl.tudelft.sem.template.submission.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.submission.models.RequestType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class HttpRequestService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String userMicroserviceUrl = "http://localhost:8085/";
    private final String reviewMicroserviceUrl = "http://localhost:8082/";

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
        if (requestType.equals(RequestType.REVIEW)) {
            return reviewMicroserviceUrl + apiUrl;
        }
        return userMicroserviceUrl + apiUrl;
    }

    /**
     * get request method.
     *
     * @param url where to send the get request to
     * @return a string which is the result of the get query
     * @throws IOException          an exception that may be thrown
     * @throws InterruptedException an exception that may be thrown
     */
    public <T> T get(String url, Class<T> responseType, RequestType requestType) {
        url = buildUrl(url, requestType);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            String response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            T object;
            try {
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
     * @return a string which is the result of the get query
     * @throws IOException          an exception that may be thrown
     * @throws InterruptedException an exception that may be thrown
     */
    public <T> List<T> getList(String url, Class<T> responseType, RequestType requestType) {
        url = buildUrl(url, requestType);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            String response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            List<T> objects;
            try {
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


    /**
     * post request method.
     *
     * @param url         where to send the post request to
     * @param requestBody what you are sending with the post request
     * @return a string which is the result of the post query
     */
    public HttpResponse<String> post(String url, String requestBody) throws IOException, InterruptedException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException exception) {
            throw new BadResponseException("Error occurred while making the HTTP request");
        }
    }

    /**
     * put request method.
     *
     * @param url         where to send the put request to
     * @param requestBody what you ase sending with the put request
     * @return a string which is the result of the put request
     */
    public HttpResponse<String> put(String url, String requestBody) throws IOException, InterruptedException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException exception) {
            throw new BadResponseException("Error occurred while making the HTTP request");
        }
    }
}
