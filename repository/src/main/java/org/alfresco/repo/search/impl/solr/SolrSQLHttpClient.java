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

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.index.shard.Floc;
import org.alfresco.repo.index.shard.ShardRegistry;
import org.alfresco.repo.search.impl.lucene.JSONResult;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.repo.search.impl.lucene.SolrJsonProcessor;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.StatsParameters;
import org.alfresco.service.cmr.search.StatsResultSet;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.extensions.surf.util.I18NUtil;
/**
 * HTTP Client that queries Solr using the sql handler.
 * @author Michael Suzuki
 */
public class SolrSQLHttpClient extends AbstractSolrQueryHTTPClient implements SolrQueryClient
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrSQLHttpClient.class);
    private HashMap<StoreRef, SolrStoreMappingWrapper> mappingLookup = new HashMap<StoreRef, SolrStoreMappingWrapper>();
    private List<SolrStoreMapping> storeMappings;
    private BeanFactory beanFactory;
    private RepositoryState repositoryState;
    private boolean includeGroupsForRoleAdmin = false;
    private boolean anyDenyDenies;
    private boolean useDynamicShardRegistration;
    private ShardRegistry shardRegistry;
    private TenantService tenantService;
    private PermissionService permissionService;
    
    public static final int DEFAULT_SAVEPOST_BUFFER = 4096;
    @Override
    public void afterPropertiesSet() throws Exception
    {
        mappingLookup.clear();
        for(SolrStoreMapping mapping : storeMappings)
        {
            mappingLookup.put(mapping.getStoreRef(), new ExplicitSolrStoreMappingWrapper(mapping, beanFactory));
        }
        
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }
    public void init()
    {
        PropertyCheck.mandatory(this, "PermissionService", permissionService);
        PropertyCheck.mandatory(this, "StoreMappings", storeMappings);
        PropertyCheck.mandatory(this, "RepositoryState", repositoryState);
        PropertyCheck.mandatory(this, "TenantService", tenantService);
    }
    
    public ResultSet executeQuery(SearchParameters searchParameters, String language) 
    {
        if(repositoryState.isBootstrapping())
        {
            throw new AlfrescoRuntimeException("SOLR queries can not be executed while the repository is bootstrapping");
        }
        if(StringUtils.isEmpty(searchParameters.getQuery()))
        {
            throw new AlfrescoRuntimeException("SOLR query statement is missing");
        }
        try
        {
            StoreRef store = SolrClientUtil.extractStoreRef(searchParameters);
            SolrStoreMappingWrapper mapping = SolrClientUtil.extractMapping(store,
                                                                            mappingLookup,
                                                                            shardRegistry, 
                                                                            useDynamicShardRegistration,
                                                                            beanFactory);
            
            //Extract collection name from stmt.
            Pair<HttpClient, String> httpClientAndBaseUrl = mapping.getHttpClientAndBaseUrl();
            HttpClient httpClient = httpClientAndBaseUrl.getFirst();

            URLCodec encoder = new URLCodec();
            StringBuilder url = new StringBuilder();
            url.append(httpClientAndBaseUrl.getSecond());
         
            if(!url.toString().endsWith("/"))
            {
                url.append("/");
            }
            url.append("sql?stmt=" + encoder.encode(searchParameters.getQuery()));
            SearchParameters sp = (SearchParameters) searchParameters;
            url.append("&includeMetadata=" + sp.isIncludeMetadata());
            url.append("&aggregationMode=facet");
            if(searchParameters.getTimezone() != null && !searchParameters.getTimezone().isEmpty())
            {
                url.append("&TZ=").append(encoder.encode(searchParameters.getTimezone(), "UTF-8"));
            }
            url.append("&alfresco.shards=");
            /*
             * When sharded we pass array of shard instances otherwise we pass the local instance url which
             * is http://url:port/solr/collection_name
             */
            if(mapping.isSharded())
            {
                url.append(mapping.getShards());
            }
            else
            {
               String solrurl = httpClient.getHostConfiguration().getHostURL() + httpClientAndBaseUrl.getSecond();
                url.append(solrurl);
            }
            JSONObject body = new JSONObject();
            
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

            JSONArray filterQueries = new JSONArray();
            for (String filterQuery : searchParameters.getFilterQueries())
            {
                filterQueries.put(filterQuery);
            }
            body.put("filterQueries", filterQueries);

            return postSolrQuery(httpClient, url.toString(), body,
                        json -> new SolrSQLJSONResultSet(json, searchParameters));
        }
        catch (ConnectException ce)
        {
            throw new LuceneQueryParserException("Unable to reach InsightEngine", ce);
        }
        catch (JSONException | IOException | EncoderException e)
        {
            throw new LuceneQueryParserException("Unable to parse the solr response ", e);
        }
        
    }

    protected ResultSet postSolrQuery(HttpClient httpClient, String url, JSONObject body,
                SolrJsonProcessor<?> jsonProcessor) throws IOException, JSONException
    {
        JSONObject json = postQuery(httpClient, url, body);
        JSONResult results = jsonProcessor.getResult(json);
        LOGGER.debug("Sent : {}", url);
        LOGGER.debug("with: {}", body);
        LOGGER.debug("Got: {} in {} ms", results.getNumberFound(), results.getQueryTime());
        return (ResultSet) results;
    }

    
    public void setStoreMappings(List<SolrStoreMapping> storeMappings)
    {
        this.storeMappings = storeMappings;
    }

    public void setRepositoryState(RepositoryState repositoryState)
    {
        this.repositoryState = repositoryState;
    }
    /**
     * @param includeGroupsForRoleAdmin the includeGroupsForRoleAdmin to set
     */
    public void setIncludeGroupsForRoleAdmin(boolean includeGroupsForRoleAdmin)
    {
        this.includeGroupsForRoleAdmin = includeGroupsForRoleAdmin;
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

    public void setMappingLookup(HashMap<StoreRef, SolrStoreMappingWrapper> mappingLookup)
    {
        this.mappingLookup = mappingLookup;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setUseDynamicShardRegistration(boolean useDynamicShardRegistration)
    {
        this.useDynamicShardRegistration = useDynamicShardRegistration;
    }

    public void setShardRegistry(ShardRegistry shardRegistry)
    {
        this.shardRegistry = shardRegistry;
    }

    public RepositoryState getRepositoryState()
    {
        return repositoryState;
    }

    public boolean isIncludeGroupsForRoleAdmin()
    {
        return includeGroupsForRoleAdmin;
    }

    public boolean isAnyDenyDenies()
    {
        return anyDenyDenies;
    }

    public boolean isUseDynamicShardRegistration()
    {
        return useDynamicShardRegistration;
    }

    public ShardRegistry getShardRegistry()
    {
        return shardRegistry;
    }

    public TenantService getTenantService()
    {
        return tenantService;
    }

    public PermissionService getPermissionService()
    {
        return permissionService;
    }

    @Override
    public StatsResultSet executeStatsQuery(StatsParameters searchParameters)
    {
        // TODO remove this as its not needed
        return null;
    }

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

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
}
