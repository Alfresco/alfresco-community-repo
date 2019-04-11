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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * HTTP Client providing GET invocations to SOLR.
 * These invocations are used for the SOLR CoreAdmin API and for the SOLR Backup API.
 * 
 * @author aborroy
 * @since 6.2
 *
 */
public abstract class AbstractSolrAdminHTTPClient
{
    
    /**
     * Executes an action or a command in SOLR using REST API 
     * 
     * @param httpClient HTTP Client to be used for the invocation
     * @param url Complete URL of SOLR REST API Endpoint
     * @return A JSON Object including SOLR response
     * @throws UnsupportedEncodingException
     */
    protected JSONObject getOperation(HttpClient httpClient, String url) throws UnsupportedEncodingException 
    {

        GetMethod get = new GetMethod(url);
        
        try {
            
            httpClient.executeMethod(get);
            if(get.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY || get.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY)
            {
                Header locationHeader = get.getResponseHeader("location");
                if (locationHeader != null)
                {
                    String redirectLocation = locationHeader.getValue();
                    get.setURI(new URI(redirectLocation, true));
                    httpClient.executeMethod(get);
                }
            }
            if (get.getStatusCode() != HttpServletResponse.SC_OK)
            {
                throw new LuceneQueryParserException("Request failed " + get.getStatusCode() + " " + url.toString());
            }

            Reader reader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), get.getResponseCharSet()));
            JSONObject json = new JSONObject(new JSONTokener(reader));
            return json;
            
        }
        catch (IOException | JSONException e) 
        {
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        } 
        finally 
        {
            get.releaseConnection();
        }
    }

}
