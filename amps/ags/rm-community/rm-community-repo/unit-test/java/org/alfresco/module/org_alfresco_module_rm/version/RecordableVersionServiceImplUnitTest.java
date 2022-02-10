/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.version;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.version.common.VersionImpl;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Recordable version service implementation unit test.
 *
 * @author Roy Wetherall
 * @since 2.3
 */
@RunWith(MockitoJUnitRunner.class)
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
    private @InjectMocks @Spy TestRecordableVersionServiceImpl recordableVersionService;

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

        versionProperties = new HashMap<>(5);

        recordableVersionService.initialise();

        doReturn(generateChildAssociationRef(null, generateNodeRef(Version2Model.TYPE_QNAME_VERSION_HISTORY)))
            .when(mockedDbNodeService).createNode(nullable(NodeRef.class),
                                                  nullable(QName.class),
                                                  nullable(QName.class),
                                                  eq(Version2Model.TYPE_QNAME_VERSION_HISTORY),
                                                  nullable(Map.class));

        doReturn(filePlan).when(mockedFilePlanService).getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);

        record = generateCmContent(CONTENT_NAME);
        version = generateNodeRef(TYPE_CONTENT);
        doReturn(generateChildAssociationRef(null, version)).when(mockedDbNodeService).createNode(
                                                                nullable(NodeRef.class),
                                                                eq(Version2Model.CHILD_QNAME_VERSIONS),
                                                                nullable(QName.class),
                                                                eq(TYPE_CONTENT),
                                                                nullable(Map.class));
        recordableVersionService.setDbNodeService(mockedDbNodeService);
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
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
        versionProperties.put(RecordableVersionServiceImpl.KEY_RECORDABLE_VERSION, true);

        // when version is created
        recordableVersionService.createVersion(nodeRef, versionProperties);

        // then the recorded version is created
        verify(mockedRecordService, times(1)).createRecordFromCopy(filePlan, nodeRef);
    }
    
    /**
     * Given that a node is not versionable
     * When I try and create a record from the latest version
     * Then nothing will happen, because there is not version to record
     */
    @Test
    public void notVersionableCreateRecordFromVersion()
    {
        // content node is not versionable
        doReturn(false).when(mockedNodeService).hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE);
        
        // create record from version
        recordableVersionService.createRecordFromLatestVersion(filePlan, nodeRef);
        
        // nothing happens
        verify(mockedRecordService, never()).createRecordFromCopy(eq(filePlan), any(NodeRef.class));
    }
    
    /**
     * Given that a nodes last version is recorded
     * When I try and create a record from the latest version
     * Then nothing will happen, because the latest version is already recorded
     */
    @Test
    public void alreadyRecordedCreateRecordFromVersion()
    {
        // latest version is already recorded
        Version mockedVersion = mock(VersionImpl.class);
        NodeRef versionNodeRef = generateNodeRef();
        when(mockedVersion.getFrozenStateNodeRef())
            .thenReturn(versionNodeRef);
        
        when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
            .thenReturn(true);
        when(mockedDbNodeService.hasAspect(versionNodeRef, RecordableVersionModel.ASPECT_RECORDED_VERSION))
            .thenReturn(true);
        doReturn(mockedVersion)
           .when(recordableVersionService).getCurrentVersion(nodeRef);
        
        // create record from version
        recordableVersionService.createRecordFromLatestVersion(filePlan, nodeRef);
        
        // nothing happens
        verify(mockedRecordService, never()).createRecordFromCopy(eq(filePlan), any(NodeRef.class));        
    }
    
    /**
     * Given that a nodes last version is not recorded
     * When I try to create a record from the latest version
     * Then the latest version is marked as record and a new record version is created to store the version state
     */
    @SuppressWarnings("unchecked")
    @Test
    public void notRecordedCreateRecordFromVersion()
    {
        // latest version is not recorded
        Version mockedVersion = mock(VersionImpl.class);
        NodeRef versionNodeRef = generateNodeRef();
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE);
        
        // version history
        NodeRef versionHistoryNodeRef = generateNodeRef();
        doReturn(versionHistoryNodeRef).when(mockedDbNodeService).getChildByName(nullable(NodeRef.class), eq(Version2Model.CHILD_QNAME_VERSION_HISTORIES), nullable(String.class));
        
        // version number
        doReturn(mockedVersion).when(recordableVersionService).getCurrentVersion(nodeRef);
        doReturn(versionNodeRef).when(recordableVersionService).convertNodeRef(nullable(NodeRef.class));
        makePrimaryParentOf(versionNodeRef, versionHistoryNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "something-0"), mockedDbNodeService);
        
        // created version
        NodeRef newVersionNodeRef = generateNodeRef();
        doReturn(generateChildAssociationRef(versionHistoryNodeRef, newVersionNodeRef)).when(mockedDbNodeService).createNode(
                eq(versionHistoryNodeRef),
                eq(Version2Model.CHILD_QNAME_VERSIONS),
                nullable(QName.class),
                nullable(QName.class),
                nullable(Map.class));
        
        // created record
        NodeRef newRecordNodeRef = generateNodeRef();
        doReturn(newRecordNodeRef).when(mockedRecordService).createRecordFromContent(
                eq(filePlan),
                nullable(String.class),
                nullable(QName.class),
                nullable(Map.class),
                nullable(ContentReader.class));
                
        // create record from version
        recordableVersionService.createRecordFromLatestVersion(filePlan, nodeRef);
        
        // verify that the version is converted to a recorded version
        verify(mockedRecordService, times(1)).createRecordFromContent(
                eq(filePlan),
                nullable(String.class),
                nullable(QName.class),
                any(Map.class),
                nullable(ContentReader.class));
        verify(mockedDbNodeService, times(1)).deleteNode(any(NodeRef.class));
        verify(mockedDbNodeService, times(1)).createNode(
                eq(versionHistoryNodeRef),
                eq(Version2Model.CHILD_QNAME_VERSIONS),
                nullable(QName.class),
                nullable(QName.class),
                nullable(Map.class));
        verify(mockedNodeService, times(1)).addAspect(eq(newVersionNodeRef), eq(Version2Model.ASPECT_VERSION), any(Map.class));
        verify(mockedNodeService, times(1)).addAspect(
                newVersionNodeRef, 
                RecordableVersionModel.ASPECT_RECORDED_VERSION, 
                Collections.singletonMap(RecordableVersionModel.PROP_RECORD_NODE_REF, (Serializable)newRecordNodeRef));        
    }
    
    
    /**
     * given the destroyed prop isn't set
     * when I ask if the version is destroyed
     * then the result is false
     */
    @Test
    public void propNotSetVersionNotDestroyed()
    {
        // set up version
        Version mockedVersion = mock(VersionImpl.class);
        NodeRef versionNodeRef = generateNodeRef();
        when(mockedVersion.getFrozenStateNodeRef())
            .thenReturn(versionNodeRef);
        
        // set prop not set
        when(mockedDbNodeService.getProperty(versionNodeRef, RecordableVersionModel.PROP_DESTROYED))
            .thenReturn(null);
        
        // is version destroyed
        assertFalse(recordableVersionService.isRecordedVersionDestroyed(mockedVersion));            
    }
    
    /**
     * given the destroyed prop is set
     * when I ask if the version is destroyed
     * then the result matches the value set in the destroy property
     */
    @Test
    public void propSetVersionDestroyed()
    {
        // set up version
        Version mockedVersion = mock(VersionImpl.class);
        NodeRef versionNodeRef = generateNodeRef();
        when(mockedVersion.getFrozenStateNodeRef())
            .thenReturn(versionNodeRef);
        
        // set prop
        when(mockedDbNodeService.getProperty(versionNodeRef, RecordableVersionModel.PROP_DESTROYED))
            .thenReturn(Boolean.TRUE);
        
        // is version destroyed
        assertTrue(recordableVersionService.isRecordedVersionDestroyed(mockedVersion));     
        
        // set prop
        when(mockedDbNodeService.getProperty(versionNodeRef, RecordableVersionModel.PROP_DESTROYED))
            .thenReturn(Boolean.FALSE);
        
        // is version destroyed
        assertFalse(recordableVersionService.isRecordedVersionDestroyed(mockedVersion));    
    }
    
    /**
     * given that the version node doesn't have the recorded version aspect applied
     * when I mark the version as destroyed
     * then nothing happens
     */
    @Test
    public void noAspectMarkAsDestroyed()
    {
        // set up version
        Version mockedVersion = mock(VersionImpl.class);
        NodeRef versionNodeRef = generateNodeRef();
        when(mockedVersion.getFrozenStateNodeRef())
            .thenReturn(versionNodeRef);
        
        // indicate that the version doesn't have the aspect
        when(mockedDbNodeService.hasAspect(versionNodeRef, RecordableVersionModel.ASPECT_RECORDED_VERSION))
            .thenReturn(false);
        
        // mark as destroyed
        recordableVersionService.destroyRecordedVersion(mockedVersion);
        
        // verify nothing happened
        verify(mockedDbNodeService, never())
            .setProperty(versionNodeRef, RecordableVersionModel.PROP_DESTROYED, Boolean.TRUE);        
    }
    
    /**
     * given that the version node ref has the recorded version aspect applied
     * and the record version reference exists
     * when I mark the version as destroyed
     * then the version is marked as destroyed
     */
    @Test
    public void markAsDestroyed()
    {
        // set up version
        Version mockedVersion = mock(VersionImpl.class);
        NodeRef versionNodeRef = generateNodeRef();
        when(mockedVersion.getFrozenStateNodeRef())
            .thenReturn(versionNodeRef);

        // indicate that the version doesn't have the aspect
        when(mockedDbNodeService.hasAspect(versionNodeRef, RecordableVersionModel.ASPECT_RECORDED_VERSION))
            .thenReturn(true);
        
        // mark as destroyed
        recordableVersionService.destroyRecordedVersion(mockedVersion);
        
        // verify that the version was marked as destroyed
        verify(mockedDbNodeService)
            .setProperty(versionNodeRef, RecordableVersionModel.PROP_DESTROYED, Boolean.TRUE);   
        // and the reference to the version record was cleared
        verify(mockedDbNodeService)
            .setProperty(versionNodeRef, RecordableVersionModel.PROP_RECORD_NODE_REF, null);  
    }
    
    /**
     * given that the version node ref has the recorded version aspect applied
     * and the associated version record has been deleted
     * when I mark the version as destroyed
     * then the version is marked as destroyed
     * and the reference to the deleted version record is removed
     */
    @Test
    public void markAsDestroyedClearNodeRef()
    {
        // set up version
        Version mockedVersion = mock(VersionImpl.class);
        NodeRef versionNodeRef = generateNodeRef();
        when(mockedVersion.getFrozenStateNodeRef())
            .thenReturn(versionNodeRef);

        // indicate that the version doesn't have the aspect
        when(mockedDbNodeService.hasAspect(versionNodeRef, RecordableVersionModel.ASPECT_RECORDED_VERSION))
            .thenReturn(true);
        
        // mark as destroyed
        recordableVersionService.destroyRecordedVersion(mockedVersion);
        
        // verify that the version was marked as destroyed
        verify(mockedDbNodeService)
            .setProperty(versionNodeRef, RecordableVersionModel.PROP_DESTROYED, Boolean.TRUE);      
        // and the reference to the version record was cleared
        verify(mockedDbNodeService)
            .setProperty(versionNodeRef, RecordableVersionModel.PROP_RECORD_NODE_REF, null);  
    }
}
