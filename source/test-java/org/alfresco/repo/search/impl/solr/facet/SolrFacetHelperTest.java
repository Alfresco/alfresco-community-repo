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
import org.alfresco.repo.search.impl.solr.facet.SolrFacetHelper.FacetLabel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.util.ApplicationContextHelper;
import org.joda.time.LocalDate;
import org.junit.*;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.*;

/**
 * This class contains tests for the class <code>{@link SolrFacetHelper}</code>.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class SolrFacetHelperTest
{
    private static ApplicationContext context;
    private static SolrFacetHelper helper;
    
    @BeforeClass
    public static void initStaticData() throws Exception
    {
        context = ApplicationContextHelper.getApplicationContext();
        ServiceRegistry serviceRegistry = (ServiceRegistry) context.getBean("ServiceRegistry");
        helper = new SolrFacetHelper(serviceRegistry);
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
     * Run the List<String> createFacetQueriesFromSearchQuery(String) method test.
     *
     * @throws Exception
     *
     */
    @Test
    public void testCreateFacetQueriesFromSearchQuery() throws Exception
    {
        String searchQueryWithCreatedDate = "query=(test  AND ({http://www.alfresco.org/model/content/1.0}created:(\"2014-05-30\"..\"2014-06-06\" ))"
                             + " AND (+TYPE:\"cm:content\" OR +TYPE:\"cm:folder\")) AND -TYPE:\"cm:thumbnail\" AND"
                             + " -TYPE:\"cm:failedThumbnail\" AND -TYPE:\"cm:rating\" AND -TYPE:\"st:site\" AND"
                             + " -ASPECT:\"st:siteContainer\" AND -ASPECT:\"sys:hidden\" AND"
                             + " -cm:creator:system, stores=[workspace://SpacesStore]";

        List<String> result = helper.createFacetQueriesFromSearchQuery(searchQueryWithCreatedDate);
        assertNotNull(result);
        
        LocalDate currentDate = LocalDate.now();
        String nowStr = " TO " + currentDate.toString();
        String yesterday = currentDate.minusDays(1).toString() + nowStr;
        String lastWeek = currentDate.minusWeeks(1).toString() + nowStr;
        String lastMonth = currentDate.minusMonths(1).toString() + nowStr;
        String last6Months = currentDate.minusMonths(6).toString() + nowStr;
        String lastYear = currentDate.minusYears(1).toString() + nowStr;
        
        // As the created date has been specified in the search, we don't create
        // the rest of the buckets for the created date facet.
        assertEquals(12, result.size());
        
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}created:[2014-05-30 TO 2014-06-06]"));

        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[" + yesterday + "]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[" + lastWeek + "]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[" + lastMonth + "]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[" + last6Months + "]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[" + lastYear + "]"));

        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[0 TO 10240]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[10240 TO 102400]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[102400 TO 1048576]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[1048576 TO 16777216]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[16777216 TO 134217728]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[134217728 TO MAX]"));
        
        String searchQueryWithCreatedAndModifiedDate = "query=(test  AND ({http://www.alfresco.org/model/content/1.0}created:(\"2014-05-30\"..\"2014-06-06\" ) AND AND ({http://www.alfresco.org/model/content/1.0}modified:(\"2014-05-30\"..\"2014-05-31\" ))"
                    + " AND (+TYPE:\"cm:content\" OR +TYPE:\"cm:folder\")) AND -TYPE:\"cm:thumbnail\" AND"
                    + " -TYPE:\"cm:failedThumbnail\" AND -TYPE:\"cm:rating\" AND -TYPE:\"st:site\" AND"
                    + " -ASPECT:\"st:siteContainer\" AND -ASPECT:\"sys:hidden\" AND"
                    + " -cm:creator:system, stores=[workspace://SpacesStore]";

        result = helper.createFacetQueriesFromSearchQuery(searchQueryWithCreatedAndModifiedDate);
        assertNotNull(result);

        currentDate = LocalDate.now();
        nowStr = " TO " + currentDate.toString();
        yesterday = currentDate.minusDays(1).toString() + nowStr;
        lastWeek = currentDate.minusWeeks(1).toString() + nowStr;
        lastMonth = currentDate.minusMonths(1).toString() + nowStr;
        last6Months = currentDate.minusMonths(6).toString() + nowStr;
        lastYear = currentDate.minusYears(1).toString() + nowStr;

        // As the created and modified dates have been specified in the search,
        // we don't create the rest of the buckets for the created and modified date facets.
        assertEquals(8, result.size());

        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}created:[2014-05-30 TO 2014-06-06]"));

        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[2014-05-30 TO 2014-05-31]"));

        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[0 TO 10240]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[10240 TO 102400]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[102400 TO 1048576]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[1048576 TO 16777216]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[16777216 TO 134217728]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[134217728 TO MAX]"));
    }

    /**
     * Run the Set<String> getBucketedFieldFacets() method test.
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
        
        LocalDate currentDate = LocalDate.now();
        String nowStr = " TO " + currentDate.toString();
        String yesterday = currentDate.minusDays(1).toString() + nowStr;
        String lastWeek = currentDate.minusWeeks(1).toString() + nowStr;
        String lastMonth = currentDate.minusMonths(1).toString() + nowStr;
        String last6Months =currentDate.minusMonths(6).toString() + nowStr;
        String lastYear = currentDate.minusYears(1).toString() + nowStr;
        
        assertNotNull(result);
        assertEquals(16, result.size());
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}created:[" + yesterday + "]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}created:[" + lastWeek + "]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}created:[" + lastMonth + "]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}created:[" + last6Months + "]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}created:[" + lastYear + "]"));
        
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[" + yesterday + "]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[" + lastWeek + "]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[" + lastMonth + "]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[" + last6Months + "]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}modified:[" + lastYear + "]"));
        
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[0 TO 10240]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[10240 TO 102400]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[102400 TO 1048576]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[1048576 TO 16777216]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[16777216 TO 134217728]"));
        assertTrue(result.contains("@{http://www.alfresco.org/model/content/1.0}content.size:[134217728 TO MAX]"));
    }
    
    /**
     * Run the SolrFacetHelper.FacetLabelDisplayHandler getDisplayHandler(String) method test.
     *
     * @throws Exception
     *
     */
    @Test
    public void testGetUserNameDisplayHandler() throws Exception
    {
        // Username handler
        SolrFacetHelper.FacetLabelDisplayHandler userNameHandler = helper.getDisplayHandler("@{http://www.alfresco.org/model/content/1.0}creator.__.u");
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
     * Run the SolrFacetHelper.FacetLabelDisplayHandler getDisplayHandler(String) method test.
     *
     * @throws Exception
     *
     */
    @Test
    public void testGetMimetypeDisplayHandler() throws Exception
    {
        // Mimetype handler
        SolrFacetHelper.FacetLabelDisplayHandler mimeTypeHandler = helper.getDisplayHandler("@{http://www.alfresco.org/model/content/1.0}content.mimetype");
        assertNotNull(mimeTypeHandler);
        FacetLabel mimetype = mimeTypeHandler.getDisplayLabel("someMimetype123");
        assertNotNull(mimetype);
        assertEquals("someMimetype123 is not a registered mimetype, hence, the handler should return the passed-in mimetype.", "someMimetype123", mimetype.getLabel());
        mimetype = mimeTypeHandler.getDisplayLabel("text/plain");
        assertNotNull(mimetype);
        assertEquals("Expected [text/plain] display name.", "Plain Text", mimetype.getLabel());
    }
    
    /**
     * Run the SolrFacetHelper.FacetLabelDisplayHandler getDisplayHandler(String) method test.
     *
     * @throws Exception
     *
     */
    @Test
    public void testGetDateBucketsDisplayHandler() throws Exception
    {
        // Date buckets handler
        SolrFacetHelper.FacetLabelDisplayHandler dateBucketeHandler = helper.getDisplayHandler("@{http://www.alfresco.org/model/content/1.0}created");
        assertNotNull(dateBucketeHandler);
        
        LocalDate currentDate = LocalDate.now();
        String nowStr = " TO " + currentDate.toString();
        String yesterday = '[' + currentDate.minusDays(1).toString() + nowStr + ']';
        String lastWeek = '[' + currentDate.minusWeeks(1).toString() + nowStr + ']';
        String lastMonth = '[' + currentDate.minusMonths(1).toString() + nowStr + ']';
        String last6Months = '[' + currentDate.minusMonths(6).toString() + nowStr + ']';
        String lastYear = '[' + currentDate.minusYears(1).toString() + nowStr + ']';
        
        FacetLabel dateLabel = dateBucketeHandler.getDisplayLabel(yesterday);
        assertNotNull(dateLabel);
        assertEquals("faceted-search.date.one-day.label", dateLabel.getLabel());
        assertEquals("Yesterday date bucket should have a sorting index of 0.", 0, dateLabel.getLabelIndex());
        
        dateLabel = dateBucketeHandler.getDisplayLabel(lastWeek);
        assertNotNull(dateLabel);
        assertEquals("faceted-search.date.one-week.label", dateLabel.getLabel());
        assertEquals("Last week date bucket should have a sorting index of 1.", 1, dateLabel.getLabelIndex());
        
        dateLabel = dateBucketeHandler.getDisplayLabel(lastMonth);
        assertNotNull(dateLabel);
        assertEquals("faceted-search.date.one-month.label", dateLabel.getLabel());
        assertEquals("Last month date bucket should have a sorting index of 2.", 2, dateLabel.getLabelIndex());
        
        dateLabel = dateBucketeHandler.getDisplayLabel(last6Months);
        assertNotNull(dateLabel);
        assertEquals("faceted-search.date.six-months.label", dateLabel.getLabel());
        assertEquals("Last 6 months date bucket should have a sorting index of 3.", 3, dateLabel.getLabelIndex());
        
        dateLabel = dateBucketeHandler.getDisplayLabel(lastYear);
        assertNotNull(dateLabel);
        assertEquals("faceted-search.date.one-year.label", dateLabel.getLabel());
        assertEquals("Last year date bucket should have a sorting index of 4.", 4, dateLabel.getLabelIndex());
    }
    
    /**
     * Run the SolrFacetHelper.FacetLabelDisplayHandler getDisplayHandler(String) method test.
     *
     * @throws Exception
     *
     */
    @Test
    public void testGetContentSizeBucketsDisplayHandler() throws Exception
    {
        // Date buckets handler
        SolrFacetHelper.FacetLabelDisplayHandler contentSizeBucketeHandler = helper.getDisplayHandler("@{http://www.alfresco.org/model/content/1.0}content.size");
        assertNotNull(contentSizeBucketeHandler);
        
        int KB = 1024;
        int MB = KB * 1024;
        int tiny = 10 * KB;
        int small = 100 * KB;
        int medium = MB;
        int large = 16 * MB;
        int huge = 128 * MB;

        FacetLabel sizeLabel = contentSizeBucketeHandler.getDisplayLabel("[0 TO " + tiny + "]");
        assertNotNull(sizeLabel);        
        assertEquals("faceted-search.size.0-10KB.label", sizeLabel.getLabel());
        assertEquals("0-10KB size bucket should have a sorting index of 0.", 0, sizeLabel.getLabelIndex());
        
        sizeLabel = contentSizeBucketeHandler.getDisplayLabel("[" + tiny + " TO " + small + "]");
        assertNotNull(sizeLabel);
        assertEquals("faceted-search.size.10-100KB.label", sizeLabel.getLabel());
        assertEquals("10-100KB size bucket should have a sorting index of 1.", 1, sizeLabel.getLabelIndex());
        
        sizeLabel = contentSizeBucketeHandler.getDisplayLabel("[" + small + " TO " + medium + "]");
        assertNotNull(sizeLabel);
        assertEquals("faceted-search.size.100KB-1MB.label", sizeLabel.getLabel());
        assertEquals("100KB-1MB size bucket should have a sorting index of 2.", 2, sizeLabel.getLabelIndex());
        
        sizeLabel = contentSizeBucketeHandler.getDisplayLabel("[" + medium + " TO " + large + "]");
        assertNotNull(sizeLabel);
        assertEquals("faceted-search.size.1-16MB.label", sizeLabel.getLabel());
        assertEquals("1-16MB size bucket should have a sorting index of 3.", 3, sizeLabel.getLabelIndex());
        
        sizeLabel = contentSizeBucketeHandler.getDisplayLabel("[" + large + " TO " + huge + "]");
        assertNotNull(sizeLabel);
        assertEquals("faceted-search.size.16-128MB.label", sizeLabel.getLabel());
        assertEquals("16-128MB size bucket should have a sorting index of 4.", 4, sizeLabel.getLabelIndex());
        
        sizeLabel = contentSizeBucketeHandler.getDisplayLabel("[" + huge + " TO MAX]");
        assertNotNull(sizeLabel);
        assertEquals("faceted-search.size.over128.label", sizeLabel.getLabel());
        assertEquals("over128MB size bucket should have a sorting index of 5.", 5, sizeLabel.getLabelIndex());
    }
}