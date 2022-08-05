package server;

import basket.api.util.FatalError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import main.Main;
import server.common.model.app.App;
import server.common.model.user.User;
import util.ThreadHandler;

import static basket.api.util.uri.URIConstructor.makeURI;
import static basket.api.util.uri.URIConstructor.newURIBuilder;

public class ServerHandler {

    private record Credentials(String username, String password) {}

    private static final class InstanceHolder {
        private static final ServerHandler instance = new ServerHandler();
    }

    public static ServerHandler getInstance() {
        return InstanceHolder.instance;
    }

    private final String address;

    private final HttpClient client;

    private Credentials credentials;

    private final CookieManager cookieManager;

    private final ObjectMapper objectMapper;

    private ServerHandler() {
        URL url = Main.class.getResource(Main.class.getSimpleName() + ".class");

        if (url != null && !url.getProtocol().equals("file")) {
            address = "https://basket-io.com";
        } else {
            address = "http://localhost:8080";
        }

        cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

        client = HttpClient.newBuilder()
                .followRedirects(Redirect.NORMAL)
                .cookieHandler(cookieManager)
                .build();

        ThreadHandler.getExecutorService().scheduleAtFixedRate(this::keepSessionAlive, 10, 10, TimeUnit.MINUTES);

        objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    public void keepSessionAlive() {
        if (credentials == null) {
            return;
        }
        try {
            client.send(HttpRequest.newBuilder().uri(makeURI(address)).GET().build(),
                    BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            // ignore
        }
    }

    public boolean login(String username, String password) throws ServerConnectionException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(newURIBuilder(address + "/login")
                        .addKeyValuePair("username", "userA")
                        .addKeyValuePair("password", "a12341234")
                        .build())
                .POST(BodyPublishers.noBody())
                .build();

        HttpResponse<Void> res;
        try {
            res = client.send(req, BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            throw new ServerConnectionException(e);
        }

        if (res.statusCode() == 200) {
            credentials = new Credentials(username, password);
            return true;
        } else {
            return false;
        }
    }

    public void logout() {
        credentials = null;
        cookieManager.getCookieStore().removeAll();
    }

    public boolean serverSleeping() throws ServerConnectionException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(makeURI(address))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        try {
            client.send(request, BodyHandlers.discarding());
        } catch (HttpTimeoutException e) {
            return true;
        } catch (IOException | InterruptedException e) {
            throw new ServerConnectionException(e);
        }
        return false;
    }

    private <T> T sendAndParseGetRequest(HttpRequest request, TypeReference<T> type) throws ServerConnectionException {
        if (!request.method().equals("GET")) {
            throw new IllegalArgumentException("request should be of type 'GET'");
        }

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new ServerConnectionException(e);
        }

        if (response.statusCode() != 200) {
            throw new ServerConnectionException("Request failed");
        }

        try {
            return objectMapper.readValue(response.body(), type);
        } catch (JsonProcessingException e) {
            throw new FatalError(e);
        }
    }

    public App getApp(String appId) throws ServerConnectionException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(newURIBuilder(address + "/api/v1/app/get/one")
                        .addKeyValuePair("appId", appId)
                        .build())
                .GET()
                .build();

        return sendAndParseGetRequest(request, new TypeReference<>() {});
    }

    public List<App> getStoreApps() throws ServerConnectionException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(makeURI(address + "/api/v1/app/get/all"))
                .GET()
                .build();

        return sendAndParseGetRequest(request, new TypeReference<>() {});
    }

    public List<App> getLibraryApps() throws ServerConnectionException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(makeURI(address + "/api/v1/app/get/user-library"))
                .GET()
                .build();

        return sendAndParseGetRequest(request, new TypeReference<>() {});
    }

    public HttpResponse<InputStream> getDownloadStream(String appId, String fileName) throws ServerConnectionException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(newURIBuilder(address + "/api/v1/storage/download")
                        .addKeyValuePair("appId", appId)
                        .addKeyValuePair("fileName", fileName)
                        .build())
                .GET()
                .build();

        HttpResponse<InputStream> response;
        try {
            response = client.send(request, BodyHandlers.ofInputStream());
        } catch (IOException | InterruptedException e) {
            throw new ServerConnectionException(e);
        }

        if (response.statusCode() != 200) {
            throw new ServerConnectionException("Request failed");
        }

        return response;
    }

    public enum LibraryAction {
        add, remove
    }

    public void modifyLibrary(String appId, LibraryAction action) throws ServerConnectionException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(newURIBuilder(address + "/api/v1/user/library/" + action)
                        .addKeyValuePair("appId", appId)
                        .build())
                .POST(BodyPublishers.noBody())
                .build();

        HttpResponse<Void> response;
        try {
            response = client.send(request, BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            throw new ServerConnectionException(e);
        }

        if (response.statusCode() != 200) {
            throw new ServerConnectionException("Request failed");
        }
    }

    public User getUserInfo() throws ServerConnectionException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(makeURI(address + "/api/v1/user/info"))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new ServerConnectionException(e);
        }

        if (response.statusCode() != 200) {
            throw new ServerConnectionException("Request failed");
        }

        try {
            return objectMapper.readValue(response.body(), User.class);
        } catch (JsonProcessingException e) {
            throw new FatalError(e);
        }
    }
}
