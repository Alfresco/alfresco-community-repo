/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.httpclient.HttpClientFactory;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.repo.search.impl.lucene.SolrJSONResultSet;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacetMethod;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacetSort;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Andy
 */
public class SolrAdminHTTPClient
{
    static Log s_logger = LogFactory.getLog(SolrAdminHTTPClient.class);

    private String baseUrl;

    private HttpClient httpClient;
	private HttpClientFactory httpClientFactory;
	
    public SolrAdminHTTPClient()
    {
    }

    public void init()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("/solr/admin/cores");
    	this.baseUrl = sb.toString();

    	httpClient = httpClientFactory.getHttpClient();
    	HttpClientParams params = httpClient.getParams();
    	params.setBooleanParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, true);
    	httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new UsernamePasswordCredentials("admin", "admin"));
    }

    public void setHttpClientFactory(HttpClientFactory httpClientFactory)
	{
		this.httpClientFactory = httpClientFactory;
	}

	public JSONObject execute(HashMap<String, String>args)
    {   
        try
        {   
            URLCodec encoder = new URLCodec();
            StringBuilder url = new StringBuilder();
            
            for(String key : args.keySet())
            {
                String value = args.get(key);
                if(url.length() == 0)
                {
                    url.append(baseUrl);
                    url.append("?");
                    url.append(encoder.encode(key, "UTF-8"));
                    url.append("=");
                    url.append(encoder.encode(value, "UTF-8"));
                }
                else
                {
                    url.append("&");
                    url.append(encoder.encode(key, "UTF-8"));
                    url.append("=");
                    url.append(encoder.encode(value, "UTF-8")); 
                }
                
            }
          
            PostMethod post = new PostMethod(url.toString());
            
            try
            {
                httpClient.executeMethod(post);

                if(post.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY || post.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY)
                {
	    	        Header locationHeader = post.getResponseHeader("location");
	    	        if (locationHeader != null)
	    	        {
	    	            String redirectLocation = locationHeader.getValue();
	    	            post.setURI(new URI(redirectLocation, true));
	    	            httpClient.executeMethod(post);
	    	        }
                }

                if (post.getStatusCode() != HttpServletResponse.SC_OK)
                {
                    throw new LuceneQueryParserException("Request failed " + post.getStatusCode() + " " + url.toString());
                }

                Reader reader = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
                // TODO - replace with streaming-based solution e.g. SimpleJSON ContentHandler
                JSONObject json = new JSONObject(new JSONTokener(reader));
                return json;
            }
            finally
            {
                post.releaseConnection();
            }
        }
        catch (UnsupportedEncodingException e)
        {
            throw new LuceneQueryParserException("", e);
        }
        catch (HttpException e)
        {
            throw new LuceneQueryParserException("", e);
        }
        catch (IOException e)
        {
            throw new LuceneQueryParserException("", e);
        }
        catch (JSONException e)
        {
            throw new LuceneQueryParserException("", e);
        }
    }

}
