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

import static org.alfresco.util.SearchDateConversion.parseDateInterval;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.opencmis.dictionary.CMISStrictDictionaryService;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.index.shard.Floc;
import org.alfresco.repo.index.shard.ShardRegistry;
import org.alfresco.repo.search.impl.QueryParserUtils;
import org.alfresco.repo.search.impl.lucene.JSONResult;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.repo.search.impl.lucene.SolrJSONResultSet;
import org.alfresco.repo.search.impl.lucene.SolrJsonProcessor;
import org.alfresco.repo.search.impl.lucene.SolrStatsResult;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.BasicSearchParameters;
import org.alfresco.service.cmr.search.FieldHighlightParameters;
import org.alfresco.service.cmr.search.Interval;
import org.alfresco.service.cmr.search.IntervalSet;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.RangeParameters;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacetMethod;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacetSort;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.StatsParameters;
import org.alfresco.service.cmr.search.StatsRequestParameters;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.params.HighlightParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Andy
 */
public class SolrQueryHTTPClient extends AbstractSolrQueryHTTPClient implements SolrQueryClient
{
    static Log s_logger = LogFactory.getLog(SolrQueryHTTPClient.class);

    private DictionaryService dictionaryService;

    private NodeService nodeService;

    private PermissionService permissionService;
    
    private NodeDAO nodeDAO;
    
    private TenantService tenantService;
    
    private ShardRegistry shardRegistry;

    private Map<String, String> languageMappings;

    private List<SolrStoreMapping> storeMappings;

    private HashMap<StoreRef, SolrStoreMappingWrapper> mappingLookup = new HashMap<StoreRef, SolrStoreMappingWrapper>();

	private String alternativeDictionary = CMISStrictDictionaryService.DEFAULT;
	
	private RepositoryState repositoryState;

    private BeanFactory beanFactory;
    
    private boolean includeGroupsForRoleAdmin = false;
    
    private int maximumResultsFromUnlimitedQuery = Integer.MAX_VALUE;

    private boolean anyDenyDenies;
    
    private boolean useDynamicShardRegistration = false;
    
    private int defaultUnshardedFacetLimit = 100;
    
    private int defaultShardedFacetLimit = 20;

    private NamespaceDAO namespaceDAO;

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
        PropertyCheck.mandatory(this, "namespaceDAO", namespaceDAO);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
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
     * @param nodeDAO the nodeDao to set
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

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespaceDAO(NamespaceDAO namespaceDAO)
    {
        this.namespaceDAO = namespaceDAO;
    }

    public void setShardRegistry(ShardRegistry shardRegistry)
    {
        this.shardRegistry = shardRegistry;
    }

    public void setUseDynamicShardRegistration(boolean useDynamicShardRegistration)
    {
        this.useDynamicShardRegistration = useDynamicShardRegistration;
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
     * When set, a single DENIED ACL entry for any authority will result in
     * access being denied as a whole. See system property {@code security.anyDenyDenies}
     * 
     * @param anyDenyDenies boolean
     */
    public void setAnyDenyDenies(boolean anyDenyDenies)
    {
        this.anyDenyDenies = anyDenyDenies;
    }
    
    /**
     * @param defaultUnshardedFacetLimit the defaultUnshardedFacetLimit to set
     */
    public void setDefaultUnshardedFacetLimit(int defaultUnshardedFacetLimit)
    {
        this.defaultUnshardedFacetLimit = defaultUnshardedFacetLimit;
    }

    /**
     * @param defaultShardedFacetLimit the defaultShardedFacetLimit to set
     */
    public void setDefaultShardedFacetLimit(int defaultShardedFacetLimit)
    {
        this.defaultShardedFacetLimit = defaultShardedFacetLimit;
    }

    /**
     * Executes a solr query for statistics
     * 
     * @param searchParameters StatsParameters
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
            StoreRef store = SolrClientUtil.extractStoreRef(searchParameters);
            SolrStoreMappingWrapper mapping = 
                    SolrClientUtil.extractMapping(store, 
                                                  mappingLookup,
                                                  shardRegistry, 
                                                  useDynamicShardRegistration,
                                                  beanFactory);
            
            Locale locale = SolrClientUtil.extractLocale(searchParameters);
            
            Pair<HttpClient, String> httpClientAndBaseUrl = mapping.getHttpClientAndBaseUrl();
            HttpClient httpClient = httpClientAndBaseUrl.getFirst();
            String url = buildStatsUrl(searchParameters, httpClientAndBaseUrl.getSecond(), locale, mapping);
            JSONObject body = buildStatsBody(searchParameters, tenantService.getCurrentUserDomain(), locale);
            
            if(httpClient == null)
            {
                throw new AlfrescoRuntimeException("No http client for store " + store.toString());
            }
            
            return (SolrStatsResult) postSolrQuery(httpClient, url, body, json ->
            {
                return new SolrStatsResult(json, searchParameters.isDateSearch());
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

    protected String buildStatsUrl(StatsParameters searchParameters, String baseUrl, Locale locale, SolrStoreMappingWrapper mapping) throws UnsupportedEncodingException
    {
        URLCodec encoder = new URLCodec();
        StringBuilder url = new StringBuilder();
        String languageUrlFragment = SolrClientUtil.extractLanguageFragment(languageMappings, searchParameters.getLanguage());
        
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
        
        if((mapping != null) && ((searchParameters.getStores().size() > 1) || (mapping.isSharded())))
        {
            url.append("&shards=");
            buildShards(url, searchParameters.getStores());
        }
        
        return url.toString();
    }

    protected void buildShards(StringBuilder url, List<StoreRef> storeRefs)
    {
        boolean requiresSeparator = false;
        for(StoreRef storeRef : storeRefs)
        {
            SolrStoreMappingWrapper storeMapping = SolrClientUtil.extractMapping(storeRef, mappingLookup, shardRegistry, requiresSeparator, beanFactory);

            if(requiresSeparator)
            {
                url.append(',');
            }
            else
            {
                requiresSeparator = true;
            }

            url.append(storeMapping.getShards());

        }
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
            StoreRef store = SolrClientUtil.extractStoreRef(searchParameters);
            SolrStoreMappingWrapper mapping = SolrClientUtil.extractMapping(store, 
                                                                            mappingLookup,
                                                                            shardRegistry,
                                                                            useDynamicShardRegistration,
                                                                            beanFactory);
            
            Pair<HttpClient, String> httpClientAndBaseUrl = mapping.getHttpClientAndBaseUrl();
            HttpClient httpClient = httpClientAndBaseUrl.getFirst();

            URLCodec encoder = new URLCodec();
            StringBuilder url = new StringBuilder();
            url.append(httpClientAndBaseUrl.getSecond());
         
            String languageUrlFragment = SolrClientUtil.extractLanguageFragment(languageMappings, language);
            if(!url.toString().endsWith("/"))
            {
                url.append("/");
            }
            url.append(languageUrlFragment);

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

            if((searchParameters.getStores().size() > 1) || (mapping.isSharded()))
            {
                boolean requiresSeparator = false;
                url.append("&shards=");
                for(StoreRef storeRef : searchParameters.getStores())
                {
                    SolrStoreMappingWrapper storeMapping =
                            SolrClientUtil.extractMapping(storeRef, 
                                                          mappingLookup, shardRegistry, 
                                                          requiresSeparator, beanFactory);

                    if(requiresSeparator)
                    {
                        url.append(',');
                    }
                    else
                    {
                        requiresSeparator = true;
                    }

                    url.append(storeMapping.getShards());

                }
            }

            buildUrlParameters(searchParameters, mapping.isSharded(), encoder, url);

            final String searchTerm = searchParameters.getSearchTerm();
            String spellCheckQueryStr = null;
            if (searchTerm != null && searchParameters.isSpellCheck())
            {
                StringBuilder builder = new StringBuilder();
                builder.append("&spellcheck.q=").append(encoder.encode(searchTerm, "UTF-8"));
                builder.append("&spellcheck=").append(encoder.encode("true", "UTF-8"));
                spellCheckQueryStr = builder.toString();
                url.append(spellCheckQueryStr);
            }

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
            body.put("anyDenyDenies", anyDenyDenies);
            
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
            
            return (ResultSet) postSolrQuery(httpClient, url.toString(), body, json ->
            {
                return new SolrJSONResultSet(json, searchParameters, nodeService, nodeDAO, limitBy, maximumResults);
            }, spellCheckQueryStr);
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

    /**
     * Builds most of the Url parameters for a Solr Http request.
     * @param searchParameters
     * @param isSharded
     * @param encoder
     * @param url
     * @throws UnsupportedEncodingException
     */
    public void buildUrlParameters(SearchParameters searchParameters, boolean isSharded, URLCodec encoder, StringBuilder url)
                throws UnsupportedEncodingException
    {
        Locale locale = SolrClientUtil.extractLocale(searchParameters);
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

        if(searchParameters.getTimezone() != null && !searchParameters.getTimezone().isEmpty())
        {
            url.append("&TZ=").append(encoder.encode(searchParameters.getTimezone(), "UTF-8"));
        }

        // filter queries
        for(String filterQuery : searchParameters.getFilterQueries())
        {
            if (!filterQuery.startsWith("{!afts"))
            {
                filterQuery = "{!afts}"+filterQuery;
            }
            url.append("&fq=").append(encoder.encode(filterQuery, "UTF-8"));
        }

        buildFacetParameters(searchParameters, isSharded, encoder, url);
        buildPivotParameters(searchParameters, encoder, url);
        buildStatsParameters(searchParameters, encoder, url);
        buildFacetIntervalParameters(searchParameters, encoder, url);
        buildRangeParameters(searchParameters, encoder, url);
        buildHightlightParameters(searchParameters, encoder, url);
    }

    protected void buildFacetParameters(SearchParameters searchParameters, boolean isSharded, URLCodec encoder, StringBuilder url)
                throws UnsupportedEncodingException
    {
        if(searchParameters.getFieldFacets().size() > 0 || searchParameters.getFacetQueries().size() > 0)
        {
            url.append("&facet=").append(encoder.encode("true", "UTF-8"));
            for(FieldFacet facet : searchParameters.getFieldFacets())
            {
                url.append("&facet.field=");
                String field = facet.getField();
                StringBuilder prefix = new StringBuilder("{!afts ");

                int startIndex = field.startsWith("{!afts")?7:0;

                if (facet.getExcludeFilters() != null && !facet.getExcludeFilters().isEmpty())
                {
                    prefix.append("ex="+String.join(",", facet.getExcludeFilters())+" ");
                }

                if (facet.getLabel() != null && !facet.getLabel().isEmpty())
                {
                    prefix.append("key="+facet.getLabel()+" ");
                }

                if (startIndex!=0)
                {
                    int endIndex = field.indexOf("}");
                    prefix.append(field.substring(startIndex,endIndex>startIndex?endIndex:startIndex));
                    field = field.substring(endIndex+1);
                }

                if (prefix.length() > 7)
                {
                    url.append(encoder.encode(prefix.toString().trim(), "UTF-8"));
                    url.append(encoder.encode("}", "UTF-8"));
                }

                url.append(encoder.encode(field, "UTF-8"));

                if(facet.getEnumMethodCacheMinDF() != 0)
                {
                    url.append("&").append(encoder.encode("f."+field+".facet.enum.cache.minDf", "UTF-8")).append("=").append(encoder.encode(""+facet.getEnumMethodCacheMinDF(), "UTF-8"));
                }
                int facetLimit;
                if(facet.getLimitOrNull() == null)
                {
                    if(isSharded())
                    {
                        facetLimit = defaultShardedFacetLimit;
                    }
                    else
                    {
                        facetLimit = defaultUnshardedFacetLimit;
                    }
                }
                else
                {
                    facetLimit = facet.getLimitOrNull().intValue();
                }
                url.append("&").append(encoder.encode("f."+field+".facet.limit", "UTF-8")).append("=").append(encoder.encode(""+facetLimit, "UTF-8"));
                if(facet.getMethod() != null)
                {
                    url.append("&").append(encoder.encode("f."+field+".facet.method", "UTF-8")).append("=").append(encoder.encode(facet.getMethod()== FieldFacetMethod.ENUM ?  "enum" : "fc", "UTF-8"));
                }
                if(facet.getMinCount() != 0)
                {
                    url.append("&").append(encoder.encode("f."+field+".facet.mincount", "UTF-8")).append("=").append(encoder.encode(""+facet.getMinCount(), "UTF-8"));
                }
                if(facet.getOffset() != 0)
                {
                    url.append("&").append(encoder.encode("f."+field+".facet.offset", "UTF-8")).append("=").append(encoder.encode(""+facet.getOffset(), "UTF-8"));
                }
                if(facet.getPrefix() != null)
                {
                    url.append("&").append(encoder.encode("f."+field+".facet.prefix", "UTF-8")).append("=").append(encoder.encode(""+facet.getPrefix(), "UTF-8"));
                }
                if(facet.getSort() != null)
                {
                    url.append("&").append(encoder.encode("f."+field+".facet.sort", "UTF-8")).append("=").append(encoder.encode(facet.getSort() == FieldFacetSort.COUNT ? "count" : "index", "UTF-8"));
                }
                if(facet.isCountDocsMissingFacetField() != false)
                {
                    url.append("&").append(encoder.encode("f."+field+".facet.missing", "UTF-8")).append("=").append(encoder.encode(""+facet.isCountDocsMissingFacetField(), "UTF-8"));
                }

            }
            for(String facetQuery : searchParameters.getFacetQueries())
            {
                if (!facetQuery.startsWith("{!afts"))
                {
                    facetQuery = "{!afts}"+facetQuery;
                }
                url.append("&facet.query=").append(encoder.encode(facetQuery, "UTF-8"));
            }
        }
    }

    protected void buildStatsParameters(SearchParameters searchParameters, URLCodec encoder, StringBuilder url) throws UnsupportedEncodingException
    {
        if (searchParameters.getStats() != null && !searchParameters.getStats().isEmpty())
        {
            url.append("&stats=").append(encoder.encode("true", "UTF-8"));

            for (StatsRequestParameters aStat:searchParameters.getStats())
            {
                url.append("&stats.field=");
                StringBuilder prefix = new StringBuilder("{! ");

                if (aStat.getExcludeFilters() != null && !aStat.getExcludeFilters().isEmpty())
                {
                    prefix.append("ex="+String.join(",", aStat.getExcludeFilters())+" ");
                }

                if (aStat.getLabel() != null && !aStat.getLabel().isEmpty())
                {
                    prefix.append("tag="+aStat.getLabel()+" ");
                    prefix.append("key="+aStat.getLabel()+" ");
                }

                if (aStat.getPercentiles() != null && !aStat.getPercentiles().isEmpty())
                {
                    StringJoiner joiner = new StringJoiner(",");
                    for (Float aFloat: aStat.getPercentiles()) {
                        joiner.add(aFloat.toString());
                    }
                    prefix.append("percentiles='"+joiner.toString()+"' ");
                }

                if (aStat.getCardinality())
                {
                    prefix.append("cardinality="+aStat.getCardinalityAccuracy()+" ");
                }

                prefix.append("countDistinct="+aStat.getCountDistinct()+" ");
                prefix.append("distinctValues="+aStat.getDistinctValues()+" ");
                prefix.append("min="+aStat.getMin()+" ");
                prefix.append("max="+aStat.getMax()+" ");
                prefix.append("sum="+aStat.getSum()+" ");
                prefix.append("count="+aStat.getCountValues()+" ");
                prefix.append("missing="+aStat.getMissing()+" ");
                prefix.append("sumOfSquares="+aStat.getSumOfSquares()+" ");
                prefix.append("mean="+aStat.getMean()+" ");
                prefix.append("stddev="+aStat.getStddev()+" ");

                url.append(encoder.encode(prefix.toString().trim(), "UTF-8"));
                url.append(encoder.encode("}", "UTF-8"));

                url.append(encoder.encode(aStat.getField(), "UTF-8"));
            }
        }
    }

    protected void buildPivotParameters(SearchParameters searchParameters, URLCodec encoder, StringBuilder url) throws UnsupportedEncodingException
    {
        if (searchParameters.getPivots() != null && !searchParameters.getPivots().isEmpty())
        {
            url.append("&facet=").append(encoder.encode("true", "UTF-8"));
            for (List<String> pivotKeys:searchParameters.getPivots())
            {
                List<String> pivotsList = new ArrayList<>();
                pivotsList.addAll(pivotKeys);
                url.append("&facet.pivot=");

                StringBuilder prefix = new StringBuilder("{! ");

                if (searchParameters.getStats() != null && !searchParameters.getStats().isEmpty())
                {
                    for (StatsRequestParameters aStat:searchParameters.getStats())
                    {
                        if (pivotKeys.contains(aStat.getLabel()))
                        {
                            prefix.append("stats="+aStat.getLabel()+" ");
                            pivotsList.remove(aStat.getLabel());
                            break; //only do it once
                        }
                    }
                }

                if (searchParameters.getRanges() != null && !searchParameters.getRanges().isEmpty())
                {
                    for (RangeParameters aRange:searchParameters.getRanges())
                    {
                        Optional<String> found = pivotKeys.stream().filter(aKey -> aKey.equals(aRange.getLabel())).findFirst();

                        if (found.isPresent())
                        {
                            prefix.append("range="+found.get()+" ");
                            pivotsList.remove(found.get());
                            break; //only do it once
                        }
                    }
                }

                if (prefix.length() > 3)  //We have add something
                {
                    url.append(encoder.encode(prefix.toString().trim(), "UTF-8"));
                    url.append(encoder.encode("}", "UTF-8"));
                }
                url.append(encoder.encode(String.join(",", pivotsList), "UTF-8"));
            }
        }
    }
    protected void buildRangeParameters(SearchParameters searchParameters, URLCodec encoder, StringBuilder url) throws UnsupportedEncodingException
    {
        if (searchParameters.getRanges() != null && !searchParameters.getRanges().isEmpty())
        {
            List<RangeParameters> ranges = searchParameters.getRanges();
            url.append("&facet=").append(encoder.encode("true", "UTF-8"));
            
            for(RangeParameters facetRange : ranges)
            {
                String fieldName = facetRange.getField();
                boolean isDate = false;
                PropertyDefinition propertyDef = QueryParserUtils.matchPropertyDefinition(searchParameters.getNamespace(),
                        namespaceDAO, dictionaryService, fieldName);
                if (propertyDef != null && (propertyDef.getDataType().getName().equals(DataTypeDefinition.DATETIME)
                        || propertyDef.getDataType().getName().equals(DataTypeDefinition.DATE)))
                {
                    isDate = true;
                }
                
                
                IntervalSet rangeSet =
                        parseDateInterval(
                                new IntervalSet(facetRange.getStart(), 
                                                facetRange.getEnd(), 
                                                facetRange.getGap(), 
                                                null, 
                                                null), isDate);
                url.append("&facet.range=");

                if(facetRange.getLabel()!= null && !facetRange.getLabel().isEmpty())
                {
                    url.append(encoder.encode("{!", "UTF-8"));
                    url.append(encoder.encode(String.format("tag=%s ",facetRange.getLabel()), "UTF-8"));
                    url.append(encoder.encode("}", "UTF-8"));
                }

                url.append(encoder.encode(facetRange.getField(), "UTF-8"));

                //Check if date and if inclusive or not
                url.append(String.format("&f.%s.facet.range.start=",fieldName)).append(encoder.encode(""+ rangeSet.getStart(), "UTF-8"));
                url.append(String.format("&f.%s.facet.range.end=",fieldName)).append(encoder.encode(""+ rangeSet.getEnd(), "UTF-8"));
                url.append(String.format("&f.%s.facet.range.gap=",fieldName)).append(encoder.encode(""+ rangeSet.getLabel(), "UTF-8"));
                url.append(String.format("&f.%s.facet.range.hardend=",fieldName)).append(encoder.encode("" + facetRange.isHardend(), "UTF-8"));
                if(facetRange.getInclude() != null && !facetRange.getInclude().isEmpty())
                {
                    for(String include : facetRange.getInclude())
                    {
                        url.append(String.format("&f.%s.facet.range.include=",fieldName)).append(encoder.encode("" + include, "UTF-8"));
                    }
                }
                if(facetRange.getOther() != null && !facetRange.getOther().isEmpty())
                {
                    for(String other : facetRange.getOther())
                    {
                        url.append(String.format("&f.%s.facet.range.other=",fieldName)).append(encoder.encode("" + other, "UTF-8"));
                    }
                }
                if(!facetRange.getExcludeFilters().isEmpty())
                {
                    url.append("&facet.range=");
                    if (facetRange.getExcludeFilters() != null && !facetRange.getExcludeFilters().isEmpty())
                    {
                        StringBuilder prefix = new StringBuilder("{!ex=");
                        Iterator<String> itr = facetRange.getExcludeFilters().iterator();
                        while(itr.hasNext())
                        {
                            String val = itr.next();
                            prefix.append(val);
                            if(itr.hasNext())
                            {
                                prefix.append(",");
                            }
                        }
                        prefix.append("}");
                        url.append(prefix);
                        url.append(fieldName);
                    }
                    
                }
            }
        }
    }

    protected void buildHightlightParameters(SearchParameters searchParameters, URLCodec encoder, StringBuilder url) throws UnsupportedEncodingException
    {
        if (searchParameters.getHighlight() != null)
        {
            url.append("&").append(HighlightParams.HIGHLIGHT+"=true");
            url.append("&"+HighlightParams.HIGHLIGHT+".q=").append(encoder.encode(searchParameters.getSearchTerm(), "UTF-8"));

            if (searchParameters.getHighlight().getSnippetCount() != null)
            {
                url.append("&")
                   .append(HighlightParams.SNIPPETS+"=")
                   .append(searchParameters.getHighlight().getSnippetCount());
            }
            if (searchParameters.getHighlight().getFragmentSize() != null)
            {
                url.append("&")
                   .append(HighlightParams.FRAGSIZE+"=")
                   .append(searchParameters.getHighlight().getFragmentSize());
            }
            if (searchParameters.getHighlight().getMaxAnalyzedChars() != null)
            {
                url.append("&")
                   .append(HighlightParams.MAX_CHARS+"=")
                   .append(searchParameters.getHighlight().getMaxAnalyzedChars());
            }
            if (searchParameters.getHighlight().getMergeContiguous() != null)
            {
                url.append("&")
                   .append(HighlightParams.MERGE_CONTIGUOUS_FRAGMENTS+"=")
                   .append(searchParameters.getHighlight().getMergeContiguous());
            }
            if (searchParameters.getHighlight().getUsePhraseHighlighter() != null)
            {
                url.append("&")
                   .append(HighlightParams.USE_PHRASE_HIGHLIGHTER+"=")
                   .append(searchParameters.getHighlight().getUsePhraseHighlighter());
            }
            if (searchParameters.getHighlight().getPrefix() != null)
            {
                url.append("&")
                   .append(HighlightParams.SIMPLE_PRE+"=")
                   .append(encoder.encode(searchParameters.getHighlight().getPrefix(), "UTF-8"));
            }
            if (searchParameters.getHighlight().getPostfix() != null)
            {
                url.append("&")
                   .append(HighlightParams.SIMPLE_POST+"=")
                   .append(encoder.encode(searchParameters.getHighlight().getPostfix(), "UTF-8"));
            }
            if (searchParameters.getHighlight().getFields() != null && !searchParameters.getHighlight().getFields().isEmpty())
            {
                List<String> fieldNames = new ArrayList<>(searchParameters.getHighlight().getFields().size());
                for (FieldHighlightParameters aField:searchParameters.getHighlight().getFields())
                {
                    ParameterCheck.mandatoryString("highlight field", aField.getField());
                    fieldNames.add(aField.getField());

                    if (aField.getSnippetCount() != null)
                    {
                        url.append("&f.").append(encoder.encode(aField.getField(), "UTF-8"))
                           .append("."+HighlightParams.SNIPPETS+"=")
                           .append(aField.getSnippetCount());
                    }

                    if (aField.getFragmentSize() != null)
                    {
                        url.append("&f.").append(encoder.encode(aField.getField(), "UTF-8"))
                                    .append("."+HighlightParams.FRAGSIZE+"=")
                                    .append(aField.getFragmentSize());
                    }

                    if (aField.getFragmentSize() != null)
                    {
                        url.append("&f.").append(encoder.encode(aField.getField(), "UTF-8"))
                                    .append("."+HighlightParams.FRAGSIZE+"=")
                                    .append(aField.getFragmentSize());
                    }

                    if (aField.getMergeContiguous() != null)
                    {
                        url.append("&f.").append(encoder.encode(aField.getField(), "UTF-8"))
                                    .append("."+HighlightParams.MERGE_CONTIGUOUS_FRAGMENTS+"=")
                                    .append(aField.getMergeContiguous());
                    }

                    if (aField.getPrefix() != null)
                    {
                        url.append("&f.").append(encoder.encode(aField.getField(), "UTF-8"))
                                    .append("."+HighlightParams.SIMPLE_PRE+"=")
                                    .append(encoder.encode(aField.getPrefix(), "UTF-8"));
                    }

                    if (aField.getPostfix() != null)
                    {
                        url.append("&f.").append(encoder.encode(aField.getField(), "UTF-8"))
                                    .append("."+HighlightParams.SIMPLE_POST+"=")
                                    .append(encoder.encode(aField.getPostfix(), "UTF-8"));
                    }
                }
                url.append("&")
                   .append(HighlightParams.FIELDS+"=")
                   .append(encoder.encode(String.join(",", fieldNames), "UTF-8"));
            }
        }
    }

    protected void buildFacetIntervalParameters(SearchParameters searchParameters, URLCodec encoder, StringBuilder url) throws UnsupportedEncodingException
    {
        if (searchParameters.getInterval() != null)
        {
            url.append("&facet=").append(encoder.encode("true", "UTF-8"));

            if (searchParameters.getInterval().getSets() != null)
            {
                for (IntervalSet aSet:searchParameters.getInterval().getSets())
                {
                    url.append("&facet.interval.set=").append(encoder.encode(aSet.toParam(), "UTF-8"));
                }
            }

            if (searchParameters.getInterval().getIntervals() != null)
            {
                for (Interval interval:searchParameters.getInterval().getIntervals())
                {
                    ParameterCheck.mandatory("facetIntervals intervals field", interval.getField());

                    url.append("&facet.interval=");
                    boolean isDate = false;

                    PropertyDefinition propertyDef = QueryParserUtils.matchPropertyDefinition(searchParameters.getNamespace(),
                                namespaceDAO, dictionaryService, interval.getField());
                    if (propertyDef != null && (propertyDef.getDataType().getName().equals(DataTypeDefinition.DATETIME)
                                || propertyDef.getDataType().getName().equals(DataTypeDefinition.DATE)))
                    {
                        isDate = true;
                    }

                    if (interval.getLabel() != null && !interval.getLabel().isEmpty())
                    {
                        url.append(encoder.encode("{!key="+interval.getLabel()+"}", "UTF-8"));
                    }
                    url.append(encoder.encode(interval.getField(), "UTF-8"));

                    if (interval.getSets() != null)
                    {
                        for (IntervalSet aSet:interval.getSets())
                        {
                            IntervalSet validated = parseDateInterval(aSet,isDate);
                            url.append("&").append(encoder.encode("f."+interval.getField()+".facet.interval.set", "UTF-8")).append("=").append(encoder.encode(validated.toParam(), "UTF-8"));
                        }
                    }
                }
            }
        }
    }

    protected JSONResult postSolrQuery(HttpClient httpClient, String url, JSONObject body, SolrJsonProcessor<?> jsonProcessor)
                throws UnsupportedEncodingException, IOException, HttpException, URIException,
                JSONException
    {
        return postSolrQuery(httpClient, url, body, jsonProcessor, null);
    }

    protected JSONResult postSolrQuery(HttpClient httpClient, String url, JSONObject body, SolrJsonProcessor<?> jsonProcessor, String spellCheckParams)
                throws UnsupportedEncodingException, IOException, HttpException, URIException,
                JSONException
    {
        JSONObject json = postQuery(httpClient, url, body);
        if (spellCheckParams != null)
        {
            SpellCheckDecisionManager manager = new SpellCheckDecisionManager(json, url, body, spellCheckParams);
            if (manager.isCollate())
            {
                json = postQuery(httpClient, manager.getUrl(), body);
            }
            json.put("spellcheck", manager.getSpellCheckJsonValue());
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
            // The sort can be different, see MNT-13742
            switch (sortDefinition.getSortType())
            {
                case DOCUMENT:
                    sortBuffer.append(encoder.encode("_docid_", "UTF-8")).append(encoder.encode(" ", "UTF-8"));
                    break;
                case SCORE:
                    sortBuffer.append(encoder.encode("score", "UTF-8")).append(encoder.encode(" ", "UTF-8"));
                    break;
                case FIELD:
                default:
                    sortBuffer.append(encoder.encode(sortDefinition.getField().replaceAll(" ", "%20"), "UTF-8")).append(encoder.encode(" ", "UTF-8"));
                    break;
            }
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

   



    /* (non-Javadoc)
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception
    {
        mappingLookup.clear();
        for(SolrStoreMapping mapping : storeMappings)
        {
            mappingLookup.put(mapping.getStoreRef(), new ExplicitSolrStoreMappingWrapper(mapping, beanFactory));
        }
    }

    /**
     * @param storeRef
     * @param handler
     * @param params
     * @return
     */
    public JSONObject execute(StoreRef storeRef, String handler, HashMap<String, String> params)
    {       
        try
        {
            SolrStoreMappingWrapper mapping = SolrClientUtil.extractMapping(storeRef, mappingLookup, shardRegistry, useDynamicShardRegistration, beanFactory);
            
            URLCodec encoder = new URLCodec();
            StringBuilder url = new StringBuilder();
         
            Pair<HttpClient, String> httpClientAndBaseUrl = mapping.getHttpClientAndBaseUrl();
            HttpClient httpClient = httpClientAndBaseUrl.getFirst();

            
            for (String key : params.keySet())
            {
                String value = params.get(key);
                if (url.length() == 0)
                {
                    url.append(httpClientAndBaseUrl.getSecond());
                    
                    if(!handler.startsWith("/"))
                    {
                        url.append("/");
                    }
                    url.append(handler);
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
            
            if(mapping.isSharded())
            {
                url.append("&shards=");
                url.append(mapping.getShards());
            }

            // PostMethod post = new PostMethod(url.toString());
            GetMethod get = new GetMethod(url.toString());

            try
            {
                httpClient.executeMethod(get);

                if (get.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY || get.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY)
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

                Reader reader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream()));
                // TODO - replace with streaming-based solution e.g. SimpleJSON ContentHandler
                JSONObject json = new JSONObject(new JSONTokener(reader));
                return json;
            }
            finally
            {
                get.releaseConnection();
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

    /**
     * @return
     */
    public boolean isSharded()
    {
        if((shardRegistry != null) && useDynamicShardRegistration)
        {
            for( Floc floc : shardRegistry.getFlocs().keySet())
            {
                if(floc.getNumberOfShards() > 1)
                {
                    return true;
                }
            }
            return false;
        
        }
        else
        {
            for(SolrStoreMappingWrapper mapping : mappingLookup.values())
            {
                if(mapping.isSharded())
                {
                    return true;
                }
            }
            return false;
        }
        
    }



}
