package nl.tudelft.sem.template.submission.services;

import java.net.http.HttpResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.io.IOException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HttpRequestService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public class BadResponseException extends RuntimeException {
        // Custom exception class for representing bad responses
        public BadResponseException(String message) {
            super(message);
        }
    }

    /**
     * get request method.
     *
     * @param url where to send the get request to
     * @return a string which is the result of the get query
     * @throws IOException an exception that may be thrown
     * @throws InterruptedException an exception that may be thrown
     */
    public HttpResponse<String> get(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException exception) {
            throw new BadResponseException("Error occurred while making the HTTP request");
        }

    }

    /**
     * post request method.
     *
     * @param url where to send the post request to
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
     * @param url where to send the put request to
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
