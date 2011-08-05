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
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.repo.search.impl.lucene.SolrJSONResultSet;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
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
public class SolrQueryHTTPClient
{
    static Log s_logger = LogFactory.getLog(SolrQueryHTTPClient.class);

    private NodeDAO nodeDAO;

    private PermissionService permissionService;

    private Map<String, String> languageMappings;

    private Map<String, String> storeMappings;

    private String baseUrl;

    private HttpClient httpClient;

    public SolrQueryHTTPClient()
    {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        httpClient = new HttpClient(connectionManager);
        httpClient.getParams().setBooleanParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, true);
        httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new UsernamePasswordCredentials("admin", "admin"));
    }

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setLanguageMappings(Map<String, String> languageMappings)
    {
        this.languageMappings = languageMappings;
    }

    public void setStoreMappings(Map<String, String> storeMappings)
    {
        this.storeMappings = storeMappings;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public ResultSet executeQuery(SearchParameters searchParameters, String language)
    {   
        try
        {

            URLCodec encoder = new URLCodec();
            StringBuilder url = new StringBuilder();
            url.append(baseUrl);
            if (searchParameters.getStores().size() == 0)
            {
                throw new AlfrescoRuntimeException("No store for query");
            }
            String storeUrlFragment = storeMappings.get(searchParameters.getStores().get(0).toString());
            if (storeUrlFragment == null)
            {
                throw new AlfrescoRuntimeException("No solr query support for store " + searchParameters.getStores().get(0).toString());
            }
            url.append("/").append(storeUrlFragment);
            String languageUrlFragment = languageMappings.get(language);
            if (languageUrlFragment == null)
            {
                throw new AlfrescoRuntimeException("No solr query support for language " + language);
            }
            url.append("/").append(languageUrlFragment);

            // duplicate the query in the URL
            url.append("?q=");

            url.append(encoder.encode(searchParameters.getQuery(), "UTF-8"));
            url.append("&wt=").append(encoder.encode("json", "UTF-8"));
            url.append("&fl=").append(encoder.encode("*,score", "UTF-8"));
            if (searchParameters.getMaxItems() > 0)
            {
                url.append("&rows=").append(encoder.encode("" + searchParameters.getMaxItems(), "UTF-8"));
            }
            else
            {
                url.append("&rows=").append(encoder.encode("" + Integer.MAX_VALUE, "UTF-8"));
            }
            url.append("&df=").append(encoder.encode(searchParameters.getDefaultFieldName(), "UTF-8"));
            url.append("&start=").append(encoder.encode("" + searchParameters.getSkipCount(), "UTF-8"));

            Locale locale = I18NUtil.getLocale();
            if (searchParameters.getLocales().size() > 0)
            {
                locale = searchParameters.getLocales().get(0);
            }
            url.append("&locale=");
            url.append(encoder.encode(locale.toString(), "UTF-8"));

            StringBuffer sortBuffer = new StringBuffer();
            for (SortDefinition sortDefinition : searchParameters.getSortDefinitions())
            {
                if (sortBuffer.length() == 0)
                {
                    sortBuffer.append("&sort=");
                }
                else
                {
                    sortBuffer.append(encoder.encode(", ", "UTF-8"));
                }
                sortBuffer.append(encoder.encode(sortDefinition.getField(), "UTF-8")).append(encoder.encode(" ", "UTF-8"));
                if (sortDefinition.isAscending())
                {
                    sortBuffer.append(encoder.encode("asc", "UTF-8"));
                }
                else
                {
                    sortBuffer.append(encoder.encode("desc", "UTF-8"));
                }

            }
            url.append(sortBuffer);

            // Authorities go over in body

            StringBuilder authQuery = new StringBuilder();
            for (String authority : permissionService.getAuthorisations())
            {
                if (authQuery.length() > 0)
                {
                    authQuery.append(" ");
                }
                authQuery.append("|AUTHORITY:\"").append(authority).append("\"");
            }

            // url.append("&fq=");
            // encoder = new URLCodec();
            // url.append(encoder.encode(authQuery.toString(), "UTF-8"));

            url.append("&fq=").append(encoder.encode("{!afts}AUTHORITY_FILTER_FROM_JSON", "UTF-8"));

            // facets would go on url?

            if(searchParameters.getFieldFacets().size() > 0)
            {
                url.append("&facet=").append(encoder.encode("true", "UTF-8"));
                for(FieldFacet facet : searchParameters.getFieldFacets())
                {
                    url.append("&facet.field=").append(encoder.encode(facet.getField(), "UTF-8"));
                    url.append("&").append(encoder.encode("f."+facet.getField()+".limit", "UTF-8")).append("=").append(encoder.encode(""+facet.getLimit(), "UTF-8"));
                }
            }
            
            // end of field factes
            
            JSONObject body = new JSONObject();
            body.put("query", searchParameters.getQuery());
            // body.put("defaultField", searchParameters.getDefaultFieldName());

            body.put("filter", authQuery);

            JSONArray locales = new JSONArray();
            for (Locale currentLocale : searchParameters.getLocales())
            {
                locales.put(DefaultTypeConverter.INSTANCE.convert(String.class, currentLocale));
            }
            if (locales.length() == 0)
            {
                locales.put(I18NUtil.getLocale());
            }
            body.put("locales", locales);

            JSONArray templates = new JSONArray();
            for (String templateName : searchParameters.getQueryTemplates().keySet())
            {
                JSONObject template = new JSONObject();
                template.put("name", templateName);
                template.put("template", searchParameters.getQueryTemplates().get(templateName));
                templates.put(template);
            }
            body.put("templates", templates);

            JSONArray allAttributes = new JSONArray();
            for (String attribute : searchParameters.getAllAttributes())
            {
                allAttributes.put(attribute);
            }
            body.put("allAttributes", allAttributes);

            body.put("defaultFTSOperator", searchParameters.getDefaultFTSOperator());
            body.put("defaultFTSFieldOperator", searchParameters.getDefaultFTSFieldOperator());
            if (searchParameters.getMlAnalaysisMode() != null)
            {
                body.put("mlAnalaysisMode", searchParameters.getMlAnalaysisMode().toString());
            }
            body.put("defaultNamespace", searchParameters.getNamespace());

            JSONArray textAttributes = new JSONArray();
            for (String attribute : searchParameters.getTextAttributes())
            {
                textAttributes.put(attribute);
            }
            body.put("textAttributes", textAttributes);

            PostMethod post = new PostMethod(url.toString());
            post.setRequestEntity(new ByteArrayRequestEntity(body.toString().getBytes("UTF-8"), "application/json"));

            try
            {
                httpClient.executeMethod(post);

                if (post.getStatusCode() != HttpServletResponse.SC_OK)
                {
                    throw new LuceneQueryParserException("Request failed " + post.getStatusCode() + " " + url.toString());
                }

                Reader reader = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
                // TODO - replace with streaming-based solution e.g. SimpleJSON ContentHandler
                JSONObject json = new JSONObject(new JSONTokener(reader));
                SolrJSONResultSet results = new SolrJSONResultSet(json, nodeDAO, searchParameters);
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("Sent :" + url);
                    s_logger.debug("   with: " + body.toString());
                    s_logger.debug("Got: " + results.getNumberFound() + " in " + results.getQueryTime() + " ms");
                }

                return results;
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
