import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.json.JSONObject;

public class Main {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/validatePassword", new PasswordHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server gestartet auf Port 8000");
    }

    static class PasswordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            String response = "";
            int responseCode = 200;

            try {
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    StringBuilder body = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            body.append(line);
                        }
                    }
                    JSONObject jsonObject = new JSONObject(body.toString());
                    String password = jsonObject.optString("password", "");

                    if (password.isEmpty()) {
                        response = "{\"error\":\"Passwort-Feld fehlt.\"}";
                        responseCode = 400;
                    } else if (validatePassword(password)) {
                        response = "{\"message\":\"Passwort ist stark und sicher.\"}";
                    } else {
                        response = "{\"error\":\"Passwort entspricht nicht den Sicherheitsanforderungen:<ul><li>Ihr Passwort sollte eine Mindestlänge von acht Zeichen aufweisen und eine Kombination aus Buchstaben, Zahlen sowie Sonderzeichen enthalten, um den Sicherheitsanforderungen gerecht zu werden.<br></li><li>Bitte stellen Sie sicher, dass Ihr neues Passwort sich von allen bisherigen Passwörtern unterscheidet, um die Sicherheit zu maximieren.<br></li></ul>\"}";
                        responseCode = 400;
                    }
                } else {
                    response = "{\"error\":\"Nur POST ist erlaubt.\"}";
                    responseCode = 405;
                }
            } catch (Exception e) {
                response = "{\"error\":\"Interner Serverfehler.\"}";
                responseCode = 500;
            }
            sendResponse(exchange, response, responseCode);
        }


        private void setCorsHeaders(HttpExchange exchange) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization");
        }


        private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }

    }

    public static boolean validatePassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
