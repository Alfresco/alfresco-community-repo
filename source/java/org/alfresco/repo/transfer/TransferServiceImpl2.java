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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.UserTransaction;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.repo.transfer.manifest.TransferManifestNode;
import org.alfresco.repo.transfer.manifest.TransferManifestNodeFactory;
import org.alfresco.repo.transfer.manifest.TransferManifestNodeHelper;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.repo.transfer.manifest.TransferManifestProcessor;
import org.alfresco.repo.transfer.manifest.TransferManifestWriter;
import org.alfresco.repo.transfer.manifest.XMLTransferManifestReader;
import org.alfresco.repo.transfer.manifest.XMLTransferManifestWriter;
import org.alfresco.repo.transfer.report.TransferReporter;
import org.alfresco.repo.transfer.requisite.DeltaListRequsiteProcessor;
import org.alfresco.repo.transfer.requisite.XMLTransferRequsiteReader;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.transfer.TransferCallback;
import org.alfresco.service.cmr.transfer.TransferCancelledException;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferEndEvent;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferEventCancelled;
import org.alfresco.service.cmr.transfer.TransferEventError;
import org.alfresco.service.cmr.transfer.TransferEventReport;
import org.alfresco.service.cmr.transfer.TransferEventSuccess;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferFailureException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferService2;
import org.alfresco.service.cmr.transfer.TransferTarget;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;


/**
 * Implementation of the Transfer Service.
 * 
 * @author Mark Rogers
 *
 */
public class TransferServiceImpl2 implements TransferService2
{
    private static Log logger = LogFactory.getLog(TransferServiceImpl2.class);

    private static final String MSG_NO_HOME = "transfer_service.unable_to_find_transfer_home";
    private static final String MSG_NO_GROUP = "transfer_service.unable_to_find_transfer_group";
    private static final String MSG_NO_TARGET = "transfer_service.unable_to_find_transfer_target";
    private static final String MSG_ERR_TRANSFER_ASYNC = "transfer_service.unable_to_transfer_async";
    private static final String MSG_TARGET_EXISTS = "transfer_service.target_exists";
    private static final String MSG_NO_NODES = "transfer_service.no_nodes";
    private static final String MSG_MISSING_ENDPOINT_PATH = "transfer_service.missing_endpoint_path";
    private static final String MSG_MISSING_ENDPOINT_PROTOCOL = "transfer_service.missing_endpoint_protocol";
    private static final String MSG_MISSING_ENDPOINT_HOST = "transfer_service.missing_endpoint_host";
    private static final String MSG_MISSING_ENDPOINT_PORT = "transfer_service.missing_endpoint_port";
    private static final String MSG_MISSING_ENDPOINT_USERNAME = "transfer_service.missing_endpoint_username";
    private static final String MSG_MISSING_ENDPOINT_PASSWORD = "transfer_service.missing_endpoint_password";
    private static final String MSG_FAILED_TO_GET_TRANSFER_STATUS = "transfer_service.failed_to_get_transfer_status";
    private static final String MSG_TARGET_ERROR = "transfer_service.target_error";
    private static final String MSG_UNKNOWN_TARGET_ERROR = "transfer_service.unknown_target_error";
    private static final String MSG_TARGET_NOT_ENABLED = "transfer_service.target_not_enabled";
    
    private static final String FILE_DIRECTORY = "transfer";
    private static final String FILE_SUFFIX = ".xml";
    
    private enum ClientTransferState { Begin, Prepare, Commit, Poll, Cancel, Finished, Exit; }; 
    
    /**
     * The synchronised list of transfers in progress.
     */
    private Map<String, TransferStatus> transferMonitoring = Collections.synchronizedMap(new TreeMap<String,TransferStatus>());
    
    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "searchService", getSearchService());
        PropertyCheck.mandatory(this, "transferSpaceQuery", transferSpaceQuery);
        PropertyCheck.mandatory(this, "defaultTransferGroup", defaultTransferGroup);
        PropertyCheck.mandatory(this, "transmitter", transmitter);
        PropertyCheck.mandatory(this, "namespaceResolver", transmitter);
        PropertyCheck.mandatory(this, "actionService", actionService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "descriptorService", descriptorService);
    }
    
    private String transferSpaceQuery; 
    private String defaultTransferGroup;
    private NodeService nodeService;
    private SearchService searchService;
    private TransferTransmitter transmitter;
    private TransactionService transactionService;
    private ActionService actionService;
    private TransferManifestNodeFactory transferManifestNodeFactory;
    private TransferReporter transferReporter;
    private TenantService tenantService;
    private DescriptorService descriptorService;
    
    /**
     * How long to delay while polling for commit status.
     */
    private long commitPollDelay = 2000;
    
    /**
     * Create a new in memory transfer target
     */
    public TransferTarget createTransferTarget(String name)
    {
        NodeRef dummy = lookupTransferTarget(name);
        if(dummy != null)
        {
            throw new TransferException(MSG_TARGET_EXISTS, new Object[]{name} );
        }
        
        TransferTargetImpl newTarget = new TransferTargetImpl();
        newTarget.setName(name);
        return newTarget;
    }
    
    /**
     * create transfer target
     */
    public TransferTarget createAndSaveTransferTarget(String name, String title, String description, String endpointProtocol, String endpointHost, int endpointPort, String endpointPath, String username, char[] password)
    {
        TransferTargetImpl newTarget = new TransferTargetImpl();
        newTarget.setName(name);
        newTarget.setTitle(title);
        newTarget.setDescription(description);
        newTarget.setEndpointProtocol(endpointProtocol);
        newTarget.setEndpointHost(endpointHost);
        newTarget.setEndpointPort(endpointPort);
        newTarget.setEndpointPath(endpointPath);
        newTarget.setUsername(username);
        newTarget.setPassword(password);
        return createTransferTarget(newTarget);
        
    }
    
    /**
     * create transfer target
     */
    private TransferTarget createTransferTarget(TransferTarget newTarget)
    {
        /**
         * Check whether name is already used
         */
        NodeRef dummy = lookupTransferTarget(newTarget.getName());
        if (dummy != null) { throw new TransferException(MSG_TARGET_EXISTS,
                    new Object[] { newTarget.getName() }); }

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

        // type properties
        properties.put(TransferModel.PROP_ENDPOINT_HOST, newTarget.getEndpointHost());
        properties.put(TransferModel.PROP_ENDPOINT_PORT, newTarget.getEndpointPort());
        properties.put(TransferModel.PROP_ENDPOINT_PROTOCOL, newTarget.getEndpointProtocol());
        properties.put(TransferModel.PROP_ENDPOINT_PATH, newTarget.getEndpointPath());
        properties.put(TransferModel.PROP_USERNAME, newTarget.getUsername());
        properties.put(TransferModel.PROP_PASSWORD, new String(encrypt(newTarget.getPassword())));

        // titled aspect
        properties.put(ContentModel.PROP_TITLE, newTarget.getTitle());
        properties.put(ContentModel.PROP_NAME, newTarget.getName());
        properties.put(ContentModel.PROP_DESCRIPTION, newTarget.getDescription());

        // enableable aspect
        properties.put(TransferModel.PROP_ENABLED, Boolean.TRUE);

        NodeRef home = getTransferHome();

        /**
         * Work out which group the transfer target is for, in this case the
         * default group.
         */
        NodeRef defaultGroup = nodeService.getChildByName(home, ContentModel.ASSOC_CONTAINS,
                    defaultTransferGroup);

        /**
         * Go ahead and create the new node
         */
        ChildAssociationRef ref = nodeService.createNode(defaultGroup, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(TransferModel.TRANSFER_MODEL_1_0_URI, newTarget.getName()),
                    TransferModel.TYPE_TRANSFER_TARGET, properties);

        /**
         * Now create a new TransferTarget object to return to the caller.
         */
        TransferTargetImpl retVal = new TransferTargetImpl();
        mapTransferTarget(ref.getChildRef(), retVal);

        return retVal;
    }

    /**
     * Get all transfer targets
     */
    public Set<TransferTarget> getTransferTargets()
    {
        NodeRef home = getTransferHome();
        
        Set<TransferTarget> ret = new HashSet<TransferTarget>();
        
        // get all groups
        List<ChildAssociationRef> groups = nodeService.getChildAssocs(home);
        
        // for each group
        for(ChildAssociationRef group : groups)
        {
            NodeRef groupNode = group.getChildRef();
            ret.addAll(getTransferTargets(groupNode));
        }
          
        return ret;
    }

    /**
     * Get all transfer targets in the specified group
     */
    public Set<TransferTarget> getTransferTargets(String groupName)
    {
        NodeRef home = getTransferHome();
        
        // get group with assoc groupName
        NodeRef groupNode = nodeService.getChildByName(home, ContentModel.ASSOC_CONTAINS, groupName);
        
        if(groupNode == null)
        {
            // No transfer group.
            throw new TransferException(MSG_NO_GROUP, new Object[]{groupName});
        }
        
        return getTransferTargets(groupNode);
    }
    
    /**
     * Given the noderef of a group of transfer targets, return all the contained transfer targets.
     * @param groupNode
     * @return
     */
    private Set<TransferTarget> getTransferTargets(NodeRef groupNode)
    {
        Set<TransferTarget> result = new HashSet<TransferTarget>();
        List<ChildAssociationRef>children = nodeService.getChildAssocs(groupNode, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        
        for(ChildAssociationRef child : children)
        {
            if(nodeService.getType(child.getChildRef()).equals(TransferModel.TYPE_TRANSFER_TARGET))
            {
                TransferTargetImpl newTarget = new TransferTargetImpl();
                mapTransferTarget(child.getChildRef(), newTarget);
                result.add(newTarget);
            }
        }
        return result;
    }

    /**
     * 
     */
    public void deleteTransferTarget(String name)
    {
        NodeRef nodeRef = lookupTransferTarget(name);
        
        if(nodeRef == null)
        {
            // target does not exist
            throw new TransferException(MSG_NO_TARGET, new Object[]{name} );
        }
        nodeService.deleteNode(nodeRef);
    }
    
    /**
     * Enables/Disables the named transfer target
     */
    public void enableTransferTarget(String name, boolean enable)
    {
        NodeRef nodeRef = lookupTransferTarget(name);
        nodeService.setProperty(nodeRef, TransferModel.PROP_ENABLED, new Boolean(enable));     
    }
    
    public boolean targetExists(String name)
    {
        return (lookupTransferTarget(name) != null);
    }

    /**
     * 
     */
    public TransferTarget getTransferTarget(String name)
    {
        NodeRef nodeRef = lookupTransferTarget(name);
        
        if(nodeRef == null)
        {
            // target does not exist
            throw new TransferException(MSG_NO_TARGET, new Object[]{name} );
        }
        TransferTargetImpl newTarget = new TransferTargetImpl();
        mapTransferTarget(nodeRef, newTarget);
        
        return newTarget;
    }

    /**
     * create or update a transfer target.
     */
    public TransferTarget saveTransferTarget(TransferTarget update)
    {  
        if(update.getNodeRef() == null)
        {
            // This is a save for the first time
            return createTransferTarget(update);
        }
        
        NodeRef nodeRef = lookupTransferTarget(update.getName());
        if(nodeRef == null)
        {
            // target does not exist
            throw new TransferException(MSG_NO_TARGET, new Object[]{update.getName()} );
        }
        
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(TransferModel.PROP_ENDPOINT_HOST, update.getEndpointHost());
        properties.put(TransferModel.PROP_ENDPOINT_PORT, update.getEndpointPort());
        properties.put(TransferModel.PROP_ENDPOINT_PROTOCOL, update.getEndpointProtocol());
        properties.put(TransferModel.PROP_ENDPOINT_PATH, update.getEndpointPath());
        properties.put(TransferModel.PROP_USERNAME, update.getUsername());
        properties.put(TransferModel.PROP_PASSWORD, new String(encrypt(update.getPassword())));
        
        // titled aspect
        properties.put(ContentModel.PROP_TITLE, update.getTitle());
        properties.put(ContentModel.PROP_NAME, update.getName());
        properties.put(ContentModel.PROP_DESCRIPTION, update.getDescription());
        
        properties.put(TransferModel.PROP_ENABLED, new Boolean(update.isEnabled()));
        nodeService.setProperties(nodeRef, properties);
        
        TransferTargetImpl newTarget = new TransferTargetImpl();
        mapTransferTarget(nodeRef, newTarget);
        return newTarget;
    }
    
    /**
     * Transfer async.
     * 
     * @param targetName
     * @param definition
     * @param callbacks
     * 
     */
    public void transferAsync(String targetName, TransferDefinition definition, TransferCallback... callbacks)
    {
        transferAsync(targetName, definition, Arrays.asList(callbacks));
    }
    
    /**
     * Transfer async.
     * 
     * @param targetName
     * @param definition
     * @param callbacks
     * 
     */
    public void transferAsync(String targetName, TransferDefinition definition, Collection<TransferCallback> callbacks)
    {
        /**
         * Event processor for this transfer instance
         */
        final TransferEventProcessor eventProcessor = new TransferEventProcessor();
        if(callbacks != null)
        {
            eventProcessor.observers.addAll(callbacks);
        }
        
        /*
         * Note:
         * callback should be Serializable to be passed through the action API
         * However Serializable is not used so it does not matter.   Perhaps the action API should be 
         * changed?  Or we could add a Serializable proxy here.
         */ 
       
       Map<String, Serializable> params = new HashMap<String, Serializable>();
       params.put("targetName", targetName);
       params.put("definition", definition);
       params.put("callbacks", (Serializable)callbacks);
       
       Action transferAction = getActionService().createAction("transfer-async", params); 
       
       /**
        * Execute transfer async in its own transaction.
        * The action service only runs actions in the post commit which is why there's
        * a separate transaction here.
        */
       boolean success = false;
       UserTransaction trx = transactionService.getNonPropagatingUserTransaction();
       try
       {
           trx.begin();
           logger.debug("calling action service to execute action");
           getActionService().executeAction(transferAction, null, false, true);
           trx.commit();   
           logger.debug("committed successfully");
           success = true;
       }
       catch (Exception error)
       {
           logger.error("unexpected exception", error);
           throw new AlfrescoRuntimeException(MSG_ERR_TRANSFER_ASYNC, error);
       }
       finally
       {
           if(!success)
           {
               try
               {
                   logger.debug("rolling back after error");
                    trx.rollback();
               }
               catch (Exception error)
               {
                   logger.error("unexpected exception during rollback", error);
                   // There's nothing much we can do here
               }
           }
       }
    }
    
    /**
     * Transfer Synchronous
     * 
     * @param targetName
     * @param definition
     * @param callbacks
     */
    public TransferEndEvent transfer(String targetName, TransferDefinition definition, TransferCallback... callbacks)
        throws TransferFailureException
    {
        return transfer(targetName, definition, Arrays.asList(callbacks));
    }

    /**
     * Transfer Synchronous
     * 
     * @param targetName
     * @param definition
     * @param callbacks
     */
    public TransferEndEvent transfer(String targetName, TransferDefinition definition, Collection<TransferCallback> callbacks)
        throws TransferFailureException
    {
        /**
         * Event processor for this transfer instance
         */
        final TransferEventProcessor eventProcessor = new TransferEventProcessor();
        if(callbacks != null)
        {
            eventProcessor.observers.addAll(callbacks);
        }
        
        /**
         * Now go ahead and do the transfer
         */
        return transferImpl(targetName, definition, eventProcessor);
    }
    
    private TransferEndEvent transferImpl(String targetName, final TransferDefinition definition, final TransferEventProcessor eventProcessor)
        throws TransferFailureException
    {        
        if(logger.isDebugEnabled())
        {
            logger.debug("transfer started to :" + targetName);
        }

        // transfer end event
        TransferEndEvent endEvent = null;
        Exception failureException = null;
        TransferTarget target = null;
        Transfer transfer = null;
        final List<TransferEvent> transferReportEvents = new LinkedList<TransferEvent>();
        NodeRef sourceReport = null;
        NodeRef destinationReport = null;
        File manifest = null;
        File requisite = null;
        int pollRetries = 0;
        int pollPosition = -1;
        boolean cancelled = false;
        
        final String localRepositoryId = getDescriptorService().getCurrentRepositoryDescriptor().getId();

        // Wire in the transferReport - so any callbacks are stored in transferReport
        TransferCallback reportCallback = new TransferCallback()
        {
            private static final long serialVersionUID = 4072579605731036522L;

            public void processEvent(TransferEvent event)
            {
                transferReportEvents.add(event);
            } 
        };
        eventProcessor.addObserver(reportCallback);

        // start transfer
        ClientTransferState clientState = ClientTransferState.Begin;
        while(clientState != ClientTransferState.Exit)
        {
            try
            {
                switch (clientState)
                {
                    case Begin:
                    {
                        eventProcessor.start();
                        manifest = createManifest(definition);
                        logger.debug("transfer begin");
                        target = getTransferTarget(targetName);
                        checkTargetEnabled(target);
                        transfer = transmitter.begin(target, localRepositoryId);
                        String transferId = transfer.getTransferId();
                        TransferStatus status = new TransferStatus();
                        transferMonitoring.put(transferId, status);
                        logger.debug("transfer begun transferId:" + transferId);
                        eventProcessor.begin(transferId);
                        checkCancel(transferId);
                        
                        // next state
                        clientState = ClientTransferState.Prepare;
                        break;
                    }
                    
                    case Prepare:
                    {
                        // send Manifest, get the requsite back.
                        eventProcessor.sendSnapshot(1,1);
                        
                        requisite = createRequisiteFile();
                        FileOutputStream reqOutput = new FileOutputStream(requisite);
                        transmitter.sendManifest(transfer, manifest, reqOutput);
                        logger.debug("manifest sent");
                        checkCancel(transfer.getTransferId());

                        if(logger.isDebugEnabled())
                        {
                            logger.debug("requisite file written to local filesystem");
                            try
                            {
                                outputFile(requisite);
                            }
                            catch (IOException error)
                            {
                                // This is debug code - so an exception thrown while debugging
                                logger.debug("error while outputting snapshotFile");
                                error.printStackTrace();
                            }
                        }
                        
                        sendContent(transfer, definition, eventProcessor, manifest, requisite);
                        logger.debug("content sending finished");
                        checkCancel(transfer.getTransferId());

                        // prepare
                        eventProcessor.prepare();
                        transmitter.prepare(transfer);
                        checkCancel(transfer.getTransferId());

                        // next state
                        clientState = ClientTransferState.Commit;
                        break;
                    }

                    case Commit:
                    {
                        logger.debug("about to start committing transferId:" + transfer.getTransferId());
                        eventProcessor.commit();
                        transmitter.commit(transfer);
                      
                        logger.debug("committing transferId:" + transfer.getTransferId());
                        checkCancel(transfer.getTransferId());

                        // next state
                        clientState = ClientTransferState.Poll;
                        break;
                    }
                    
                    case Poll:
                    {
                        TransferProgress progress = null;
                        try
                        {
                            progress = transmitter.getStatus(transfer);
                            
                            // reset retries for next poll
                            pollRetries = 0;
                        }
                        catch(TransferException e)
                        {
                            pollRetries++;
                            if (pollRetries == 3)
                            {
                                throw new TransferException(MSG_FAILED_TO_GET_TRANSFER_STATUS, new Object[] {target.getName()});
                            }
                        }

                        // check status
                        if (progress.getStatus() == TransferProgress.Status.ERROR)
                        {
                            Throwable targetError = progress.getError();
                            // NOTE: it's possible the error is not returned from pre v3.4 target repositories
                            if (targetError == null)
                            {
                                targetError = new TransferException(MSG_UNKNOWN_TARGET_ERROR);
                            }
                            if (Exception.class.isAssignableFrom(targetError.getClass()))
                            {
                                failureException = (Exception)targetError;
                            }
                            else
                            {
                                failureException = new TransferException(MSG_TARGET_ERROR, new Object[] {targetError.getMessage()}, targetError);
                            }
                            clientState = ClientTransferState.Finished;
                            break;
                        }
                        else if (progress.getStatus() == TransferProgress.Status.CANCELLED)
                        {
                            cancelled = true;
                            clientState = ClientTransferState.Finished;
                            break;
                        }
                        
                        // notify transfer progress
                        if (progress.getCurrentPosition() != pollPosition)
                        {
                            pollPosition = progress.getCurrentPosition();
                            logger.debug("committing :" + pollPosition);
                            eventProcessor.committing(progress.getEndPosition(), pollPosition);
                        }
                        
                        if (progress.getStatus() == TransferProgress.Status.COMPLETE)
                        {
                            clientState = ClientTransferState.Finished;
                            break;
                        }
                        
                        checkCancel(transfer.getTransferId());

                        // NOTE: stay in poll state...
                        // sleep before next poll
                        try
                        {
                            Thread.sleep(commitPollDelay);
                        }
                        catch (InterruptedException e)
                        {
                            // carry on
                        }
                        break;
                    }
                    
                    case Cancel:
                    {
                        logger.debug("Abort - waiting for target confirmation of cancel");
                        transmitter.abort(transfer);
                        
                        // next state... poll for confirmation of cancel from target
                        clientState = ClientTransferState.Poll;
                        break;
                    }
                    
                    case Finished:
                    {
                        try
                        {
                            TransferEndEventImpl endEventImpl = null;
                            String reportName = null;

                            try
                            {
                                if (failureException != null)
                                {
                                    logger.debug("TransferException - unable to transfer", failureException);
                                    TransferEventError errorEvent = new TransferEventError();
                                    errorEvent.setTransferState(TransferEvent.TransferState.ERROR);
                                    errorEvent.setException(failureException);
                                    errorEvent.setMessage(failureException.getMessage());
                                    endEventImpl = errorEvent;
                                    reportName = "error";
                                }
                                else if (cancelled)
                                {
                                    endEventImpl = new TransferEventCancelled();
                                    endEventImpl.setTransferState(TransferEvent.TransferState.CANCELLED);
                                    endEventImpl.setMessage("cancelled");
                                    reportName = "cancelled";
                                }
                                else
                                {
                                    logger.debug("committed transferId:" + transfer.getTransferId());
                                    endEventImpl = new TransferEventSuccess();
                                    endEventImpl.setTransferState(TransferEvent.TransferState.SUCCESS);
                                    endEventImpl.setMessage("success");
                                    reportName = "success";
                                }
                                
                                // manually add the terminal event to the transfer report event list
                                transferReportEvents.add(endEventImpl);
                            }
                            catch(Exception e)
                            {
                                // report this failure as last resort
                                failureException = e;
                                reportName = "error";
                                logger.warn("Exception - unable to notify end transfer state", e);
                            }
                            
                            reportName += "_" + new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date());

                            try
                            {
                                if(transfer != null)
                                {
                                    logger.debug("now pull back the destination transfer report");
                                    destinationReport = persistDestinationTransferReport(reportName, transfer, target);
                                    if (destinationReport != null)
                                    {
                                        eventProcessor.writeReport(destinationReport, TransferEventReport.ReportType.DESTINATION, endEventImpl.getTransferState());
                                    }
                                }

                                logger.debug("now persist the client side transfer report");
                                sourceReport = persistTransferReport(reportName, transfer, target, definition, transferReportEvents, manifest, failureException);
                                if (sourceReport != null)
                                {
                                    eventProcessor.writeReport(sourceReport, TransferEventReport.ReportType.SOURCE, endEventImpl.getTransferState());
                                }
                            }
                            catch(Exception e)
                            {
                                logger.warn("Exception - unable to write transfer reports", e);
                            }

                            try
                            {
                                endEventImpl.setLast(true);
                                endEventImpl.setSourceReport(sourceReport);
                                endEventImpl.setDestinationReport(destinationReport);
                                endEvent = endEventImpl;
                                eventProcessor.end(endEvent);
                            }
                            catch(Exception e)
                            {
                                // report this failure as last resort
                                failureException = e;
                                logger.warn("Exception - unable to notify end transfer state", e);
                            }
                        }
                        finally
                        {
                            clientState = ClientTransferState.Exit;
                        }
                    }
                }
            }
            catch(TransferCancelledException e)
            {
                logger.debug("Interrupted by transfer cancel request from client");
                clientState = ClientTransferState.Cancel;
            }
            catch(Exception e)
            {
                logger.debug("Exception - unable to transfer", e);
                
                /**
                 * Save the first exception that we encounter.
                 */
                if(failureException == null)
                {
                    failureException = e;
                }
                if (transfer != null && (clientState == ClientTransferState.Begin || 
                        clientState == ClientTransferState.Prepare ||
                        clientState == ClientTransferState.Commit))
                {
                    // we must first inform the target repository that a client failure has occurred to allow it to
                    // clean up appropriately, too
                    clientState = ClientTransferState.Cancel;
                }
                else
                {
                    clientState = ClientTransferState.Finished;
                }
            }
        }
        
        try
        {
            if (endEvent == null)
            {
                TransferEventError error = new TransferEventError();
                error.setTransferState(TransferEvent.TransferState.ERROR);
                TransferFailureException endException = new TransferFailureException(error);
                error.setMessage(endException.getMessage());
                error.setException(endException);
                error.setSourceReport(sourceReport);
                error.setDestinationReport(destinationReport);
                error.setLast(true);
                endEvent = error;
            }
            if (endEvent instanceof TransferEventError)
            {
                TransferEventError endError = (TransferEventError)endEvent;
                throw new TransferFailureException(endError);
            }
            return endEvent;
        }
        finally
        {
            // clean up
            if (transfer != null)
            {
                transferMonitoring.remove(transfer.getTransferId());
            }
            if(manifest != null)
            {
                manifest.delete();
                logger.debug("manifest file deleted");
            }
            
            if(requisite != null)
            {
                requisite.delete();
                logger.debug("requisite file deleted");
            }
        }
    }
    
    private File createManifest(TransferDefinition definition)
        throws IOException, SAXException
    {
        // which nodes to write to the snapshot
        Set<NodeRef>nodes = definition.getNodes();
    
        if(nodes == null || nodes.size() == 0)
        {
            logger.debug("no nodes to transfer");
            throw new TransferException(MSG_NO_NODES);
        }
    
        /**
         * create snapshot
         */
        logger.debug("create snapshot");
    
        // where to put snapshot ?
        File tempDir = TempFileProvider.getLongLifeTempDir(FILE_DIRECTORY);
        File snapshotFile = TempFileProvider.createTempFile("TRX-SNAP", FILE_SUFFIX, tempDir);
        Writer snapshotWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(snapshotFile), "UTF-8"));
        
        // Write the manifest file
        TransferManifestWriter formatter = new XMLTransferManifestWriter();
        TransferManifestHeader header = new TransferManifestHeader();
        Descriptor descriptor = descriptorService.getCurrentRepositoryDescriptor();
        header.setRepositoryId(descriptor.getId());
        header.setCreatedDate(new Date());
        header.setNodeCount(nodes.size());
        header.setSync(definition.isSync());
        header.setReadOnly(definition.isReadOnly());
        formatter.startTransferManifest(snapshotWriter);
        formatter.writeTransferManifestHeader(header);
        for(NodeRef nodeRef : nodes)
        {
            TransferManifestNode node = transferManifestNodeFactory.createTransferManifestNode(nodeRef, definition);
            formatter.writeTransferManifestNode(node);
        }
        formatter.endTransferManifest();
        snapshotWriter.close();        
        
        logger.debug("snapshot file written to local filesystem");
        // If we are debugging then write the file to stdout.
        if(logger.isDebugEnabled())
        {
            try
            {
                outputFile(snapshotFile);
            }
            catch (IOException error)
            {
                // This is debug code - so an exception thrown while debugging
                logger.debug("error while outputting snapshotFile");
                error.printStackTrace();
            }
        }
    
        return snapshotFile;
    }
    
    private File createRequisiteFile()
    {
        File tempDir = TempFileProvider.getLongLifeTempDir(FILE_DIRECTORY);
        File reqFile = TempFileProvider.createTempFile("TRX-REQ", FILE_SUFFIX, tempDir);
        return reqFile;
    }
    
    private void sendContent(final Transfer transfer, final TransferDefinition definition, final TransferEventProcessor eventProcessor,
            File manifest, File requisite)
        throws SAXException, ParserConfigurationException, IOException
    {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser parser;
        parser = saxParserFactory.newSAXParser(); 
        
        /**
         * Parse the requisite file to generate the delta list
         */
        DeltaListRequsiteProcessor reqProcessor = new DeltaListRequsiteProcessor(); 
        XMLTransferRequsiteReader reqReader = new XMLTransferRequsiteReader(reqProcessor);
        parser.parse(requisite, reqReader);
        
        final DeltaList deltaList = reqProcessor.getDeltaList();
    
        /**
         * Parse the manifest file and transfer chunks over
         * 
         * ManifestFile -> Manifest Processor -> Chunker -> Transmitter
         * 
         * Step 1: Create a chunker and wire it up to the transmitter
         */
        final ContentChunker chunker = new ContentChunkerImpl();
        final Long fRange = Long.valueOf(definition.getNodes().size()); 
        chunker.setHandler(
                new ContentChunkProcessor(){
                private long counter = 0;
                public void processChunk(Set<ContentData> data)
                {
                    checkCancel(transfer.getTransferId());
                    logger.debug("send chunk to transmitter");
                    for(ContentData file : data)
                    {
                        counter++;
                        eventProcessor.sendContent(file, fRange, counter);
                    }
                    transmitter.sendContent(transfer, data);
                }
            }
        );
    
        /**
         * Step 2 : create a manifest processor and wire it up to the chunker
         */
        TransferManifestProcessor processor = new TransferManifestProcessor()
        {
            public void processTransferManifestNode(TransferManifestNormalNode node)
            {
                Set<ContentData> data = TransferManifestNodeHelper.getContentData(node);
                for(ContentData d : data)
                {
                    checkCancel(transfer.getTransferId());
                    logger.debug("add content to chunker");
                    
                    /**
                     * Check with the deltaList whether we need to send the content item
                     */
                    if(deltaList != null)
                    {
                        String partName = TransferCommons.URLToPartName(d.getContentUrl());
                        if(deltaList.getRequiredParts().contains(partName))
                        {
                            logger.debug("content is required :" + d.getContentUrl());
                            chunker.addContent(d);
                        }
                    }
                    else
                    {
                        // No delta list - so send all content items
                        chunker.addContent(d);
                    }
                }
            }
    
            public void processTransferManifiestHeader(TransferManifestHeader header){/* NO-OP */ }
            public void startTransferManifest(){ /* NO-OP */ }
            public void endTransferManifest(){ /* NO-OP */ }
            public void processTransferManifestNode(TransferManifestDeletedNode node)
            { /* NO-OP */
            }
        };
        
        /**
         * Step 3: wire up the manifest reader to a manifest processor
         */
    
        XMLTransferManifestReader reader = new XMLTransferManifestReader(processor);
    
        /**
         * Step 4: start the magic - Give the manifest file to the manifest reader
         */
        parser.parse(manifest, reader);
        chunker.flush();
    }

    /**
     * CancelAsync
     */
    public void cancelAsync(String transferHandle)
    {
        TransferStatus status = transferMonitoring.get(transferHandle);
        if(status != null)
        {
            logger.debug("canceling transfer :" + transferHandle);
            status.cancelMe = true;
        }
    }
    
    /**
     * Check whether the specified transfer should be cancelled.
     * @param transferHandle
     * @throws TransferException -  the transfer has been cancelled.
     */
    private void checkCancel(String transferHandle) throws TransferException
    {
        TransferStatus status = transferMonitoring.get(transferHandle);
        if(status != null)
        {
            if(!status.cancelInProgress && status.cancelMe)
            {
                status.cancelInProgress = true;
                throw new TransferCancelledException();
            }
        }
    }
    
    private void checkTargetEnabled(TransferTarget target) throws TransferException
    {
        if(!target.isEnabled())
        {
            logger.debug("target is not enabled");
            throw new TransferException(MSG_TARGET_NOT_ENABLED, new Object[] {target.getName()});
        }
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public SearchService getSearchService()
    {
        return searchService;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setTransferSpaceQuery(String transferSpaceQuery)
    {
        this.transferSpaceQuery = transferSpaceQuery;
    }

    public String getTransferSpaceQuery()
    {
        return transferSpaceQuery;
    }
    
    public void setDefaultTransferGroup(String defaultGroup)
    {
        this.defaultTransferGroup = defaultGroup;
    }

    public String getDefaultTransferGroup()
    {
        return defaultTransferGroup;
    }
    
    public TransferTransmitter getTransmitter()
    {
        return transmitter;
    }

    public void setTransmitter(TransferTransmitter transmitter)
    {
        this.transmitter = transmitter;
    }

    private Map<String,NodeRef> transferHomeMap = new ConcurrentHashMap<String, NodeRef>();
    protected NodeRef getTransferHome()
    {
        String tenantDomain = tenantService.getUserDomain(AuthenticationUtil.getRunAsUser());
        NodeRef transferHome = transferHomeMap.get(tenantDomain);
        if(transferHome == null)
        {
            String query = transferSpaceQuery;
    
            ResultSet result = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, 
                    SearchService.LANGUAGE_XPATH, query);
    
            if(result.length() == 0)
            {
                // No transfer home.
                throw new TransferException(MSG_NO_HOME, new Object[]{query});
            }
            if (result.getNodeRefs().size() != 0)
            {
                transferHome = result.getNodeRef(0);
                transferHomeMap.put(tenantDomain, transferHome);
            }
        }
        return transferHome;
    }
    
    private char[] encrypt(char[] text)
    {
        // placeholder dummy implementation - add an 'E' to the start
//        String dummy = new String("E" + text);
//        String dummy = new String(text);
//        return dummy.toCharArray();
        return text;
    }
    
    private char[] decrypt(char[] text)
    {
        // placeholder dummy implementation - strips off leading 'E'
//        String dummy = new String(text);
        return text;
        //return dummy.substring(1).toCharArray();
    }
    
    /**
     * 
     * @param name
     * @return
     */
    private NodeRef lookupTransferTarget(String name)
    {
        String query = "+TYPE:\"trx:transferTarget\" +@cm\\:name:\"" +name + "\"";
        
        ResultSet result = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, 
                SearchService.LANGUAGE_LUCENE, query);
        
        if(result.length() == 1)
        {
            return result.getNodeRef(0);
        }        
        return null;
    }
    
    private void mapTransferTarget(NodeRef nodeRef, TransferTargetImpl def)
    {
        def.setNodeRef(nodeRef);
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        String name = (String)properties.get(ContentModel.PROP_NAME);
        
        String endpointPath = (String)properties.get(TransferModel.PROP_ENDPOINT_PATH);
        if (endpointPath == null)
            throw new TransferException(MSG_MISSING_ENDPOINT_PATH, new Object[] {name});
        def.setEndpointPath(endpointPath);

        String endpointProtocol = (String)properties.get(TransferModel.PROP_ENDPOINT_PROTOCOL);
        if (endpointProtocol == null)
            throw new TransferException(MSG_MISSING_ENDPOINT_PROTOCOL, new Object[] {name});
        def.setEndpointProtocol(endpointProtocol);

        String endpointHost = (String)properties.get(TransferModel.PROP_ENDPOINT_HOST);
        if (endpointHost== null)
            throw new TransferException(MSG_MISSING_ENDPOINT_HOST, new Object[] {name});
        def.setEndpointHost(endpointHost);

        Integer endpointPort = (Integer)properties.get(TransferModel.PROP_ENDPOINT_PORT);
        if (endpointPort == null)
            throw new TransferException(MSG_MISSING_ENDPOINT_PORT, new Object[] {name});
        def.setEndpointPort(endpointPort);

        String username = (String)properties.get(TransferModel.PROP_USERNAME);
        if (username == null)
            throw new TransferException(MSG_MISSING_ENDPOINT_USERNAME, new Object[] {name});
        def.setUsername(username);

        Serializable passwordVal = properties.get(TransferModel.PROP_PASSWORD);
        if (passwordVal == null)
            throw new TransferException(MSG_MISSING_ENDPOINT_PASSWORD, new Object[] {name});
        if(passwordVal.getClass().isArray())
        {
            def.setPassword(decrypt((char[])passwordVal));
        }
        if(passwordVal instanceof String)
        {
            String password = (String)passwordVal;
            def.setPassword(decrypt(password.toCharArray()));
        }
        
        def.setName(name);
        def.setTitle((String)properties.get(ContentModel.PROP_TITLE));
        def.setDescription((String)properties.get(ContentModel.PROP_DESCRIPTION));    
        
        if(nodeService.hasAspect(nodeRef, TransferModel.ASPECT_ENABLEABLE))
        {
            def.setEnabled((Boolean)properties.get(TransferModel.PROP_ENABLED));
        }
        else
        {
            // If the enableable aspect is not present then we don't want transfer failing.
            def.setEnabled(Boolean.TRUE);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#verify(org.alfresco.service.cmr.transfer.TransferTarget)
     */
    public void verify(TransferTarget target) throws TransferException
    {
        transmitter.verifyTarget(target);
    }
    
    /**
     * Utility to dump the contents of a file to the console
     * @param file
     */
    private static void outputFile(File file) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String s = reader.readLine();
        while(s != null)
        {
            System.out.println(s);
            s = reader.readLine();
        }
    }
    
  
    /**
     * Success transfer report
     */
    private NodeRef persistTransferReport(final String transferName, final Transfer transfer, final TransferTarget target, final TransferDefinition definition,
            final List<TransferEvent> events, final File snapshotFile, final Exception exception)
    {
        /**
         * persist the transfer report in its own transaction so it cannot be rolled back
         */
        NodeRef reportNode = transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                    {
                        public NodeRef execute() throws Throwable
                        {
                            logger.debug("transfer report starting");
                            NodeRef reportNode = null; 
                            if (exception != null)
                            {
                                reportNode = transferReporter.createTransferReport(transferName, exception, target, definition, events, snapshotFile);
                            
                            }
                            else
                            {
                                reportNode = transferReporter.createTransferReport(transferName, transfer, target, definition, events, snapshotFile);
                            }
                            logger.debug("transfer report done");
                            return reportNode;
                        }
                    }, false, true);
        return reportNode;
    }
    
    /**
     * Destination Transfer report
     * @return the node ref of the transfer report or null if there isn't one.
     */
    private NodeRef persistDestinationTransferReport(final String transferName, 
            final Transfer transfer, 
            final TransferTarget target)
    {
       /**
         *  in its own transaction so it cannot be rolled back
         */
        NodeRef reportNode = transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Throwable
                {
                   try
                   {
                        File tempDir = TempFileProvider.getLongLifeTempDir(FILE_DIRECTORY);
                        File destReportFile = TempFileProvider.createTempFile("TRX-DREP", FILE_SUFFIX, tempDir);
                        FileOutputStream destReportOutput = new FileOutputStream(destReportFile); 
                        transmitter.getTransferReport(transfer, destReportOutput);
                        logger.debug("transfer report (destination) starting");

                        NodeRef reportNode = transferReporter.writeDestinationReport(transferName, target, destReportFile);
                        logger.debug("transfer report (destination) done");

                        if(destReportFile != null)
                        {
                            destReportFile.delete();
                        }
                        logger.debug("destination report temp file deleted");

                        return reportNode;
                    }
                    catch(FileNotFoundException ie)
                    {
                        // there's nothing we can do here. - but we do not want the exception to propogate up.
                        logger.debug("unexpected error while obtaining destination transfer report", ie);
                        return null;
                    }
                    catch(TransferException ie)
                    {
                        // there's nothing we can do here. - but we do not want the exception to propogate up.
                        logger.debug("unexpected error while obtaining destination transfer report", ie);
                        return null;
                    }
                } // end execute
            }, false, true);
            
        return reportNode;
    }
    
    public void setTransferManifestNodeFactory(TransferManifestNodeFactory transferManifestNodeFactory)
    {
        this.transferManifestNodeFactory = transferManifestNodeFactory;
    }

    public TransferManifestNodeFactory getTransferManifestNodeFactory()
    {
        return transferManifestNodeFactory;
    }

    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    public ActionService getActionService()
    {
        return actionService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public TransactionService getTransactionService()
    {
        return transactionService;
    }

    public void setTransferReporter(TransferReporter transferReporter)
    {
        this.transferReporter = transferReporter;
    }

    public TransferReporter getTransferReporter()
    {
        return transferReporter;
    }
    
    public void setCommitPollDelay(long commitPollDelay)
    {
        this.commitPollDelay = commitPollDelay;
    }

    public long getCommitPollDelay()
    {
        return commitPollDelay;
    }

    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    public DescriptorService getDescriptorService()
    {
        return descriptorService;
    }

    private class TransferStatus 
    {
        boolean cancelMe = false;
        boolean cancelInProgress = false;
    }
   
}
