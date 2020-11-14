package advisor;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OAuth {
    public static String resourceServer = "https://api.spotify.com";
    static boolean authenticated = false;
    public static String accessServer = "https://accounts.spotify.com";
    private static String accessToken;
    private static String refreshToken;

    public static String getAccessToken() {
        return accessToken;
    }

    static void authenticate() {
        final String appKey = "fe1fbcb131c6455d935de0cf562dc8cd";
        final String appSecret = "ee589bea82654951b7e9be77022f4014";
        final String redirectURI = "http://localhost:8080";
        final String requestURI = accessServer + "/authorize?client_id=" +
                appKey +
                "&redirect_uri=" +
                redirectURI +
                "&response_type=code";

        if (authenticated)
            return;

        HttpServer server;
        try {
            final String[] code = new String[1];
            code[0] = null;

            server = HttpServer.create();
            server.bind(new InetSocketAddress(8080), 0);
            server.createContext("/",
                    exchange -> {
                        String query = exchange.getRequestURI().getQuery();
                        String ok = "Got the code. Return back to your program.";
                        String noCode = "Authorization code not found. Try again.";

                        String[] params = null;

                        if (query != null) {
                            params = query.split("=");
                        }

                        if (params != null && params.length == 2 && params[0].equals("code")) {
                            synchronized (OAuth.class) {
                                code[0] = params[1];
                            }
                            exchange.sendResponseHeaders(200, ok.length());
                            exchange.getResponseBody().write(ok.getBytes());
                        } else {
                            System.out.println("Authorization code not found. Try again.");
                            exchange.sendResponseHeaders(200, noCode.length());
                            exchange.getResponseBody().write(noCode.getBytes());
                        }
                        exchange.getResponseBody().close();
                    }
            );

            server.start();

            System.out.println("use this link to request the access code:");
            System.out.println(requestURI);
            System.out.println("waiting for code...");
            while (true) {
                synchronized (OAuth.class) {
                    if (code[0] != null) {
                        break;
                    }
                }
            }
            server.stop(1);
            System.out.println("code received\n" +
                    "making http request for access_token...");

            HttpClient client = HttpClient.newHttpClient();
            String requestTockenURI = accessServer + "/api/token";
            String requestBody = String.format("grant_type=authorization_code&" +
                    "code=%s&" +
                    "redirect_uri=http://localhost:8080&" +
                    "client_id=%s&" +
                    "client_secret=%s", code[0], appKey, appSecret);
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .uri(URI.create(requestTockenURI))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("response:");
            System.out.println(response.body());

            JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject();
            accessToken = jo.get("access_token").getAsString();
            refreshToken = jo.get("refresh_token").getAsString();

            System.out.println("---SUCCESS---");
            authenticated = true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkAuth() {
        if (authenticated)
            return true;
        else {
            System.out.println("Please, provide access for application.");
            return false;
        }
    }
}
