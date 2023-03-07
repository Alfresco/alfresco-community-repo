/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
