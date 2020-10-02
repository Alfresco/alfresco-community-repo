/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elastic;

import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Elastic Http client implementation.
 * Supports plain http and secured https connections.
 */
public class ElasticQueryHttpClientImpl implements ElasticQueryHttpClient
{

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticQueryHttpClientImpl.class);

    private String host;
    private String baseUrl;
    private Integer port;
    private Integer portSsl;
    private String secureComms;

    /**
     * Execute a query using HTTP Client for ElasticSearch server
     * @param searchParameters Query parameters
     * @param language Syntax name from SearchService.LANGUAGE_* constants 
     * @return result of the query executed
     */
    @Override
    public ResultSet executeQuery(SearchParameters searchParameters, String language)
    {
        LOGGER.debug("Execute query {} using language {} in server {}", searchParameters, language,
                "http" + (secureComms.equals("https") ? "s:" + portSsl : ":" + port) + "//" + host + baseUrl);
        return null;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public void setPort(Integer port)
    {
        this.port = port;
    }

    public void setPortSsl(Integer portSsl)
    {
        this.portSsl = portSsl;
    }

    public void setSecureComms(String secureComms)
    {
        this.secureComms = secureComms;
    }

}
