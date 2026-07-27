/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.query.aggregation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_SITE;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.search.impl.elasticsearch.query.aggregation.TermsAggregationWrapper.ComplementaryAggregation;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Unit tests for the SITE facet handling in {@link SiteTermsAggregationBuilder}.
 * <p>
 * The SITE field is not stored directly in Elasticsearch; instead the builder translates a SITE field facet into a {@code primaryHierarchy} terms aggregation scoped to visible site UUIDs plus the Shared Files folder UUID. A complementary filter aggregation is also produced so that documents outside any known site can be counted as {@code _REPOSITORY_}.
 * <p>
 * {@link SiteInfo} mocks (site1, site2) are created in {@code @Before} so they are never constructed inside a {@code thenReturn()} argument — doing so would trigger Mockito's "unfinished stubbing" detection due to the nested mock creation.
 * <p>
 * Each test that needs the Shared Files folder to resolve successfully calls {@link #setupSharedHomeResolution()} explicitly. The one test that exercises the "shared home not resolvable" path stubs only what it needs, keeping all stubs necessary.
 */
@RunWith(MockitoJUnitRunner.class)
public class SiteTermsAggregationBuilderTest
{
    private static final String SITE_UUID_1 = "aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa";
    private static final String SITE_UUID_2 = "bbbbbbbb-2222-2222-2222-bbbbbbbbbbbb";
    private static final String SHARED_UUID = "cccccccc-3333-3333-3333-cccccccccccc";
    private static final String ROOT_UUID = "root-uuid";
    private static final String COMPANY_HOME_UUID = "company-home-uuid";
    private static final String SITE_NAME_1 = "engineering";
    private static final String SITE_NAME_2 = "marketing";
    private static final String PRIMARY_HIERARCHY_FIELD = "primaryHierarchy";
    private static final String REPOSITORY_SUFFIX = "__REPOSITORY__";
    private static final String SHARED_FILES_LABEL = "_SHARED_FILES_";
    private static final String REPOSITORY_LABEL = "_REPOSITORY_";

    @Mock
    private SiteService siteService;
    @Mock
    private NodeService nodeService;
    @Mock
    private SimpleCache<String, Map<String, String>> sitesCache;

    private SiteTermsAggregationBuilder builder;

    private NodeRef rootNode;
    private NodeRef companyHome;
    private NodeRef sharedHome;

    // Pre-built site mocks — created in @Before to avoid Mockito "unfinished stubbing"
    // errors that occur when mock() + when() are called inside a thenReturn() argument.
    private SiteInfo site1;
    private SiteInfo site2;

    @Before
    public void setUp()
    {
        builder = new SiteTermsAggregationBuilder(siteService, nodeService, sitesCache);

        rootNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, ROOT_UUID);
        companyHome = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, COMPANY_HOME_UUID);
        sharedHome = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SHARED_UUID);

        // Pre-create site mocks here so they're fully configured before any when().thenReturn() call.
        site1 = buildSiteMock(SITE_UUID_1, SITE_NAME_1);
        site2 = buildSiteMock(SITE_UUID_2, SITE_NAME_2);

        // Wire the root-node lookup used by every SITE test. Tests that need the full
        // company_home/shared chain call setupSharedHomeResolution() themselves so that
        // no stub is ever left unused (strict-stubbing safe).
        when(nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)).thenReturn(rootNode);
    }

    @Test
    public void build_withSiteFacet_producesComplementaryAggregation()
    {
        setupSharedHomeResolution();
        when(siteService.listSites(null, null)).thenReturn(List.of(site1));

        Optional<TermsAggregationWrapper> wrapper = builder.build(siteFacet(FIELD_SITE));

        assertTrue("SITE wrapper must be present", wrapper.isPresent());
        assertTrue("SITE wrapper must have a complementary aggregation",
                wrapper.get().complementaryAggregation().isPresent());
    }

    @Test
    public void build_withSiteFacet_usesPrimaryHierarchyField()
    {
        setupSharedHomeResolution();
        when(siteService.listSites(null, null)).thenReturn(List.of(site1));

        Optional<TermsAggregationWrapper> wrapper = builder.build(siteFacet(FIELD_SITE));

        assertTrue(wrapper.isPresent());
        assertEquals("SITE facet must target the primaryHierarchy field",
                PRIMARY_HIERARCHY_FIELD, wrapper.get().termsAggregation().field());
    }

    @Test
    public void build_withSiteFacet_includeListContainsAllSiteUUIDs()
    {
        setupSharedHomeResolution();
        when(siteService.listSites(null, null)).thenReturn(List.of(site1, site2));

        Optional<TermsAggregationWrapper> wrapper = builder.build(siteFacet(FIELD_SITE));

        List<String> includeTerms = wrapper.get().termsAggregation().include().terms();
        assertTrue("Include list must contain site UUID 1", includeTerms.contains(SITE_UUID_1));
        assertTrue("Include list must contain site UUID 2", includeTerms.contains(SITE_UUID_2));
    }

    @Test
    public void build_withSiteFacet_includeListContainsSharedHomeUUID()
    {
        setupSharedHomeResolution();
        when(siteService.listSites(null, null)).thenReturn(List.of(site1));

        Optional<TermsAggregationWrapper> wrapper = builder.build(siteFacet(FIELD_SITE));

        List<String> includeTerms = wrapper.get().termsAggregation().include().terms();
        assertTrue("Include list must contain the Shared Files folder UUID", includeTerms.contains(SHARED_UUID));
    }

    @Test
    public void build_withSiteFacet_includeListSizeMatchesTotalEntries()
    {
        setupSharedHomeResolution();
        when(siteService.listSites(null, null)).thenReturn(List.of(site1, site2));

        Optional<TermsAggregationWrapper> wrapper = builder.build(siteFacet(FIELD_SITE));

        List<String> includeTerms = wrapper.get().termsAggregation().include().terms();
        // 2 sites + 1 shared home
        assertEquals("Include list size should equal number of sites plus shared home", 3, includeTerms.size());

        Integer aggSize = wrapper.get().termsAggregation().size();
        assertNotNull(aggSize);
        assertEquals("Aggregation size should match include list size", includeTerms.size(), aggSize.intValue());
    }

    @Test
    public void build_withSiteFacet_postProcessingDataMapsSiteUUIDsToShortNames()
    {
        setupSharedHomeResolution();
        when(siteService.listSites(null, null)).thenReturn(List.of(site1, site2));

        Optional<TermsAggregationWrapper> wrapper = builder.build(siteFacet(FIELD_SITE));

        Map<String, String> postProcessingData = wrapper.get().postProcessingData();
        assertEquals("UUID 1 should map to site short name 1", SITE_NAME_1, postProcessingData.get(SITE_UUID_1));
        assertEquals("UUID 2 should map to site short name 2", SITE_NAME_2, postProcessingData.get(SITE_UUID_2));
    }

    @Test
    public void build_withSiteFacet_postProcessingDataContainsSharedFilesLabel()
    {
        setupSharedHomeResolution();
        when(siteService.listSites(null, null)).thenReturn(List.of());

        Optional<TermsAggregationWrapper> wrapper = builder.build(siteFacet(FIELD_SITE));

        assertTrue(wrapper.isPresent());
        Map<String, String> postProcessingData = wrapper.get().postProcessingData();
        assertTrue("Post-processing data must include the shared files folder entry",
                postProcessingData.containsValue(SHARED_FILES_LABEL));
        assertEquals("Shared home UUID must map to _SHARED_FILES_",
                SHARED_FILES_LABEL, postProcessingData.get(SHARED_UUID));
    }

    @Test
    public void build_withSiteFacet_hasComplementaryAggregation()
    {
        setupSharedHomeResolution();
        when(siteService.listSites(null, null)).thenReturn(List.of(site1));

        Optional<TermsAggregationWrapper> wrapper = builder.build(siteFacet(FIELD_SITE));

        Optional<ComplementaryAggregation> complementary = wrapper.get().complementaryAggregation();
        assertTrue("A complementary aggregation must be present for SITE facets", complementary.isPresent());
    }

    @Test
    public void build_withSiteFacet_complementaryAggregationNameHasRepositorySuffix()
    {
        setupSharedHomeResolution();
        when(siteService.listSites(null, null)).thenReturn(List.of(site1));

        TermsAggregationWrapper wrapper = builder.build(siteFacet(FIELD_SITE)).get();
        ComplementaryAggregation complementary = wrapper.complementaryAggregation().get();
        assertTrue("Complementary aggregation name must end with " + REPOSITORY_SUFFIX,
                complementary.aggregationName().endsWith(REPOSITORY_SUFFIX));
        assertTrue("Complementary aggregation name must start with the main agg name",
                complementary.aggregationName().startsWith(wrapper.name()));
    }

    @Test
    public void build_withSiteFacet_complementaryAggregationDisplayLabelIsRepository()
    {
        setupSharedHomeResolution();
        when(siteService.listSites(null, null)).thenReturn(List.of(site1));

        ComplementaryAggregation complementary = builder.build(siteFacet(FIELD_SITE)).get().complementaryAggregation().get();
        assertEquals("Complementary aggregation display label must be _REPOSITORY_",
                REPOSITORY_LABEL, complementary.displayLabel());
    }

    @Test
    public void build_withSiteFacet_complementaryFilterQueryExcludesAllSiteAndSharedUUIDs()
    {
        setupSharedHomeResolution();
        when(siteService.listSites(null, null)).thenReturn(List.of(site1));

        ComplementaryAggregation complementary = builder.build(siteFacet(FIELD_SITE)).get().complementaryAggregation().get();
        assertNotNull("Complementary filter query must not be null", complementary.filterQuery());
        assertNotNull("Filter query must be a bool query", complementary.filterQuery().bool());
        assertFalse("Filter query bool must_not clause must not be empty",
                complementary.filterQuery().bool().mustNot().isEmpty());
    }

    @Test
    public void build_withSiteFacet_usesFieldNameWhenNoLabelSet()
    {
        setupSharedHomeResolution();
        when(siteService.listSites(null, null)).thenReturn(List.of(site1));

        SearchParameters.FieldFacet facet = new SearchParameters.FieldFacet(FIELD_SITE);

        Optional<TermsAggregationWrapper> wrapper = builder.build(facet);

        assertEquals("Aggregation name should default to the field name when no label is set",
                FIELD_SITE, wrapper.get().name());
    }

    @Test
    public void build_withSiteFacet_usesCustomLabelWhenSet()
    {
        setupSharedHomeResolution();
        when(siteService.listSites(null, null)).thenReturn(List.of(site1));

        String customLabel = "mySiteFacet";
        SearchParameters.FieldFacet facet = new SearchParameters.FieldFacet(FIELD_SITE);
        facet.setLabel(customLabel);

        Optional<TermsAggregationWrapper> wrapper = builder.build(facet);

        assertEquals("Aggregation name should use the custom label when set",
                customLabel, wrapper.get().name());
    }

    @Test
    public void build_withSiteFacet_noSitesButSharedHomeExists_producesWrapper()
    {
        setupSharedHomeResolution();
        when(siteService.listSites(null, null)).thenReturn(List.of());

        Optional<TermsAggregationWrapper> wrapper = builder.build(siteFacet(FIELD_SITE));

        assertTrue("Should produce a wrapper when only shared home is resolvable", wrapper.isPresent());
        List<String> includeTerms = wrapper.get().termsAggregation().include().terms();
        assertEquals("Include list should contain only the shared home UUID", 1, includeTerms.size());
        assertEquals(SHARED_UUID, includeTerms.get(0));
    }

    @Test
    public void build_withSiteFacet_noSitesAndSharedHomeNotResolvable_returnsEmptyOptional()
    {
        // Make company_home lookup fail → shared home cannot be resolved.
        // Note: rootNode is already wired in @Before; only the child-assoc lookup is overridden here.
        when(siteService.listSites(null, null)).thenReturn(List.of());
        when(nodeService.getChildAssocs(eq(rootNode), any(),
                eq(QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "company_home"))))
                        .thenReturn(List.of());

        Optional<TermsAggregationWrapper> wrapper = builder.build(siteFacet(FIELD_SITE));

        assertTrue("Should return empty Optional when no sites and shared home cannot be resolved",
                wrapper.isEmpty());
    }

    @Test
    public void build_withSiteFacet_siteWithNullNodeRef_isIgnored()
    {
        setupSharedHomeResolution();
        // siteWithNullRef is created and configured before the thenReturn() call to avoid
        // nested mock creation inside thenReturn() arguments.
        SiteInfo siteWithNullRef = mock(SiteInfo.class);
        when(siteWithNullRef.getNodeRef()).thenReturn(null);
        when(siteService.listSites(null, null)).thenReturn(List.of(siteWithNullRef, site1));

        Optional<TermsAggregationWrapper> wrapper = builder.build(siteFacet(FIELD_SITE));

        Map<String, String> postProcessingData = wrapper.get().postProcessingData();
        assertFalse("Site with null NodeRef should not appear in post-processing data",
                postProcessingData.containsValue(null));
        assertTrue("Valid site must still be in post-processing data",
                postProcessingData.containsKey(SITE_UUID_1));
    }

    private SearchParameters.FieldFacet siteFacet(String fieldName)
    {
        return new SearchParameters.FieldFacet(fieldName);
    }

    private SiteInfo buildSiteMock(String uuid, String shortName)
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, uuid);
        SiteInfo siteInfo = mock(SiteInfo.class);
        when(siteInfo.getNodeRef()).thenReturn(nodeRef);
        when(siteInfo.getShortName()).thenReturn(shortName);
        return siteInfo;
    }

    private void setupSharedHomeResolution()
    {
        ChildAssociationRef companyHomeAssoc = mock(ChildAssociationRef.class);
        when(companyHomeAssoc.getChildRef()).thenReturn(companyHome);
        when(nodeService.getChildAssocs(eq(rootNode), any(),
                eq(QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "company_home"))))
                        .thenReturn(List.of(companyHomeAssoc));

        ChildAssociationRef sharedAssoc = mock(ChildAssociationRef.class);
        when(sharedAssoc.getChildRef()).thenReturn(sharedHome);
        when(nodeService.getChildAssocs(eq(companyHome), any(),
                eq(QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "shared"))))
                        .thenReturn(List.of(sharedAssoc));
    }
}
