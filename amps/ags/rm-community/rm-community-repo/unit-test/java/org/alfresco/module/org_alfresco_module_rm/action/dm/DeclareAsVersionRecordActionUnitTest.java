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

package org.alfresco.module.org_alfresco_module_rm.action.dm;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.BaseActionUnitTest;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.extensions.webscripts.GUID;

/**
 * Declare as version record action unit test.
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public class DeclareAsVersionRecordActionUnitTest extends BaseActionUnitTest
{
    /** Sync Model */
    private static final String SYNC_MODEL_1_0_URI = "http://www.alfresco.org/model/sync/1.0";
    private static final QName ASPECT_SYNCED = QName.createQName(SYNC_MODEL_1_0_URI, "synced");
    
    /** actioned upon node reference */
    private NodeRef actionedUponNodeRef;

    /** destination record folder node reference */
    private NodeRef destinationRecordFolderNodeRef;

    /** parent destination node reference */
    private NodeRef parentDestinationNodeRef;
    
    /** declare as version record action */
    private @InjectMocks DeclareAsVersionRecordAction declareAsVersionRecordAction;

    @Mock
    private CapabilityService mockedCapabilityService;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest#before()
     */
    @Override
    public void before() throws Exception
    {
        super.before();

        // mocked action
        declareAsVersionRecordAction.setAuditable(false);
        
        // mocked actioned upon noderef
        actionedUponNodeRef = generateNodeRef();

        // mocked destination record folder nodeRef
        destinationRecordFolderNodeRef = generateNodeRef();

        // mocked parent destination nodeRef
        parentDestinationNodeRef = generateNodeRef();
    }
    
    /**
     * Given that the actioned upon node reference doesn't exist
     * When I execute the action
     * Then nothing happens
     */
    @Test
    public void actionedUponNodeRefDoesntExist()
    {
        doReturn(false).when(mockedNodeService).exists(actionedUponNodeRef);        
        declareAsVersionRecordAction.executeImpl(mock(Action.class), actionedUponNodeRef);        
        verify(mockedRecordableVersionService, never()).createRecordFromLatestVersion(filePlan, actionedUponNodeRef);
    }
    
    /**
     * Given that the actioned upon node reference isn't a subtype of cm:content
     * When I execute the action
     * Then nothing happens
     */
    @Test
    public void aciontedUponNodeRefIsntSubTypeOfCmContent()
    {
        doReturn(true).when(mockedNodeService).exists(actionedUponNodeRef);
        doReturn(false).when(mockedDictionaryService).isSubClass(any(QName.class), eq(ContentModel.TYPE_CONTENT));
        declareAsVersionRecordAction.executeImpl(mock(Action.class), actionedUponNodeRef);        
        verify(mockedRecordableVersionService, never()).createRecordFromLatestVersion(filePlan, actionedUponNodeRef);    
    }
     
    /**
     * Given that the actioned upon node reference doesn't have the versionable aspect applied
     * When I executed the action
     * Then nothing happens
     */
    @Test
    public void actionedUponNodeRefDoesntHaveVersionableApplied()
    {
        doReturn(true).when(mockedNodeService).exists(actionedUponNodeRef);
        doReturn(true).when(mockedDictionaryService).isSubClass(any(QName.class), eq(ContentModel.TYPE_CONTENT));
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_VERSIONABLE);
        declareAsVersionRecordAction.executeImpl(mock(Action.class), actionedUponNodeRef);        
        verify(mockedRecordableVersionService, never()).createRecordFromLatestVersion(filePlan, actionedUponNodeRef);   
    }
    
    /**
     * Given that the actioned upon node reference is already an record
     * When I execute the action
     * Then nothing happens
     */
    @Test
    public void actionedUponNodeRefAlreadyRecord()
    {
        doReturn(true).when(mockedNodeService).exists(actionedUponNodeRef);
        doReturn(true).when(mockedDictionaryService).isSubClass(any(QName.class), eq(ContentModel.TYPE_CONTENT));
        doReturn(true).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_VERSIONABLE); 
        doReturn(true).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD);
        declareAsVersionRecordAction.executeImpl(mock(Action.class), actionedUponNodeRef);        
        verify(mockedRecordableVersionService, never()).createRecordFromLatestVersion(filePlan, actionedUponNodeRef);
    }
    
    /** 
     * Given that the actioned upon node reference is a working copy
     * When I execute the action
     * Then nothing happens
     */
    @Test
    public void actionedUponNodeRefWorkingCopy()
    {
        doReturn(true).when(mockedNodeService).exists(actionedUponNodeRef);
        doReturn(true).when(mockedDictionaryService).isSubClass(any(QName.class), eq(ContentModel.TYPE_CONTENT));
        doReturn(true).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_VERSIONABLE); 
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD);
        doReturn(true).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_WORKING_COPY);
        declareAsVersionRecordAction.executeImpl(mock(Action.class), actionedUponNodeRef);        
        verify(mockedRecordableVersionService, never()).createRecordFromLatestVersion(filePlan, actionedUponNodeRef);
    }
    
    /** 
     * Given that the actioned upon node reference is a rejected record
     * When I execute the action
     * Then nothing happens
     */
    @Test
    public void actionedUponNodeRefRejectedRecord()
    {
        doReturn(true).when(mockedNodeService).exists(actionedUponNodeRef);
        doReturn(true).when(mockedDictionaryService).isSubClass(any(QName.class), eq(ContentModel.TYPE_CONTENT));
        doReturn(true).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_VERSIONABLE); 
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_WORKING_COPY);
        doReturn(true).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD_REJECTION_DETAILS);
        declareAsVersionRecordAction.executeImpl(mock(Action.class), actionedUponNodeRef);        
        verify(mockedRecordableVersionService, never()).createRecordFromLatestVersion(filePlan, actionedUponNodeRef);
    }
    
    /** 
     * Given that the actioned upon node reference is synced to the cloud
     * When I execute the action
     * Then nothing happens
     */
    @Test
    public void actionedUponNodeRefSynced()
    {
        doReturn(true).when(mockedNodeService).exists(actionedUponNodeRef);
        doReturn(true).when(mockedDictionaryService).isSubClass(any(QName.class), eq(ContentModel.TYPE_CONTENT));
        doReturn(true).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_VERSIONABLE); 
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_WORKING_COPY);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD_REJECTION_DETAILS);
        doReturn(true).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_SYNCED);
        declareAsVersionRecordAction.executeImpl(mock(Action.class), actionedUponNodeRef);        
        verify(mockedRecordableVersionService, never()).createRecordFromLatestVersion(filePlan, actionedUponNodeRef);
    }
    
    /** 
     * Given that no file plan is provided
     * And no default file plan exists
     * When I execute the action
     * Then an exception is thrown
     */
    @Test
    public void noFilePlanParameterNoDefaultFilePlan()
    {
        // setup
        setupMockedAspects();

        // no default file plan
        doReturn(null).when(mockedFilePlanService).getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
        
        // expect exception
        exception.expect(AlfrescoRuntimeException.class);
        
        // execute action
        declareAsVersionRecordAction.executeImpl(mock(Action.class), actionedUponNodeRef);                
    }

    /**
     * Given that no file plan is provided
     * And a default file plan exists
     * When I execute the action
     * Then a version record is declared
     */
    @Test
    public void noFilePlanParameterDefaultFilePlan()
    {
        // setup
        setupMockedAspects();

        // no default file plan
        doReturn(filePlan).when(mockedFilePlanService).getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);

        // execute action
        declareAsVersionRecordAction.executeImpl(mock(Action.class), actionedUponNodeRef);
        verify(mockedRecordableVersionService, times(1)).createRecordFromLatestVersion(filePlan, actionedUponNodeRef);
    }

    /** 
     * Given that a file plan is provided
     * And it isn't a file plan
     * When I execute the action
     * Then an exception is thrown
     */
    @Test
    public void invalidFilePlanParameter()
    {
        // setup
        setupMockedAspects();

        // not a file plan is provided in the parameters
        mockActionParameterValue(DeclareAsVersionRecordAction.PARAM_FILE_PLAN, generateNodeRef());
        
        // expect exception
        exception.expect(AlfrescoRuntimeException.class);
        
        // execute action
        declareAsVersionRecordAction.executeImpl(getMockedAction(), actionedUponNodeRef);                
    }
    
    /** 
     * Given that a file plan is provided
     * And it is a file plan
     * When I execute the action
     * Then a version record is declared
     */
    @Test
    public void validFilePlanParameter()
    {
        // setup
        setupMockedAspects();

        // not a file plan is provided in the parameters
        NodeRef myFilePlan = generateNodeRef(TYPE_FILE_PLAN);
        doReturn(true).when(mockedFilePlanService).isFilePlan(myFilePlan);
        mockActionParameterValue(DeclareAsVersionRecordAction.PARAM_FILE_PLAN, myFilePlan);
        
        // execute action
        declareAsVersionRecordAction.executeImpl(getMockedAction(), actionedUponNodeRef);   
        verify(mockedRecordableVersionService, times(1)).createRecordFromLatestVersion(myFilePlan, actionedUponNodeRef);               
    }

    /**
     * Given that a valid location is provided
     * When I execute the action
     * Then a version record is declared in the provided location
     */
    @Test
    public void validDestinationRecordFolderProvided()
    {
        String pathParameter = GUID.generate();
        // setup
        setupMockedAspects();

        mockActionParameterValue(DeclareAsVersionRecordAction.PARAM_PATH, pathParameter);

        // provided location
        doReturn(destinationRecordFolderNodeRef).when(mockedNodeService).getChildByName(filePlan, ContentModel.ASSOC_CONTAINS, pathParameter);
        doReturn(TYPE_RECORD_FOLDER).when(mockedNodeService).getType(destinationRecordFolderNodeRef);

        // capability check
        doReturn(true).when(mockedCapabilityService).hasCapability(destinationRecordFolderNodeRef, "FileVersionRecords");

        // file plan service
        doReturn(filePlan).when(mockedFilePlanService).getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);

        // execute action
        declareAsVersionRecordAction.executeImpl(getMockedAction(), actionedUponNodeRef);
        verify(mockedRecordableVersionService, times(1)).createRecordFromLatestVersion(destinationRecordFolderNodeRef, actionedUponNodeRef);
    }

    /**
     * Given that an invalid location is provided
     * When I execute the action
     * Then an exception is thrown
     */
    @Test
    public void invalidDestinationRecordFolderProvided()
    {
        String childName = GUID.generate();
        // setup
        setupMockedAspects();

        // provided location
        doReturn(destinationRecordFolderNodeRef).when(mockedNodeService).getChildByName(parentDestinationNodeRef, ContentModel.ASSOC_CONTAINS, childName);
        doReturn(TYPE_RECORD_FOLDER).when(mockedNodeService).getType(destinationRecordFolderNodeRef);

        // capability check
        doReturn(false).when(mockedCapabilityService).hasCapability(destinationRecordFolderNodeRef, "EditRecordMetadata");

        // expect exception
        exception.expect(AlfrescoRuntimeException.class);

        // execute action
        declareAsVersionRecordAction.executeImpl(getMockedAction(), actionedUponNodeRef);
    }

    private void setupMockedAspects()
    {
        doReturn(true).when(mockedNodeService).exists(actionedUponNodeRef);
        doReturn(true).when(mockedDictionaryService).isSubClass(nullable(QName.class), eq(ContentModel.TYPE_CONTENT));
        doReturn(true).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_VERSIONABLE);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_WORKING_COPY);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD_REJECTION_DETAILS);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_SYNCED);
    }
}
