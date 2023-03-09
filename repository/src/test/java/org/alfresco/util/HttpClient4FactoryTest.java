/*-
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


import org.alfresco.encryption.AlfrescoKeyStore;
import org.alfresco.encryption.KeyResourceLoader;
import org.alfresco.encryption.KeyStoreKeyProviderTest;
import org.alfresco.encryption.KeyStoreParameters;
import org.alfresco.encryption.SpringKeyResourceLoader;
import org.alfresco.encryption.ssl.SSLEncryptionParameters;
import org.alfresco.httpclient.HttpClient4Factory;
import org.alfresco.httpclient.HttpClientConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpCoreContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;

/**
 * Tests for {@link HttpClient4Factory}
 *
 * @author Kacper Magdziarz
 */
public class HttpClient4FactoryTest
{
    private Properties properties = new Properties();

    private HttpClientConfig httpClientConfig = new HttpClientConfig();

    @Before
    public void setUp() {
        String ALIAS_ONE = "mykey1";
        String ALIAS_TWO = "mykey2";
        String FILE_ONE = "classpath:alfresco/keystore-tests/ks-test-1.jks";

        properties.setProperty("httpclient.config.transform.mTLSEnabled", "true");
        properties.setProperty("httpclient.config.transform.maxTotalConnections", "40");
        properties.setProperty("httpclient.config.transform.maxHostConnections", "40");
        properties.setProperty("httpclient.config.transform.socketTimeout", "0");
        properties.setProperty("httpclient.config.transform.connectionTimeout", "0");

        //Create http client config
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

//        Map<String, String> passwords = new HashMap<String, String>(5);
//        passwords.put(AlfrescoKeyStore.KEY_KEYSTORE_PASSWORD, "ksPwd1");
//        passwords.put(ALIAS_ONE, "aliasPwd1");
//        passwords.put(ALIAS_TWO, "aliasPwd2");
//        KeyResourceLoader keyResourceLoader = new TestKeyResourceLoader(passwords);
//
//        KeyStoreParameters encryptionParameters = new KeyStoreParameters(null, "test", "JCEKS", "SunJCE", null, FILE_ONE);
//
//        SSLEncryptionParameters sslEncryptionParameters = new SSLEncryptionParameters(encryptionParameters, encryptionParameters);
//
        httpClientConfig.setProperties(properties);
        httpClientConfig.setServiceName("transform");
        httpClientConfig.setKeyResourceLoader((KeyResourceLoader) ctx.getBean("springKeyResourceLoader"));
        httpClientConfig.setSslEncryptionParameters((SSLEncryptionParameters) ctx.getBean("sslEncryptionParameters"));
        httpClientConfig.init();

    }

    @Test
    public void testHttpClientFactoryForTransform() throws IOException
    {
        HttpClient4Factory.setHttpRequestInterceptor((request, context) -> {
            context.toString();
        });
        CloseableHttpClient httpClient = HttpClient4Factory.createHttpClient(httpClientConfig);
        String testUrl = "https://localhost:8000/request";

        HttpGet getRequest = new HttpGet(testUrl);
        httpClient.execute(getRequest);
    }

    private static class TestKeyResourceLoader extends SpringKeyResourceLoader
    {
        private Properties props;

        TestKeyResourceLoader(Map<String, String> passwords)
        {
            StringBuilder aliases = new StringBuilder();
            props = new Properties();

            int i = 0;
            for(Map.Entry<String, String> password : passwords.entrySet())
            {
                props.put(password.getKey() + ".password", password.getValue());

                aliases.append(password.getKey());
                if(i < passwords.size() - 1)
                {
                    aliases.append(",");
                    i++;
                }
            }

            props.put("aliases", aliases.toString());
        }

        @Override
        public Properties loadKeyMetaData(String keyMetaDataFileLocation)
                throws IOException, FileNotFoundException
        {
            return props;
        }
    }
}
