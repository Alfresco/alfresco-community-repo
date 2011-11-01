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
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferProgress.Status;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.service.cmr.transfer.TransferServicePolicies;
import org.alfresco.service.cmr.transfer.TransferServicePolicies.BeforeStartInboundTransferPolicy;
import org.alfresco.service.cmr.transfer.TransferServicePolicies.OnEndInboundTransferPolicy;
import org.alfresco.service.cmr.transfer.TransferServicePolicies.OnStartInboundTransferPolicy;
import org.alfresco.service.cmr.transfer.TransferVersion;
import org.alfresco.service.descriptor.Descriptor;
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
 * The Repo Transfer Receiver is the "Back-End" for transfer subsystem.
 * <p>
 * Provides the implementation of the transfer commands on the destination repository.
 * <p>
 * Provides callback handlers for Aliens and Transferred Aspects.
 * <p>
 * Calls transfer policies.
 * <p>
 * Co-ordinates locking and logging as the transfer progresses.
 *
 * @author brian
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
    private static final String MSG_ERROR_WHILE_STARTING = "transfer_service.receiver.error_start";
    private static final String MSG_TRANSFER_TEMP_FOLDER_NOT_FOUND = "transfer_service.receiver.temp_folder_not_found";
    private static final String MSG_TRANSFER_LOCK_UNAVAILABLE = "transfer_service.receiver.lock_unavailable";
    private static final String MSG_INBOUND_TRANSFER_FOLDER_NOT_FOUND = "transfer_service.receiver.record_folder_not_found";

    private static final String MSG_ERROR_WHILE_ENDING_TRANSFER = "transfer_service.receiver.error_ending_transfer";
    private static final String MSG_ERROR_WHILE_STAGING_SNAPSHOT = "transfer_service.receiver.error_staging_snapshot";
    private static final String MSG_ERROR_WHILE_STAGING_CONTENT = "transfer_service.receiver.error_staging_content";
    private static final String MSG_NO_SNAPSHOT_RECEIVED = "transfer_service.receiver.no_snapshot_received";
    private static final String MSG_ERROR_WHILE_COMMITTING_TRANSFER = "transfer_service.receiver.error_committing_transfer";
    private static final String MSG_ERROR_WHILE_GENERATING_REQUISITE = "transfer_service.receiver.error_generating_requisite";
    private static final String MSG_LOCK_TIMED_OUT = "transfer_service.receiver.lock_timed_out";
    private static final String MSG_LOCK_NOT_FOUND = "transfer_service.receiver.lock_not_found";
    private static final String MSG_TRANSFER_TO_SELF = "transfer_service.receiver.error.transfer_to_self";
    private static final String MSG_INCOMPATIBLE_VERSIONS = "transfer_service.incompatible_versions";

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
    private JobLockService jobLockService;
    private TransferVersionChecker transferVersionChecker;

    /**
     * Where the temporary files are stored.    Tenant Domain Name, NodeRef
     */
    private Map<String,NodeRef> transferTempFolderMap = new ConcurrentHashMap<String, NodeRef>();

    /**
     * Where the destination side transfer report is generated.    Tenant Domain Name, NodeRef
     */
    private Map<String,NodeRef> inboundTransferRecordsFolderMap = new ConcurrentHashMap<String, NodeRef>();

    private ClassPolicyDelegate<BeforeStartInboundTransferPolicy> beforeStartInboundTransferDelegate;
    private ClassPolicyDelegate<OnStartInboundTransferPolicy> onStartInboundTransferDelegate;
    private ClassPolicyDelegate<OnEndInboundTransferPolicy> onEndInboundTransferDelegate;

    /**
     * Locks for the transfers in progress
     * <p>
     * TransferId, Lock
     */
    private Map<String, Lock> locks = new ConcurrentHashMap<String, Lock>();

    /**
     * How many mS before refreshing the lock?
     */
    private long lockRefreshTime = 60000;

    /**
     * How many times to retry to obtain the lock
     */
    private int lockRetryCount = 2;

    /**
     * How long to wait between retries
     */
    private long lockRetryWait = 100;

    /**
     * How long in mS to keep the lock before giving up and ending the transfer,
     * possibly the client has terminated?
     */
    private long lockTimeOut = 3600000;

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
        PropertyCheck.mandatory(this, "jobLockService", getJobLockService());
        PropertyCheck.mandatory(this, "transferVersionChecker", getTransferVersionChecker());

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
    public String start(String fromRepositoryId, boolean transferToSelf, TransferVersion fromVersion)
    {
        log.debug("Start transfer");

        /**
         * Check that transfer is allowed to this repository
         */
        checkTransfer(fromRepositoryId, transferToSelf);

        /**
         * Check that the versions are compatible
         */
        TransferVersion toVersion = getVersion();

        if(!getTransferVersionChecker().checkTransferVersions(fromVersion, toVersion))
        {
            throw new TransferException(MSG_INCOMPATIBLE_VERSIONS, new Object[] {"None", fromVersion, toVersion});
        }

        /**
         * First get the transfer lock for this domain
         */
        String tenantDomain = tenantService.getUserDomain(AuthenticationUtil.getRunAsUser());
        String lockStr = tenantDomain.isEmpty() ? "transfer.server.default" : "transfer.server.tenant." + tenantDomain;
        QName lockQName = QName.createQName(TransferModel.TRANSFER_MODEL_1_0_URI, lockStr);
        Lock lock = new Lock(lockQName);

        try
        {
            TransferServicePolicies.BeforeStartInboundTransferPolicy beforeStartPolicy =
                beforeStartInboundTransferDelegate.get(TransferModel.TYPE_TRANSFER_RECORD);
            beforeStartPolicy.beforeStartInboundTransfer();

            lock.makeLock();

            /**
             * Transfer Lock held if we get this far
             */
            String transferId = null;

            try
            {
                /**
                 * Now create a transfer record and use its NodeRef as the transfer id
                 */
                RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();

                transferId = txHelper.doInTransaction(
                    new RetryingTransactionHelper.RetryingTransactionCallback<String>()
                        {
                            public String execute() throws Throwable
                        {
                            final NodeRef relatedTransferRecord = createTransferRecord();
                            String transferId = relatedTransferRecord.toString();
                            getTempFolder(transferId);
                            getStagingFolder(transferId);

                            TransferServicePolicies.OnStartInboundTransferPolicy onStartPolicy =
                                onStartInboundTransferDelegate.get(TransferModel.TYPE_TRANSFER_RECORD);
                            onStartPolicy.onStartInboundTransfer(transferId);

                            return transferId;
                        }
                    }, false, true);
            }
            catch (Exception e)
            {
                log.debug("Exception while staring transfer", e);
                log.debug("releasing lock - we never created the transfer id");
                lock.releaseLock();
                throw new TransferException(MSG_ERROR_WHILE_STARTING, e);
            }

            /**
             * Here if we have begun a transfer and have a valid transfer id
             */
            lock.transferId = transferId;
            locks.put(transferId, lock);
            log.info("transfer started:" + transferId);
            lock.enableLockTimeout();
            return transferId;

        }
        catch (LockAcquisitionException lae)
        {
            log.debug("transfer lock is already taken", lae);
            // lock is already taken.
            throw new TransferException(MSG_TRANSFER_LOCK_UNAVAILABLE);
        }
    }

    /**
     * @return
     */
    private NodeRef createTransferRecord()
    {
        log.debug("Receiver createTransferRecord");
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

    /**
     * Timeout a transfer. Called after the lock has been released via a timeout.
     *
     * This is the last chance to clean up.
     *
     * @param transferId
     */
    private void timeout(final String transferId)
    {
        log.info("Inbound Transfer has timed out transferId:" + transferId);
        /*
         * There is no transaction or authentication context in this method since it is called via a
         * timer thread.
         */
        final RetryingTransactionCallback<Void> timeoutCB = new RetryingTransactionCallback<Void>() {


            public Void execute() throws Throwable
            {
                TransferProgress progress = getProgressMonitor().getProgress(transferId);

                if (progress.getStatus().equals(TransferProgress.Status.PRE_COMMIT))
                {
                    log.warn("Inbound Transfer Lock Timeout - transferId:" + transferId);
                    /**
                     * Did not get out of PRE_COMMIT.   The client has probably "gone away" after calling
                     * "start", but before calling commit, cancel or error.
                     */
                    locks.remove(transferId);
                    removeTempFolders(transferId);
                    Object[] msgParams = { transferId };
                    getProgressMonitor().logException(transferId, "transfer timeout", new TransferException(MSG_LOCK_TIMED_OUT, msgParams));
                    getProgressMonitor().updateStatus(transferId, TransferProgress.Status.ERROR);
                }
                else
                {
                    // We got beyond PRE_COMMIT, therefore leave the clean up to either
                    // commit, cancel or error command, since there may still be "in-flight"
                    // transfer in another thread.   Although why, in that case, are we here?
                    log.warn("Inbound Transfer Lock Timeout - already past PRE-COMMIT - do no cleanup transferId:" + transferId);
                }
                return null;
            }
        };

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>()
        {
            public String doWork() throws Exception
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(timeoutCB, false, true);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
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
            Lock lock = locks.get(transferId);
            if(lock != null)
            {
                log.debug("releasing lock:" + lock.lockToken);
                lock.releaseLock();
                locks.remove(lock);
            }

            removeTempFolders(transferId);


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

    private void removeTempFolders(final String transferId)
    {
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
    }


    public void cancel(String transferId) throws TransferException
    {
        // no need to check the lock
        TransferProgress progress = getProgressMonitor().getProgress(transferId);
        getProgressMonitor().updateStatus(transferId, TransferProgress.Status.CANCELLED);
        if (progress.getStatus().equals(TransferProgress.Status.PRE_COMMIT))
        {
            end(transferId);
        }
    }

    public void prepare(String transferId) throws TransferException
    {
        // Check that this transfer still owns the lock
        Lock lock = checkLock(transferId);
        try
        {

        }
        finally
        {
            lock.enableLockTimeout();
        }

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

    public Lock checkLock(final String transferId) throws TransferException
    {
        if (transferId == null)
        {
            throw new IllegalArgumentException("nudgeLock: transferId = null");
        }

        Lock lock = locks.get(transferId);
        if(lock != null)
        {
            if(lock.isActive())
            {
                lock.suspendLockTimeout();
                return lock;
            }
            else
            {
                // lock is no longer active
                log.debug("lock not active");
                throw new TransferException(MSG_LOCK_TIMED_OUT, new Object[]{transferId});

            }
        }
        else
        {
            log.debug("lock not found");
            throw new TransferException(MSG_LOCK_NOT_FOUND, new Object[]{transferId});
            // lock not found
        }
    }

    public void saveSnapshot(String transferId, InputStream openStream) throws TransferException
    {
        // Check that this transfer still owns the lock
        Lock lock = checkLock(transferId);
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("Saving snapshot for transferId =" + transferId);
            }

            File snapshotFile = new File(getStagingFolder(transferId), SNAPSHOT_FILE_NAME);
            try
            {
                if (snapshotFile.createNewFile())
                {
                    FileCopyUtils.copy(openStream, new BufferedOutputStream(new FileOutputStream(snapshotFile)));
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
        finally
        {
            lock.enableLockTimeout();
        }
    }

    public void saveContent(String transferId, String contentFileId, InputStream contentStream)
            throws TransferException
    {
        Lock lock = checkLock(transferId);
        try
        {
            File stagedFile = new File(getStagingFolder(transferId), contentFileId);
            if (stagedFile.createNewFile())
            {
                FileCopyUtils.copy(contentStream, new BufferedOutputStream(new FileOutputStream(stagedFile)));
            }
        }
        catch (Exception ex)
        {
            throw new TransferException(MSG_ERROR_WHILE_STAGING_CONTENT, new Object[]{transferId, contentFileId}, ex);
        }
        finally
        {
            lock.enableLockTimeout();
        }
    }

    public void commitAsync(String transferId)
    {
        /**
         * A side-effect of checking the lock here is that the lock timeout is suspended.
         *
         */
        Lock lock = checkLock(transferId);
        try
        {
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
        catch (Exception error)
        {
            /**
             * Error somewhere in the action service?
             */
            //TODO consider whether the methods in this class should be retried/retryable..

            // need to re-enable the lock timeout otherwise we will hold the lock forever...
            lock.enableLockTimeout();

            throw new TransferException(MSG_ERROR_WHILE_COMMITTING_TRANSFER, new Object[]{transferId}, error);
        }

        /**
         * Lock intentionally not re-enabled here
         */
    }

    public void commit(final String transferId) throws TransferException
    {
        if (log.isDebugEnabled())
        {
            log.debug("Committing transferId=" + transferId);
        }

        /**
         * A side-effect of checking the lock here is that it ensures that the lock timeout is suspended.
         */
        checkLock(transferId);

        /**
         * Turn off rules while transfer is being committed.
         */
        boolean rulesEnabled = ruleService.isEnabled();
        ruleService.disableRules();

        try
        {
            /* lock is going to be released */ checkLock(transferId);
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
                            behaviourFilter.disableBehaviour();

                            try
                            {
                                parser.parse(snapshotFile, reader);
                            }
                            finally
                            {
                                behaviourFilter.enableBehaviour();
                            }
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

    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    public JobLockService getJobLockService()
    {
        return jobLockService;
    }

    public void setLockRetryCount(int lockRetryCount)
    {
        this.lockRetryCount = lockRetryCount;
    }

    public int getLockRetryCount()
    {
        return lockRetryCount;
    }

    public void setLockRetryWait(long lockRetryWait)
    {
        this.lockRetryWait = lockRetryWait;
    }

    public long getLockRetryWait()
    {
        return lockRetryWait;
    }

    public void setLockTimeOut(long lockTimeOut)
    {
        this.lockTimeOut = lockTimeOut;
    }

    public long getLockTimeOut()
    {
        return lockTimeOut;
    }

    public void setLockRefreshTime(long lockRefreshTime)
    {
        this.lockRefreshTime = lockRefreshTime;
    }

    public long getLockRefreshTime()
    {
        return lockRefreshTime;
    }

    /**
     * A Transfer Lock
     */
    private class Lock implements JobLockService.JobLockRefreshCallback
    {
        /**
         * The name of the lock - unique for each domain
         */
        QName lockQName;

        /**
         * The unique token for this lock instance.
         */
        String lockToken;

        /**
         * The transfer that this lock belongs to.
         */
        String transferId;

        /**
         * Is the lock active ?
         */
        private boolean active = false;

        /**
         * Is the server processing ?
         */
        private boolean processing = false;

        /**
         * When did we last check whether the lock is active
         */
        long lastActive = System.currentTimeMillis();

        public Lock(QName lockQName)
        {
            this.lockQName = lockQName;
        }


        /**
         * Make the lock - called on main thread
         *
         * @throws LockAquisitionException
         */
        public synchronized void makeLock()
        {
            if(log.isDebugEnabled())
            {
                log.debug("makeLock" + lockQName);
            }

            lockToken = getJobLockService().getLock(lockQName, getLockRefreshTime(), getLockRetryWait(), getLockRetryCount());

            // Got the lock, so mark as active
            active = true;

            if (log.isDebugEnabled())
            {
                log.debug("lock taken: name" + lockQName + " token:" +lockToken);
                log.debug("register lock callback, target lock refresh time :" + getLockRefreshTime());
            }
            getJobLockService().refreshLock(lockToken, lockQName, getLockRefreshTime(), this);
            if (log.isDebugEnabled())
            {
                log.debug("refreshLock callback registered");
            }
        }

        /**
         * Check that the lock is still active
         *
         * Called on main transfer thread as transfer proceeds.
         * @throws TransferException (Lock timeout)
         */
        public synchronized void suspendLockTimeout()
        {
            log.debug("suspend lock called");
            if (active)
            {
                processing = true;
            }
            else
            {
                // lock is no longer active
                log.debug("lock not active, throw timed out exception");
                throw new TransferException(MSG_LOCK_TIMED_OUT);
            }
        }

        public synchronized void enableLockTimeout()
        {
            long now = System.currentTimeMillis();
            // Update lastActive to 1S boundary
            if(now > lastActive + 1000L)
            {
                lastActive = now;
                log.debug("start waiting : lastActive:" + lastActive);
            }

            processing = false;
        }

        /**
         * Release the lock
         *
         * Called on main thread
         */
        public synchronized void releaseLock()
        {
            if(log.isDebugEnabled())
            {
                log.debug("transfer service about to releaseLock : " + lockQName);
            }

            if (active)
            {
                active = false;
                getJobLockService().releaseLock(lockToken, lockQName);
            }
        }

        /**
         * Called by Job Lock Service to determine whether the lock is still active
         */
        @Override
        public synchronized boolean isActive()
        {
            long now = System.currentTimeMillis();

            if(active)
            {
                if(!processing)
                {
                    if(now > lastActive + getLockTimeOut())
                    {
                        return false;
                    }
                }
            }

            if(log.isDebugEnabled())
            {
                log.debug("transfer service callback isActive: " + active);
            }

            return active;
        }

        /**
         * Called by Job Lock Service on release of the lock after time-out
         */
        @Override
        public synchronized void lockReleased()
        {
            if(active)
            {
                active = false;
                log.info("transfer service: lock has timed out, timeout :" + lockQName);
                timeout(transferId);
            }
        }
    }

    /**
     * Check Whether transfer is allowed from the specified repository.
     * Called prior to "begin".
     */

    private void checkTransfer(String fromRepository, boolean transferToSelf)
    {
        if(log.isDebugEnabled())
        {
            log.debug("checkTransfer fromRepository:" + fromRepository + ", transferToSelf:" + transferToSelf );
        }
        final String localRepositoryId = descriptorService.getCurrentRepositoryDescriptor().getId();

        if(!transferToSelf)
        {
            if(fromRepository != null)
            {
                if(fromRepository.equalsIgnoreCase(localRepositoryId))
                {
                    throw new TransferException(MSG_TRANSFER_TO_SELF);
                }
            }
            else
            {
                throw new TransferException("from repository id is missing");
            }
        }
    }

    public void setTransferVersionChecker(TransferVersionChecker transferVersionChecker)
    {
        this.transferVersionChecker = transferVersionChecker;
    }

    public TransferVersionChecker getTransferVersionChecker()
    {
        return transferVersionChecker;
    }

    @Override
    public TransferVersion getVersion()
    {
        Descriptor d = descriptorService.getServerDescriptor();
        // needs to be serverDescriptor to pick up versionEdition
        return new TransferVersionImpl(d);
    }

    public void setTransferRootNode(String rootFileSystem)
    {
        //just ignore, no relevant for transferring on file system
    }
}
