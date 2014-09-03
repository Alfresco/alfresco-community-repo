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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.search.impl.solr.facet.Exceptions.DuplicateFacetId;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.MissingFacetId;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.UnrecognisedFacetId;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Function;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * Integration tests for {@link SolrFacetServiceImpl}.
 */
public class SolrFacetServiceImplTest
{
    private static final Log logger = LogFactory.getLog(SolrFacetServiceImplTest.class);

    // Rule to initialise the default Alfresco spring configuration
    @ClassRule public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    
    @Rule public RunAsFullyAuthenticatedRule runAsRule = new RunAsFullyAuthenticatedRule(AuthenticationUtil.getAdminUserName());

    private static final List<String> FILTERS = new ArrayList<>();
    // Various services
    private static SolrFacetService          SOLR_FACET_SERVICE;
    private static RetryingTransactionHelper TRANSACTION_HELPER;
    
    @BeforeClass public static void initStaticData() throws Exception
    {
        SOLR_FACET_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("solrFacetService", SolrFacetService.class);
        TRANSACTION_HELPER = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
    }
    
    @AfterClass
    public static void cleanup()
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                for (String filter : FILTERS)
                {
                    try
                    {
                        SOLR_FACET_SERVICE.deleteFacet(filter);
                    }
                    catch (SolrFacetConfigException sfe)
                    {
                        logger.info("Cannot delete filter [" + filter + "]. " + sfe);
                    }
                }
                return null;
            }
        });

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

    @Test
    public void updateFacet()
    {
        final String filterName = getFilterName();
        final SolrFacetProperties facetProps = new SolrFacetProperties.Builder()
                    .filterID(filterName)
                    .facetQName(QName.createQName("{http://www.alfresco.org/model/content/1.0}test"))
                    .displayName("faceted-search.facet-menu.facet.test")
                    .displayControl("alfresco/search/FacetFilters")
                    .maxFilters(5)
                    .hitThreshold(1)
                    .minFilterValueLength(4)
                    .sortBy("ALPHABETICALLY")
                    .isEnabled(false)
                    .scope("ALL").build();

        // Update a facet which isn't there
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                try
                {
                    SOLR_FACET_SERVICE.updateFacet(facetProps);
                    fail("Shouldn't be able to update a facet that does not exist");
                }
                catch (SolrFacetConfigException exception)
                {
                    // Expected
                }
                return null;
            }
        });

        // Create the facet
        this.createFacet(facetProps);

        // Update maxFilters
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                SolrFacetProperties updatedFacetProps = new SolrFacetProperties.Builder(facetProps).maxFilters(10)
                            .build();
                SOLR_FACET_SERVICE.updateFacet(updatedFacetProps);

                // Retrieve the updated facet
                updatedFacetProps = SOLR_FACET_SERVICE.getFacet(filterName);

                assertEquals(10, updatedFacetProps.getMaxFilters());
                // Check rest of the values haven't been changed
                assertEquals(filterName, updatedFacetProps.getFilterID());
                assertEquals("{http://www.alfresco.org/model/content/1.0}test", updatedFacetProps.getFacetQName().toString());
                assertEquals("faceted-search.facet-menu.facet.test", updatedFacetProps.getDisplayName());
                assertEquals("alfresco/search/FacetFilters", updatedFacetProps.getDisplayControl());
                assertEquals(4, updatedFacetProps.getMinFilterValueLength());
                assertEquals(1, updatedFacetProps.getHitThreshold());
                assertEquals("ALPHABETICALLY", updatedFacetProps.getSortBy());
                assertEquals("ALL", updatedFacetProps.getScope());
                assertFalse(updatedFacetProps.isDefault());
                assertFalse(updatedFacetProps.isEnabled());

                return null;
            }
        });
    }

    @Test(expected = SolrFacetConfigException.class)
    public void deleteFacet_DoesNotExist()
    {
        // Delete a facet which isn't there
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                SOLR_FACET_SERVICE.deleteFacet(GUID.generate());

                return null;
            }
        });
    }

    @Test
    public void deleteDefaultFacet()
    {
        // Delete a default facet, assuming it isn't persisted yet
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                List<SolrFacetProperties> defaultFacet = getDefaultFilters();
                assertTrue(defaultFacet.size() > 0);
                try
                {
                    SOLR_FACET_SERVICE.deleteFacet(defaultFacet.get(0).getFilterID());
                    fail("Shouldn't be able to delete a default facet.");
                }
                catch (SolrFacetConfigException sfex)
                {
                    // Expected
                }

                return null;
            }
        });

        // Update a value so that the default facet can be persisted
        final String defaultFilterName = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<String>()
        {
            @Override
            public String execute() throws Throwable
            {
                List<SolrFacetProperties> defaultFacet = getDefaultFilters();
                assertTrue(defaultFacet.size() > 0);
                SolrFacetProperties facetProperties = defaultFacet.get(0);
                assertNotNull(facetProperties);
                final String filterName = facetProperties.getFilterID();

                int maxFilters = facetProperties.getMaxFilters();
                
                facetProperties = new SolrFacetProperties.Builder().filterID(filterName).maxFilters(maxFilters + 1).build();
                SOLR_FACET_SERVICE.updateFacet(facetProperties);

                facetProperties = SOLR_FACET_SERVICE.getFacet(filterName);
                assertEquals(maxFilters + 1, facetProperties.getMaxFilters());

                return filterName;
            }
        });

        // Delete a default facet which has been persisted
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                try
                {
                    SOLR_FACET_SERVICE.deleteFacet(defaultFilterName);
                    fail("Shouldn't be able to delete a default facet.");
                }
                catch (SolrFacetConfigException sfex)
                {
                    // expected
                }
                return null;
            }
        });
    }

    @Test
    public void deleteFacet()
    {
        final String filterName = getFilterName();
        final SolrFacetProperties facetProps = new SolrFacetProperties.Builder()
                    .filterID(filterName)
                    .facetQName(QName.createQName("{http://www.alfresco.org/model/content/1.0}test2"))
                    .displayName("faceted-search.facet-menu.facet.test2")
                    .displayControl("alfresco/search/FacetFilters")
                    .maxFilters(5)
                    .hitThreshold(1)
                    .minFilterValueLength(2)
                    .sortBy("ALPHABETICALLY")
                    .isEnabled(true)
                    .scope("ALL").build();

        // Create the facet
        this.createFacet(facetProps);

        // Delete the facet created above
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                SOLR_FACET_SERVICE.deleteFacet(filterName);
                SolrFacetProperties solrFacetProperties = SOLR_FACET_SERVICE.getFacet(filterName);
                assertNull(solrFacetProperties);

                return null;
            }
        });
    }

    @Test
    public void createFacet()
    {
        final String filterName = getFilterName();
        final SolrFacetProperties facetProps = new SolrFacetProperties.Builder()
                    .filterID(filterName)
                    .facetQName(QName.createQName("{http://www.alfresco.org/model/content/1.0}test3"))
                    .displayName("faceted-search.facet-menu.facet.test3")
                    .displayControl("alfresco/search/FacetFilters")
                    .maxFilters(5)
                    .hitThreshold(1)
                    .minFilterValueLength(2)
                    .sortBy("ALPHABETICALLY")
                    .isEnabled(false)
                    .scope("ALL").build();

        // Create the facet
        this.createFacet(facetProps);

        // Retrieve the created facet
        SolrFacetProperties facetProperties = SOLR_FACET_SERVICE.getFacet(filterName);
        assertEquals(filterName, facetProperties.getFilterID());

        // Test for duplicate facet error
        try
        {
            facetProperties = new SolrFacetProperties.Builder().filterID(filterName).build();

            this.createFacet(facetProperties);
            fail("Shouldn't be able to create a duplicate facet.");
        }
        catch (SolrFacetConfigException ex)
        {
            // Expected
        }
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

    private NodeRef createFacet(final SolrFacetProperties facetProps)
    {
        // Create the facet
        return TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                NodeRef nodeRef = SOLR_FACET_SERVICE.createFacetNode(facetProps);

                return nodeRef;
            }
        });
    }

    private String getFilterName()
    {
        String name = "test_filter" + System.currentTimeMillis();
        FILTERS.add(name);
        return name;
    }

    private List<SolrFacetProperties> getDefaultFilters()
    {
        List<SolrFacetProperties> defaultFilters = new ArrayList<>();

        List<SolrFacetProperties> filters = SOLR_FACET_SERVICE.getFacets();
        for (SolrFacetProperties fp : filters)
        {
            if (fp.isDefault())
            {
                defaultFilters.add(fp);
            }
        }

        return defaultFilters;
    }
}
