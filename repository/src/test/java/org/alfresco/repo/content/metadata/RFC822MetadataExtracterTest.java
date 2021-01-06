/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.content.metadata;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;

import static org.alfresco.repo.content.metadata.RFC822MetadataExtracter.ASPECT_DOD_5015_RECORD;
import static org.alfresco.repo.content.metadata.RFC822MetadataExtracter.ASPECT_RECORD;
import static org.alfresco.repo.content.metadata.RFC822MetadataExtracter.DOD_URI;
import static org.alfresco.repo.content.metadata.RFC822MetadataExtracter.RM_URI;
import static org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI;
import static org.mockito.Mockito.when;

/**
 * Test the ability of RFC822MetadataExtracter when overridden by RM, to control which properties are extracted
 * from T-Engines. RFC822MetadataExtracter no longer extracts.
 *
 * @author adavis
 */
//@RunWith(MockitoJUnitRunner.class)
public class RFC822MetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private RFC822MetadataExtracter extracter;
    private RFC822MetadataExtracter rmExtracter;
    @Mock private NodeService mockNodeService;

    private NodeRef nodeRefWithDodRecord = new NodeRef("workspace://spacesStore/test-dod");
    private NodeRef nodeRefWithRecord = new NodeRef("workspace://spacesStore/test-rm");
    private NodeRef nodeRefWithBoth = new NodeRef("workspace://spacesStore/test-both");
    private NodeRef nodeRefWithNeither = new NodeRef("workspace://spacesStore/test-neither");

    private static final QName MESSAGE_FROM_TEST_PROPERTY =
            QName.createQName("MessageToTest");
    private static final QName MESSAGE_TO_TEST_PROPERTY =
            QName.createQName("MessageFromTest");
    private static final QName MESSAGE_CC_TEST_PROPERTY =
            QName.createQName("MessageCCTest");

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        extracter = (RFC822MetadataExtracter) ctx.getBean("extracter.RFC822");

        MockitoAnnotations.initMocks(this);
        when(mockNodeService.hasAspect(nodeRefWithDodRecord, ASPECT_DOD_5015_RECORD)).thenReturn(true);
        when(mockNodeService.hasAspect(nodeRefWithRecord, ASPECT_RECORD)).thenReturn(true);
        when(mockNodeService.hasAspect(nodeRefWithBoth, ASPECT_DOD_5015_RECORD)).thenReturn(true);
        when(mockNodeService.hasAspect(nodeRefWithBoth, ASPECT_RECORD)).thenReturn(true);

        rmExtracter = new RFC822MetadataExtracter()
        {
            @Override
            // Needed so the init method runs.
            protected Map<String, Set<QName>> getDefaultMapping()
            {
                return Collections.emptyMap();
            }
        };
        rmExtracter.setNodeService(mockNodeService);
        rmExtracter.init();
    }

    @Override
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    @Override
    protected void testFileSpecificMetadata(String mimetype, Map<QName, Serializable> properties)
    {
        // ignore as this is no longer an extractor
    }

    public void testMatch()
    {
        assertFalse("Normal class should never match", extracter.match(MimetypeMap.MIMETYPE_RFC822));
        assertTrue("RM class should match with correct type", rmExtracter.match(MimetypeMap.MIMETYPE_RFC822));
        assertFalse("RM class should not match with other types", rmExtracter.match(MimetypeMap.MIMETYPE_PDF));
    }

    public void testGetExtractMapping()
    {
        Properties properties = new Properties();
        properties.put("namespace.prefix.rm", RM_URI);
        properties.put("namespace.prefix.dod", DOD_URI);
        properties.put("namespace.prefix.cm", CONTENT_MODEL_1_0_URI);
        properties.put("a", "cm:a");
        properties.put("b", "rm:b, dod:b");
        properties.put("c", "rm:c");
        properties.put("d", "cm:d, rm:d1, rm:d2");
        rmExtracter.setMappingProperties(properties);

        assertEquals("No properties should have been removed", 7, countSystemProperties(nodeRefWithBoth));
        assertEquals("The 1 dod and 4 record properties should have been removed", 2, countSystemProperties(nodeRefWithNeither));
        assertEquals("The 4 record properties should have been removed", 3, countSystemProperties(nodeRefWithDodRecord));
        assertEquals("The 1 dod property should have been removed", 6, countSystemProperties(nodeRefWithRecord));

        // Check that we have the fully qualified version as the T-Engine know nothing about the repo's prefixes.
        // Check just one of them.
        assertEquals("{http://www.alfresco.org/model/content/1.0}d, " +
                "{http://www.alfresco.org/model/content/1.0}a, " +
                "{http://www.alfresco.org/model/dod5015/1.0}b", getSystemProperties(nodeRefWithDodRecord));
    }

    private int countSystemProperties(NodeRef nodeRef)
    {
        Map<String, Set<String>> extractMapping = rmExtracter.getExtractMapping(nodeRef);
        AtomicInteger count = new AtomicInteger();
        extractMapping.forEach((k,v) -> count.addAndGet(v.size()));
        return count.get();
    }

    private String getSystemProperties(NodeRef nodeRef)
    {
        Map<String, Set<String>> extractMapping = rmExtracter.getExtractMapping(nodeRef);
        StringJoiner sj = new StringJoiner(", ");
        extractMapping.forEach((k,v) -> v.forEach(p -> sj.add(p.toString())));
        return sj.toString();
    }
}
