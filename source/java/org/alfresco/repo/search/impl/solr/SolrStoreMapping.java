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

import java.util.LinkedHashSet;

import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.beans.factory.BeanNameAware;

/**
 * @author Andy
 *
 */
public class SolrStoreMapping implements BeanNameAware
{
    StoreRef storeRef;
    
    String httpClientFactory;
    
    String baseUrl;
    
    String protocol;
    
    String identifier;

    private String beanName;
    
    private String[] nodes = new String[0];

    private int numShards = 1;

    private int replicationFactor = 1;
    
    public SolrStoreMapping()
    {
        
    }
    
//    public SolrStoreMapping(String protocol, String identifier, String httpClientFactory, String baseUrl)
//    {
//        this.protocol = protocol;
//        this.identifier = identifier;
//        this.httpClientFactory = httpClientFactory;
//        this.baseUrl = baseUrl;
//    }

    /**
     * @return the storeRef
     */
    public StoreRef getStoreRef()
    {
        return storeRef;
    }


    /**
     * @return the protocol
     */
    public String getProtocol()
    {
        return protocol;
    }

    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
        setStoreRef();
    }

    /**
     * @return the identifier
     */
    public String getIdentifier()
    {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
        setStoreRef();
    }

    /**
     * @return the httpClientFactory
     */
    public String getHttpClientFactory()
    {
        return httpClientFactory;
    }

    /**
     * @param httpClientFactory the httpClientFactory to set
     */
    public void setHttpClientFactory(String httpClientFactory)
    {
        this.httpClientFactory = httpClientFactory;
    }

    /**
     * @return the baseUrl
     */
    public String getBaseUrl()
    {
        return baseUrl;
    }

    /**
     * @param baseUrl the baseUrl to set
     */
    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    @Override
    public void setBeanName(String beanName)
    {
        this.beanName = beanName;
    }

    private void setStoreRef()
    {
        if((protocol != null) && (identifier != null))
        {
            this.storeRef = new StoreRef(protocol, identifier);
        }
    }
    
    /**
     * @return the nodes
     */
    public String getNodeString()
    {
        StringBuilder builder = new StringBuilder();
        for(String node : nodes)
        {
            if(builder.length() > 0)
            {
                builder.append(',');
            }
            builder.append(node);
        }
        return builder.toString();
    }
    
    /**
     * @return the nodes
     */
    public String[] getNodes()
    {
        return nodes;
    }

    /**
     * @param nodes
     *            the nodes to set
     */
//    public void setNodes(String[] nodes)
//    {
//        LinkedHashSet<String> unique = new LinkedHashSet<String>();
//        for(String node : nodes)
//        {
//            for(String split : node.split(","))
//            {
//                unique.add(split.trim());
//            }
//        }
//        
//        this.nodes = unique.toArray(new String[0]);
//    }
    
    public void setNodeString(String nodes)
    {
        LinkedHashSet<String> unique = new LinkedHashSet<String>();

        for(String split : nodes.split(","))
        {
            unique.add(split.trim());
        }


        this.nodes = unique.toArray(new String[0]);
    }
    
    /**
     * @return the numShards
     */
    public int getNumShards()
    {
        return numShards;
    }

    /**
     * @param numShards
     *            the numShards to set
     */
    public void setNumShards(int numShards)
    {
        this.numShards = numShards;
    }

    /**
     * @return the replicationFactor
     */
    public int getReplicationFactor()
    {
        return replicationFactor;
    }

    /**
     * @param replicationFactor
     *            the replicationFactor to set
     */
    public void setReplicationFactor(int replicationFactor)
    {
        this.replicationFactor = replicationFactor;
    }

}
