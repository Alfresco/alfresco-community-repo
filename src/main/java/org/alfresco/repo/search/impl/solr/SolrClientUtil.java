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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.index.shard.ShardInstance;
import org.alfresco.repo.index.shard.ShardRegistry;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.BasicSearchParameters;
import org.alfresco.service.cmr.search.SearchParameters;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import org.apache.commons.logging.LogFactory;
/**
 * Helper class for solr http client.
 * @author Michael Suzuki
 *
 */
public class SolrClientUtil
{
    private static Log logger = LogFactory.getLog(SolrClientUtil.class);
    public static Locale extractLocale(BasicSearchParameters searchParameters)
    {
        Locale locale = I18NUtil.getLocale();
        if (searchParameters.getLocales().size() > 0)
        {
            locale = searchParameters.getLocales().get(0);
        }
        return locale;
    }
    
    public static StoreRef extractStoreRef(BasicSearchParameters searchParameters)
    {
        if (searchParameters.getStores().size() == 0)
        {
            throw new AlfrescoRuntimeException("No store for query");
        }
        
        StoreRef store = searchParameters.getStores().get(0);
        return store;
    }
    
    public static String extractLanguageFragment(Map<String, String> languageMappings, String language)
    {
        String languageUrlFragment = languageMappings.get(language);
        if (languageUrlFragment == null)
        {
            throw new AlfrescoRuntimeException("No solr query support for language " + language);
        }
        return languageUrlFragment;
    }
    public static SolrStoreMappingWrapper extractMapping(StoreRef store, 
            HashMap<StoreRef, SolrStoreMappingWrapper> mappingLookup, ShardRegistry shardRegistry,
            boolean useDynamicShardRegistration,BeanFactory beanFactory)
    {
        if((shardRegistry != null) && useDynamicShardRegistration)
        {
            SearchParameters sp = new SearchParameters();
            sp.addStore(store);
            List<ShardInstance> slice = shardRegistry.getIndexSlice(sp);
            if((slice == null) || (slice.size() == 0))
            {
                logger.error("No available shards for solr query of store " + store + " - trying non-dynamic configuration");
                SolrStoreMappingWrapper mappings = mappingLookup.get(store);
                if (mappings == null)
                {
                    throw new LuceneQueryParserException("No solr query support for store " + store);
                }
                return mappings;
            }
            return DynamicSolrStoreMappingWrapperFactory.wrap(slice, beanFactory);
        }
        else
        {
            SolrStoreMappingWrapper mappings = mappingLookup.get(store);

            if (mappings == null)
            {
                throw new LuceneQueryParserException("No solr query support for store " + store);
            }
            return mappings;
        }
    }
    
}
