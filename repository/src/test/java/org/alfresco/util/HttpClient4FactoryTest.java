package org.alfresco.util;

import java.io.IOException;
import java.util.Properties;


import org.alfresco.encryption.KeyResourceLoader;
import org.alfresco.encryption.ssl.SSLEncryptionParameters;
import org.alfresco.httpclient.HttpClient4Factory;
import org.alfresco.httpclient.HttpClientConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class HttpClient4FactoryTest
{
    @Mock
    RestTemplate restTemplate;

    private Properties properties = new Properties();

    private HttpClientConfig httpClientConfig = new HttpClientConfig();

    @Before
    public void setUp() {
        properties.setProperty("httpclient.config.transform.mTLSEnabled", "true");
        properties.setProperty("httpclient.config.transform.maxTotalConnections", "40");
        properties.setProperty("httpclient.config.transform.maxHostConnections", "40");
        properties.setProperty("httpclient.config.transform.socketTimeout", "0");
        properties.setProperty("httpclient.config.transform.connectionTimeout", "0");

        //Create http client config
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

        httpClientConfig.setProperties(properties);
        httpClientConfig.setServiceName("transform");
        httpClientConfig.setKeyResourceLoader((KeyResourceLoader) ctx.getBean("springKeyResourceLoader"));
        httpClientConfig.setSslEncryptionParameters((SSLEncryptionParameters) ctx.getBean("sslEncryptionParameters"));
        httpClientConfig.init();

    }

    @Test
    public void testHttpClientFactoryForTransform() throws IOException
    {
        CloseableHttpClient httpClient = HttpClient4Factory.createHttpClient(httpClientConfig);
        String testUrl = "http://localhost:8080/request";

        HttpGet getRequest = new HttpGet(testUrl);

        Mockito.when(restTemplate.getForEntity(testUrl, String.class)).then(Object::toString)
               .thenReturn(new ResponseEntity("Ok", HttpStatus.OK));

        httpClient.execute(getRequest);

    }
}
