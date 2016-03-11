package org.alfresco.module.org_alfresco_module_rm.action.impl;

import static org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock.generateText;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.BaseActionUnitTest;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Unit test for unlink from action 
 *
 * @author Roy Wetherall
 * @since 2.3
 */
public class UnlinkFromActionUnitTest extends BaseActionUnitTest
{
    private NodeRef record;
    private NodeRef recordFolder;
    
    @InjectMocks
    private UnlinkFromAction action;
    
    @Before
    @Override
    public void before() throws Exception
    {
        super.before();
        
        record = generateRecord();
        recordFolder = generateRecordFolder();
    }
    
    /**
     * Given the actioned upon node does not exist
     * When the action is executed
     * Then nothing happens
     */
    @Test
    public void nodeDoesNotExist()
    {
        doReturn(false).when(mockedNodeService).exists(record);
        action.executeImpl(mock(Action.class), record);
        verify(mockedRecordService, never()).unlink(any(NodeRef.class), any(NodeRef.class));
    }
    
    /** 
     * Given the actioned upon node is pending delete
     * When the action is executed
     * Then nothing happens
     */
    @Test
    public void nodePendingDelete()
    {
        doReturn(true).when(mockedNodeService).exists(record);
        doReturn(true).when(mockedNodeService).hasAspect(record, ASPECT_PENDING_DELETE);
        action.executeImpl(mock(Action.class), record);
        verify(mockedRecordService, never()).unlink(any(NodeRef.class), any(NodeRef.class));
    }
    
    /** 
     * Given that actioned upon node is not a record
     * When the action is executed
     * Then nothing happens
     */
    @Test
    public void nodeNotRecord()
    {
        NodeRef notRecord = generateCmContent(generateText());
        doReturn(true).when(mockedNodeService).exists(notRecord);
        doReturn(false).when(mockedNodeService).hasAspect(notRecord, ASPECT_PENDING_DELETE);
        action.executeImpl(mock(Action.class), notRecord);
        verify(mockedRecordService, never()).unlink(any(NodeRef.class), any(NodeRef.class));
    }
    
    /**
     * Given that the record folder parameter is not provided
     * When the action is executed
     * Then an exception is thrown
     */
    @Test(expected=AlfrescoRuntimeException.class)
    public void recordFolderParamMissing()
    {
        // setup record
        doReturn(true).when(mockedNodeService).exists(record);
        doReturn(false).when(mockedNodeService).hasAspect(record, ASPECT_PENDING_DELETE);
        
        // create action mock
        mockActionParameterValue(UnlinkFromAction.PARAM_RECORD_FOLDER, null);
        
        // execute action
        action.executeImpl(getMockedAction(), record);        
    }
    
    /**
     * Given that a valid record folder is provided
     * When the action is executed
     * Then the record is unlinked from the record folder
     */
    @Test
    public void validUnlink()
    {
        // setup record
        doReturn(true).when(mockedNodeService).exists(record);
        doReturn(false).when(mockedNodeService).hasAspect(record, ASPECT_PENDING_DELETE);
        
        // create action mock
        mockActionParameterValue(UnlinkFromAction.PARAM_RECORD_FOLDER, recordFolder.toString());
        
        // execute action
        action.executeImpl(getMockedAction(), record);     
        
        // verify unlink
        verify(mockedRecordService, times(1)).unlink(record, recordFolder);
    }

}
