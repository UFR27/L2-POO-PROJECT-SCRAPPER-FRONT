package fr.pantheonsorbonne.ufr27.miashs.poo.business;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.apache.http.impl.client.HttpClients;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.plugins.interceptors.AcceptEncodingGZIPFilter;
import org.jboss.resteasy.plugins.interceptors.GZIPDecodingInterceptor;
import org.jboss.resteasy.plugins.interceptors.GZIPEncodingInterceptor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class WebPageSourceCodeService {

    @Inject
    PrettyPrintService prettyPrintService;


    public String getURLContent(String url) {
        return getURLContent(url, new HashMap<>());
    }


    public String getURLContent(String url, Map<String, NewCookie> cookies) {
        StringBuilder content = new StringBuilder();
        try {
            boolean redirect = true;
            String currentUrl = url;

            while (redirect) {
                URL urlObj = new URL(currentUrl);
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                connection.setInstanceFollowRedirects(false); // Disable automatic redirects
                setCookies(connection, cookies);

                // Handle response
                int responseCode = connection.getResponseCode();
                if (isRedirect(responseCode)) {
                    // Handle redirection
                    String newUrl = connection.getHeaderField("Location");
                    if (newUrl == null) {
                        throw new RuntimeException("Redirect response without Location header");
                    }
                    currentUrl = newUrl;

                    // Update cookies from "Set-Cookie" header
                    extractCookies(connection, cookies);

                } else {
                    redirect = false;

                    // Read content
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {
                        content.append(reader.lines().collect(Collectors.joining("\n")));
                    }

                    // Extract final cookies
                    extractCookies(connection, cookies);
                }

                connection.disconnect();
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching URL content", e);
        }
        return prettyPrintService.prettyHTML(content.toString());
    }

    private boolean isRedirect(int responseCode) {
        return responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_SEE_OTHER;
    }

    private void setCookies(HttpURLConnection connection, Map<String, NewCookie> cookies) {
        if (cookies != null && !cookies.isEmpty()) {
            String cookieHeader = cookies.values().stream()
                    .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                    .collect(Collectors.joining("; "));
            connection.setRequestProperty("Cookie", cookieHeader);
        }
    }

    private void extractCookies(HttpURLConnection connection, Map<String, NewCookie> cookies) {
        Map<String, List<String>> headers = connection.getHeaderFields();
        List<String> setCookieHeaders = headers.get("Set-Cookie");
        if (setCookieHeaders != null) {
            for (String setCookie : setCookieHeaders) {
                NewCookie cookie = parseSetCookieHeader(setCookie);
                cookies.put(cookie.getName(), cookie);
            }
        }
    }

    private NewCookie parseSetCookieHeader(String setCookieHeader) {
        String[] parts = setCookieHeader.split(";");
        String[] nameValue = parts[0].split("=", 2);
        String name = nameValue[0].trim();
        String value = nameValue.length > 1 ? nameValue[1].trim() : "";
        return new NewCookie(name, value);
    }


}
