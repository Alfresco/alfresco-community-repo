/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.BaseActionUnitTest;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.mockito.InjectMocks;

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
    
    /** declare as version record action */
    private @InjectMocks DeclareAsVersionRecordAction declareAsVersionRecordAction;
    
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
        doReturn(true).when(mockedNodeService).exists(actionedUponNodeRef);
        doReturn(true).when(mockedDictionaryService).isSubClass(any(QName.class), eq(ContentModel.TYPE_CONTENT));
        doReturn(true).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_VERSIONABLE); 
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_WORKING_COPY);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD_REJECTION_DETAILS);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_SYNCED);
      
        // no default file plan
        doReturn(null).when(mockedFilePlanService).getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
        
        // expect exception
        exception.expect(AlfrescoRuntimeException.class);
        
        // exceute action
        declareAsVersionRecordAction.executeImpl(mock(Action.class), actionedUponNodeRef);                
    }
    
    /** 
     * Given that no file plan is provided
     * And adefault file plan exists
     * When I execute the action
     * Then a version record is declared
     */
    @Test
    public void noFilePlanParameterDefaultFilePlan()
    {
        // setup
        doReturn(true).when(mockedNodeService).exists(actionedUponNodeRef);
        doReturn(true).when(mockedDictionaryService).isSubClass(any(QName.class), eq(ContentModel.TYPE_CONTENT));
        doReturn(true).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_VERSIONABLE); 
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_WORKING_COPY);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD_REJECTION_DETAILS);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_SYNCED);
      
        // no default file plan
        doReturn(filePlan).when(mockedFilePlanService).getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
        
        // exceute action
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
        doReturn(true).when(mockedNodeService).exists(actionedUponNodeRef);
        doReturn(true).when(mockedDictionaryService).isSubClass(any(QName.class), eq(ContentModel.TYPE_CONTENT));
        doReturn(true).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_VERSIONABLE); 
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_WORKING_COPY);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD_REJECTION_DETAILS);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_SYNCED);
      
        // not a file plan is provided in the parameters
        mockActionParameterValue(DeclareAsVersionRecordAction.PARAM_FILE_PLAN, generateNodeRef());
        
        // expect exception
        exception.expect(AlfrescoRuntimeException.class);
        
        // exceute action
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
        doReturn(true).when(mockedNodeService).exists(actionedUponNodeRef);
        doReturn(true).when(mockedDictionaryService).isSubClass(any(QName.class), eq(ContentModel.TYPE_CONTENT));
        doReturn(true).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_VERSIONABLE); 
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ContentModel.ASPECT_WORKING_COPY);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_RECORD_REJECTION_DETAILS);
        doReturn(false).when(mockedNodeService).hasAspect(actionedUponNodeRef, ASPECT_SYNCED);
      
        // not a file plan is provided in the parameters
        NodeRef myFilePlan = generateNodeRef(TYPE_FILE_PLAN);
        doReturn(true).when(mockedFilePlanService).isFilePlan(myFilePlan);
        mockActionParameterValue(DeclareAsVersionRecordAction.PARAM_FILE_PLAN, myFilePlan);
        
        // exceute action
        declareAsVersionRecordAction.executeImpl(getMockedAction(), actionedUponNodeRef);   
        verify(mockedRecordableVersionService, times(1)).createRecordFromLatestVersion(myFilePlan, actionedUponNodeRef);               
    }
}
