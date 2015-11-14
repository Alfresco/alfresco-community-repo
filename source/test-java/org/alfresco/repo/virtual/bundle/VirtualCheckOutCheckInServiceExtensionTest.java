/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */
package org.alfresco.repo.virtual.bundle;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServiceImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

public class VirtualCheckOutCheckInServiceExtensionTest extends VirtualizationIntegrationTest
{
    private static final String PROP_VERSION_LABEL_3 = "1.0";

    private static final String PROP_VERSION_LABEL_2 = "0.2";

    private static final String PROP_VERSION_LABEL_1 = "0.1";

    private static final String PROP_VERSION_DESCRIPTION_3 = "dd2";

    private static final String PROP_VERSION_DESCRIPTION_2 = "dd1";

    private static final String PROP_VERSION_DESCRIPTION_1 = "dd";

    private static final String TEST_CONTENT_3 = "2";

    private static final String TEST_CONTENT_2 = "1";

    private static final String TEST_CONTENT_1 = "0";

    private static final String PROP_FILE_NAME = "originalFile";

    private static final String PROP_WORKING_COPY_NAME = CheckOutCheckInServiceImpl
                .createWorkingCopyName(PROP_FILE_NAME,
                                       I18NUtil.getMessage("coci_service.working_copy_label"));

    private CheckOutCheckInService checkOutCheckInService;

    private VersionService versionService;

    private NodeRef originalContentNodeRef;

    private NodeRef node;

    private NodeRef physicalFileNodeRef;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        checkOutCheckInService = VirtualCheckOutCheckInServiceExtensionTest.ctx.getBean("checkOutCheckInService",
                                                                                        CheckOutCheckInService.class);
        versionService = VirtualCheckOutCheckInServiceExtensionTest.ctx.getBean("versionService",
                                                                                VersionService.class);

        node = nodeService.getChildByName(virtualFolder1NodeRef,
                                          ContentModel.ASSOC_CONTAINS,
                                          "Node1");
        originalContentNodeRef = createContent(node,
                                               PROP_FILE_NAME,
                                               TEST_CONTENT_1,
                                               MimetypeMap.MIMETYPE_TEXT_PLAIN,
                                               "UTF-8").getChildRef();
        nodeService.addAspect(originalContentNodeRef,
                              ContentModel.ASPECT_VERSIONABLE,
                              null);
        physicalFileNodeRef = nodeService.getChildByName(virtualFolder1NodeRef,
                                                         ContentModel.ASSOC_CONTAINS,
                                                         PROP_FILE_NAME);
    }
    
    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    @Test
    public void testCheckOut() throws Exception
    {
        // method1
        NodeRef workingCopy = checkOutCheckInService.checkout(originalContentNodeRef);
        assertNotNull(workingCopy);
        assertTrue(checkOutCheckInService.isCheckedOut(physicalFileNodeRef));

        assertEquals(checkOutCheckInService.getWorkingCopy(originalContentNodeRef),
                     workingCopy);
        checkOutCheckInService.cancelCheckout(workingCopy);

        // method2
        ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(node);
        NodeRef workingCopy1 = checkOutCheckInService.checkout(originalContentNodeRef,
                                                               childAssocRef.getParentRef(),
                                                               childAssocRef.getTypeQName(),
                                                               childAssocRef.getQName());

        assertNotNull(workingCopy1);
        assertTrue(checkOutCheckInService.isCheckedOut(physicalFileNodeRef));

        assertEquals(checkOutCheckInService.getWorkingCopy(physicalFileNodeRef),
                     workingCopy1);
        checkOutCheckInService.cancelCheckout(workingCopy1);

    }

    @Test
    public void testCheckIn() throws Exception
    {
        checkOutCheckInService.checkout(originalContentNodeRef);
        NodeRef workingCopyVirtualContext = nodeService.getChildByName(node,
                                                                       ContentModel.ASSOC_CONTAINS,
                                                                       PROP_WORKING_COPY_NAME);

        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        versionProperties.put(VersionModel.PROP_DESCRIPTION,
                              PROP_VERSION_DESCRIPTION_1);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE,
                              VersionType.MINOR);
        NodeRef origNodeRef = checkOutCheckInService.checkin(workingCopyVirtualContext,
                                                             versionProperties);

        assertNotNull(origNodeRef);
        assertEquals(originalContentNodeRef,
                     origNodeRef);
        assertFalse(checkOutCheckInService.isCheckedOut(physicalFileNodeRef));

        // Check that the version history is correct
        Version version = this.versionService.getCurrentVersion(origNodeRef);
        assertNotNull(version);
        assertEquals(PROP_VERSION_DESCRIPTION_1,
                     version.getDescription());
        assertEquals(VersionType.MINOR,
                     version.getVersionType());
        assertEquals(PROP_VERSION_LABEL_1,
                     version.getVersionLabel());

        // method2

        checkOutCheckInService.checkout(originalContentNodeRef);
        workingCopyVirtualContext = nodeService.getChildByName(node,
                                                               ContentModel.ASSOC_CONTAINS,
                                                               PROP_WORKING_COPY_NAME);

        versionProperties = new HashMap<String, Serializable>();
        versionProperties.put(VersionModel.PROP_DESCRIPTION,
                              PROP_VERSION_DESCRIPTION_2);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE,
                              VersionType.MINOR);

        ContentWriter tempWriter = this.contentService.getWriter(workingCopyVirtualContext,
                                                                 ContentModel.PROP_CONTENT,
                                                                 false);
        assertNotNull(tempWriter);
        tempWriter.putContent(TEST_CONTENT_2);

        String contentUrl = tempWriter.getContentUrl();
        origNodeRef = checkOutCheckInService.checkin(workingCopyVirtualContext,
                                                     versionProperties,
                                                     contentUrl);
        assertNotNull(origNodeRef);
        assertEquals(originalContentNodeRef,
                     origNodeRef);
        assertFalse(checkOutCheckInService.isCheckedOut(physicalFileNodeRef));

        // Check the checked in content
        ContentReader contentReader = this.contentService.getReader(origNodeRef,
                                                                    ContentModel.PROP_CONTENT);
        assertNotNull(contentReader);
        assertEquals(TEST_CONTENT_2,
                     contentReader.getContentString());

        // Check that the version history is correct

        version = this.versionService.getCurrentVersion(origNodeRef);
        assertNotNull(version);
        assertEquals(PROP_VERSION_DESCRIPTION_2,
                     version.getDescription());
        assertEquals(VersionType.MINOR,
                     version.getVersionType());
        assertEquals(PROP_VERSION_LABEL_2,
                     version.getVersionLabel());

        // method3

        checkOutCheckInService.checkout(originalContentNodeRef);
        workingCopyVirtualContext = nodeService.getChildByName(node,
                                                               ContentModel.ASSOC_CONTAINS,
                                                               PROP_WORKING_COPY_NAME);

        versionProperties = new HashMap<String, Serializable>();
        versionProperties.put(VersionModel.PROP_DESCRIPTION,
                              PROP_VERSION_DESCRIPTION_3);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE,
                              VersionType.MAJOR);

        tempWriter = this.contentService.getWriter(workingCopyVirtualContext,
                                                   ContentModel.PROP_CONTENT,
                                                   false);
        assertNotNull(tempWriter);
        tempWriter.putContent(TEST_CONTENT_3);

        contentUrl = tempWriter.getContentUrl();
        origNodeRef = checkOutCheckInService.checkin(workingCopyVirtualContext,
                                                     versionProperties,
                                                     contentUrl,
                                                     false);
        assertNotNull(origNodeRef);
        assertEquals(originalContentNodeRef,
                     origNodeRef);
        assertFalse(checkOutCheckInService.isCheckedOut(physicalFileNodeRef));

        // Check the checked in content
        contentReader = this.contentService.getReader(origNodeRef,
                                                      ContentModel.PROP_CONTENT);
        assertNotNull(contentReader);
        assertEquals(TEST_CONTENT_3,
                     contentReader.getContentString());

        // Check that the version history is correct

        version = this.versionService.getCurrentVersion(origNodeRef);
        assertNotNull(version);
        assertEquals(PROP_VERSION_DESCRIPTION_3,
                     version.getDescription());
        assertEquals(VersionType.MAJOR,
                     version.getVersionType());
        assertEquals(PROP_VERSION_LABEL_3,
                     version.getVersionLabel());
    }

    @Test
    public void testCancelCheckout() throws Exception
    {
        checkOutCheckInService.checkout(originalContentNodeRef);
        assertTrue(checkOutCheckInService.isCheckedOut(physicalFileNodeRef));

        NodeRef workingCopyVirtualContext = nodeService.getChildByName(node,
                                                                       ContentModel.ASSOC_CONTAINS,
                                                                       PROP_WORKING_COPY_NAME);
        assertNotNull(workingCopyVirtualContext);
        checkOutCheckInService.cancelCheckout(workingCopyVirtualContext);

        assertFalse(checkOutCheckInService.isCheckedOut(physicalFileNodeRef));
        workingCopyVirtualContext = nodeService.getChildByName(node,
                                                               ContentModel.ASSOC_CONTAINS,
                                                               PROP_WORKING_COPY_NAME);
        assertNull(workingCopyVirtualContext);

    }

    @Test
    public void testGetWorkingCopy() throws Exception
    {
        NodeRef checkedOut = checkOutCheckInService.checkout(originalContentNodeRef);

        NodeRef workingCopy = checkOutCheckInService.getWorkingCopy(originalContentNodeRef);
        assertNotNull(workingCopy);
        assertEquals(checkedOut,
                     workingCopy);

        checkOutCheckInService.cancelCheckout(workingCopy);

        workingCopy = checkOutCheckInService.getWorkingCopy(originalContentNodeRef);
        assertNull(workingCopy);
        assertNull(checkOutCheckInService.getWorkingCopy(physicalFileNodeRef));

    }

    @Test
    public void testGetCheckedOut() throws Exception
    {
        checkOutCheckInService.checkout(originalContentNodeRef);
        NodeRef workingCopyVirtualContext = nodeService.getChildByName(node,
                                                                       ContentModel.ASSOC_CONTAINS,
                                                                       PROP_WORKING_COPY_NAME);
        assertNotNull(workingCopyVirtualContext);

        NodeRef checkedOut = checkOutCheckInService.getCheckedOut(workingCopyVirtualContext);
        assertNotNull(checkedOut);
        assertEquals(originalContentNodeRef,
                     checkedOut);

        checkOutCheckInService.cancelCheckout(workingCopyVirtualContext);

        NodeRef checkedOut2 = checkOutCheckInService.getCheckedOut(originalContentNodeRef);
        assertNull(checkedOut2);

    }

    @Test
    public void testIsWorkingCopy() throws Exception
    {
        checkOutCheckInService.checkout(originalContentNodeRef);
        NodeRef workingCopyVirtualContext = nodeService.getChildByName(node,
                                                                       ContentModel.ASSOC_CONTAINS,
                                                                       PROP_WORKING_COPY_NAME);
        assertNotNull(workingCopyVirtualContext);

        assertTrue(checkOutCheckInService.isWorkingCopy(workingCopyVirtualContext));

        checkOutCheckInService.cancelCheckout(workingCopyVirtualContext);
    }

    @Test
    public void testIsCheckedOut() throws Exception
    {
        NodeRef workingCopy = checkOutCheckInService.checkout(originalContentNodeRef);
        assertNotNull(workingCopy);

        boolean checkedOut = checkOutCheckInService.isCheckedOut(originalContentNodeRef);
        assertTrue(checkedOut);

        checkOutCheckInService.cancelCheckout(workingCopy);
        checkedOut = checkOutCheckInService.isCheckedOut(originalContentNodeRef);
        assertFalse(checkedOut);

    }

    @Test
    public void testAssocsReferences() throws Exception
    {
        checkOutCheckInService.checkout(originalContentNodeRef);
        NodeRef workingCopyVirtualContext = nodeService.getChildByName(node,
                                                                       ContentModel.ASSOC_CONTAINS,
                                                                       PROP_WORKING_COPY_NAME);

        //test target association reference
        List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(originalContentNodeRef,
                                                                        ContentModel.ASSOC_WORKING_COPY_LINK);
        assertNotNull(targetAssocs);
        assertEquals(1,
                     targetAssocs.size());
        assertEquals(workingCopyVirtualContext,
                     targetAssocs.get(0).getTargetRef());

        //test source association reference
        List<AssociationRef> sourceAssocs = nodeService.getSourceAssocs(workingCopyVirtualContext,
                                                                        ContentModel.ASSOC_WORKING_COPY_LINK);
        assertNotNull(sourceAssocs);
        assertEquals(1,
                     sourceAssocs.size());
        assertEquals(originalContentNodeRef,
                     sourceAssocs.get(0).getSourceRef());
        checkOutCheckInService.cancelCheckout(workingCopyVirtualContext);
    }
}
