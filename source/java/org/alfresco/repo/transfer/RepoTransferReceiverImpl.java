/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.transfer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transfer.manifest.TransferManifestProcessor;
import org.alfresco.repo.transfer.manifest.XMLTransferManifestReader;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.service.cmr.transfer.TransferProgress.Status;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.PropertyCheck;
import org.springframework.util.FileCopyUtils;

/**
 * @author brian
 * 
 */
public class RepoTransferReceiverImpl implements TransferReceiver
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
    private TransferProgressMonitor progressMonitor;
    private ActionService actionService;

    private NodeRef transferLockFolder;
    private NodeRef transferTempFolder;
    private NodeRef inboundTransferRecordsFolder;

    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "searchService", searchService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "transferLockFolderPath", transferLockFolderPath);
        PropertyCheck.mandatory(this, "inboundTransferRecordsPath", inboundTransferRecordsPath);
        PropertyCheck.mandatory(this, "rootStagingDirectory", rootStagingDirectory);
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
        // Have we already resolved the node that is the parent of the lock node?
        // If not then do so.
        if (transferLockFolder == null)
        {
            synchronized (this)
            {
                if (transferLockFolder == null)
                {
                    ResultSet rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                            SearchService.LANGUAGE_XPATH, transferLockFolderPath);
                    if (rs.length() > 0)
                    {
                        transferLockFolder = rs.getNodeRef(0);
                    }
                    else
                    {
                        throw new TransferException(MSG_TRANSFER_LOCK_FOLDER_NOT_FOUND,
                                new Object[] { transferLockFolderPath });
                    }
                }
            }
        }
        return transferLockFolder;

    }

    public NodeRef getTempFolder(String transferId)
    {
        // Have we already resolved the node that is the temp folder?
        // If not then do so.
        if (transferTempFolder == null)
        {
            synchronized (this)
            {
                if (transferTempFolder == null)
                {
                    ResultSet rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                            SearchService.LANGUAGE_XPATH, transferTempFolderPath);
                    if (rs.length() > 0)
                    {
                        transferTempFolder = rs.getNodeRef(0);
                    }
                    else
                    {
                        throw new TransferException(MSG_TRANSFER_TEMP_FOLDER_NOT_FOUND, new Object[] { transferId,
                                transferTempFolderPath });
                    }
                }
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
        NodeRef relatedTransferRecord = null;

        RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();
        try
        {
            relatedTransferRecord = txHelper.doInTransaction(
                    new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                    {
                        public NodeRef execute() throws Throwable
                        {
                            final NodeRef relatedTransferRecord = createTransferRecord();
                            getTempFolder(relatedTransferRecord.toString());

                            Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                            props.put(ContentModel.PROP_NAME, LOCK_FILE_NAME);
                            props.put(TransferModel.PROP_TRANSFER_ID, relatedTransferRecord.toString());

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
                            return relatedTransferRecord;
                        }
                    }, false, true);
        }
        catch (DuplicateChildNodeNameException ex)
        {
            log.debug("lock is already taken");
            // lock is already taken.
            throw new TransferException(MSG_TRANSFER_LOCK_UNAVAILABLE);
        }
        String transferId = relatedTransferRecord.toString();
        getStagingFolder(transferId);
        return transferId;
    }

    /**
     * @return
     */
    private NodeRef createTransferRecord()
    {
        log.debug("->createTransferRecord");
        if (inboundTransferRecordsFolder == null)
        {
            synchronized (this)
            {
                if (inboundTransferRecordsFolder == null)
                {
                    log.debug("Trying to find transfer records folder: " + inboundTransferRecordsPath);
                    ResultSet rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                            SearchService.LANGUAGE_XPATH, inboundTransferRecordsPath);
                    if (rs.length() > 0)
                    {
                        inboundTransferRecordsFolder = rs.getNodeRef(0);
                        log.debug("Found inbound transfer records folder: " + inboundTransferRecordsFolder);
                    }
                    else
                    {
                        throw new TransferException(MSG_INBOUND_TRANSFER_FOLDER_NOT_FOUND,
                                new Object[] { inboundTransferRecordsPath });
                    }
                }
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSSZ");
        String timeNow = format.format(new Date());
        QName recordName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, timeNow);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, timeNow);
        props.put(TransferModel.PROP_PROGRESS_POSITION, 0);
        props.put(TransferModel.PROP_PROGRESS_ENDPOINT, 1);
        props.put(TransferModel.PROP_TRANSFER_STATUS, TransferProgress.Status.PRE_COMMIT.toString());

        log.debug("Creating transfer record with name: " + timeNow);
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
            throw new IllegalArgumentException("transferId = " + transferId);
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
                                log.debug("delete lock node :" + lockId);
                                nodeService.deleteNode(lockId);
                                log.debug("lock deleted :" + lockId);
                            }
                            return null;
                        }
                    }, false, true);
            log.debug("delete staging folder " + transferId);
            // Delete the staging folder.
            File stagingFolder = getStagingFolder(transferId);
            deleteFile(stagingFolder);
            log.debug("Staging folder deleted");
        }
        catch (TransferException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new TransferException(MSG_ERROR_WHILE_ENDING_TRANSFER, ex);
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
            throw new TransferException(MSG_ERROR_WHILE_STAGING_SNAPSHOT, ex);
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
            throw new TransferException(MSG_ERROR_WHILE_STAGING_CONTENT, ex);
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
                            behaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
                            try
                            {
                                parser.parse(snapshotFile, reader);
                            }
                            finally
                            {
                                behaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
                            }
                            nudgeLock(transferId);
                            parser.reset();
                        }
                    }
                    else
                    {
                        progressMonitor.log(transferId, "Unable to start commit. No snapshot file received",
                                new TransferException(MSG_NO_SNAPSHOT_RECEIVED));
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
                    throw new TransferException(MSG_ERROR_WHILE_COMMITTING_TRANSFER, error);
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
            /**
             * Clean up at the end of the transfer
             */
            try
            {
                log.debug("calling end");
                end(transferId);
                log.debug("called end");
            }
            catch (Exception ex)
            {
                log.error("Failed to clean up transfer. Lock may still be in place: " + transferId);
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
        this.progressMonitor = progressMonitor;
    }

    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

}
