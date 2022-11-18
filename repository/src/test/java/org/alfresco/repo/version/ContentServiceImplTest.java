/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.version;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.directurl.SystemWideDirectUrlConfig;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;


/**
 * Tests for getting content readers and writers.
 * 
 * @author Roy Wetherall
 */
@Category(OwnJVMTestsCategory.class)
@Transactional
public class ContentServiceImplTest extends BaseVersionStoreTest
{
    private static final Boolean ENABLED = Boolean.TRUE;

    /**
     * Test content data
     */
    private final static String UPDATED_CONTENT = "This content has been updated with a new value.";
    private static final QName QNAME = ContentModel.PROP_CONTENT;
    
    /**
     * The version content store
     */
    @InjectMocks
    private ContentService contentService;

    private ContentStore contentStore;

    @Mock
    private SystemWideDirectUrlConfig mockSystemWideDirectUrlConfig;

    @Before
    public void before() throws Exception
    {
        super.before();
        
        // Get the instance of the required content service
        this.contentService = (ContentService)this.applicationContext.getBean("contentService");
        this.contentStore = (ContentStore) ReflectionTestUtils.getField(contentService, "store");
    }
    
    /**
     * Test getReader
     */
    @Test
    public void testGetReader()
    {
        // Create a new versionable node
        NodeRef versionableNode = createNewVersionableNode();
        
        // Create a new version
        Version version = createVersion(versionableNode, this.versionProperties);
        NodeRef versionNodeRef = version.getFrozenStateNodeRef();
		
        // Get the content reader for the frozen node
        ContentReader contentReader = this.contentService.getReader(versionNodeRef, ContentModel.PROP_CONTENT);
        assertNotNull(contentReader);
        assertEquals(TEST_CONTENT, contentReader.getContentString());
        
        // Now update the content and verison again
        ContentWriter contentWriter = this.contentService.getWriter(versionableNode, ContentModel.PROP_CONTENT, true);
        assertNotNull(contentWriter);
        contentWriter.putContent(UPDATED_CONTENT);        
        Version version2 = createVersion(versionableNode, this.versionProperties);
        NodeRef version2NodeRef = version2.getFrozenStateNodeRef();
		
        // Get the content reader for the new verisoned content
        ContentReader contentReader2 = this.contentService.getReader(version2NodeRef, ContentModel.PROP_CONTENT);
        assertNotNull(contentReader2);
        assertEquals(UPDATED_CONTENT, contentReader2.getContentString());
    }

    /**
     * Test getWriter
     */
    @Test
    public void testGetWriter()
    {
        // Create a new versionable node
        NodeRef versionableNode = createNewVersionableNode();
        
        // Create a new version
        Version version = createVersion(versionableNode, this.versionProperties);
        
        // Get writer is not supported by the version content service
        try
        {
            ContentWriter contentWriter = this.contentService.getWriter(
                    version.getFrozenStateNodeRef(),
                    ContentModel.PROP_CONTENT,
                    true);
            contentWriter.putContent("bobbins");
            fail("This operation is not supported.");
        }
        catch (Exception exception)
        {
            // An exception should be raised
        }
    }

    @Test
    public void testWhenRequestContentDirectUrlIsNotSupported()
    {
        openMocks(this);
        when(mockSystemWideDirectUrlConfig.isEnabled()).thenReturn(ENABLED);
        when(mockSystemWideDirectUrlConfig.getDefaultExpiryTimeInSec()).thenReturn(30L);
        when(mockSystemWideDirectUrlConfig.getMaxExpiryTimeInSec()).thenReturn(300L);

        assertFalse(contentStore.isContentDirectUrlEnabled());

        // Set the presigned URL to expire after one minute.
        Long validFor = 60L;

        assertThrows("nodeRef has no content", IllegalArgumentException.class, () -> {
            // Create a node without content
            NodeRef nodeRef = this.dbNodeService
                    .createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}MyNoContentNode"), TEST_TYPE_QNAME, this.nodeProperties).getChildRef();

            assertNull(contentService.requestContentDirectUrl(nodeRef, QNAME, true, validFor));
        });

        assertThrows("nodeRef is null", IllegalArgumentException.class, () -> {
            assertNull(contentService.requestContentDirectUrl(null, null, true, null));
        });

        assertThrows("propertyQName has no content", NullPointerException.class, () -> {
            // Create a node without content
            NodeRef nodeRef = this.dbNodeService
                    .createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}MyNoContentNode"), TEST_TYPE_QNAME, this.nodeProperties).getChildRef();

            contentService.requestContentDirectUrl(nodeRef, null, true, validFor);
        });

        // Create a node with content
        NodeRef nodeRef = createNewVersionableNode();

        assertNull(contentService.requestContentDirectUrl(nodeRef, QNAME, true, null));
        assertNull(contentService.requestContentDirectUrl(nodeRef, QNAME, true, validFor));
    }
}
