/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.search.impl.solr.facet;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.search.impl.solr.facet.Exceptions.DuplicateFacetId;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.MissingFacetId;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.UnrecognisedFacetId;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Function;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * Integration tests for {@link SolrFacetServiceImpl}.
 */
public class SolrFacetServiceImplTest
{
    // Rule to initialise the default Alfresco spring configuration
    @ClassRule public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    
    @Rule public RunAsFullyAuthenticatedRule runAsRule = new RunAsFullyAuthenticatedRule(AuthenticationUtil.getAdminUserName());

    // Various services
    private static SolrFacetService          SOLR_FACET_SERVICE;
    private static RetryingTransactionHelper TRANSACTION_HELPER;
    
    @BeforeClass public static void initStaticData() throws Exception
    {
        SOLR_FACET_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("solrFacetService", SolrFacetService.class);
        TRANSACTION_HELPER = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
    }
    
    // TODO Ensure non-admin, non-search-admin user cannot access SolrFacetService
    
    @Test public void getFacetsAndReorderThem() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
                final List<String> facetIds = getExistingFacetIds();
                final List<String> reorderedFacetIds = new ArrayList<>(facetIds);
                Collections.reverse(reorderedFacetIds);
                
                SOLR_FACET_SERVICE.reorderFacets(reorderedFacetIds);
                
                final List<String> newfacetIds = getExistingFacetIds();
                
                assertEquals(reorderedFacetIds, newfacetIds);
                
                return null;
            }
        });
    }
    
    @Test(expected=NullPointerException.class)
    public void reorderNullFacetIdsShouldFail() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
                SOLR_FACET_SERVICE.reorderFacets(null);
                return null;
            }
        });
    }
    
    @Test(expected=MissingFacetId.class)
    public void reorderEmptyFacetIdsShouldFail() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
                SOLR_FACET_SERVICE.reorderFacets(Collections.<String>emptyList());
                return null;
            }
        });
    }
    
    @Test(expected=DuplicateFacetId.class)
    public void reorderDuplicateFacetIdsShouldFail() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
                final List<String> facetIds = getExistingFacetIds();
                facetIds.add(facetIds.get(0));
                
                SOLR_FACET_SERVICE.reorderFacets(facetIds);
                return null;
            }
        });
    }
    
    @Test(expected=UnrecognisedFacetId.class)
    public void reorderUnrecognisedFacetIdsShouldFail() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
                final List<String> facetIds = getExistingFacetIds();
                facetIds.add("unrecognisedID");
                
                SOLR_FACET_SERVICE.reorderFacets(facetIds);
                return null;
            }
            
        });
    }
    
    private List<String> getExistingFacetIds()
    {
        final List<SolrFacetProperties> facetProps = SOLR_FACET_SERVICE.getFacets();
        final List<String> facetIds = CollectionUtils.transform(facetProps,
                                                                new Function<SolrFacetProperties, String>()
                                                                {
                                                                    @Override public String apply(SolrFacetProperties value)
                                                                    {
                                                                        return value.getFilterID();
                                                                    }
                                                                });
        return facetIds;
    }
}
