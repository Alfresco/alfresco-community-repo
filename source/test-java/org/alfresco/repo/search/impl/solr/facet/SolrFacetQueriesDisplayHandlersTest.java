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

import java.util.List;
import java.util.Set;

import org.alfresco.repo.search.impl.solr.facet.handler.FacetLabel;
import org.alfresco.repo.search.impl.solr.facet.handler.FacetLabelDisplayHandler;
import org.alfresco.repo.search.impl.solr.facet.handler.FacetLabelDisplayHandlerRegistry;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.*;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.*;

/**
 * This class contains tests for the class <code>{@link SolrFacetHelper}</code> and <code>{@link FacetLabelDisplayHandler}</code>.
 *
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class SolrFacetQueriesDisplayHandlersTest
{
    private static ApplicationContext context;
    private static SolrFacetHelper helper;
    private static FacetLabelDisplayHandlerRegistry displayHandlerRegistry;

    @BeforeClass
    public static void initStaticData() throws Exception
    {
        context = ApplicationContextHelper.getApplicationContext();

        helper = (SolrFacetHelper) context.getBean("facet.solrFacetHelper");
        displayHandlerRegistry = (FacetLabelDisplayHandlerRegistry) context.getBean("facet.facetLabelDisplayHandlerRegistry");
    }

    /**
     * Perform pre-test initialization.
     *
     * @throws Exception
     *         if the initialization fails for some reason
     *
     */
    @Before
    public void setUp() throws Exception
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    /**
     * Perform post-test clean-up.
     *
     * @throws Exception
     *         if the clean-up fails for some reason
     *
     */
    @After
    public void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    /**
     * Run the String createFacetQueriesFromSearchQuery(String) method test.
     *
     * @throws Exception
     *
     */
    @Test
    public void testCreateFacetQueryFromSearchQuery() throws Exception
    {
        String searchQueryWithCreatedDate = "query=(test  AND ({http://www.alfresco.org/model/content/1.0}created:(\"NOW/DAY-7DAYS\"..\"NOW/DAY+1DAY\" ))"
                    + " AND (+TYPE:\"cm:content\" OR +TYPE:\"cm:folder\")) AND -TYPE:\"cm:thumbnail\" AND"
                    + " -TYPE:\"cm:failedThumbnail\" AND -TYPE:\"cm:rating\" AND -TYPE:\"st:site\" AND"
                    + " -ASPECT:\"st:siteContainer\" AND -ASPECT:\"sys:hidden\" AND"
                    + " -cm:creator:system, stores=[workspace://SpacesStore]";

        String result = helper.createFacetQueriesFromSearchQuery("{http://www.alfresco.org/model/content/1.0}created", searchQueryWithCreatedDate);
        assertNotNull(result);
        assertEquals("@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-7DAYS TO NOW/DAY+1DAY]", result);

        String searchQueryWithCreatedAndModifiedDate = "query=(test  AND ({http://www.alfresco.org/model/content/1.0}created:(\"NOW/DAY-7DAYS\"..\"NOW/DAY+1DAY\" ) AND ({http://www.alfresco.org/model/content/1.0}modified:(\"NOW/DAY-1DAY\"..\"NOW/DAY+1DAY\" ))"
                    + " AND (+TYPE:\"cm:content\" OR +TYPE:\"cm:folder\")) AND -TYPE:\"cm:thumbnail\" AND"
                    + " -TYPE:\"cm:failedThumbnail\" AND -TYPE:\"cm:rating\" AND -TYPE:\"st:site\" AND"
                    + " -ASPECT:\"st:siteContainer\" AND -ASPECT:\"sys:hidden\" AND"
                    + " -cm:creator:system, stores=[workspace://SpacesStore]";

        String[] fields = { "{http://www.alfresco.org/model/content/1.0}created", "{http://www.alfresco.org/model/content/1.0}modified" };

        result = helper.createFacetQueriesFromSearchQuery(fields[0], searchQueryWithCreatedAndModifiedDate);
        assertNotNull(result);
        assertEquals("@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-7DAYS TO NOW/DAY+1DAY]", result);

        result = helper.createFacetQueriesFromSearchQuery(fields[1], searchQueryWithCreatedAndModifiedDate);
        assertNotNull(result);
        assertEquals("@{http://www.alfresco.org/model/content/1.0}modified:[NOW/DAY-1DAY TO NOW/DAY+1DAY]", result);

    }

    /**
     * Run the List<String> getBucketedFieldFacets() method test.
     *
     * @throws Exception
     *
     */
    @Test
    public void testGetBucketedFieldFacets() throws Exception
    {
        Set<String> result = helper.getBucketedFieldFacets();
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}created"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified"));
    }

    /**
     * Run the List<String> getDefaultFacetQueries() method test.
     *
     * @throws Exception
     *
     */
    @Test
    public void testGetDefaultFacetQueries() throws Exception
    {
        List<String> result = helper.getDefaultFacetQueries();

        assertNotNull(result);
        assertEquals(16, result.size());
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-1DAY TO NOW/DAY+1DAY]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-7DAYS TO NOW/DAY+1DAY]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-1MONTH TO NOW/DAY+1DAY]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-6MONTHS TO NOW/DAY+1DAY]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-1YEAR TO NOW/DAY+1DAY]"));

        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[NOW/DAY-1DAY TO NOW/DAY+1DAY]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[NOW/DAY-7DAYS TO NOW/DAY+1DAY]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[NOW/DAY-1MONTH TO NOW/DAY+1DAY]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[NOW/DAY-6MONTHS TO NOW/DAY+1DAY]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[NOW/DAY-1YEAR TO NOW/DAY+1DAY]"));

        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[0 TO 10240]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[10240 TO 102400]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[102400 TO 1048576]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[1048576 TO 16777216]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[16777216 TO 134217728]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[134217728 TO MAX]"));
    }

    /**
     * User name display handler test.
     *
     * @throws Exception
     */
    @Test
    public void testGetUserNameDisplayHandler() throws Exception
    {
        FacetLabelDisplayHandler userNameHandler = displayHandlerRegistry.getDisplayHandler("@{http://www.alfresco.org/model/content/1.0}creator");
        assertNotNull(userNameHandler);
        String randomUserName = "randomUserName" + System.currentTimeMillis();
        FacetLabel name = userNameHandler.getDisplayLabel(randomUserName);
        assertNotNull(name);
        assertEquals("There is no user with the username [" + randomUserName + "], hence, the handler should return the passed-in username.", randomUserName, name.getLabel());
        name = userNameHandler.getDisplayLabel(AuthenticationUtil.getAdminUserName());
        assertNotNull(name);
        assertEquals("Expected admin's full name.", "Administrator", name.getLabel());
    }

    /**
     * MimeType display handler test.
     *
     * @throws Exception
     *
     */
    @Test
    public void testGetMimetypeDisplayHandler() throws Exception
    {
         // Mimetype handler
        FacetLabelDisplayHandler mimeTypeHandler = displayHandlerRegistry.getDisplayHandler("@{http://www.alfresco.org/model/content/1.0}content.mimetype");
        assertNotNull(mimeTypeHandler);
        FacetLabel mimetype = mimeTypeHandler.getDisplayLabel("someMimetype123");
        assertNotNull(mimetype);
        assertEquals("someMimetype123 is not a registered mimetype, hence, the handler should return the passed-in mimetype.", "someMimetype123", mimetype.getLabel());
        mimetype = mimeTypeHandler.getDisplayLabel("text/plain");
        assertNotNull(mimetype);
        assertEquals("Expected [text/plain] display name.", "Plain Text", mimetype.getLabel());
    }

    /**
     * Created date buckets display handler test.
     *
     * @throws Exception
     *
     */
    @Test
    public void testGetCreatedDateBucketsDisplayHandler() throws Exception
    {
        final String createdDateField = "@{http://www.alfresco.org/model/content/1.0}created";
        FacetLabelDisplayHandler dateBucketeHandler = displayHandlerRegistry.getDisplayHandler(createdDateField);
        assertNotNull(dateBucketeHandler);

        FacetLabel dateLabel = dateBucketeHandler.getDisplayLabel(createdDateField+":[NOW/DAY-1DAY TO NOW/DAY+1DAY]");
        assertNotNull(dateLabel);
        assertEquals("faceted-search.date.one-day.label", dateLabel.getLabel());
        assertEquals("Yesterday date bucket should have a sorting index of 0.", 0, dateLabel.getLabelIndex());

        dateLabel = dateBucketeHandler.getDisplayLabel(createdDateField+":[NOW/DAY-7DAYS TO NOW/DAY+1DAY]");
        assertNotNull(dateLabel);
        assertEquals("faceted-search.date.one-week.label", dateLabel.getLabel());
        assertEquals("Last week date bucket should have a sorting index of 1.", 1, dateLabel.getLabelIndex());

        dateLabel = dateBucketeHandler.getDisplayLabel(createdDateField+":[NOW/DAY-1MONTH TO NOW/DAY+1DAY]");
        assertNotNull(dateLabel);
        assertEquals("faceted-search.date.one-month.label", dateLabel.getLabel());
        assertEquals("Last month date bucket should have a sorting index of 2.", 2, dateLabel.getLabelIndex());

        dateLabel = dateBucketeHandler.getDisplayLabel(createdDateField+":[NOW/DAY-6MONTHS TO NOW/DAY+1DAY]");
        assertNotNull(dateLabel);
        assertEquals("faceted-search.date.six-months.label", dateLabel.getLabel());
        assertEquals("Last 6 months date bucket should have a sorting index of 3.", 3, dateLabel.getLabelIndex());

        dateLabel = dateBucketeHandler.getDisplayLabel(createdDateField+":[NOW/DAY-1YEAR TO NOW/DAY+1DAY]");
        assertNotNull(dateLabel);
        assertEquals("faceted-search.date.one-year.label", dateLabel.getLabel());
        assertEquals("Last year date bucket should have a sorting index of 4.", 4, dateLabel.getLabelIndex());
    }

    /**
     * Content size buckets display handler test.
     *
     * @throws Exception
     *
     */
    @Test
    public void testGetContentSizeBucketsDisplayHandler() throws Exception
    {
        final String contentSizeField = "@{http://www.alfresco.org/model/content/1.0}content.size";
        FacetLabelDisplayHandler contentSizeBucketeHandler = displayHandlerRegistry.getDisplayHandler(contentSizeField);
        assertNotNull(contentSizeBucketeHandler);

        int KB = 1024;
        int MB = KB * 1024;
        int tiny = 10 * KB;
        int small = 100 * KB;
        int medium = MB;
        int large = 16 * MB;
        int huge = 128 * MB;


        FacetLabel sizeLabel = contentSizeBucketeHandler.getDisplayLabel(contentSizeField + ":[0 TO " + tiny + "]");
        assertNotNull(sizeLabel);
        assertEquals("faceted-search.size.0-10KB.label", sizeLabel.getLabel());
        assertEquals("0-10KB size bucket should have a sorting index of 0.", 0, sizeLabel.getLabelIndex());

        sizeLabel = contentSizeBucketeHandler.getDisplayLabel(contentSizeField + ":[" + tiny + " TO " + small + "]");
        assertNotNull(sizeLabel);
        assertEquals("faceted-search.size.10-100KB.label", sizeLabel.getLabel());
        assertEquals("10-100KB size bucket should have a sorting index of 1.", 1, sizeLabel.getLabelIndex());

        sizeLabel = contentSizeBucketeHandler.getDisplayLabel(contentSizeField + ":[" + small + " TO " + medium + "]");
        assertNotNull(sizeLabel);
        assertEquals("faceted-search.size.100KB-1MB.label", sizeLabel.getLabel());
        assertEquals("100KB-1MB size bucket should have a sorting index of 2.", 2, sizeLabel.getLabelIndex());

        sizeLabel = contentSizeBucketeHandler.getDisplayLabel(contentSizeField + ":[" + medium + " TO " + large + "]");
        assertNotNull(sizeLabel);
        assertEquals("faceted-search.size.1-16MB.label", sizeLabel.getLabel());
        assertEquals("1-16MB size bucket should have a sorting index of 3.", 3, sizeLabel.getLabelIndex());

        sizeLabel = contentSizeBucketeHandler.getDisplayLabel(contentSizeField + ":[" + large + " TO " + huge + "]");
        assertNotNull(sizeLabel);
        assertEquals("faceted-search.size.16-128MB.label", sizeLabel.getLabel());
        assertEquals("16-128MB size bucket should have a sorting index of 4.", 4, sizeLabel.getLabelIndex());

        sizeLabel = contentSizeBucketeHandler.getDisplayLabel(contentSizeField + ":[" + huge + " TO MAX]");
        assertNotNull(sizeLabel);
        assertEquals("faceted-search.size.over128.label", sizeLabel.getLabel());
        assertEquals("over128MB size bucket should have a sorting index of 5.", 5, sizeLabel.getLabelIndex());
    }
    
    /**
     * Site title display handler test.
     *
     * @throws Exception
     */
    @Test
    public void testGetSiteTitleDisplayHandler() throws Exception
    {
        String defaultSiteName= "swsdp";
        FacetLabelDisplayHandler siteHandler = displayHandlerRegistry.getDisplayHandler("SITE");
        assertNotNull(siteHandler);
        
        String randomSiteName = "randomSiteName" + System.currentTimeMillis();
        FacetLabel name = siteHandler.getDisplayLabel(randomSiteName);
        assertNotNull(name);
        assertEquals("There is no site with the name [" + randomSiteName + "], hence, the handler should return the passed-in short name.", randomSiteName, name.getLabel());
        name = siteHandler.getDisplayLabel(defaultSiteName);
        assertNotNull(name);
        assertEquals("Sample: Web Site Design Project", name.getLabel());
    }
}
