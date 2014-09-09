/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.httpclient.HttpClientFactory;
import org.alfresco.opencmis.dictionary.CMISStrictDictionaryService;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.search.impl.lucene.JSONResult;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.repo.search.impl.lucene.SolrJSONResultSet;
import org.alfresco.repo.search.impl.lucene.SolrJsonProcessor;
import org.alfresco.repo.search.impl.lucene.SolrStatsResult;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.BasicSearchParameters;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacetMethod;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacetSort;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.StatsParameters;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Andy
 */
public class SolrQueryHTTPClient implements BeanFactoryAware
{
    static Log s_logger = LogFactory.getLog(SolrQueryHTTPClient.class);

    private NodeService nodeService;

    private PermissionService permissionService;
    
    private NodeDAO nodeDAO;
    
    private TenantService tenantService;

    private Map<String, String> languageMappings;

    private List<SolrStoreMapping> storeMappings;

    private HashMap<StoreRef, HttpClient> httpClients = new HashMap<StoreRef, HttpClient>();
    
    private HashMap<StoreRef, SolrStoreMapping> mappingLookup = new HashMap<StoreRef, SolrStoreMapping>();

	private String alternativeDictionary = CMISStrictDictionaryService.DEFAULT;
	
	private RepositoryState repositoryState;

    private BeanFactory beanFactory;
    
    private boolean includeGroupsForRoleAdmin = false;
    
    private int maximumResultsFromUnlimitedQuery = Integer.MAX_VALUE;
	
    public static final int DEFAULT_SAVEPOST_BUFFER = 4096;

    public SolrQueryHTTPClient()
    {
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "NodeService", nodeService);
        PropertyCheck.mandatory(this, "PermissionService", permissionService);
        PropertyCheck.mandatory(this, "TenantService", tenantService);
        PropertyCheck.mandatory(this, "LanguageMappings", languageMappings);
        PropertyCheck.mandatory(this, "StoreMappings", storeMappings);
        PropertyCheck.mandatory(this, "RepositoryState", repositoryState);

        for(SolrStoreMapping mapping : storeMappings)
        {
            mappingLookup.put(mapping.getStoreRef(), mapping);
            
            HttpClientFactory httpClientFactory = (HttpClientFactory)beanFactory.getBean(mapping.getHttpClientFactory());
            HttpClient httpClient = httpClientFactory.getHttpClient();
            HttpClientParams params = httpClient.getParams();
            params.setBooleanParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, true);
            httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new UsernamePasswordCredentials("admin", "admin"));
            httpClients.put(mapping.getStoreRef(), httpClient);
        }
    }

    public void setAlternativeDictionary(String alternativeDictionary)
    {
        this.alternativeDictionary = alternativeDictionary;
    }

    /**
     * @param repositoryState the repositoryState to set
     */
    public void setRepositoryState(RepositoryState repositoryState)
    {
        this.repositoryState = repositoryState;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param nodeDao the nodeDao to set
     */
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setLanguageMappings(Map<String, String> languageMappings)
    {
        this.languageMappings = languageMappings;
    }

    public void setStoreMappings(List storeMappings)
    {
        this.storeMappings = storeMappings;
    }
    
	/**
     * @param includeGroupsForRoleAdmin the includeGroupsForRoleAdmin to set
     */
    public void setIncludeGroupsForRoleAdmin(boolean includeGroupsForRoleAdmin)
    {
        this.includeGroupsForRoleAdmin = includeGroupsForRoleAdmin;
    }
    
    /**
     * @param maximumResultsFromUnlimitedQuery
     *            the maximum number of results to request from an otherwise unlimited query
     */
    public void setMaximumResultsFromUnlimitedQuery(int maximumResultsFromUnlimitedQuery)
    {
        this.maximumResultsFromUnlimitedQuery = maximumResultsFromUnlimitedQuery;
    }

    /**
     * Executes a solr query for statistics
     * 
     * @param searchParameters
     * @return SolrStatsResult
     */
    public SolrStatsResult executeStatsQuery(final StatsParameters searchParameters)
    {   
        if(repositoryState.isBootstrapping())
        {
            throw new AlfrescoRuntimeException("SOLR stats queries can not be executed while the repository is bootstrapping");
        }    
         
        try 
        { 
            StoreRef store = extractStoreRef(searchParameters);            
            SolrStoreMapping mapping = extractMapping(store);
            Locale locale = extractLocale(searchParameters);
            
            String url = buildStatsUrl(searchParameters, mapping.getBaseUrl(), locale);
            JSONObject body = buildStatsBody(searchParameters, tenantService.getCurrentUserDomain(), locale);
            
            return (SolrStatsResult) postSolrQuery(store, url, body, new SolrJsonProcessor<SolrStatsResult>() {

                @Override
                public SolrStatsResult getResult(JSONObject json)
                {
                    return new SolrStatsResult(json, searchParameters.isDateSearch());
                }
                
            });
            
        }
        catch (UnsupportedEncodingException e)
        {
            throw new LuceneQueryParserException("stats", e);
        }
        catch (HttpException e)
        {
            throw new LuceneQueryParserException("stats", e);
        }
        catch (IOException e)
        {
            throw new LuceneQueryParserException("stats", e);
        }
        catch (JSONException e)
        {
            throw new LuceneQueryParserException("stats", e);
        }
    }

    protected String buildStatsUrl(StatsParameters searchParameters, String baseUrl, Locale locale) throws UnsupportedEncodingException
    {
        URLCodec encoder = new URLCodec();
        StringBuilder url = new StringBuilder();
        String languageUrlFragment = extractLanguageFragment(searchParameters.getLanguage());
        
        url.append(baseUrl);
        url.append("/").append(languageUrlFragment);
        url.append("?wt=").append(encoder.encode("json", "UTF-8"));
        url.append("&locale=").append(encoder.encode(locale.toString(), "UTF-8"));
        
        url.append(buildSortParameters(searchParameters, encoder));
        
        url.append("&stats=true");
        url.append("&rows=0");
        if (!StringUtils.isBlank(searchParameters.getFilterQuery()))
        {
            url.append("?fq=").append(encoder.encode(searchParameters.getFilterQuery(), "UTF-8")); 
        }

        for(Entry<String, String> entry : searchParameters.getStatsParameters().entrySet())
        {
            url.append("&stats.").append(entry.getKey()).append("=").append(encoder.encode(entry.getValue(), "UTF-8"));
        }
        
        return url.toString();
    }
    

    protected JSONObject buildStatsBody(StatsParameters searchParameters, String tenant, Locale locale) throws JSONException
    {
        JSONObject body = new JSONObject();
        body.put("query", searchParameters.getQuery());
        
        JSONArray tenants = new JSONArray();
        tenants.put(tenant);
        body.put("tenants", tenants);
        
        JSONArray locales = new JSONArray();
        locales.put(locale);
        body.put("locales", locales);
        
        return body;
    }
    
    public ResultSet executeQuery(final SearchParameters searchParameters, String language)
    {   
	    if(repositoryState.isBootstrapping())
	    {
	        throw new AlfrescoRuntimeException("SOLR queries can not be executed while the repository is bootstrapping");
	    }
	    
        try
        {
            StoreRef store = extractStoreRef(searchParameters);            
            SolrStoreMapping mapping = extractMapping(store);
            Locale locale = extractLocale(searchParameters);
            
            URLCodec encoder = new URLCodec();
            StringBuilder url = new StringBuilder();
            url.append(mapping.getBaseUrl());
         
            String languageUrlFragment = extractLanguageFragment(language);
            url.append("/").append(languageUrlFragment);

            // Send the query in JSON only
            // url.append("?q=");
            // url.append(encoder.encode(searchParameters.getQuery(), "UTF-8"));
            url.append("?wt=").append(encoder.encode("json", "UTF-8"));
            url.append("&fl=").append(encoder.encode("DBID,score", "UTF-8"));
            
            // Emulate old limiting behaviour and metadata
            final LimitBy limitBy;
            int maxResults = -1;
            if (searchParameters.getMaxItems() >= 0)
            {
                maxResults = searchParameters.getMaxItems();
                limitBy = LimitBy.FINAL_SIZE;
            }
            else if(searchParameters.getLimitBy() == LimitBy.FINAL_SIZE && searchParameters.getLimit() >= 0)
            {
                maxResults = searchParameters.getLimit();
                limitBy = LimitBy.FINAL_SIZE;
            }
            else
            {
                maxResults = searchParameters.getMaxPermissionChecks();
                if (maxResults < 0)
                {
                    maxResults = maximumResultsFromUnlimitedQuery;
                }
                limitBy = LimitBy.NUMBER_OF_PERMISSION_EVALUATIONS;
            }
            url.append("&rows=").append(String.valueOf(maxResults));
            
            url.append("&df=").append(encoder.encode(searchParameters.getDefaultFieldName(), "UTF-8"));
            url.append("&start=").append(encoder.encode("" + searchParameters.getSkipCount(), "UTF-8"));

            url.append("&locale=");
            url.append(encoder.encode(locale.toString(), "UTF-8"));
            url.append("&").append(SearchParameters.ALTERNATIVE_DICTIONARY).append("=").append(alternativeDictionary);
            for(String paramName : searchParameters.getExtraParameters().keySet())
            {
            	url.append("&").append(paramName).append("=").append(searchParameters.getExtraParameters().get(paramName));
            }
            StringBuffer sortBuffer = buildSortParameters(searchParameters, encoder);
            url.append(sortBuffer);

            if(searchParameters.getPermissionEvaluation() != PermissionEvaluationMode.NONE)
            {
                url.append("&fq=").append(encoder.encode("{!afts}AUTHORITY_FILTER_FROM_JSON", "UTF-8"));
            }
            
            if(searchParameters.getExcludeTenantFilter() == false)
            {
                url.append("&fq=").append(encoder.encode("{!afts}TENANT_FILTER_FROM_JSON", "UTF-8"));
            }

            if(searchParameters.getFieldFacets().size() > 0)
            {
                url.append("&facet=").append(encoder.encode("true", "UTF-8"));
                for(FieldFacet facet : searchParameters.getFieldFacets())
                {
                    url.append("&facet.field=").append(encoder.encode(facet.getField(), "UTF-8"));
                    if(facet.getEnumMethodCacheMinDF() != 0)
                    {
                        url.append("&").append(encoder.encode("f."+facet.getField()+".facet.enum.cache.minDf", "UTF-8")).append("=").append(encoder.encode(""+facet.getEnumMethodCacheMinDF(), "UTF-8"));
                    }
                    url.append("&").append(encoder.encode("f."+facet.getField()+".facet.limit", "UTF-8")).append("=").append(encoder.encode(""+facet.getLimit(), "UTF-8"));
                    if(facet.getMethod() != null)
                    {
                        url.append("&").append(encoder.encode("f."+facet.getField()+".facet.method", "UTF-8")).append("=").append(encoder.encode(facet.getMethod()==FieldFacetMethod.ENUM ?  "enum" : "fc", "UTF-8"));
                    }
                    if(facet.getMinCount() != 0)
                    {
                        url.append("&").append(encoder.encode("f."+facet.getField()+".facet.mincount", "UTF-8")).append("=").append(encoder.encode(""+facet.getMinCount(), "UTF-8"));
                    }
                    if(facet.getOffset() != 0)
                    {
                        url.append("&").append(encoder.encode("f."+facet.getField()+".facet.offset", "UTF-8")).append("=").append(encoder.encode(""+facet.getOffset(), "UTF-8"));
                    }
                    if(facet.getPrefix() != null)
                    {
                        url.append("&").append(encoder.encode("f."+facet.getField()+".facet.prefix", "UTF-8")).append("=").append(encoder.encode(""+facet.getPrefix(), "UTF-8"));
                    }
                    if(facet.getSort() != null)
                    {
                        url.append("&").append(encoder.encode("f."+facet.getField()+".facet.sort", "UTF-8")).append("=").append(encoder.encode(facet.getSort() == FieldFacetSort.COUNT ? "count" : "index", "UTF-8"));
                    }
                    
                }
                for(String facetQuery : searchParameters.getFacetQueries())
                {
                    url.append("&facet.query=").append(encoder.encode("{!afts}"+facetQuery, "UTF-8"));
                }                
            }
            
            // end of field factes
            
            JSONObject body = new JSONObject();
            body.put("query", searchParameters.getQuery());

            
            // Authorities go over as is - and tenant mangling and query building takes place on the SOLR side

            Set<String> allAuthorisations = permissionService.getAuthorisations();
            boolean includeGroups = includeGroupsForRoleAdmin ? true : !allAuthorisations.contains(PermissionService.ADMINISTRATOR_AUTHORITY);
            
            JSONArray authorities = new JSONArray();
            for (String authority : allAuthorisations)
            {
                if(includeGroups)
                {
                    authorities.put(authority);
                }
                else
                {
                    if(AuthorityType.getAuthorityType(authority) != AuthorityType.GROUP)
                    {
                        authorities.put(authority);
                    }
                }
            }
            body.put("authorities", authorities);
            
            JSONArray tenants = new JSONArray();
            tenants.put(tenantService.getCurrentUserDomain());
            body.put("tenants", tenants);

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
            body.put("queryConsistency", searchParameters.getQueryConsistency());
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

            final int maximumResults = maxResults;  //just needed for the final parameter
            return (ResultSet) postSolrQuery(store, url.toString(), body, new SolrJsonProcessor<SolrJSONResultSet>() {

                @Override
                public SolrJSONResultSet getResult(JSONObject json)
                {
                    return new SolrJSONResultSet(json, searchParameters, nodeService, nodeDAO, limitBy, maximumResults);
                }
                
            });
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

    protected JSONResult postSolrQuery(StoreRef store, String url, JSONObject body, SolrJsonProcessor<?> jsonProcessor)
                throws UnsupportedEncodingException, IOException, HttpException, URIException,
                JSONException
    {
        PostMethod post = new PostMethod(url);
        if (body.toString().length() > DEFAULT_SAVEPOST_BUFFER)
        {
            post.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
        }
        post.setRequestEntity(new ByteArrayRequestEntity(body.toString().getBytes("UTF-8"), "application/json"));

        try
        {
            HttpClient httpClient = httpClients.get(store);
            
            if(httpClient == null)
            {
                throw new AlfrescoRuntimeException("No http client for store " + store.toString());
            }
            
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

            Reader reader = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream(), post.getResponseCharSet()));
            // TODO - replace with streaming-based solution e.g. SimpleJSON ContentHandler
            JSONObject json = new JSONObject(new JSONTokener(reader));

            if (json.has("status"))
            {
                JSONObject status = json.getJSONObject("status");
                if (status.getInt("code") != HttpServletResponse.SC_OK)
                {
                    throw new LuceneQueryParserException("SOLR side error: " + status.getString("message"));
                }
            }

            JSONResult results = jsonProcessor.getResult(json);

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

    private StringBuffer buildSortParameters(BasicSearchParameters searchParameters, URLCodec encoder)
                throws UnsupportedEncodingException
    {
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
            // MNT-8557 fix, manually replace ' ' with '%20'
            sortBuffer.append(encoder.encode(sortDefinition.getField().replaceAll(" ", "%20"), "UTF-8")).append(encoder.encode(" ", "UTF-8"));
            if (sortDefinition.isAscending())
            {
                sortBuffer.append(encoder.encode("asc", "UTF-8"));
            }
            else
            {
                sortBuffer.append(encoder.encode("desc", "UTF-8"));
            }

        }
        return sortBuffer;
    }

    private Locale extractLocale(BasicSearchParameters searchParameters)
    {
        Locale locale = I18NUtil.getLocale();
        if (searchParameters.getLocales().size() > 0)
        {
            locale = searchParameters.getLocales().get(0);
        }
        return locale;
    }

    private String extractLanguageFragment(String language)
    {
        String languageUrlFragment = languageMappings.get(language);
        if (languageUrlFragment == null)
        {
            throw new AlfrescoRuntimeException("No solr query support for language " + language);
        }
        return languageUrlFragment;
    }

    private SolrStoreMapping extractMapping(StoreRef store)
    {
        SolrStoreMapping mapping = mappingLookup.get(store);
        
        if (mapping == null)
        {
            throw new AlfrescoRuntimeException("No solr query support for store " + store);
        }
        return mapping;
    }

    private StoreRef extractStoreRef(BasicSearchParameters searchParameters)
    {
        if (searchParameters.getStores().size() == 0)
        {
            throw new AlfrescoRuntimeException("No store for query");
        }
        
        StoreRef store = searchParameters.getStores().get(0);
        return store;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }

}
