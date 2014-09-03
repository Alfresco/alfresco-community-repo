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

import java.util.Map;
import java.util.Properties;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.*;

import static org.junit.Assert.*;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
        context = new ClassPathXmlApplicationContext(new String[] { "classpath:facets/test-facet-property-context.xml" },
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
        assertEquals("Incorrect number of properties", 4, defaultProps.size());

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
        SolrFacetProperties descFP = defaultProps.get("test_filter_description");
        assertEquals("Incorrect QNAME", "{http://www.alfresco.org/model/content/1.0}description", descFP.getFacetQName().toString());
        assertEquals("faceted-search.facet-menu.facet.description", descFP.getDisplayName());
        assertEquals("alfresco/search/FacetFilters", descFP.getDisplayControl());
        assertEquals(3, descFP.getMaxFilters());
        assertEquals(1, descFP.getHitThreshold());
        assertEquals(2, descFP.getMinFilterValueLength());
        assertEquals("DESCENDING", descFP.getSortBy());
        assertEquals("SCOPED_SITES", descFP.getScope());
        assertEquals(0, descFP.getScopedSites().size());
        assertEquals(true, descFP.isEnabled());

        // See if the overrides worked
        SolrFacetProperties creatorFP = defaultProps.get("test_filter_creator");
        assertEquals("Incorrect QNAME", "{http://www.alfresco.org/model/content/1.0}creator.__.u", creatorFP.getFacetQName().toString());

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
        ApplicationEvent applicationEvent = new ApplicationEvent(this)
        {
            private static final long serialVersionUID = 1L;
        };

        /*
         * Override order: default,custom
         */
        SolrFacetConfig config = new SolrFacetConfig(rawProperties, "default,custom");
        config.setNamespaceService(context.getBean("namespaceService", NamespaceService.class));
        config.onBootstrap(applicationEvent);

        SolrFacetProperties creatorFP = config.getDefaultFacets().get("test_filter_creator");
        assertEquals("Incorrect QNAME", "{http://www.alfresco.org/model/content/1.0}creator.__.u", creatorFP.getFacetQName().toString());
        assertEquals(10, creatorFP.getMaxFilters());
        assertEquals(5, creatorFP.getHitThreshold());
        assertEquals(14, creatorFP.getMinFilterValueLength());
        assertEquals(1, creatorFP.getScopedSites().size());
        assertEquals("site1", creatorFP.getScopedSites().iterator().next());

        /*
         * Override order: custom,default
         */
        config = new SolrFacetConfig(rawProperties, "custom,default");
        config.setNamespaceService(context.getBean("namespaceService", NamespaceService.class));
        config.onBootstrap(applicationEvent);

        creatorFP = config.getDefaultFacets().get("test_filter_creator");
        assertEquals("Incorrect QNAME", "{http://www.alfresco.org/model/content/1.0}creator.__.u", creatorFP.getFacetQName().toString());
        assertEquals(5, creatorFP.getMaxFilters());
        assertEquals(1, creatorFP.getHitThreshold());
        assertEquals(4, creatorFP.getMinFilterValueLength());
        assertEquals(0, creatorFP.getScopedSites().size());
    }
}