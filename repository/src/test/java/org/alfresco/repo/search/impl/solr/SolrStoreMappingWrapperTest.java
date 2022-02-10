/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.solr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.io.UnsupportedEncodingException;

import org.alfresco.httpclient.HttpClientFactory;
import org.alfresco.httpclient.RequestHeadersHttpClient;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.protocol.Protocol;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;

/**
 * @author Andy
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class SolrStoreMappingWrapperTest
{
    SolrStoreMapping mapping;
    
    ExplicitSolrStoreMappingWrapper wrapper;
    
    @Mock
    HttpClientFactory httpClientFactory;
    
    @Mock
    RequestHeadersHttpClient httpClientCommon;
    
    @Mock
    RequestHeadersHttpClient httpClient1;
    
    @Mock
    RequestHeadersHttpClient httpClient2;
    
    @Mock
    RequestHeadersHttpClient httpClient3;
    
    @Mock
    RequestHeadersHttpClient httpClient4;
    
    @Mock
    RequestHeadersHttpClient httpClient5;
    
    @Mock
    RequestHeadersHttpClient httpClient6;
    
    @Mock
    RequestHeadersHttpClient httpClient7;
    
    @Mock
    RequestHeadersHttpClient httpClient8;
    
    @Mock
    RequestHeadersHttpClient httpClient9;
    
    @Mock
    HostConfiguration hostConfigurationCommon;
    
    @Mock
    HostConfiguration hostConfiguration1;
    
    @Mock
    HostConfiguration hostConfiguration2;
    
    @Mock
    HostConfiguration hostConfiguration3;
    
    @Mock
    HostConfiguration hostConfiguration4;
    
    @Mock
    HostConfiguration hostConfiguration5;
    
    @Mock
    HostConfiguration hostConfiguration6;
    
    @Mock
    HostConfiguration hostConfiguration7;
    
    @Mock
    HostConfiguration hostConfiguration8;
    
    @Mock
    HostConfiguration hostConfiguration9;

    @Mock
    Protocol protocol;

    private SolrStoreMapping unsharded;
    
    private ExplicitSolrStoreMappingWrapper unshardedWrapper;

    @Mock
    private BeanFactory beanFactory;
    
    @Before
    public void init()
    {
        doReturn("https").when(protocol).getScheme();

        doReturn("common").when(hostConfigurationCommon).getHost();
        doReturn(999).when(hostConfigurationCommon).getPort();
        doReturn(protocol).when(hostConfigurationCommon).getProtocol();


        doReturn("host").when(hostConfiguration1).getHost();
        doReturn(999).when(hostConfiguration1).getPort();
        // getProtocol() is not called for hostConfiguration1

        doReturn("common").when(hostConfiguration2).getHost();
        doReturn(123).when(hostConfiguration2).getPort();
        doReturn(protocol).when(hostConfiguration2).getProtocol();

        doReturn("port").when(hostConfiguration3).getHost();
        doReturn(234).when(hostConfiguration3).getPort();
        doReturn(protocol).when(hostConfiguration3).getProtocol();

        doReturn("full").when(hostConfiguration4).getHost();
        doReturn(345).when(hostConfiguration4).getPort();
        doReturn(protocol).when(hostConfiguration4).getProtocol();

        doReturn("common").when(hostConfiguration5).getHost();
        doReturn(456).when(hostConfiguration5).getPort();
        doReturn(protocol).when(hostConfiguration5).getProtocol();

        doReturn("base").when(hostConfiguration6).getHost();
        doReturn(999).when(hostConfiguration6).getPort();
        doReturn(protocol).when(hostConfiguration6).getProtocol();

        doReturn("common").when(hostConfiguration7).getHost();
        doReturn(567).when(hostConfiguration7).getPort();
        doReturn(protocol).when(hostConfiguration7).getProtocol();

        doReturn("common").when(hostConfiguration8).getHost();
        doReturn(678).when(hostConfiguration8).getPort();
        doReturn(protocol).when(hostConfiguration8).getProtocol();

        doReturn("common").when(hostConfiguration9).getHost();
        doReturn(789).when(hostConfiguration9).getPort();
        doReturn(protocol).when(hostConfiguration9).getProtocol();

        doReturn(hostConfigurationCommon).when(httpClientCommon).getHostConfiguration();
        doReturn(hostConfiguration1).when(httpClient1).getHostConfiguration();
        doReturn(hostConfiguration2).when(httpClient2).getHostConfiguration();
        doReturn(hostConfiguration3).when(httpClient3).getHostConfiguration();
        doReturn(hostConfiguration4).when(httpClient4).getHostConfiguration();
        doReturn(hostConfiguration5).when(httpClient5).getHostConfiguration();
        doReturn(hostConfiguration6).when(httpClient6).getHostConfiguration();
        doReturn(hostConfiguration7).when(httpClient7).getHostConfiguration();
        doReturn(hostConfiguration8).when(httpClient8).getHostConfiguration();
        doReturn(hostConfiguration9).when(httpClient9).getHostConfiguration();
        
        doReturn(httpClientCommon).when(httpClientFactory).getHttpClient();
        doReturn("common").when(httpClientFactory).getHost();
        doReturn(999).when(httpClientFactory).getPort();
        doReturn(httpClientCommon).when(httpClientFactory).getHttpClient(eq("common"), eq(999));
        doReturn(httpClient1).when(httpClientFactory).getHttpClient(eq("host"), eq(999));
        doReturn(httpClient2).when(httpClientFactory).getHttpClient(eq("common"), eq(123));
        doReturn(httpClient3).when(httpClientFactory).getHttpClient(eq("port"), eq(234));
        doReturn(httpClient4).when(httpClientFactory).getHttpClient(eq("full"), eq(345));
        doReturn(httpClient5).when(httpClientFactory).getHttpClient(eq("common"), eq(456));
        doReturn(httpClient6).when(httpClientFactory).getHttpClient(eq("base"), eq(999));
        doReturn(httpClient7).when(httpClientFactory).getHttpClient(eq("common"), eq(567));
        doReturn(httpClient8).when(httpClientFactory).getHttpClient(eq("common"), eq(678));
        doReturn(httpClient9).when(httpClientFactory).getHttpClient(eq("common"), eq(789));
        
        doReturn(httpClientFactory).when(beanFactory).getBean(eq("httpClientFactory"));
        
       
        mapping = new SolrStoreMapping();
        mapping.setBaseUrl("/solr4");
        mapping.setHttpClientFactory("httpClientFactory");
        mapping.setIdentifier(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier());
        mapping.setNodeString("host, 123, /woof, port:234, full:345/meep/sheep, 456/cabbage, base/url,,:,:/,:567,:678/,789/more,/");
        mapping.setNumShards(24);
        mapping.setProtocol(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getProtocol());
        mapping.setReplicationFactor(3);
        wrapper = new ExplicitSolrStoreMappingWrapper(mapping, beanFactory);
   
        
        
        unsharded = new SolrStoreMapping();
        unsharded.setBaseUrl("/solr4");
        unsharded.setHttpClientFactory("httpClientFactory");
        unsharded.setIdentifier(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier());
        unsharded.setProtocol(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getProtocol());
        unshardedWrapper =  new ExplicitSolrStoreMappingWrapper(unsharded, beanFactory);
    }
    

    @Test
    public void testBasics()
    {
        assertEquals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier(), mapping.getIdentifier());
        assertEquals(24, mapping.getNumShards());
        assertEquals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getProtocol() , mapping.getProtocol());
        assertEquals(3, mapping.getReplicationFactor());
        assertEquals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, mapping.getStoreRef());
        assertEquals(12, wrapper.getNodeCount());
        assertTrue(wrapper.isSharded());
    }
    
    
    @Test
    public void testShards()
    {
        String shards = wrapper.getShards();
        assertNotNull(shards);
        assertTrue(shards.length() > 0);
        String[] fragments = shards.split(",");
        assertEquals(mapping.getNumShards(), fragments.length);
    }

    @Test
    public void testSingleShard() throws UnsupportedEncodingException
    {
        URLCodec encoder = new URLCodec();
        String shards = unshardedWrapper.getShards();
        assertNotNull(shards);
        assertEquals(encoder.encode("https://", "UTF-8")
                + "common:999" + encoder.encode("/solr4", "UTF-8"), shards);
    }

    @Test
    public void testDistribution()
    {
        // default seed gives /woof
        Pair<HttpClient, String> distributor = wrapper.getHttpClientAndBaseUrl();
        assertNotNull(distributor);
        assertEquals("/woof-14", distributor.getSecond());
        assertEquals("common", distributor.getFirst().getHostConfiguration().getHost());
        assertEquals(999, distributor.getFirst().getHostConfiguration().getPort());
    }
    
    @Test
    public void testUnsharded()
    {
        assertTrue(unshardedWrapper.isSharded() == false);

        Pair<HttpClient, String> distributor = unshardedWrapper.getHttpClientAndBaseUrl();
        assertNotNull(distributor);
        assertEquals("/solr4", distributor.getSecond());
        assertEquals("common", distributor.getFirst().getHostConfiguration().getHost());
        assertEquals(999, distributor.getFirst().getHostConfiguration().getPort());
    }

}
