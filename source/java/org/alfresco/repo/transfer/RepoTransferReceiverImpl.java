/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transfer.manifest.TransferManifestProcessor;
import org.alfresco.repo.transfer.manifest.XMLTransferManifestReader;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferReceiver;
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
public class RepoTransferReceiverImpl implements TransferReceiver
{
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
                throw new TransferException(MSG_FAILED_TO_CREATE_STAGING_FOLDER, new Object[] {transferId});
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
                            SearchService.LANGUAGE_LUCENE, "PATH:\"" + transferLockFolderPath + "\"");
                    if (rs.length() > 0)
                    {
                        transferLockFolder = rs.getNodeRef(0);
                    } else
                    {
                        throw new TransferException(MSG_TRANSFER_LOCK_FOLDER_NOT_FOUND, new Object[] {transferLockFolderPath});
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
                            SearchService.LANGUAGE_LUCENE, "PATH:\"" + transferTempFolderPath + "\"");
                    if (rs.length() > 0)
                    {
                        transferTempFolder = rs.getNodeRef(0);
                    } else
                    {
                        throw new TransferException(MSG_TRANSFER_TEMP_FOLDER_NOT_FOUND, new Object[] {transferId, transferTempFolderPath});
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
                    ContentModel.TYPE_FOLDER, props).getChildRef();
        } else
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
        log.debug("start");
        final NodeRef relatedTransferRecord = createTransferRecord();
        final NodeRef lockFolder = getLockFolder();

        RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();
        try
        {
            txHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Throwable
                {
                    Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                    props.put(ContentModel.PROP_NAME, LOCK_FILE_NAME);
                    props.put(TransferModel.PROP_TRANSFER_ID, relatedTransferRecord.toString());

                    log.error("Creating transfer lock associated with this transfer record: " + relatedTransferRecord);
                    ChildAssociationRef assoc = nodeService.createNode(lockFolder, ContentModel.ASSOC_CONTAINS,
                            LOCK_QNAME, TransferModel.TYPE_TRANSFER_LOCK, props);
                    log.error("Transfer lock created as node " + assoc.getChildRef());
                    return assoc.getChildRef();
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
                            SearchService.LANGUAGE_LUCENE, "PATH:\"" + inboundTransferRecordsPath + "\"");
                    if (rs.length() > 0)
                    {
                        inboundTransferRecordsFolder = rs.getNodeRef(0);
                        log.debug("Found inbound transfer records folder: " + inboundTransferRecordsFolder);
                    } else
                    {
                        throw new TransferException(MSG_INBOUND_TRANSFER_FOLDER_NOT_FOUND, new Object[] {inboundTransferRecordsPath});
                    }
                }
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmssSSSZ");
        String timeNow = format.format(new Date());
        QName recordName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, timeNow);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, timeNow);

        log.debug("Creating transfer record with name: " + timeNow);
        ChildAssociationRef assoc = nodeService.createNode(inboundTransferRecordsFolder, ContentModel.ASSOC_CONTAINS,
                recordName, ContentModel.TYPE_CONTENT, props);
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
        log.debug("end transferId:" + transferId);
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
                                    throw new TransferException(MSG_NOT_LOCK_OWNER, new Object[] {transferId});
                                }
                                // Delete the lock node.
                                log.debug("delete lock node :" + lockId);
                                nodeService.deleteNode(lockId);

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

    public void abort(String transferId) throws TransferException
    {
        //TODO Think about the relationship between abort and end.
        end(transferId);
    }

    public void prepare(String transferId) throws TransferException
    {
    }

    /**
     * @param stagingFolder
     */
    private void deleteFile(File file)
    {
        if (!file.isDirectory()) file.delete();
        File[] fileList = file.listFiles();
        if (fileList != null) {
            for (File currentFile : fileList) {
                deleteFile(currentFile);
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
        log.debug("save snapshot transferId=" + transferId);
        // Check that this transfer owns the lock and give it a nudge to stop it expiring
        nudgeLock(transferId);
        File snapshotFile = new File(getStagingFolder(transferId), SNAPSHOT_FILE_NAME);
        try
        {
            if (snapshotFile.createNewFile())
            {
                FileCopyUtils.copy(openStream, new FileOutputStream(snapshotFile));
            }
            log.debug("saved snapshot for transferId=" + transferId);
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

    public void commit(String transferId) throws TransferException
    {
        log.debug("commit transferId=" + transferId);
        try
        {
            nudgeLock(transferId);
            List<TransferManifestProcessor> commitProcessors = manifestProcessorFactory.getCommitProcessors(this, transferId);
            
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser parser = saxParserFactory.newSAXParser();
            File snapshotFile = getSnapshotFile(transferId);

            if (snapshotFile.exists())
            {
                log.debug("processing manifest file:" + snapshotFile.getAbsolutePath());
                //We parse the file as many times as we have processors
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
                log.debug("no snapshot received");
                throw new TransferException(MSG_NO_SNAPSHOT_RECEIVED);
            }
            
            
            /**
             * Successfully transfred
             */
            log.debug("commit success transferId=" + transferId);
            
        } 
        catch (TransferException ex)
        {
            log.debug("unable to commit", ex);
            throw ex;
        } 
        catch (Exception ex)
        {
            log.debug("unable to commit", ex);
            throw new TransferException(MSG_ERROR_WHILE_COMMITTING_TRANSFER, ex);
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
     * @param transferTempFolderPath the transferTempFolderPath to set
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
     * @param manifestProcessorFactory the manifestProcessorFactory to set
     */
    public void setManifestProcessorFactory(ManifestProcessorFactory manifestProcessorFactory)
    {
        this.manifestProcessorFactory = manifestProcessorFactory;
    }

    /**
     * @param behaviourFilter the behaviourFilter to set
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

}
