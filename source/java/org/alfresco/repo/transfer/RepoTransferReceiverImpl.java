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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transfer.ChangeCapturingProgressMonitor.TransferChangesRecord;
import org.alfresco.repo.transfer.manifest.TransferManifestProcessor;
import org.alfresco.repo.transfer.manifest.XMLTransferManifestReader;
import org.alfresco.repo.transfer.requisite.XMLTransferRequsiteWriter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.service.cmr.transfer.TransferServicePolicies;
import org.alfresco.service.cmr.transfer.TransferProgress.Status;
import org.alfresco.service.cmr.transfer.TransferServicePolicies.BeforeStartInboundTransferPolicy;
import org.alfresco.service.cmr.transfer.TransferServicePolicies.OnEndInboundTransferPolicy;
import org.alfresco.service.cmr.transfer.TransferServicePolicies.OnStartInboundTransferPolicy;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.FileCopyUtils;

/**
 * @author brian
 * 
 */
public class RepoTransferReceiverImpl implements TransferReceiver,  
    NodeServicePolicies.OnCreateChildAssociationPolicy,
    NodeServicePolicies.BeforeDeleteNodePolicy,
    NodeServicePolicies.OnRestoreNodePolicy,
    NodeServicePolicies.OnMoveNodePolicy,
    ContentServicePolicies.OnContentUpdatePolicy
    
{
    /**
     * This embedded class is used to push requests for asynchronous commits onto a different thread
     * 
     * @author Brian
     * 
     */
    public class AsyncCommitCommand implements Runnable
    {

        private String transferId;
        private String runAsUser;

        public AsyncCommitCommand(String transferId)
        {
            this.transferId = transferId;
            this.runAsUser = AuthenticationUtil.getFullyAuthenticatedUser();
        }

        public void run()
        {
            RunAsWork<Object> actionRunAs = new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    return transactionService.getRetryingTransactionHelper().doInTransaction(
                            new RetryingTransactionCallback<Object>()
                            {
                                public Object execute()
                                {
                                    commit(transferId);
                                    return null;
                                }
                            }, false, true);
                }
            };
            AuthenticationUtil.runAs(actionRunAs, runAsUser);
        }

    }

    private final static Log log = LogFactory.getLog(RepoTransferReceiverImpl.class);

    private static final String MSG_FAILED_TO_CREATE_STAGING_FOLDER = "transfer_service.receiver.failed_to_create_staging_folder";
    private static final String MSG_TRANSFER_LOCK_FOLDER_NOT_FOUND = "transfer_service.receiver.lock_folder_not_found";
    private static final String MSG_TRANSFER_TEMP_FOLDER_NOT_FOUND = "transfer_service.receiver.temp_folder_not_found";
    private static final String MSG_TRANSFER_LOCK_UNAVAILABLE = "transfer_service.receiver.lock_unavailable";
    private static final String MSG_INBOUND_TRANSFER_FOLDER_NOT_FOUND = "transfer_service.receiver.record_folder_not_found";
    private static final String MSG_NOT_LOCK_OWNER = "transfer_service.receiver.not_lock_owner";
    private static final String MSG_ERROR_WHILE_ENDING_TRANSFER = "transfer_service.receiver.error_ending_transfer";
    private static final String MSG_ERROR_WHILE_STAGING_SNAPSHOT = "transfer_service.receiver.error_staging_snapshot";
    private static final String MSG_ERROR_WHILE_STAGING_CONTENT = "transfer_service.receiver.error_staging_content";
    private static final String MSG_NO_SNAPSHOT_RECEIVED = "transfer_service.receiver.no_snapshot_received";
    private static final String MSG_ERROR_WHILE_COMMITTING_TRANSFER = "transfer_service.receiver.error_committing_transfer";
    private static final String MSG_ERROR_WHILE_GENERATING_REQUISITE = "transfer_service.receiver.error_generating_requsite";
    
    private static final String LOCK_FILE_NAME = ".lock";
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, LOCK_FILE_NAME);
    private static final String SNAPSHOT_FILE_NAME = "snapshot.xml";

    private NodeService nodeService;
    private SearchService searchService;
    private TransactionService transactionService;
    private String transferLockFolderPath;
    private String inboundTransferRecordsPath;
    private String rootStagingDirectory;
    private String transferTempFolderPath;
    private ManifestProcessorFactory manifestProcessorFactory;
    private BehaviourFilter behaviourFilter;
    private ChangeCapturingProgressMonitor progressMonitor;
    private ActionService actionService;
    private TenantService tenantService;
    private RuleService ruleService;
    private PolicyComponent policyComponent;
    private DescriptorService descriptorService;
    private AlienProcessor alienProcessor;
    
    //private String localRepositoryId = descriptorService.getCurrentRepositoryDescriptor().getId();

    private Map<String,NodeRef> transferLockFolderMap = new ConcurrentHashMap<String, NodeRef>();
    private Map<String,NodeRef> transferTempFolderMap = new ConcurrentHashMap<String, NodeRef>();
    private Map<String,NodeRef> inboundTransferRecordsFolderMap = new ConcurrentHashMap<String, NodeRef>();
    
    private ClassPolicyDelegate<BeforeStartInboundTransferPolicy> beforeStartInboundTransferDelegate;
    private ClassPolicyDelegate<OnStartInboundTransferPolicy> onStartInboundTransferDelegate;
    private ClassPolicyDelegate<OnEndInboundTransferPolicy> onEndInboundTransferDelegate;
    
    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "searchService", searchService);
        PropertyCheck.mandatory(this, "ruleService", ruleService);
        PropertyCheck.mandatory(this, "actionService", actionService);
        PropertyCheck.mandatory(this, "behaviourFilter", behaviourFilter);
        PropertyCheck.mandatory(this, "tennantService", tenantService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "transferLockFolderPath", transferLockFolderPath);
        PropertyCheck.mandatory(this, "inboundTransferRecordsPath", inboundTransferRecordsPath);
        PropertyCheck.mandatory(this, "rootStagingDirectory", rootStagingDirectory);
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "descriptorService", descriptorService);
        PropertyCheck.mandatory(this, "alienProcessor", alienProcessor);

        beforeStartInboundTransferDelegate = policyComponent.registerClassPolicy(TransferServicePolicies.BeforeStartInboundTransferPolicy.class);
        onStartInboundTransferDelegate = policyComponent.registerClassPolicy(TransferServicePolicies.OnStartInboundTransferPolicy.class);
        onEndInboundTransferDelegate = policyComponent.registerClassPolicy(TransferServicePolicies.OnEndInboundTransferPolicy.class);

        /**
         * For every new child of a node with the trx:transferred aspect run this.onCreateChildAssociation
         */
        this.getPolicyComponent().bindAssociationBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                TransferModel.ASPECT_TRANSFERRED, 
                new JavaBehaviour(this, "onCreateChildAssociation", NotificationFrequency.EVERY_EVENT));
        
        /**
         * For every update of a transferred node 
         */ 
        this.getPolicyComponent().bindClassBehaviour(
                ContentServicePolicies.OnContentUpdatePolicy.QNAME,
                TransferModel.ASPECT_TRANSFERRED, 
                new JavaBehaviour(this, "onContentUpdate", NotificationFrequency.EVERY_EVENT));

        /**
         * For every copy of a transferred node run onCopyTransferred
         */
        this.getPolicyComponent().bindClassBehaviour(
                CopyServicePolicies.OnCopyNodePolicy.QNAME,
                TransferModel.ASPECT_TRANSFERRED, 
                new JavaBehaviour(this, "onCopyTransferred", NotificationFrequency.EVERY_EVENT));
        
        /**
         * For every new child of a node with the trx:alien aspect run this.onCreateChildAssociation
         */
        this.getPolicyComponent().bindAssociationBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                TransferModel.ASPECT_ALIEN, 
                new JavaBehaviour(this, "onCreateChildAssociation", NotificationFrequency.EVERY_EVENT));
        
        /**
         * For every node with the trx:alien aspect run this.beforeDeleteNode
         */
        this.getPolicyComponent().bindClassBehaviour(
                NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, 
                TransferModel.ASPECT_ALIEN,
                new JavaBehaviour(this, "beforeDeleteNode", NotificationFrequency.EVERY_EVENT));   
        
        /**
         * For every restore of a node with the trx:alien aspect
         */
        this.getPolicyComponent().bindClassBehaviour(
                NodeServicePolicies.OnRestoreNodePolicy.QNAME,
                TransferModel.ASPECT_ALIEN, 
                new JavaBehaviour(this, "onRestoreNode", NotificationFrequency.EVERY_EVENT));
        
        /**
         * For every move of a node with the trx:alien aspect.
         */
        this.getPolicyComponent().bindClassBehaviour(
                NodeServicePolicies.OnMoveNodePolicy.QNAME,
                TransferModel.ASPECT_ALIEN, 
                new JavaBehaviour(this, "onMoveNode", NotificationFrequency.EVERY_EVENT));
        
        /**
         * For every copy of an alien node remove the alien aspect
         */
        this.getPolicyComponent().bindClassBehaviour(
                CopyServicePolicies.OnCopyNodePolicy.QNAME,
                TransferModel.ASPECT_ALIEN, 
                new JavaBehaviour(this, "onCopyAlien", NotificationFrequency.EVERY_EVENT));      
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.repo.web.scripts.transfer.TransferReceiver#getStagingFolder(org.alfresco.service.cmr.repository.
     * NodeRef)
     */
    public File getStagingFolder(String transferId)
    {
        if (transferId == null)
        {
            throw new IllegalArgumentException("transferId = " + transferId);
        }
        NodeRef transferNodeRef = new NodeRef(transferId);
        File tempFolder;
        String tempFolderPath = rootStagingDirectory + "/" + transferNodeRef.getId();
        tempFolder = new File(tempFolderPath);
        if (!tempFolder.exists())
        {
            if (!tempFolder.mkdirs())
            {
                tempFolder = null;
                throw new TransferException(MSG_FAILED_TO_CREATE_STAGING_FOLDER, new Object[] { transferId });
            }
        }
        return tempFolder;

    }

    private NodeRef getLockFolder()
    {
        String tenantDomain = tenantService.getUserDomain(AuthenticationUtil.getRunAsUser());
        NodeRef transferLockFolder = transferLockFolderMap.get(tenantDomain);
        
        // Have we already resolved the node that is the parent of the lock node?
        // If not then do so.
        if (transferLockFolder == null)
        {
            ResultSet rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH,
                    transferLockFolderPath);
            if (rs.length() > 0)
            {
                transferLockFolder = rs.getNodeRef(0);
                transferLockFolderMap.put(tenantDomain, transferLockFolder);
            }
            else
            {
                throw new TransferException(MSG_TRANSFER_LOCK_FOLDER_NOT_FOUND, new Object[] { transferLockFolderPath });
            }
        }
        return transferLockFolder;

    }

    public NodeRef getTempFolder(String transferId)
    {
        String tenantDomain = tenantService.getUserDomain(AuthenticationUtil.getRunAsUser());
        NodeRef transferTempFolder = transferTempFolderMap.get(tenantDomain);
        
        // Have we already resolved the node that is the temp folder?
        // If not then do so.
        if (transferTempFolder == null)
        {
            ResultSet rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH,
                    transferTempFolderPath);
            if (rs.length() > 0)
            {
                transferTempFolder = rs.getNodeRef(0);
                transferTempFolderMap.put(tenantDomain, transferTempFolder);
            }
            else
            {
                throw new TransferException(MSG_TRANSFER_TEMP_FOLDER_NOT_FOUND, new Object[] { transferId,
                        transferTempFolderPath });
            }
        }

        NodeRef transferNodeRef = new NodeRef(transferId);
        String tempTransferFolderName = transferNodeRef.getId();
        NodeRef tempFolderNode = null;
        QName folderName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, tempTransferFolderName);

        // Do we already have a temp folder for this transfer?
        List<ChildAssociationRef> tempChildren = nodeService.getChildAssocs(transferTempFolder,
                RegexQNamePattern.MATCH_ALL, folderName);
        if (tempChildren.isEmpty())
        {
            // No, we don't have a temp folder for this transfer yet. Create it...
            Map<QName, Serializable> props = new HashMap<QName, Serializable>();
            props.put(ContentModel.PROP_NAME, tempTransferFolderName);
            tempFolderNode = nodeService.createNode(transferTempFolder, ContentModel.ASSOC_CONTAINS, folderName,
                    TransferModel.TYPE_TEMP_TRANSFER_STORE, props).getChildRef();
        }
        else
        {
            // Yes, we do have a temp folder for this transfer already. Return it.
            tempFolderNode = tempChildren.get(0).getChildRef();
        }
        return tempFolderNode;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.web.scripts.transfer.TransferReceiver#start()
     */
    public String start()
    {
        final NodeRef lockFolder = getLockFolder();
        String transferId = null;

        RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();
        try
        {
            transferId = txHelper.doInTransaction(
                    new RetryingTransactionHelper.RetryingTransactionCallback<String>()
                    {
                        public String execute() throws Throwable
                        {
                            TransferServicePolicies.BeforeStartInboundTransferPolicy beforeStartPolicy = 
                                beforeStartInboundTransferDelegate.get(TransferModel.TYPE_TRANSFER_RECORD);
                            beforeStartPolicy.beforeStartInboundTransfer();
                            
                            final NodeRef relatedTransferRecord = createTransferRecord();
                            String transferId = relatedTransferRecord.toString();
                            getTempFolder(transferId);

                            Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                            props.put(ContentModel.PROP_NAME, LOCK_FILE_NAME);
                            props.put(TransferModel.PROP_TRANSFER_ID, transferId);

                            if (log.isInfoEnabled())
                            {
                                log.info("Creating transfer lock associated with this transfer record: "
                                        + relatedTransferRecord);
                            }

                            ChildAssociationRef assoc = nodeService.createNode(lockFolder, ContentModel.ASSOC_CONTAINS,
                                    LOCK_QNAME, TransferModel.TYPE_TRANSFER_LOCK, props);

                            if (log.isInfoEnabled())
                            {
                                log.info("Transfer lock created as node " + assoc.getChildRef());
                            }

                            TransferServicePolicies.OnStartInboundTransferPolicy onStartPolicy = 
                                onStartInboundTransferDelegate.get(TransferModel.TYPE_TRANSFER_RECORD);
                            onStartPolicy.onStartInboundTransfer(transferId);
                            
                            return transferId;
                        }
                    }, false, true);
        }
        catch (DuplicateChildNodeNameException ex)
        {
            log.debug("lock is already taken");
            // lock is already taken.
            throw new TransferException(MSG_TRANSFER_LOCK_UNAVAILABLE);
        }
        getStagingFolder(transferId);
        return transferId;
    }

    /**
     * @return
     */
    private NodeRef createTransferRecord()
    {
        String tenantDomain = tenantService.getUserDomain(AuthenticationUtil.getRunAsUser());
        NodeRef inboundTransferRecordsFolder = inboundTransferRecordsFolderMap.get(tenantDomain);

        if (inboundTransferRecordsFolder == null)
        {
            log.debug("Trying to find transfer records folder: " + inboundTransferRecordsPath);
            ResultSet rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH,
                    inboundTransferRecordsPath);
            if (rs.length() > 0)
            {
                inboundTransferRecordsFolder = rs.getNodeRef(0);
                inboundTransferRecordsFolderMap.put(tenantDomain, inboundTransferRecordsFolder);
                log.debug("Found inbound transfer records folder: " + inboundTransferRecordsFolder);
            }
            else
            {
                throw new TransferException(MSG_INBOUND_TRANSFER_FOLDER_NOT_FOUND,
                        new Object[] { inboundTransferRecordsPath });
            }
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSSZ");
        String timeNow = format.format(new Date());
        String name = timeNow + ".xml";
        
        QName recordName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, name);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, name);
        props.put(TransferModel.PROP_PROGRESS_POSITION, 0);
        props.put(TransferModel.PROP_PROGRESS_ENDPOINT, 1);
        props.put(TransferModel.PROP_TRANSFER_STATUS, TransferProgress.Status.PRE_COMMIT.toString());

        log.debug("Creating transfer record with name: " + name);
        ChildAssociationRef assoc = nodeService.createNode(inboundTransferRecordsFolder, ContentModel.ASSOC_CONTAINS,
                recordName, TransferModel.TYPE_TRANSFER_RECORD, props);
        log.debug("<-createTransferRecord: " + assoc.getChildRef());

        return assoc.getChildRef();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.web.scripts.transfer.TransferReceiver#end(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void end(final String transferId)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Request to end transfer " + transferId);
        }
        if (transferId == null)
        {
            throw new IllegalArgumentException("transferId = null");
        }

        try
        {
            // We remove the lock node in a separate transaction, since it was created in a separate transaction
            transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                    {
                        public NodeRef execute() throws Throwable
                        {
                            // Find the lock node
                            NodeRef lockId = getLockNode();
                            if (lockId != null)
                            {
                                if (!testLockedTransfer(lockId, transferId))
                                {
                                    throw new TransferException(MSG_NOT_LOCK_OWNER, new Object[] { transferId });
                                }
                                // Delete the lock node.
                                log.debug("Deleting lock node :" + lockId);
                                nodeService.deleteNode(lockId);
                                log.debug("Lock deleted :" + lockId);
                            }
                            return null;
                        }
                    }, false, true);
            
            NodeRef tempStoreNode = null;
            try
            {
                log.debug("Deleting temporary store node...");
                tempStoreNode = getTempFolder(transferId);
                nodeService.deleteNode(tempStoreNode);
                log.debug("Deleted temporary store node.");
            }
            catch (Exception ex)
            {
                log.warn("Failed to delete temp store node for transfer id " + transferId + 
                        "\nTemp store noderef = " + tempStoreNode);
            }
            
            File stagingFolder = null;
            try
            {
                log.debug("delete staging folder " + transferId);
                // Delete the staging folder.
                stagingFolder = getStagingFolder(transferId);
                deleteFile(stagingFolder);
                log.debug("Staging folder deleted");
            }
            catch(Exception ex)
            {
                log.warn("Failed to delete staging folder for transfer id " + transferId + 
                        "\nStaging folder = " + stagingFolder.toString());
            }

            //Fire the OnEndInboundTransfer policy
            Set<NodeRef> createdNodes = Collections.emptySet();
            Set<NodeRef> updatedNodes = Collections.emptySet();
            Set<NodeRef> deletedNodes = Collections.emptySet();
            TransferChangesRecord changesRecord = progressMonitor.removeChangeRecord(transferId);
            if (changesRecord != null)
            {
                createdNodes = new HashSet<NodeRef>(changesRecord.getCreatedNodes());
                updatedNodes = new HashSet<NodeRef>(changesRecord.getUpdatedNodes());
                deletedNodes = new HashSet<NodeRef>(changesRecord.getDeletedNodes());
            }
            TransferServicePolicies.OnEndInboundTransferPolicy onEndPolicy = 
                onEndInboundTransferDelegate.get(TransferModel.TYPE_TRANSFER_RECORD);
            onEndPolicy.onEndInboundTransfer(transferId, createdNodes, updatedNodes, deletedNodes);
        }
        catch (TransferException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new TransferException(MSG_ERROR_WHILE_ENDING_TRANSFER, new Object[] {transferId}, ex);
        }
    }

    public void cancel(String transferId) throws TransferException
    {
        TransferProgress progress = getProgressMonitor().getProgress(transferId);
        getProgressMonitor().updateStatus(transferId, TransferProgress.Status.CANCELLED);
        if (progress.getStatus().equals(TransferProgress.Status.PRE_COMMIT))
        {
            end(transferId);
        }
    }

    public void prepare(String transferId) throws TransferException
    {
    }

    /**
     * @param stagingFolder
     */
    private void deleteFile(File file)
    {
        if (file.isDirectory())
        {
            File[] fileList = file.listFiles();
            if (fileList != null)
            {
                for (File currentFile : fileList)
                {
                    deleteFile(currentFile);
                }
            }
        }
        file.delete();
    }

    private NodeRef getLockNode()
    {
        final NodeRef lockFolder = getLockFolder();
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(lockFolder, ContentModel.ASSOC_CONTAINS,
                LOCK_QNAME);
        NodeRef lockId = assocs.size() == 0 ? null : assocs.get(0).getChildRef();
        return lockId;
    }

    private boolean testLockedTransfer(NodeRef lockId, String transferId)
    {
        if (lockId == null)
        {
            throw new IllegalArgumentException("lockId = null");
        }
        if (transferId == null)
        {
            throw new IllegalArgumentException("transferId = null");
        }
        String currentTransferId = (String) nodeService.getProperty(lockId, TransferModel.PROP_TRANSFER_ID);
        // Check that the lock is held for the specified transfer (error if not)
        return (transferId.equals(currentTransferId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.transfer.TransferReceiver#nudgeLock(java.lang.String)
     */
    public void nudgeLock(final String transferId) throws TransferException
    {
        if (transferId == null)
            throw new IllegalArgumentException("transferId = null");

        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        // Find the lock node
                        NodeRef lockId = getLockNode();
                        // Check that the specified transfer is the one that owns the lock
                        if (!testLockedTransfer(lockId, transferId))
                        {
                            throw new TransferException(MSG_NOT_LOCK_OWNER);
                        }
                        // Just write the lock file name again (no change, but forces the modified time to be updated)
                        nodeService.setProperty(lockId, ContentModel.PROP_NAME, LOCK_FILE_NAME);
                        return null;
                    }
                }, false, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.transfer.TransferReceiver#saveSnapshot(java.io.InputStream)
     */
    public void saveSnapshot(String transferId, InputStream openStream) throws TransferException
    {
        // Check that this transfer owns the lock and give it a nudge to stop it expiring
        nudgeLock(transferId);

        if (log.isDebugEnabled())
        {
            log.debug("Saving snapshot for transferId =" + transferId);
        }
        File snapshotFile = new File(getStagingFolder(transferId), SNAPSHOT_FILE_NAME);
        try
        {
            if (snapshotFile.createNewFile())
            {
                FileCopyUtils.copy(openStream, new FileOutputStream(snapshotFile));
            }
            if (log.isDebugEnabled())
            {
                log.debug("Saved snapshot for transferId =" + transferId);
            }
        }
        catch (Exception ex)
        {
            throw new TransferException(MSG_ERROR_WHILE_STAGING_SNAPSHOT, new Object[]{transferId}, ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.transfer.TransferReceiver#saveContent(java.lang.String, java.lang.String,
     * java.io.InputStream)
     */
    public void saveContent(String transferId, String contentFileId, InputStream contentStream)
            throws TransferException
    {
        nudgeLock(transferId);
        File stagedFile = new File(getStagingFolder(transferId), contentFileId);
        try
        {
            if (stagedFile.createNewFile())
            {
                FileCopyUtils.copy(contentStream, new BufferedOutputStream(new FileOutputStream(stagedFile)));
            }
        }
        catch (Exception ex)
        {
            throw new TransferException(MSG_ERROR_WHILE_STAGING_CONTENT, new Object[]{transferId, contentFileId}, ex);
        }
    }

    public void commitAsync(String transferId)
    {
        nudgeLock(transferId);
        progressMonitor.updateStatus(transferId, Status.COMMIT_REQUESTED);
        Action commitAction = actionService.createAction(TransferCommitActionExecuter.NAME);
        commitAction.setParameterValue(TransferCommitActionExecuter.PARAM_TRANSFER_ID, transferId);
        commitAction.setExecuteAsynchronously(true);
        actionService.executeAction(commitAction, new NodeRef(transferId));
        if (log.isDebugEnabled())
        {
            log.debug("Registered transfer commit for asynchronous execution: " + transferId);
        }
    }

    public void commit(final String transferId) throws TransferException
    {
        if (log.isDebugEnabled())
        {
            log.debug("Committing transferId=" + transferId);
        }
        
        /**
         * Turn off rules while transfer is being committed.
         */
        boolean rulesEnabled = ruleService.isEnabled();
        ruleService.disableRules();
        
        try
        {
            nudgeLock(transferId);
            progressMonitor.updateStatus(transferId, Status.COMMITTING);

            RetryingTransactionHelper.RetryingTransactionCallback<Object> commitWork = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    AlfrescoTransactionSupport.bindListener(new TransferCommitTransactionListener(transferId,
                            RepoTransferReceiverImpl.this));

                    List<TransferManifestProcessor> commitProcessors = manifestProcessorFactory.getCommitProcessors(
                            RepoTransferReceiverImpl.this, transferId);

                    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                    SAXParser parser = saxParserFactory.newSAXParser();
                    File snapshotFile = getSnapshotFile(transferId);

                    if (snapshotFile.exists())
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("Processing manifest file:" + snapshotFile.getAbsolutePath());
                        }
                        // We parse the file as many times as we have processors
                        for (TransferManifestProcessor processor : commitProcessors)
                        {
                            XMLTransferManifestReader reader = new XMLTransferManifestReader(processor);

                            //behaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
                            behaviourFilter.disableAllBehaviours();
                       
                            try
                            {
                                parser.parse(snapshotFile, reader);
                            }
                            finally
                            {
                              //  behaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
                                behaviourFilter.enableAllBehaviours();
                            }
                            nudgeLock(transferId);
                            parser.reset();
                        }
                    }
                    else
                    {
                        progressMonitor.logException(transferId, "Unable to start commit. No snapshot file received",
                                new TransferException(MSG_NO_SNAPSHOT_RECEIVED, new Object[]{transferId}));
                    }
                    return null;
                }
            };

            transactionService.getRetryingTransactionHelper().doInTransaction(commitWork, false, true);

            Throwable error = progressMonitor.getProgress(transferId).getError();
            if (error != null)
            {
                if (TransferException.class.isAssignableFrom(error.getClass()))
                {
                    throw (TransferException) error;
                }
                else
                {
                    throw new TransferException(MSG_ERROR_WHILE_COMMITTING_TRANSFER, new Object[]{transferId}, error);
                }
            }

            /**
             * Successfully committed
             */
            if (log.isDebugEnabled())
            {
                log.debug("Commit success transferId=" + transferId);
            }
        }
        catch (Exception ex)
        {
            if (TransferException.class.isAssignableFrom(ex.getClass()))
            {
                throw (TransferException) ex;
            }
            else
            {
                throw new TransferException(MSG_ERROR_WHILE_COMMITTING_TRANSFER, ex);
            }
        }
        finally
        {
            if(rulesEnabled)
            {
                /**
                 * Turn rules back on if we turned them off earlier.
                 */
                ruleService.enableRules();
            }
            
            /**
             * Clean up at the end of the transfer
             */
            try
            {
                end(transferId);
            }
            catch (Exception ex)
            {
                log.error("Failed to clean up transfer. Lock may still be in place: " + transferId, ex);
            }
        }
    }

    public TransferProgress getStatus(String transferId) throws TransferException
    {
        return getProgressMonitor().getProgress(transferId);
    }

    private File getSnapshotFile(String transferId)
    {
        return new File(getStagingFolder(transferId), SNAPSHOT_FILE_NAME);
    }

    /**
     * @param searchService
     *            the searchService to set
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @param transactionService
     *            the transactionService to set
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * @param transferLockFolderPath
     *            the transferLockFolderPath to set
     */
    public void setTransferLockFolderPath(String transferLockFolderPath)
    {
        this.transferLockFolderPath = transferLockFolderPath;
    }

    /**
     * @param transferTempFolderPath
     *            the transferTempFolderPath to set
     */
    public void setTransferTempFolderPath(String transferTempFolderPath)
    {
        this.transferTempFolderPath = transferTempFolderPath;
    }

    /**
     * @param rootStagingDirectory
     *            the rootTransferFolder to set
     */
    public void setRootStagingDirectory(String rootStagingDirectory)
    {
        this.rootStagingDirectory = rootStagingDirectory;
    }

    /**
     * @param inboundTransferRecordsPath
     *            the inboundTransferRecordsPath to set
     */
    public void setInboundTransferRecordsPath(String inboundTransferRecordsPath)
    {
        this.inboundTransferRecordsPath = inboundTransferRecordsPath;
    }

    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param manifestProcessorFactory
     *            the manifestProcessorFactory to set
     */
    public void setManifestProcessorFactory(ManifestProcessorFactory manifestProcessorFactory)
    {
        this.manifestProcessorFactory = manifestProcessorFactory;
    }

    /**
     * @param behaviourFilter
     *            the behaviourFilter to set
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * @return the progressMonitor
     */
    public TransferProgressMonitor getProgressMonitor()
    {
        return progressMonitor;
    }

    /**
     * @param progressMonitor
     *            the progressMonitor to set
     */
    public void setProgressMonitor(TransferProgressMonitor progressMonitor)
    {
        this.progressMonitor = new ChangeCapturingProgressMonitor(progressMonitor);
    }

    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }
    
    /**
     * Set the ruleService
     * @param ruleService
     *            the ruleService to set
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    /**
     * Get the rule service
     * @return the rule service
     */
    public RuleService getRuleService()
    {
        return this.ruleService;
    }

    /**
     * Generate the requsite
     */
    public void generateRequsite(String transferId, OutputStream out) throws TransferException
    {
        log.debug("Generate Requsite for transfer:" + transferId);
        try 
        {
            File snapshotFile = getSnapshotFile(transferId);

            if (snapshotFile.exists())
            {
                log.debug("snapshot does exist");
                SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                SAXParser parser = saxParserFactory.newSAXParser();  
                OutputStreamWriter dest = new OutputStreamWriter(out, "UTF-8");
                
                XMLTransferRequsiteWriter writer = new XMLTransferRequsiteWriter(dest);
                TransferManifestProcessor processor = manifestProcessorFactory.getRequsiteProcessor(
                        RepoTransferReceiverImpl.this, 
                        transferId,
                        writer);

                XMLTransferManifestReader reader = new XMLTransferManifestReader(processor);

                /**
                 * Now run the parser
                 */
                parser.parse(snapshotFile, reader);
                
                /**
                 * And flush the destination in case any content remains in the writer.
                 */
                dest.flush();
                
            }
            log.debug("Generate Requsite done transfer:" + transferId);
            
        }
        catch (Exception ex)
        {
            if (TransferException.class.isAssignableFrom(ex.getClass()))
            {
                throw (TransferException) ex;
            }
            else
            {
                throw new TransferException(MSG_ERROR_WHILE_GENERATING_REQUISITE, ex);
            }
        }
    }
    
    public InputStream getTransferReport(String transferId)
    {
        return progressMonitor.getLogInputStream(transferId);
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public PolicyComponent getPolicyComponent()
    {
        return policyComponent;
    }

    /**
     * When a new node is created as a child of a Transferred or Alien node then
     * the new node needs to be marked as an alien. 
     * <p>
     * Then the tree needs to be walked upwards to mark all parent 
     * transferred nodes as alien.
     */
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef,
            boolean isNewNode)
    {
        
        log.debug("on create child association to transferred node");
        
        final String localRepositoryId = descriptorService.getCurrentRepositoryDescriptor().getId();
        alienProcessor.onCreateChild(childAssocRef, localRepositoryId, isNewNode);   
    }
 
    /**
     * When an alien node is deleted the it may be the last alien invader
     * <p>
     * Walk the tree checking the invasion status!
     */
    public void beforeDeleteNode(NodeRef deletedNodeRef)
    {
        log.debug("on delete node - need to check for transferred node");
        alienProcessor.beforeDeleteAlien(deletedNodeRef, null);
    }
    
    /**
     * When a transferred node is restored it may be a new invader or it may no 
     * longer be an invader.
     * <p>
     * Walk the tree checking the invasion status!
     */
    public void onRestoreNode(ChildAssociationRef childAssocRef)
    {
        log.debug("on restore node");
        log.debug("restoredAssocRef:" + childAssocRef);
        alienProcessor.afterMoveAlien(childAssocRef);
    }
    
    /**
     * When an alien node is moved it may un-invade its old location and invade a new 
     * location.   The node may also cease to be alien.
     */
    public void onMoveNode(ChildAssociationRef oldChildAssocRef,
            ChildAssociationRef newChildAssocRef)
    {

        log.debug("onMoveNode"); 
        log.debug("oldChildAssocRef:" + oldChildAssocRef);
        log.debug("newChildAssocRef:" + newChildAssocRef);
        
        NodeRef oldParentRef = oldChildAssocRef.getParentRef();
        NodeRef newParentRef = newChildAssocRef.getParentRef();
        
        if(newParentRef.equals(oldParentRef))
        {
            log.debug("old parent and new parent are the same - this is a rename, do nothing");
        }
        else
        {
            if(log.isDebugEnabled())
            {
                log.debug("moving node from oldParentRef:" + oldParentRef +" to:" + newParentRef);
            }
            alienProcessor.beforeDeleteAlien(newChildAssocRef.getChildRef(), oldChildAssocRef);
            alienProcessor.afterMoveAlien(newChildAssocRef);
        }
    }
    
    /**
     * When a transferred node is copied,  don't copy the transferred aspect.
     */
    public CopyBehaviourCallback onCopyTransferred(QName classRef,
            CopyDetails copyDetails)
    {
        return TransferredAspectCopyBehaviourCallback.INSTANCE;   
    }
    
    /**
     * When an alien node is copied,  don't copy the alien aspect.
     */
    public CopyBehaviourCallback onCopyAlien(QName classRef,
            CopyDetails copyDetails)
    {
        return AlienAspectCopyBehaviourCallback.INSTANCE;   
    }
        
    /**
     * Extends the default copy behaviour to prevent copying of transferred aspect and properties.
     * 
     * @author Mark Rogers
     * @since 3.4
     */
    private static class TransferredAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private static final CopyBehaviourCallback INSTANCE = new TransferredAspectCopyBehaviourCallback();
        
        /**
         * @return          Returns an empty map
         */
        @Override
        public Map<QName, Serializable> getCopyProperties(
                QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties)
        {
            return Collections.emptyMap();
        }
        
        /**
         * Don't copy the transferred aspect.
         * 
         * @return          Returns <tt>true</tt> always
         */
        @Override
        public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
        {
            if(classQName.equals(TransferModel.ASPECT_TRANSFERRED))
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }
    
    /**
     * Extends the default copy behaviour to prevent copying of alien aspect and properties.
     * 
     * @author Mark Rogers
     * @since 3.4
     */
    private static class AlienAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private static final CopyBehaviourCallback INSTANCE = new AlienAspectCopyBehaviourCallback();
        
        /**
         * @return          Returns an empty map
         */
        @Override
        public Map<QName, Serializable> getCopyProperties(
                QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties)
        {
            return Collections.emptyMap();
        }
        
        /**
         * Don't copy the transferred aspect.
         * 
         * @return          Returns <tt>true</tt> always
         */
        @Override
        public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
        {
            if(classQName.equals(TransferModel.ASPECT_ALIEN))
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }

       
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    public DescriptorService getDescriptorService()
    {
        return descriptorService;
    }
    
    public void setAlienProcessor(AlienProcessor alienProcessor)
    {
        this.alienProcessor = alienProcessor;
    }

    public AlienProcessor getAlienProcessor()
    {
        return alienProcessor;
    }

    @Override
    public void onContentUpdate(NodeRef nodeRef, boolean newContent)
    {
        /**
         *  On update of a transferred node remove the from content from property.
         */
        log.debug("on content update called:" + nodeRef);
        if(newContent)
        {
            log.debug("new content remove PROP_FROM_CONTENT from node:" + nodeRef);
            nodeService.setProperty(nodeRef, TransferModel.PROP_FROM_CONTENT, null);
        }
    }
}
