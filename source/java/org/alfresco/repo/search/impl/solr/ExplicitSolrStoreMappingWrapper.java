/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.search.impl.solr;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.httpclient.HttpClientFactory;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;
import org.alfresco.util.shard.ExplicitShardingPolicy;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;

/**
 * @author Andy
 */
public class ExplicitSolrStoreMappingWrapper implements SolrStoreMappingWrapper
{
    
    private HttpClientFactory httpClientFactory;

    private LinkedHashSet<HttpClientAndBaseUrl> httpClientsAndBaseURLs = new LinkedHashSet<HttpClientAndBaseUrl>();  

    private ExplicitShardingPolicy policy;

    private Random random;

    private BeanFactory beanFactory;

    private SolrStoreMapping wrapped;

    public ExplicitSolrStoreMappingWrapper(SolrStoreMapping wrapped, BeanFactory beanFactory)
    {
        this.wrapped = wrapped;
        this.beanFactory = beanFactory;
        init();
    }

    public void init()
    {
        httpClientFactory = (HttpClientFactory)beanFactory.getBean(wrapped.getHttpClientFactory());
        random = new Random(123);

        if ((wrapped.getNodes() == null) || (wrapped.getNodes().length == 0))
        {
            HttpClient httpClient = httpClientFactory.getHttpClient();
            HttpClientParams params = httpClient.getParams();
            //params.setBooleanParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, true);
            //httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new UsernamePasswordCredentials("admin", "admin"));
            httpClientsAndBaseURLs.add(new HttpClientAndBaseUrl(httpClient, wrapped.getBaseUrl()));
        }
        else
        {
            for (String node : wrapped.getNodes())
            {
                String nodeHost = httpClientFactory.getHost();
                String nodePort = "" + httpClientFactory.getPort();
                String nodeBaseUrl = wrapped.getBaseUrl();

                if (node.length() > 0)
                {
                    int colon = node.indexOf(':');
                    int forward = (colon > -1) ? node.indexOf('/', colon) : node.indexOf('/');

                    if (colon == -1)
                    {
                        if (forward == -1)
                        {
                            // single value
                            if (node.startsWith("/"))
                            {
                                nodeBaseUrl = node;
                            }
                            try
                            {
                                int port = Integer.parseInt(node);
                                nodePort = "" + port;
                            }
                            catch (NumberFormatException nfe)
                            {
                                nodeHost = node;
                            }
                        }
                        else
                        {
                            try
                            {
                                String potentialPort = node.substring(0, forward);
                                if (potentialPort.length() > 0)
                                {
                                    int port = Integer.parseInt(potentialPort);
                                    nodePort = "" + port;
                                }
                            }
                            catch (NumberFormatException nfe)
                            {
                                nodeHost = node.substring(0, forward);
                            }
                            nodeBaseUrl = node.substring(forward);
                        }
                    }
                    else
                    {
                        if (forward == -1)
                        {
                            if (colon > 0)
                            {
                                nodeHost = node.substring(0, colon);
                            }
                            if (colon + 1 < node.length())
                            {
                                String port = node.substring(colon + 1);
                                if (port.length() > 0)
                                {
                                    nodePort = port;
                                }
                            }
                        }
                        else
                        {
                            if (colon > 0)
                            {
                                nodeHost = node.substring(0, colon);
                            }

                            String port = node.substring(colon + 1, forward);
                            if (port.length() > 0)
                            {
                                nodePort = port;
                            }
                            nodeBaseUrl = node.substring(forward);

                        }
                    }
                }

                try
                {
                    int realPort = Integer.parseInt(nodePort);
                    httpClientsAndBaseURLs.add(new HttpClientAndBaseUrl(httpClientFactory.getHttpClient(nodeHost, realPort), nodeBaseUrl));
                }
                catch (NumberFormatException nfe)
                {
                    httpClientsAndBaseURLs.add(new HttpClientAndBaseUrl(httpClientFactory.getHttpClient(nodeHost, httpClientFactory.getPort()), nodeBaseUrl));
                }
            }
        }

        policy = new ExplicitShardingPolicy(wrapped.getNumShards(), wrapped.getReplicationFactor(), httpClientsAndBaseURLs.size());

    }

  

    
    public boolean isSharded()
    {
        return wrapped.getNumShards() > 1;
    }

    public String getShards()
    {

        if (!policy.configurationIsValid())
        {
            throw new AlfrescoRuntimeException("Invalid shard configuration: shard = "
                    + wrapped.getNumShards() + "   reoplicationFactor = " + wrapped.getReplicationFactor() + " with node count = " + httpClientsAndBaseURLs.size());
        }
        
        return getShards2();
    }

    private String getShards1()
    {
        try
        {
            URLCodec encoder = new URLCodec();
            StringBuilder builder = new StringBuilder();

            Set<Integer> shards = new HashSet<Integer>();
            for (int i = 0; i < httpClientsAndBaseURLs.size(); i += wrapped.getReplicationFactor())
            {
                for (Integer shardId : policy.getShardIdsForNode(i + 1))
                {
                    if (!shards.contains(shardId % wrapped.getNumShards()))
                    {
                        if (shards.size() > 0)
                        {
                            builder.append(',');
                        }
                        HttpClientAndBaseUrl httpClientAndBaseUrl = httpClientsAndBaseURLs.toArray(new HttpClientAndBaseUrl[0])[i];
                        builder.append(encoder.encode(httpClientAndBaseUrl.getHost(), "UTF-8"));
                        builder.append(':');
                        builder.append(encoder.encode("" + httpClientAndBaseUrl.getPort(), "UTF-8"));
                        if (httpClientAndBaseUrl.getBaseUrl().startsWith("/"))
                        {
                            builder.append(encoder.encode(httpClientAndBaseUrl.getBaseUrl(), "UTF-8"));
                        }
                        else
                        {
                            builder.append(encoder.encode("/" + httpClientAndBaseUrl.getBaseUrl(), "UTF-8"));
                        }

                        builder.append('-').append(shardId);

                        shards.add(shardId % wrapped.getNumShards());
                    }

                }
            }
            return builder.toString();
        }
        catch (UnsupportedEncodingException e)
        {
            throw new LuceneQueryParserException("", e);
        }
    }

    private String getShards2()
    {
        try
        {
            URLCodec encoder = new URLCodec();
            StringBuilder builder = new StringBuilder();

            for (int shard = 0; shard < wrapped.getNumShards(); shard++)
            {
                int position = random.nextInt(wrapped.getReplicationFactor());
                List<Integer> nodeInstances = policy.getNodeInstancesForShardId(shard);
                Integer nodeId = nodeInstances.get(position);
                
                if (builder.length() > 0)
                {
                    builder.append(',');
                }
                HttpClientAndBaseUrl httpClientAndBaseUrl = httpClientsAndBaseURLs.toArray(new HttpClientAndBaseUrl[0])[nodeId-1];
                builder.append(encoder.encode(httpClientAndBaseUrl.getHost(), "UTF-8"));
                builder.append(':');
                builder.append(encoder.encode("" + httpClientAndBaseUrl.getPort(), "UTF-8"));
                if (httpClientAndBaseUrl.getBaseUrl().startsWith("/"))
                {
                    builder.append(encoder.encode(httpClientAndBaseUrl.getBaseUrl(), "UTF-8"));
                }
                else
                {
                    builder.append(encoder.encode("/" + httpClientAndBaseUrl.getBaseUrl(), "UTF-8"));
                }

                builder.append('-').append(shard);

            }
            return builder.toString();
        }
        catch (UnsupportedEncodingException e)
        {
            throw new LuceneQueryParserException("", e);
        }
    }

    /**
     * @return
     */
    public int getNodeCount()
    {
        return httpClientsAndBaseURLs.size();
    }

    private static class HttpClientAndBaseUrl
    {

        private HttpClient httpClient;

        private String baseUrl;

        HttpClientAndBaseUrl(HttpClient httpClient, String baseUrl)
        {
            this.httpClient = httpClient;
            this.baseUrl = baseUrl;
        }

        public String getBaseUrl()
        {
            return baseUrl;
        }

        public String getHost()
        {
            return httpClient.getHostConfiguration().getHost();
        }

        public int getPort()
        {
            return httpClient.getHostConfiguration().getPort();
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((baseUrl == null) ? 0 : baseUrl.hashCode());
            result = prime * result + ((getHost() == null) ? 0 : getHost().hashCode());
            result = prime * result + getPort();
            return result;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            HttpClientAndBaseUrl other = (HttpClientAndBaseUrl) obj;
            if (baseUrl == null)
            {
                if (other.baseUrl != null)
                    return false;
            }
            else if (!baseUrl.equals(other.baseUrl))
                return false;
            if (httpClient == null)
            {
                if (other.httpClient != null)
                    return false;
            }
            else if (!getHost().equals(other.getHost()))
                return false;
            else if (getPort() != other.getPort())
                return false;
            return true;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "HttpClientAndBaseUrl [getBaseUrl()=" + getBaseUrl() + ", getHost()=" + getHost() + ", getPort()=" + getPort() + "]";
        }

    }

    /**
     * @return
     */
    public Pair<HttpClient, String> getHttpClientAndBaseUrl()
    {

        if (!policy.configurationIsValid())
        {
            throw new AlfrescoRuntimeException("Invalid shard configuration: shard = "
                    + wrapped.getNumShards() + "   reoplicationFactor = " + wrapped.getReplicationFactor() + " with node count = " + httpClientsAndBaseURLs.size());
        }
        
        int shard = random.nextInt(wrapped.getNumShards());
        int position =  random.nextInt(wrapped.getReplicationFactor());
        List<Integer> nodeInstances = policy.getNodeInstancesForShardId(shard);
        Integer nodeId = nodeInstances.get(position);         
        HttpClientAndBaseUrl httpClientAndBaseUrl = httpClientsAndBaseURLs.toArray(new HttpClientAndBaseUrl[0])[nodeId-1];
        return new Pair<>(httpClientAndBaseUrl.httpClient, isSharded() ? httpClientAndBaseUrl.baseUrl+"-"+shard : httpClientAndBaseUrl.baseUrl);
    }
    
}
