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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
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
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferService;
import org.alfresco.service.cmr.transfer.TransferTarget;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.util.PropertyCheck;


public class TransferServiceImpl implements TransferService
{
    private static final String MSG_NO_HOME = "transfer_service.unable_to_find_transfer_home";
    private static final String MSG_NO_GROUP = "transfer_service.unable_to_find_transfer_group";
    private static final String MSG_NO_TARGET = "transfer_service.unable_to_find_transfer_target";
    private static final String MSG_ERR_TRANSFER_ASYNC = "transfer_service.unable_to_transfer_async";
    private static final String MSG_TARGET_EXISTS = "transfer_service.target_exists";
    private static final String MSG_CANCELLED = "transfer_service.cancelled";
    private static final String MSG_NO_NODES = "transfer_service.no_nodes";
    
    /**
     * The synchronised list of transfers in progress.
     */
    private Map<String, TransferStatus> transferMonitoring = Collections.synchronizedMap(new TreeMap<String,TransferStatus>());
    
    private static Log logger = LogFactory.getLog(TransferServiceImpl.class);
    
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
        properties.put(TransferModel.PROP_PASSWORD, encrypt(newTarget.getPassword()));

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
        properties.put(TransferModel.PROP_PASSWORD, encrypt(update.getPassword()));
        
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
     * Transfer sync without callbacks.
     */
    public NodeRef transfer(String targetName, TransferDefinition definition)
    {
        final TransferEventProcessor eventProcessor = new TransferEventProcessor();
        return transferImpl(targetName, definition, eventProcessor);
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
     * @param targetName
     * @param definition
     * @param callbacks
     */
    public NodeRef transfer(String targetName, TransferDefinition definition, TransferCallback... callbacks)
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

    public NodeRef transfer(String targetName, TransferDefinition definition, Collection<TransferCallback> callbacks)
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


    /**
     * Transfer Implementation 
     * @param targetName name of transfer target
     * @param definition thranser definition
     * @param eventProcessor
     */
    private NodeRef transferImpl(String targetName, final TransferDefinition definition, final TransferEventProcessor eventProcessor)
    {        
        if(logger.isDebugEnabled())
        {
            logger.debug("transfer started to :" + targetName);
        }
        
        /**
         * Wire in the transferReport - so any callbacks are stored in transferReport
         */
        final List<TransferEvent> transferReport = new LinkedList<TransferEvent>();
        TransferCallback reportCallback = new TransferCallback()
        {
            private static final long serialVersionUID = 4072579605731036522L;

            public void processEvent(TransferEvent event)
            {
                transferReport.add(event);
            } 
        };
        eventProcessor.addObserver(reportCallback);
        
        File snapshotFile = null;
        File reqFile = null;
        
        TransferTarget target = null;
        try
        { 
            target = getTransferTarget(targetName);
        
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
            String prefix = "TRX-SNAP";
            String suffix = ".xml";
                        
            logger.debug("create snapshot");
        
            // where to put snapshot ?
            File tempDir = TempFileProvider.getLongLifeTempDir("transfer");
            snapshotFile = TempFileProvider.createTempFile(prefix, suffix, tempDir);
            reqFile = TempFileProvider.createTempFile("TRX-REQ", suffix, tempDir);
            FileOutputStream reqOutput = new FileOutputStream(reqFile);
  
            FileWriter snapshotWriter = new FileWriter(snapshotFile);
            
            // Write the manifest file
            TransferManifestWriter formatter = new XMLTransferManifestWriter();
            TransferManifestHeader header = new TransferManifestHeader();
            Descriptor descriptor = descriptorService.getCurrentRepositoryDescriptor();
            header.setRepositoryId(descriptor.getId());
            header.setCreatedDate(new Date());
            header.setNodeCount(nodes.size());
            header.setSync(definition.isSync());
            formatter.startTransferManifest(snapshotWriter);
            formatter.writeTransferManifestHeader(header);
            for(NodeRef nodeRef : nodes)
            {
                TransferManifestNode node = transferManifestNodeFactory.createTransferManifestNode(nodeRef);
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
        
            /**
             * Begin
             */
            logger.debug("transfer begin");
            eventProcessor.start();
            final Transfer transfer = transmitter.begin(target);
            if(transfer != null)
            {
                String transferId = transfer.getTransferId();
                TransferStatus status = new TransferStatus(transferId);
                transferMonitoring.put(transferId, status);
                logger.debug("transfer begun transferId:" + transferId);
                
                boolean prepared = false;
                try
                {
                    eventProcessor.begin(transferId);
                    checkCancel(transferId);
                    
                    /**
                     * send Manifest, get the requsite back.
                     */
                    eventProcessor.sendSnapshot(1,1);
                    transmitter.sendManifest(transfer, snapshotFile, reqOutput);
                    
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("requsite file written to local filesystem");
                        try
                        {
                            outputFile(reqFile);
                        }
                        catch (IOException error)
                        {
                            // This is debug code - so an exception thrown while debugging
                            logger.debug("error while outputting snapshotFile");
                            error.printStackTrace();
                        }
                    }

                    
                    logger.debug("manifest sent");
                    
                    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                    SAXParser parser;
                    parser = saxParserFactory.newSAXParser(); 
                    
                    /**
                     * Parse the requsite file to generate the delta list
                     */
                    DeltaListRequsiteProcessor reqProcessor = new DeltaListRequsiteProcessor(); 
                    XMLTransferRequsiteReader reqReader = new XMLTransferRequsiteReader(reqProcessor);
                    parser.parse(reqFile, reqReader);
                    
                    final DeltaList deltaList = reqProcessor.getDeltaList();
        
                    /**
                     * Parse the manifest file and transfer chunks over
                     * 
                     * ManifestFile -> Manifest Processor -> Chunker -> Transmitter
                     * 
                     * Step 1: Create a chunker and wire it up to the transmitter
                     */
                    final ContentChunker chunker = new ContentChunkerImpl();
                    final Long fRange = Long.valueOf(nodes.size()); 
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
                    parser.parse(snapshotFile, reader);
                    chunker.flush();
                    
                    /**
                     * Content all sent over 
                     */
                    logger.debug("content sending finished");
                    checkCancel(transfer.getTransferId());
                    
                    /**
                     * prepare
                     */
                    eventProcessor.prepare();
                    transmitter.prepare(transfer);
                    logger.debug("prepared transferId:" + transferId);
                    checkCancel(transfer.getTransferId());
        
                    /**
                     * committing
                     */
                    eventProcessor.commit();
                    transmitter.commit(transfer);
                    logger.debug("committing transferId:" + transferId);
                    checkCancel(transfer.getTransferId());
                    
                    /**
                     * need to poll for committed status
                     */
                    TransferProgress progress = null;
                    
                    int position = -1;
                    for(int retries = 0; retries < 3; retries++)
                    {
                        checkCancel(transfer.getTransferId()); 
                        try
                        {
                            progress = transmitter.getStatus(transfer);
                            if(progress.getCurrentPosition() != position)
                            {
                                position = progress.getCurrentPosition();
                                eventProcessor.committing(progress.getEndPosition(), position);
                            }
                            if(progress.isFinished())
                            {
                                logger.debug("isFinished=true");
                                break;
                            }
                            retries = 0;
                        }
                        catch (TransferException te)
                        {
                            logger.debug("error while committing - retrying", te);
                        }
                        
                        /**
                         * Now sleep for a while.
                         */
                        Thread.sleep(commitPollDelay);
                    }
                    logger.debug("Finished transferId:" + transferId);
                    
                    checkCancel(transfer.getTransferId()); 
                    
                    /**
                     * committed
                     */                    
                    eventProcessor.success();
                    checkCancel(transfer.getTransferId());
                    prepared = true;
                    
                    logger.debug("committed - write transfer report transferId:" + transferId);
                    
                    /**
                     *  Write the Successful transfer report if we get here
                     */
                    NodeRef reportNode = persistTransferReport(transfer, target, definition, transferReport, snapshotFile);
                                       
                    logger.debug("success - at end of method transferId:" + transferId);
                    return reportNode;
                }
                finally
                {
                    logger.debug("remove monitoring for transferId:" + transferId);
                    transferMonitoring.remove(transferId);
                    logger.debug("removed monitoring for transferId:" + transferId);
                    
                    if(!prepared)
                    {
                        logger.debug("abort incomplete transfer");
                        transmitter.abort(transfer);
                    }
                }
            }
            
            //TODO Do we ever get here ?
            logger.debug("returning null - unable lock target");
            return null;
        }
        catch (TransferException t)
        {
            logger.debug("TransferException - unable to transfer", t);
            eventProcessor.error(t);
            
            /**
             * Write the transfer report.  This is an error report so needs to be out 
             */
            if(target != null )
            {
                persistTransferReport(t, target, definition, transferReport, snapshotFile);
            }
            throw t;
        }
        catch (Exception t)
        {
            // Wrap any other exception as a transfer exception
            logger.debug("Exception - unable to transfer", t);
            eventProcessor.error(t);
            
            /**
             * Write the transfer report.  This is an error report so needs to be out 
             */
            if(target != null )
            {
                persistTransferReport(t, target, definition, transferReport, snapshotFile);
            }
            
            /**
             * Wrap the exception as a transfer exception
             */
            throw new TransferException("unable to transfer:" + t.toString(), t);
        }
        finally
        {
            /**
             * clean up
             */
            if(snapshotFile != null)
            {
                snapshotFile.delete();
            }
            logger.debug("snapshot file deleted");
            
            if(reqFile != null)
            {
                reqFile.delete();
            }
            logger.debug("req file deleted");

        } 
    } // end of transferImpl
    
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
            if(status.cancelMe)
            {
                throw new TransferException(MSG_CANCELLED);
            }
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
        def.setEndpointPath((String)properties.get(TransferModel.PROP_ENDPOINT_PATH));
        def.setEndpointProtocol((String)properties.get(TransferModel.PROP_ENDPOINT_PROTOCOL));
        def.setEndpointHost((String)properties.get(TransferModel.PROP_ENDPOINT_HOST));
        def.setEndpointPort((Integer)properties.get(TransferModel.PROP_ENDPOINT_PORT));
        Serializable passwordVal = properties.get(TransferModel.PROP_PASSWORD);
        
        if(passwordVal.getClass().isArray())
        {
            def.setPassword(decrypt((char[])passwordVal));
        }
        if(passwordVal instanceof String)
        {
            String password = (String)passwordVal;
            def.setPassword(decrypt(password.toCharArray()));
        }
        
        
        def.setUsername((String)properties.get(TransferModel.PROP_USERNAME));
        def.setName((String)properties.get(ContentModel.PROP_NAME));
        def.setTitle((String)properties.get(ContentModel.PROP_TITLE));
        def.setDescription((String)properties.get(ContentModel.PROP_DESCRIPTION));    
        
        if(nodeService.hasAspect(nodeRef, TransferModel.ASPECT_ENABLEABLE))
        {
            def.setEnabled((Boolean)properties.get(TransferModel.PROP_ENABLED));
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
        BufferedReader reader = new BufferedReader(new FileReader(file));
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
    private NodeRef persistTransferReport(final Transfer transfer, final TransferTarget target, final TransferDefinition definition, final List<TransferEvent> events, final File snapshotFile)
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
                            NodeRef reportNode = transferReporter.createTransferReport(transfer, target, definition, events, snapshotFile);
                            logger.debug("transfer report done");
                            return reportNode;
                        }
                    }, false, true);
        return reportNode;
    }
    
    /**
     * Error Transfer report
     */
    private NodeRef persistTransferReport(final Exception t, final TransferTarget target, final TransferDefinition definition, final List<TransferEvent> events, final File snapshotFile)
    {
        // in its own transaction so it cannot be rolled back
        NodeRef reportNode = transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                    {
                        public NodeRef execute() throws Throwable
                        {
                            logger.debug("transfer report starting");
                            NodeRef reportNode = transferReporter.createTransferReport(t, target, definition, events, snapshotFile);
                            logger.debug("transfer report done");
                            return reportNode;
                        }
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
        TransferStatus(String transferId)
        {
            this.transferId = transferId;
        }
        String transferId;
        boolean cancelMe = false;
    }


}
