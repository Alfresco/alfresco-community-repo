/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.solr.facet;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Properties;

import org.junit.*;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ApplicationContextHelper;

/**
 * This class contains tests for the class {@link SolrFacetConfig}
 *
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class SolrFacetConfigTest
{
    private static ClassPathXmlApplicationContext context;
    private static Properties rawProperties;
    private static SolrFacetConfig facetConfig;

    @BeforeClass
    public static void setUp() throws Exception
    {
        context = new ClassPathXmlApplicationContext(new String[]{"classpath:facets/test-facet-property-context.xml"},
                ApplicationContextHelper.getApplicationContext());

        rawProperties = context.getBean("solrFacetRawPropertiesTest", Properties.class);
        facetConfig = context.getBean("solrFacetConfigsTest", SolrFacetConfig.class);

    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        context.close();
    }

    @Test
    public void testBasic() throws Exception
    {
        SolrFacetConfig config = null;
        try
        {
            config = new SolrFacetConfig(null, "");
            fail("Null properties should have been detected");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
        try
        {
            config = new SolrFacetConfig(rawProperties, null);
            fail("Null properties should have been detected");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
        config = new SolrFacetConfig(rawProperties, "default,custom");
        config.setNamespaceService(context.getBean("namespaceService", NamespaceService.class));
        try
        {
            config.getDefaultFacets();
            fail("Initialization should be done.");
        }
        catch (IllegalStateException e)
        {
            // Expected
        }
    }

    @Test
    public void testDefault() throws Exception
    {
        Map<String, SolrFacetProperties> defaultProps = facetConfig.getDefaultFacets();
        assertNotNull(defaultProps);
        assertEquals("Incorrect number of properties", 6, defaultProps.size());

        // loaded from /facets/facets-config-sample.properties
        SolrFacetProperties contentSizeFP = defaultProps.get("test_filter_content_size");
        assertEquals("Incorrect QNAME", "{http://www.alfresco.org/model/content/1.0}content.size", contentSizeFP.getFacetQName().toString());
        assertEquals("faceted-search.facet-menu.facet.size", contentSizeFP.getDisplayName());
        assertEquals("alfresco/search/FacetFilters", contentSizeFP.getDisplayControl());
        assertEquals(5, contentSizeFP.getMaxFilters());
        assertEquals(1, contentSizeFP.getHitThreshold());
        assertEquals(4, contentSizeFP.getMinFilterValueLength());
        assertEquals("ALPHABETICALLY", contentSizeFP.getSortBy());
        assertEquals("ALL", contentSizeFP.getScope());
        assertEquals(0, contentSizeFP.getScopedSites().size());
        assertEquals(true, contentSizeFP.isEnabled());
        assertEquals(1, contentSizeFP.getCustomProperties().size());
        String customValue = (String) contentSizeFP.getCustomProperties().iterator().next().getValue();
        assertTrue(Boolean.valueOf(customValue));

        // loaded from /facets/extension/facets-config-custom-sample.properties
        SolrFacetProperties mimeTypeFP = defaultProps.get("test_filter_mimetype");
        assertEquals("Incorrect QNAME", "{http://www.alfresco.org/model/content/1.0}content.mimetype", mimeTypeFP.getFacetQName().toString());
        assertEquals("faceted-search.facet-menu.facet.formats", mimeTypeFP.getDisplayName());
        assertEquals("alfresco/search/FacetFilters", mimeTypeFP.getDisplayControl());
        assertEquals(5, mimeTypeFP.getMaxFilters());
        assertEquals(1, mimeTypeFP.getHitThreshold());
        assertEquals(4, mimeTypeFP.getMinFilterValueLength());
        assertEquals("DESCENDING", mimeTypeFP.getSortBy());
        assertEquals("ALL", mimeTypeFP.getScope());
        assertEquals(0, mimeTypeFP.getScopedSites().size());
        assertEquals(true, mimeTypeFP.isEnabled());

        // test SITE (solr special), loaded from /facets/extension/facets-config-custom-sample.properties
        SolrFacetProperties siteFP = defaultProps.get("site_filter");
        assertEquals("There shouldn't be any namespace (solr special).", "{}SITE", siteFP.getFacetQName().toString());
        assertEquals("SiteExtDisplayName", siteFP.getDisplayName());
        assertEquals("alfresco/search/FacetFilters", siteFP.getDisplayControl());
        assertEquals(20, siteFP.getMaxFilters());
        assertEquals(1, siteFP.getHitThreshold());
        assertEquals(1, siteFP.getMinFilterValueLength());
        assertEquals("ALPHABETICALLY", siteFP.getSortBy());
        assertEquals("ALL", siteFP.getScope());
        assertEquals(0, siteFP.getScopedSites().size());
        assertEquals(true, siteFP.isEnabled());

        // test TAG (solr special), loaded from /facets/extension/facets-config-custom-sample.properties
        SolrFacetProperties tagFP = defaultProps.get("tag_filter");
        assertEquals("There shouldn't be any namespace (solr special)", "{}TAG", tagFP.getFacetQName().toString());
        assertEquals("TagExtDisplayName", tagFP.getDisplayName());
        assertEquals("alfresco/search/FacetFilters", tagFP.getDisplayControl());
        assertEquals(3, tagFP.getMaxFilters());
        assertEquals(1, tagFP.getHitThreshold());
        assertEquals(2, tagFP.getMinFilterValueLength());
        assertEquals("DESCENDING", tagFP.getSortBy());
        assertEquals("ALL", tagFP.getScope());
        assertEquals(0, tagFP.getScopedSites().size());
        assertEquals(true, tagFP.isEnabled());

        // See if the overrides worked
        SolrFacetProperties creatorFP = defaultProps.get("test_filter_creator");
        assertEquals("Incorrect QNAME", "{http://www.alfresco.org/model/content/1.0}creator", creatorFP.getFacetQName().toString());

        String msg = "The value has not been overridden with the value from the custom properties";
        assertEquals(msg, 10, creatorFP.getMaxFilters());
        assertEquals(msg, 5, creatorFP.getHitThreshold());
        assertEquals(msg, 14, creatorFP.getMinFilterValueLength());
        assertEquals(msg, 1, creatorFP.getScopedSites().size());
        assertEquals("site1", creatorFP.getScopedSites().iterator().next());

    }

    @Test
    public void testOverrideOrder() throws Exception
    {
        ApplicationEvent applicationEvent = new ApplicationEvent(this) {
            private static final long serialVersionUID = 1L;
        };

        /* Override order: default,custom */
        SolrFacetConfig config = new SolrFacetConfig(rawProperties, "default,custom");
        config.setNamespaceService(context.getBean("namespaceService", NamespaceService.class));
        config.onBootstrap(applicationEvent);

        SolrFacetProperties creatorFP = config.getDefaultFacets().get("test_filter_creator");
        assertEquals("Incorrect QNAME", "{http://www.alfresco.org/model/content/1.0}creator", creatorFP.getFacetQName().toString());
        assertEquals(10, creatorFP.getMaxFilters());
        assertEquals(5, creatorFP.getHitThreshold());
        assertEquals(14, creatorFP.getMinFilterValueLength());
        assertEquals(1, creatorFP.getScopedSites().size());
        assertEquals("site1", creatorFP.getScopedSites().iterator().next());

        /* Override order: custom,default */
        config = new SolrFacetConfig(rawProperties, "custom,default");
        config.setNamespaceService(context.getBean("namespaceService", NamespaceService.class));
        config.onBootstrap(applicationEvent);

        creatorFP = config.getDefaultFacets().get("test_filter_creator");
        assertEquals("Incorrect QNAME", "{http://www.alfresco.org/model/content/1.0}creator", creatorFP.getFacetQName().toString());
        assertEquals(5, creatorFP.getMaxFilters());
        assertEquals(1, creatorFP.getHitThreshold());
        assertEquals(4, creatorFP.getMinFilterValueLength());
        assertEquals(0, creatorFP.getScopedSites().size());
    }
}
