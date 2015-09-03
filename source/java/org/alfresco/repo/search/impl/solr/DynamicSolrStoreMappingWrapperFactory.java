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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.alfresco.httpclient.HttpClientFactory;
import org.alfresco.repo.index.shard.ShardInstance;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.util.Pair;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.HttpClient;
import org.springframework.beans.factory.BeanFactory;

/**
 * @author Andy
 *
 */
public class DynamicSolrStoreMappingWrapperFactory
{
    static ConcurrentHashMap<Pair<String, Integer>, HttpClient> clients = new ConcurrentHashMap<Pair<String, Integer>, HttpClient>();

    /**
     * @param slice
     * @param beanFactory
     * @return
     */
    public static SolrStoreMappingWrapper wrap(List<ShardInstance> slice, BeanFactory beanFactory)
    {
        HttpClientFactory httpClientFactory = (HttpClientFactory)beanFactory.getBean("solrHttpClientFactory");
        for(ShardInstance instance : slice)
        {
            Pair<String, Integer> key = new Pair<String, Integer>(instance.getHostName(), instance.getPort());
            if(!clients.contains(key))
            {
                clients.put(key, httpClientFactory.getHttpClient(key.getFirst(), key.getSecond()));
            }
        }
        
        return new DynamicSolrStoreMappingWrapper(slice);
    }

    static class DynamicSolrStoreMappingWrapper implements SolrStoreMappingWrapper
    {
        private List<ShardInstance> slice;

        DynamicSolrStoreMappingWrapper(List<ShardInstance> slice)
        {
            this.slice = slice;
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.search.impl.solr.SolrStoreMappingWrapper#getHttpClientAndBaseUrl()
         */
        @Override
        public Pair<HttpClient, String> getHttpClientAndBaseUrl()
        {
           int base = ThreadLocalRandom.current().nextInt(slice.size());
           ShardInstance instance = slice.get(base);
           Pair<String, Integer> key = new Pair<String, Integer>(instance.getHostName(), instance.getPort());
           HttpClient client = clients.get(key);
           return new Pair<HttpClient, String>(client, instance.getBaseUrl());
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.search.impl.solr.SolrStoreMappingWrapper#isSharded()
         */
        @Override
        public boolean isSharded()
        {
            return slice.size() > 1;
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.search.impl.solr.SolrStoreMappingWrapper#getShards()
         */
        @Override
        public String getShards()
        {
            try
            {
                URLCodec encoder = new URLCodec();
                StringBuilder builder = new StringBuilder();

                for(ShardInstance instance : slice)
                {
                    if (builder.length() > 0)
                    {
                        builder.append(',');
                    }
                    builder.append(encoder.encode(instance.getHostName(), "UTF-8"));
                    builder.append(':');
                    builder.append(encoder.encode("" + instance.getPort(), "UTF-8"));
                    if(!instance.getBaseUrl().startsWith("/"))
                    {
                        builder.append('/');
                    }
                    builder.append(encoder.encode(instance.getBaseUrl(), "UTF-8"));
                }
                
                return builder.toString();
            }
            catch (UnsupportedEncodingException e)
            {
                throw new LuceneQueryParserException("", e);
            }
        }
        
    }
}
