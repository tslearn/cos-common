package org.companyos.dev.cos_common;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class CCHttpClient {
  private static PoolingHttpClientConnectionManager connectionManager = null;
  private static HttpClientBuilder httpBulder = null;
  private static RequestConfig requestConfig = null;
  private static int MAXCONNECTION = 2000;

  static {
    requestConfig = RequestConfig.custom()
        .setSocketTimeout(10000)
        .setConnectTimeout(10000)
        .setConnectionRequestTimeout(10000)
        .build();

    connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setMaxTotal(MAXCONNECTION);
    httpBulder = HttpClients.custom();
    httpBulder.setConnectionManager(connectionManager);
  }

  private static CloseableHttpClient getConnection() {
    CloseableHttpClient httpClient = httpBulder.build();
    httpClient = httpBulder.build();
    return httpClient;
  }

  private static HttpUriRequest getRequestMethod(BasicNameValuePair[] params, String url, String method) {
    HttpUriRequest reqMethod = null;
    if ("post".equals(method)) {
      reqMethod = RequestBuilder.post().setUri(url)
          .addParameters(params)
          .setConfig(requestConfig).build();
    } else if ("get".equals(method)) {
      reqMethod = RequestBuilder.get().setUri(url)
          .addParameters(params)
          .setConfig(requestConfig).build();
    }
    return reqMethod;
  }

  public static CCReturn<String> post(String path, String encoding) {
    return post(path, encoding, null);
  }


  public static CCReturn<String> post(String path, String encoding, BasicNameValuePair[] params) {
    try {
      if (params == null) {
        params = new BasicNameValuePair[0];
      }

      HttpClient client = CCHttpClient.getConnection();
      HttpUriRequest post = CCHttpClient.getRequestMethod(params, path, "post");

      HttpResponse response = client.execute(post);

      if (response.getStatusLine().getStatusCode() == 200) {
        HttpEntity entity = response.getEntity();
        String message = EntityUtils.toString(entity, encoding);
        System.out.println(message);
        return CCReturn.success(message);
      } else {
        return CCReturn.error("Http Error " + response.getStatusLine().getStatusCode());
      }
    }
    catch (Exception e) {
      return CCReturn.error(e.toString());
    }
  }

  public static CCReturn<String> get(String path, String encoding){
    return get(path, encoding, null);
  }

  public static CCReturn<String> get(String path, String encoding, BasicNameValuePair[] params){
    try {
      if (params == null) {
        params = new BasicNameValuePair[0];
      }

      HttpClient client = CCHttpClient.getConnection();
      HttpUriRequest post = CCHttpClient.getRequestMethod(params, path, "get");

      HttpResponse response = client.execute(post);

      if (response.getStatusLine().getStatusCode() == 200) {
        HttpEntity entity = response.getEntity();
        String message = EntityUtils.toString(entity, encoding);
        return CCReturn.success(message);
      } else {
        return CCReturn.error("Http Error " + response.getStatusLine().getStatusCode());
      }
    }
    catch (Exception e) {
      return CCReturn.error(e.toString());
    }
  }
}
