package server;

import basket.api.common.FatalError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.App;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

import static basket.api.util.uri.URIConstructor.makeURI;

public class ServerHandler {

    public static final String ADDRESS = "https://basket-server.herokuapp.com";

    public static boolean serverSleeping() throws ServerConnectionException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(makeURI(ADDRESS))
                .timeout(Duration.ofSeconds(1))
                .GET()
                .build();

        try {
            HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
            return true;
        } catch (IOException | InterruptedException e) {
            throw new ServerConnectionException(e);
        }
        return false;
    }

    private static List<App> sendAndParseGetRequest(HttpRequest request) throws ServerConnectionException {
        if (!request.method().equals("GET")) {
            throw new IllegalArgumentException("request should be of type 'GET'");
        }

        HttpResponse<String> response;
        try {
            response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new ServerConnectionException(e);
        }

        try {
            return new ObjectMapper().readValue(response.body(), new TypeReference<>() { });
        } catch (JsonProcessingException e) {
            throw new FatalError(e);
        }
    }

    public static List<App> getStoreApps() throws ServerConnectionException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(makeURI(ADDRESS + "/apps"))
                .GET()
                .build();

        return sendAndParseGetRequest(request);
    }

    private static String toHeaderFormat(Collection<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : list) {
            stringBuilder.append(string);
            stringBuilder.append(", ");
        }
        int length = stringBuilder.length();
        stringBuilder.delete(length - 2, length);
        return stringBuilder.toString();
    }

    public static List<App> getLibraryApps(Collection<String> names) throws ServerConnectionException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(makeURI(ADDRESS + "/apps/names"))
                .header("names", toHeaderFormat(names))
                .GET()
                .build();

        return sendAndParseGetRequest(request);
    }
}
