package client;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class CustomHttpResponse implements HttpResponse<String> {

    private int statusCode;
    private HttpRequest request;
    private Optional<HttpResponse<String>> previousResponse;
    private HttpHeaders headers;
    private String body;
    private Optional<SSLSession> sslSession;
    private URI uri;
    private HttpClient.Version version;

    @Override
    public int statusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public HttpRequest request() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    @Override
    public Optional<HttpResponse<String>> previousResponse() {
        return previousResponse;
    }

    public void setPreviousResponse(Optional<HttpResponse<String>> previousResponse) {
        this.previousResponse = previousResponse;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public String body() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public Optional<SSLSession> sslSession() {
        return sslSession;
    }

    public void setSslSession(Optional<SSLSession> sslSession) {
        this.sslSession = sslSession;
    }

    @Override
    public URI uri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    @Override
    public HttpClient.Version version() {
        return version;
    }

    public void setVersion(HttpClient.Version version) {
        this.version = version;
    }
}
