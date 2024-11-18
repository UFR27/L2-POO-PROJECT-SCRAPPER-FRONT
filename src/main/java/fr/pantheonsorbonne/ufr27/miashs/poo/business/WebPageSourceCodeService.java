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

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class WebPageSourceCodeService {

    @Inject
    PrettyPrintService prettyPrintService;

    private WebDriver createWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("start-maximized");
        options.addArguments("enable-automation");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-browser-side-navigation");
        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--disable-blink-features=AutomationControlled");
        // options.addArguments("--headless")
        options.addArguments("--disable-gpu");
        options.addArguments("--log-level=3");

        return new ChromeDriver(options);
    }

    public String getURLContent(String url) {
        return getURLContent(url, new HashMap<>());
    }


    public String getURLContent(String url, Map<String, NewCookie> cookies) {

        var driver = createWebDriver();
        try {
            driver.get(url.toString());


            return driver.getPageSource();
        } finally {
            driver.quit();
        }

    }


}
