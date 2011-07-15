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

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryLanguage;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.repo.search.impl.lucene.SolrJSONResultSet;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
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
public class SolrCMISQueryLanguage extends AbstractLuceneQueryLanguage
{
    static Log s_logger = LogFactory.getLog(SolrCMISQueryLanguage.class);

    private NodeDAO nodeDAO;

    private PermissionService permissionService;

    public SolrCMISQueryLanguage()
    {
        this.setName(SearchService.LANGUAGE_SOLR_CMIS);
    }
    
    /**
     * @param nodeDAO
     *            the nodeDAO to set
     */
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    /**
     * @param permissionService
     *            the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /*
     * (non-Javadoc)
     * @seeorg.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI#executeQuery(org.alfresco.service.cmr.search.
     * SearchParameters, org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl)
     */
    @Override
    public ResultSet executeQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
        try
        {
            HttpClient httpClient = new HttpClient();
            httpClient.getParams().setBooleanParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, true);
            httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new UsernamePasswordCredentials("admin", "admin"));

            StringBuilder url = new StringBuilder("http://localhost:8080/solr/alfresco/cmis");
            // duplicate the query in the URL
            url.append("?q=");
            URLCodec encoder = new URLCodec();
            url.append(encoder.encode(searchParameters.getQuery(), "UTF-8"));
            url.append("&wt=json");
            url.append("&fl=*,score");
            if (searchParameters.getMaxItems() > 0)
            {
                url.append("&rows=").append(searchParameters.getMaxItems());
            }
            else
            {
                url.append("&rows=").append(Integer.MAX_VALUE);
            }
            url.append("&df=").append(searchParameters.getDefaultFieldName());
            url.append("&start=").append(searchParameters.getSkipCount());

            Locale locale = I18NUtil.getLocale();
            if (searchParameters.getLocales().size() > 0)
            {
                locale = searchParameters.getLocales().get(0);
            }
            url.append("&locale=");
            encoder = new URLCodec();
            url.append(encoder.encode(locale.toString(), "UTF-8"));

            // Will use this search if not specified as part of the query )no order by clause)
            // If the query contains an order by clause this will be used instead.
            StringBuffer sortBuffer = new StringBuffer();
            for (SortDefinition sortDefinition : searchParameters.getSortDefinitions())
            {
                if (sortBuffer.length() == 0)
                {
                    sortBuffer.append("&sort=");
                }
                else
                {
                    sortBuffer.append(", ");
                }
                sortBuffer.append(sortDefinition.getField()).append(" ");
                if (sortDefinition.isAscending())
                {
                    sortBuffer.append("asc");
                }
                else
                {
                    sortBuffer.append("desc");
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
                authQuery.append("AUTHORITY:\"").append(authority).append("\"");
            }

            // url.append("&fq=");
            // encoder = new URLCodec();
            // url.append(encoder.encode(authQuery.toString(), "UTF-8"));

            url.append("&fq=");
            url.append(encoder.encode("{!afts}", "UTF-8"));
            url.append("AUTHORITY_FILTER_FROM_JSON");

            // facets would go on url?

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

            // templates etc may affect CONTAINS() clause
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

    public static void main(String[] args)
    {
        SolrCMISQueryLanguage solrAlfrescoFTSQueryLanguage = new SolrCMISQueryLanguage();
        SearchParameters sp = new SearchParameters();
        sp.setQuery("PATH:\"//.\"");
        sp.setMaxItems(100);
        sp.setSkipCount(12);
        ResultSet rs = solrAlfrescoFTSQueryLanguage.executeQuery(sp, null);
        System.out.println("Found " + rs.length());
        System.out.println("More " + rs.hasMore());
        System.out.println("Start " + rs.getStart());

        for (ResultSetRow row : rs)
        {
            System.out.println("Score " + row.getScore());
        }
        rs.close();
    }

}
