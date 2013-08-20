/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transfer.reportd.XMLTransferDestinationReportWriter;
import org.alfresco.repo.transfer.requisite.XMLTransferRequsiteWriter;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.transfer.TransferCallback;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferEventBegin;
import org.alfresco.service.cmr.transfer.TransferEventCommittingStatus;
import org.alfresco.service.cmr.transfer.TransferEventEndState;
import org.alfresco.service.cmr.transfer.TransferEventEnterState;
import org.alfresco.service.cmr.transfer.TransferEventError;
import org.alfresco.service.cmr.transfer.TransferEventReport;
import org.alfresco.service.cmr.transfer.TransferEventSendingContent;
import org.alfresco.service.cmr.transfer.TransferEventSendingSnapshot;
import org.alfresco.service.cmr.transfer.TransferEventSuccess;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferFailureException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferService2;
import org.alfresco.service.cmr.transfer.TransferTarget;
import org.alfresco.service.cmr.transfer.TransferEvent.TransferState;
import org.alfresco.service.cmr.transfer.TransferProgress.Status;
import org.alfresco.service.cmr.transfer.TransferVersion;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;

public class TransferServiceCallbackTest extends TestCase
{
//    private static Log log = LogFactory.getLog(TransferServiceImplUnitTest.class);
    
    private static final String TRANSFER_TARGET_NAME = "TransferServiceImplUnitTest";
    
    private ApplicationContext applicationContext;
    private TransferServiceImpl2 transferServiceImpl;
    private AuthenticationComponent authenticationComponent;
    private TransferTransmitter mockedTransferTransmitter;
    private TransferService2 transferService;
    private DescriptorService descriptorService;
    private TransactionService transactionService;
    private UserTransaction tx;
    private Repository repository;
    private NodeRef companyHome;
    private FileFolderService fileFolderService;
    private NodeRef folder1;
    private NodeRef file1;
    private NodeRef file2;
    private NodeRef file3;
    private String file1ContentUrl;
    private String file2ContentUrl;
    private String file3ContentUrl;
    private TransferVersion version;
    
    private String localRepositoryId; 

    private TransferTarget target;

    private Transfer transfer;

    private TransferCallback mockedCallback;

    @Override
    protected void setUp() throws Exception
    {
        applicationContext = ApplicationContextHelper.getApplicationContext();

        // Get the required services
        descriptorService = (DescriptorService) this.applicationContext.getBean("DescriptorService");
        transferServiceImpl = (TransferServiceImpl2) this.applicationContext.getBean("transferService2");
        transferService = transferServiceImpl;
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        transactionService = (TransactionService) applicationContext.getBean("transactionComponent");
        repository = (Repository) applicationContext.getBean("repositoryHelper");
        fileFolderService = (FileFolderService) applicationContext.getBean("fileFolderService");
               
        localRepositoryId = descriptorService.getCurrentRepositoryDescriptor().getId();
        version =  new TransferVersionImpl(descriptorService.getServerDescriptor());
        
        mockedTransferTransmitter = mock(TransferTransmitter.class);
        mockedCallback = mock(TransferCallback.class);
        transferServiceImpl.setTransmitter(mockedTransferTransmitter);

        authenticationComponent.setCurrentUser("admin");
        
        tx = transactionService.getUserTransaction();
        tx.begin();
        target = createTransferTarget(TRANSFER_TARGET_NAME);
        tx.commit();
            
        tx = transactionService.getUserTransaction();
        tx.begin();
        
        transfer = new Transfer();
        transfer.setTransferTarget(target);
        transfer.setTransferId(GUID.generate());
        transfer.setToVersion(version);
        
        companyHome = repository.getCompanyHome();
        
        folder1 = fileFolderService.create(companyHome, "folder1", ContentModel.TYPE_FOLDER).getNodeRef();

        file1 = fileFolderService.create(folder1, "file1", ContentModel.TYPE_CONTENT).getNodeRef();
        fileFolderService.getWriter(file1).putContent("This is purely test content.");
        file1ContentUrl = fileFolderService.getFileInfo(file1).getContentData().getContentUrl();

        file2 = fileFolderService.create(folder1, "file2", ContentModel.TYPE_CONTENT).getNodeRef();
        fileFolderService.getWriter(file2).putContent("This is purely test content.");
        file2ContentUrl = fileFolderService.getFileInfo(file2).getContentData().getContentUrl();
        
        file3 = fileFolderService.create(folder1, "file3", ContentModel.TYPE_CONTENT).getNodeRef();
        fileFolderService.getWriter(file3).putContent("This is purely test content.");
        file3ContentUrl = fileFolderService.getFileInfo(file3).getContentData().getContentUrl();
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (tx.getStatus() == javax.transaction.Status.STATUS_ACTIVE)
        {
            tx.rollback();
        }
        authenticationComponent.clearCurrentSecurityContext();
        super.tearDown();
    }

    public void testCompleteSuccess()
    {
        TransferProgress status0 = new TransferProgress();
        status0.setStatus(Status.COMMIT_REQUESTED);
        status0.setCurrentPosition(0);
        status0.setEndPosition(0);
        
        TransferProgress status1 = new TransferProgress();
        status1.setStatus(Status.COMMITTING);
        status1.setCurrentPosition(0);
        status1.setEndPosition(4);
        
        TransferProgress status2 = new TransferProgress();
        status2.setStatus(Status.COMMITTING);
        status2.setCurrentPosition(3);
        status2.setEndPosition(4);
        
        TransferProgress status3 = new TransferProgress();
        status3.setStatus(Status.COMMITTING);
        status3.setCurrentPosition(5);
        status3.setEndPosition(8);
        
        TransferProgress status4 = new TransferProgress();
        status4.setStatus(Status.COMPLETE);
        status4.setCurrentPosition(8);
        status4.setEndPosition(8);
        
        TransferProgress[] statuses = new TransferProgress[] {status0, status1, status2, status3, status4};
        configureBasicMockTransmitter(statuses);
        when(mockedTransferTransmitter.begin(target, localRepositoryId, version)).thenReturn(transfer);

        TransferDefinition transferDef = new TransferDefinition();
        transferDef.setNodes(folder1, file1, file2, file3);
        transferService.transfer(TRANSFER_TARGET_NAME, transferDef, mockedCallback);

        List<TransferEvent> expectedEvents = new ArrayList<TransferEvent>();
        TransferEventImpl event;

        event = new TransferEventEnterState();
        event.setTransferState(TransferState.START);
        expectedEvents.add(event);
        
        event = new TransferEventBegin();
        event.setTransferState(TransferState.START);
        expectedEvents.add(event);
        
        event = new TransferEventEndState();
        event.setTransferState(TransferState.START);
        expectedEvents.add(event);
        
        event = new TransferEventEnterState();
        event.setTransferState(TransferState.SENDING_SNAPSHOT);
        expectedEvents.add(event);
        
        event = new TransferEventSendingSnapshot();
        event.setTransferState(TransferState.SENDING_SNAPSHOT);
        expectedEvents.add(event);
        
        event = new TransferEventEndState();
        event.setTransferState(TransferState.SENDING_SNAPSHOT);
        expectedEvents.add(event);
        
        event = new TransferEventEnterState();
        event.setTransferState(TransferState.SENDING_CONTENT);
        expectedEvents.add(event);
        
        event = new TransferEventSendingContent();
        event.setTransferState(TransferState.SENDING_CONTENT);
        expectedEvents.add(event);
        
        event = new TransferEventSendingContent();
        event.setTransferState(TransferState.SENDING_CONTENT);
        expectedEvents.add(event);
        
        event = new TransferEventSendingContent();
        event.setTransferState(TransferState.SENDING_CONTENT);
        expectedEvents.add(event);
        
        event = new TransferEventEndState();
        event.setTransferState(TransferState.SENDING_CONTENT);
        expectedEvents.add(event);
        
        event = new TransferEventEnterState();
        event.setTransferState(TransferState.PREPARING);
        expectedEvents.add(event);
        
        event = new TransferEventEndState();
        event.setTransferState(TransferState.PREPARING);
        expectedEvents.add(event);
        
        event = new TransferEventEnterState();
        event.setTransferState(TransferState.COMMITTING);
        expectedEvents.add(event);
        
        event = new TransferEventCommittingStatus();
        event.setTransferState(TransferState.COMMITTING);
        expectedEvents.add(event);
        
        event = new TransferEventCommittingStatus();
        event.setTransferState(TransferState.COMMITTING);
        expectedEvents.add(event);
        
        event = new TransferEventCommittingStatus();
        event.setTransferState(TransferState.COMMITTING);
        expectedEvents.add(event);
        
        event = new TransferEventCommittingStatus();
        event.setTransferState(TransferState.COMMITTING);
        expectedEvents.add(event);

        event = new TransferEventEndState();
        event.setTransferState(TransferState.COMMITTING);
        expectedEvents.add(event);
        
        event = new TransferEventEnterState();
        event.setTransferState(TransferState.SUCCESS);
        expectedEvents.add(event);
        
        event = new TransferEventReport();
        event.setTransferState(TransferState.SUCCESS);
        expectedEvents.add(event);
        
        event = new TransferEventReport();
        event.setTransferState(TransferState.SUCCESS);
        expectedEvents.add(event);

        event = new TransferEventSuccess();
        event.setTransferState(TransferState.SUCCESS);
        expectedEvents.add(event);
        
        verifyCallback(expectedEvents);
    }
    
    public void testErrorDuringCommit()
    {
        Exception error = new TransferException("Commit failed");
        
        TransferProgress status0 = new TransferProgress();
        status0.setStatus(Status.COMMIT_REQUESTED);
        status0.setCurrentPosition(0);
        status0.setEndPosition(0);
        
        TransferProgress status1 = new TransferProgress();
        status1.setStatus(Status.COMMITTING);
        status1.setCurrentPosition(0);
        status1.setEndPosition(4);
        
        TransferProgress status2 = new TransferProgress();
        status2.setStatus(Status.COMMITTING);
        status2.setCurrentPosition(3);
        status2.setEndPosition(4);
        
        TransferProgress status3 = new TransferProgress();
        status3.setStatus(Status.COMMITTING);
        status3.setCurrentPosition(5);
        status3.setEndPosition(8);
        
        TransferProgress status4 = new TransferProgress();
        status4.setStatus(Status.ERROR);
        status4.setCurrentPosition(8);
        status4.setEndPosition(8);
        status4.setError(error);
        
        TransferProgress[] statuses = new TransferProgress[] {status0, status1, status2, status3, status4};
        configureBasicMockTransmitter(statuses);
        when(mockedTransferTransmitter.begin(target, localRepositoryId, version)).thenReturn(transfer);

        TransferDefinition transferDef = new TransferDefinition();
        transferDef.setNodes(folder1, file1, file2, file3);
        try
        {
            transferService.transfer(TRANSFER_TARGET_NAME, transferDef, mockedCallback);
            fail();
        }
        catch (TransferFailureException ex)
        {
            List<TransferEvent> expectedEvents = new ArrayList<TransferEvent>();
            TransferEventImpl event;
    
            event = new TransferEventEnterState();
            event.setTransferState(TransferState.START);
            expectedEvents.add(event);
            
            event = new TransferEventBegin();
            event.setTransferState(TransferState.START);
            expectedEvents.add(event);
            
            event = new TransferEventEndState();
            event.setTransferState(TransferState.START);
            expectedEvents.add(event);
            
            event = new TransferEventEnterState();
            event.setTransferState(TransferState.SENDING_SNAPSHOT);
            expectedEvents.add(event);
            
            event = new TransferEventSendingSnapshot();
            event.setTransferState(TransferState.SENDING_SNAPSHOT);
            expectedEvents.add(event);
            
            event = new TransferEventEndState();
            event.setTransferState(TransferState.SENDING_SNAPSHOT);
            expectedEvents.add(event);
            
            event = new TransferEventEnterState();
            event.setTransferState(TransferState.SENDING_CONTENT);
            expectedEvents.add(event);
            
            event = new TransferEventSendingContent();
            event.setTransferState(TransferState.SENDING_CONTENT);
            expectedEvents.add(event);
            
            event = new TransferEventSendingContent();
            event.setTransferState(TransferState.SENDING_CONTENT);
            expectedEvents.add(event);
            
            event = new TransferEventSendingContent();
            event.setTransferState(TransferState.SENDING_CONTENT);
            expectedEvents.add(event);
            
            event = new TransferEventEndState();
            event.setTransferState(TransferState.SENDING_CONTENT);
            expectedEvents.add(event);
            
            event = new TransferEventEnterState();
            event.setTransferState(TransferState.PREPARING);
            expectedEvents.add(event);
            
            event = new TransferEventEndState();
            event.setTransferState(TransferState.PREPARING);
            expectedEvents.add(event);
            
            event = new TransferEventEnterState();
            event.setTransferState(TransferState.COMMITTING);
            expectedEvents.add(event);
            
            event = new TransferEventCommittingStatus();
            event.setTransferState(TransferState.COMMITTING);
            expectedEvents.add(event);
            
            event = new TransferEventCommittingStatus();
            event.setTransferState(TransferState.COMMITTING);
            expectedEvents.add(event);
            
            event = new TransferEventCommittingStatus();
            event.setTransferState(TransferState.COMMITTING);
            expectedEvents.add(event);

            event = new TransferEventEndState();
            event.setTransferState(TransferState.COMMITTING);
            expectedEvents.add(event);

            event = new TransferEventEnterState();
            event.setTransferState(TransferState.ERROR);
            expectedEvents.add(event);

            event = new TransferEventReport();
            event.setTransferState(TransferState.ERROR);
            expectedEvents.add(event);
            
            event = new TransferEventReport();
            event.setTransferState(TransferState.ERROR);
            expectedEvents.add(event);
    
            event = new TransferEventError();
            event.setTransferState(TransferState.ERROR);
            ((TransferEventError)event).setException(error);
            expectedEvents.add(event);
    
            verifyCallback(expectedEvents);
        }
    }
    
    public void testTargetAlreadyLocked()
    {
        configureBasicMockTransmitter(null);
        when(mockedTransferTransmitter.begin(target, "localRepositoryId", version)).thenThrow(new TransferException("Simulate lock unavailable"));
        
        TransferDefinition transferDef = new TransferDefinition();
        transferDef.setNodes(folder1, file1, file2, file3);
        try
        {
            transferService.transfer(TRANSFER_TARGET_NAME, transferDef, mockedCallback);
            fail("Transfer expected to throw an exception, but it didn't.");
        }
        catch(TransferFailureException ex)
        {
            List<TransferEvent> expectedEvents = new ArrayList<TransferEvent>();
            TransferEventImpl event;

            event = new TransferEventEnterState();
            event.setTransferState(TransferState.START);
            expectedEvents.add(event);

            event = new TransferEventEndState();
            event.setTransferState(TransferState.START);
            expectedEvents.add(event);

            event = new TransferEventEnterState();
            event.setTransferState(TransferState.ERROR);
            expectedEvents.add(event);
            
            event = new TransferEventReport();
            event.setTransferState(TransferState.ERROR);
            expectedEvents.add(event);

            event = new TransferEventError();
            event.setTransferState(TransferState.ERROR);
            ((TransferEventError)event).setException((Exception)ex.getCause());
            expectedEvents.add(event);
            
            verifyCallback(expectedEvents);
        }
    }

    public void testSendContentFailed()
    {
        TransferProgress status0 = new TransferProgress();
        status0.setStatus(Status.CANCELLED);
        status0.setCurrentPosition(0);
        status0.setEndPosition(0);
        TransferProgress[] statuses = new TransferProgress[] {status0};
        configureBasicMockTransmitter(statuses);
        when(mockedTransferTransmitter.begin(target, localRepositoryId, version)).thenReturn(transfer);
        doThrow(new TransferException("Simulate failure to write content")).when(mockedTransferTransmitter).sendManifest(any(Transfer.class), any(File.class), any(OutputStream.class));
        when(mockedTransferTransmitter.getStatus(transfer)).thenReturn(statuses[0]);
        
        TransferDefinition transferDef = new TransferDefinition();
        transferDef.setNodes(folder1, file1, file2, file3);
        try
        {
            transferService.transfer(TRANSFER_TARGET_NAME, transferDef, mockedCallback);
            fail("Transfer expected to throw an exception, but it didn't.");
        }
        catch(TransferFailureException ex)
        {
            List<TransferEvent> expectedEvents = new ArrayList<TransferEvent>();
            TransferEventImpl event;

            event = new TransferEventEnterState();
            event.setTransferState(TransferState.START);
            expectedEvents.add(event);
            
            event = new TransferEventBegin();
            event.setTransferState(TransferState.START);
            expectedEvents.add(event);
            
            event = new TransferEventEndState();
            event.setTransferState(TransferState.START);
            expectedEvents.add(event);
            
            event = new TransferEventEnterState();
            event.setTransferState(TransferState.SENDING_SNAPSHOT);
            expectedEvents.add(event);
            
            event = new TransferEventSendingSnapshot();
            event.setTransferState(TransferState.SENDING_SNAPSHOT);
            expectedEvents.add(event);
            
            event = new TransferEventEndState();
            event.setTransferState(TransferState.SENDING_SNAPSHOT);
            expectedEvents.add(event);
            
            event = new TransferEventEnterState();
            event.setTransferState(TransferState.ERROR);
            expectedEvents.add(event);
            
            event = new TransferEventReport();
            event.setTransferState(TransferState.ERROR);
            expectedEvents.add(event);

            event = new TransferEventReport();
            event.setTransferState(TransferState.ERROR);
            expectedEvents.add(event);

            event = new TransferEventError();
            event.setTransferState(TransferState.ERROR);
            ((TransferEventError)event).setException((Exception)ex.getCause());
            expectedEvents.add(event);
            
            verifyCallback(expectedEvents);
        }
    }

    private void verifyCallback(List<TransferEvent> expectedEvents)
    {
        ArgumentCaptor<TransferEvent> eventCaptor = ArgumentCaptor.forClass(TransferEvent.class);
        verify(mockedCallback, atLeastOnce()).processEvent(eventCaptor.capture());
        List<TransferEvent> capturedEvents = eventCaptor.getAllValues();
        assertEquals(expectedEvents.size(), capturedEvents.size());
        int count = capturedEvents.size();
        for (int index = 0; index < count; ++index)
        {
            TransferEvent expectedEvent = expectedEvents.get(index);
            TransferEvent capturedEvent = capturedEvents.get(index);
            assertEquals("Event " + index, expectedEvent.getClass(), capturedEvent.getClass());
            assertEquals("Event " + index, expectedEvent.getTransferState(), capturedEvent.getTransferState());
            if (TransferEventError.class.isAssignableFrom(expectedEvent.getClass()))
            {
                assertEquals(((TransferEventError)expectedEvent).getException(), 
                        ((TransferEventError)capturedEvent).getException());
            }
        }
    }

    private void configureBasicMockTransmitter(TransferProgress[] statuses)
    {
        doAnswer(new Answer<Object>()
                {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable
                    {
                        OutputStream os = (OutputStream)invocation.getArguments()[2];
                        Writer writer = new OutputStreamWriter(os); 
                        XMLTransferRequsiteWriter requisiteWriter = new XMLTransferRequsiteWriter(writer);
                        requisiteWriter.startTransferRequsite();
                        requisiteWriter.missingContent(file1, ContentModel.PROP_CONTENT, 
                                TransferCommons.URLToPartName(file1ContentUrl));
                        requisiteWriter.missingContent(file2, ContentModel.PROP_CONTENT, 
                                TransferCommons.URLToPartName(file2ContentUrl));
                        requisiteWriter.missingContent(file3, ContentModel.PROP_CONTENT, 
                                TransferCommons.URLToPartName(file3ContentUrl));
                        requisiteWriter.endTransferRequsite();
                        writer.flush();
                        writer.close();
                        return null;
                    }
                }).when(mockedTransferTransmitter).sendManifest(any(Transfer.class), any(File.class), any(OutputStream.class));
                
        doAnswer(new Answer<Object>()
                {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable
                    {
                        OutputStream os = (OutputStream)invocation.getArguments()[1];
                        Writer writer = new OutputStreamWriter(os); 
                        XMLTransferDestinationReportWriter reportWriter = new XMLTransferDestinationReportWriter();
                        reportWriter.startTransferReport("UTF-8", writer);
                        reportWriter.writeComment("This is a comment");
                        reportWriter.writeChangeState("COMMITTING");
                        reportWriter.writeCreated(file1, file1, folder1, "");
                        reportWriter.writeDeleted(file3, file3, "");
                        reportWriter.writeMoved(file2, file2, "", folder1, "");
                        reportWriter.endTransferReport();
                        return null;
                    }
                }).when(mockedTransferTransmitter).getTransferReport(any(Transfer.class), any(OutputStream.class));
            
        if (statuses != null)
        {
            if (statuses.length > 1)
            {
                when(mockedTransferTransmitter.getStatus(transfer)).
                        thenReturn(statuses[0], Arrays.copyOfRange(statuses, 1, statuses.length));
            }
            else if (statuses.length == 1)
            {
                when(mockedTransferTransmitter.getStatus(transfer)).
                        thenReturn(statuses[0]);
            }
        }
    }

    private TransferTarget createTransferTarget(String name)
    {
        TransferTarget target;
        try
        {
            target = transferService.getTransferTarget(name);
        }
        catch(TransferException ex)
        {
            String title = "title";
            String description = "description";
            String endpointProtocol = "http";
            String endpointHost = "host";
            int endpointPort = 8080;
            String endpointPath = "/alfresco/service/api/transfer";
            String username = "user";
            char[] password = "password".toCharArray();

            /**
             * Now go ahead and create our first transfer target
             */
            target = transferService.createAndSaveTransferTarget(name, title, description, endpointProtocol,
                    endpointHost, endpointPort, endpointPath, username, password);
        }
        return target;
    }

}
