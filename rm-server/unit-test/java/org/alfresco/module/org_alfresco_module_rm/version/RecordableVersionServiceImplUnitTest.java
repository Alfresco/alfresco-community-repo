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
package org.alfresco.module.org_alfresco_module_rm.version;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Recordable version service implementation unit test.
 *
 * @author Roy Wetherall
 * @since 2.3
 */
public class RecordableVersionServiceImplUnitTest extends BaseUnitTest
{
    /** versioned content name */
    private static final String CONTENT_NAME = "test.txt";

    /** versioned node reference */
    private NodeRef nodeRef;
    private NodeRef record;
    private NodeRef unfiledRecordContainer;
    private NodeRef version;

    /** mocked version properties */
    private Map<String, Serializable> versionProperties;

    /** mocked services */
    private @Mock(name="dbNodeService")   NodeService mockedDbNodeService;

    /** recordable version service */
    private @InjectMocks TestRecordableVersionServiceImpl recordableVersionService;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest#before()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void before() throws Exception
    {
        super.before();

        nodeRef = generateCmContent(CONTENT_NAME);
        doReturn(123l).when(mockedNodeService).getProperty(nodeRef, ContentModel.PROP_NODE_DBID);

        versionProperties = new HashMap<String, Serializable>(5);

        recordableVersionService.initialise();

        doReturn(generateChildAssociationRef(null, generateNodeRef(Version2Model.TYPE_QNAME_VERSION_HISTORY)))
            .when(mockedDbNodeService).createNode(any(NodeRef.class),
                                                  any(QName.class),
                                                  any(QName.class),
                                                  eq(Version2Model.TYPE_QNAME_VERSION_HISTORY),
                                                  anyMap());
        doReturn(generateChildAssociationRef(null, generateNodeRef(TYPE_CONTENT)))
        .when(mockedDbNodeService).createNode(any(NodeRef.class),
                                              any(QName.class),
                                              any(QName.class),
                                              eq(TYPE_CONTENT),
                                              anyMap());

        doReturn(filePlan).when(mockedFilePlanService).getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
        doReturn(unfiledRecordContainer).when(mockedFilePlanService).getUnfiledContainer(any(NodeRef.class));

        record = generateCmContent(CONTENT_NAME);
        FileInfo mockedFileInfo = mock(FileInfo.class);
        doReturn(record).when(mockedFileInfo).getNodeRef();
        doReturn(mockedFileInfo).when(mockedFileFolderService).copy(any(NodeRef.class),
                                                                    any(NodeRef.class),
                                                                    any(String.class));
        version = generateNodeRef(TYPE_CONTENT);
        doReturn(generateChildAssociationRef(null, version)).when(mockedDbNodeService).createNode(
                                                                any(NodeRef.class),
                                                                eq(Version2Model.CHILD_QNAME_VERSIONS),
                                                                any(QName.class),
                                                                eq(TYPE_CONTENT),
                                                                anyMap());
    }

    /**
     * Given that the node has no recordable version aspect
     * When I create a version
     * Then version service creates a normal version.
     */
    @Test
    public void noAspect() throws Exception
    {
        // setup given conditions
        doReturn(false).when(mockedNodeService).hasAspect(nodeRef, RecordableVersionModel.ASPECT_VERSIONABLE);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);

        // when version is created
        recordableVersionService.createVersion(nodeRef, versionProperties);

        // then a normal version is created
        verifyNormalVersion();
    }

    /**
     * Given that the node has a recordable version policy of null
     * When I create a version
     * Then the version service creates a normal version.
     */
     @Test
     public void policyNull() throws Exception
     {
         // setup given conditions
         doReturn(false).when(mockedNodeService).hasAspect(nodeRef, RecordableVersionModel.ASPECT_VERSIONABLE);
         doReturn(null).when(mockedNodeService).getProperty(nodeRef, RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY);
         versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);

         // when version is created
         recordableVersionService.createVersion(nodeRef, versionProperties);

         // then a normal version is created
         verifyNormalVersion();
     }

    /**
     * Given that the node has a recordable version policy of NONE
     * When I create a version
     * Then the version service creates a normal version.
     */
    @Test
    public void policyNone() throws Exception
    {
        // setup given conditions
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, RecordableVersionModel.ASPECT_VERSIONABLE);
        doReturn(RecordableVersionPolicy.NONE.toString()).when(mockedNodeService).getProperty(nodeRef, RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);

        // when version is created
        recordableVersionService.createVersion(nodeRef, versionProperties);

        // then a normal version is created
        verifyNormalVersion();
    }

    /**
     * Given that the node has a recordable version policy of ALL
     * When I create a MINOR version then
     * the version service creates a recorded version
     */
    @Test
    public void policyAllVersionMinor() throws Exception
    {
        // setup given conditions
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, RecordableVersionModel.ASPECT_VERSIONABLE);
        doReturn(RecordableVersionPolicy.ALL.toString()).when(mockedNodeService).getProperty(nodeRef, RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);

        // when version is created
        recordableVersionService.createVersion(nodeRef, versionProperties);

        // then the recorded version is created
        verify(mockedRecordService, times(1)).createRecordFromCopy(filePlan, nodeRef);
    }

    /**
     * Helper method that verified that a recorded version was not created.
     */
    @SuppressWarnings("unchecked")
    private void verifyNormalVersion() throws Exception
    {
        // verify no interactions
        verify(mockedFilePlanService, never()).getUnfiledContainer(any(NodeRef.class));
        verify(mockedFileFolderService, never()).copy(eq(nodeRef),
                                                       eq(unfiledRecordContainer),
                                                       anyString());

        // then the version is created
        verify(mockedDbNodeService, times(1)).createNode(any(NodeRef.class),
                                                         eq(Version2Model.CHILD_QNAME_VERSIONS),
                                                         any(QName.class),
                                                         eq(TYPE_CONTENT),
                                                         anyMap());
        verify(mockedNodeService, times(1)).addAspect(eq(version), eq(Version2Model.ASPECT_VERSION), anyMap());
        verify(mockedNodeService, never()).addAspect(eq(version), eq(RecordableVersionModel.PROP_RECORD_NODE_REF), anyMap());
    }

    /**
     * Given that the node has a recordable version policy of ALL
     * When I create a MAJOR version then
     * the version service creates a recorded version
     */
    @Test
    public void policyAllVersionMajor() throws Exception
    {
        // setup given conditions
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, RecordableVersionModel.ASPECT_VERSIONABLE);
        doReturn(RecordableVersionPolicy.ALL.toString()).when(mockedNodeService).getProperty(nodeRef, RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);

        // when version is created
        recordableVersionService.createVersion(nodeRef, versionProperties);

        // then the recorded version is created
        verify(mockedRecordService, times(1)).createRecordFromCopy(filePlan, nodeRef);

    }

    /**
     * Given that the node has a recordable version policy of MAJOR_ONLY
     * When I create a MINOR version then
     * the version service creates a normal version
     */
    @Test
    public void policyMajorOnlyVersionMinor() throws Exception
    {
        // setup given conditions
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, RecordableVersionModel.ASPECT_VERSIONABLE);
        doReturn(RecordableVersionPolicy.MAJOR_ONLY.toString()).when(mockedNodeService).getProperty(nodeRef, RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);

        // when version is created
        recordableVersionService.createVersion(nodeRef, versionProperties);

        // then a normal version is created
        verifyNormalVersion();
    }

    /**
     * Given that the node has a recordable version policy of MAJOR_ONLY
     * When I create a MAJOR version then
     * the version service creates a recorded version
     */
    @Test
    public void policyMajorOnlyVersionMajor() throws Exception
    {
        // setup given conditions
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, RecordableVersionModel.ASPECT_VERSIONABLE);
        doReturn(RecordableVersionPolicy.MAJOR_ONLY.toString()).when(mockedNodeService).getProperty(nodeRef, RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);

        // when version is created
        recordableVersionService.createVersion(nodeRef, versionProperties);

        // then the recorded version is created
        verify(mockedRecordService, times(1)).createRecordFromCopy(filePlan, nodeRef);
    }

    /**
     * Given that the node has a valid recordable version policy
     * And there is no file plan
     * When I create a new version
     * Then an exception should be thrown to indicate that there is no file plan
     */
    @Test
    public void noFilePlan() throws Exception
    {
        // setup given conditions
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, RecordableVersionModel.ASPECT_VERSIONABLE);
        doReturn(RecordableVersionPolicy.MAJOR_ONLY.toString()).when(mockedNodeService).getProperty(nodeRef, RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
        doReturn(null).when(mockedFilePlanService).getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);

        // expected exception
        exception.expect(AlfrescoRuntimeException.class);

        // when version is created
        recordableVersionService.createVersion(nodeRef, versionProperties);
    }

    /**
     * Given that the node has a valid recordable version policy
     * And that I set a specific file plan in the version properties
     * When I create a new version
     * Then the recorded version should be directed to the specified file plan, not the default file plan
     */
    @Test
    public void filePlanSpecifiedWithPolicy() throws Exception
    {
        // setup given conditions
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, RecordableVersionModel.ASPECT_VERSIONABLE);
        doReturn(RecordableVersionPolicy.MAJOR_ONLY.toString()).when(mockedNodeService).getProperty(nodeRef, RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);

        // specify the file plan
        NodeRef anotherFilePlan = generateNodeRef(TYPE_FILE_PLAN);
        versionProperties.put(RecordableVersionServiceImpl.KEY_FILE_PLAN, anotherFilePlan);

        // when version is created
        recordableVersionService.createVersion(nodeRef, versionProperties);

        // then the recorded version is created
        verify(mockedRecordService, times(0)).createRecordFromCopy(filePlan, nodeRef);
    }

    /**
     * Given that the node has specifically indicated that a recorded version should be created
     * And that I set a specific file plan in the version properties
     * When I create a new version
     * Then the recorded version should be directed to the specified file plan, not the default file plan
     */
    @Test
    public void filePlanSpecifiedNoPolicy() throws Exception
    {
        // setup given conditions
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, RecordableVersionModel.ASPECT_VERSIONABLE);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
        versionProperties.put(RecordableVersionServiceImpl.KEY_RECORDABLE_VERSION, true);

        // specify the file plan
        NodeRef anotherFilePlan = generateNodeRef(TYPE_FILE_PLAN);
        versionProperties.put(RecordableVersionServiceImpl.KEY_FILE_PLAN, anotherFilePlan);

        // when version is created
        recordableVersionService.createVersion(nodeRef, versionProperties);

        // then the recorded version is created
        verify(mockedRecordService, times(0)).createRecordFromCopy(filePlan, nodeRef);
    }

    @Test
    public void adHocRecordedVersionNoPolicy() throws Exception
    {
        // setup given conditions
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, RecordableVersionModel.ASPECT_VERSIONABLE);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
        versionProperties.put(RecordableVersionServiceImpl.KEY_RECORDABLE_VERSION, true);

        // when version is created
        recordableVersionService.createVersion(nodeRef, versionProperties);

        // then the recorded version is created
        verify(mockedRecordService, times(1)).createRecordFromCopy(filePlan, nodeRef);
    }

    @Test
    public void adHocRecordedVersionOverridePolicy() throws Exception
    {
        // setup given conditions
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, RecordableVersionModel.ASPECT_VERSIONABLE);
        doReturn(RecordableVersionPolicy.MAJOR_ONLY.toString()).when(mockedNodeService).getProperty(nodeRef, RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
        versionProperties.put(RecordableVersionServiceImpl.KEY_RECORDABLE_VERSION, true);

        // when version is created
        recordableVersionService.createVersion(nodeRef, versionProperties);

        // then the recorded version is created
        verify(mockedRecordService, times(1)).createRecordFromCopy(filePlan, nodeRef);
    }
}