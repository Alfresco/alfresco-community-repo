package org.alfresco.filesys.repo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.alfresco.filesys.alfresco.ExtendedDiskInterface;
import org.alfresco.filesys.alfresco.RepositoryDiskInterface;
import org.alfresco.filesys.repo.FilesystemTransactionAdvice.PropagatingException;
import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.filesys.repo.rules.Operation;
import org.alfresco.filesys.repo.rules.OperationExecutor;
import org.alfresco.filesys.repo.rules.commands.CloseFileCommand;
import org.alfresco.filesys.repo.rules.commands.CompoundCommand;
import org.alfresco.filesys.repo.rules.commands.CopyContentCommand;
import org.alfresco.filesys.repo.rules.commands.CreateFileCommand;
import org.alfresco.filesys.repo.rules.commands.DeleteFileCommand;
import org.alfresco.filesys.repo.rules.commands.DoNothingCommand;
import org.alfresco.filesys.repo.rules.commands.OpenFileCommand;
import org.alfresco.filesys.repo.rules.commands.ReduceQuotaCommand;
import org.alfresco.filesys.repo.rules.commands.RemoveNoContentFileOnError;
import org.alfresco.filesys.repo.rules.commands.RemoveTempFileCommand;
import org.alfresco.filesys.repo.rules.commands.RenameFileCommand;
import org.alfresco.filesys.repo.rules.commands.ReturnValueCommand;
import org.alfresco.filesys.repo.rules.operations.CreateFileOperation;
import org.alfresco.filesys.repo.rules.operations.DeleteFileOperation;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.filesys.AccessMode;
import org.alfresco.jlan.server.filesys.FileAction;
import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.smb.SharingMode;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;

/**
 * Content Disk Driver Command Executor
 * <p>
 * Executes commands against the repository.
 */
public class CommandExecutorImpl implements CommandExecutor
{
    private static Log logger = LogFactory.getLog(CommandExecutorImpl.class);
    
    // Services go here.
    private TransactionService transactionService;
    private RepositoryDiskInterface repositoryDiskInterface;
    private ExtendedDiskInterface diskInterface;

    
    public void init()
    {
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "diskInterface", diskInterface);
        PropertyCheck.mandatory(this, "repositoryDiskInterface", getRepositoryDiskInterface()); 
    }
        
    @Override
    public Object execute(final SrvSession sess, final TreeConnection tree, final Command command) throws IOException
    {
        TxnReadState readState = command.getTransactionRequired();
        
        Object ret = null;
        
        // No transaction required.
        if(readState == TxnReadState.TXN_NONE)
        {
            ret = executeInternal(sess, tree, command, null);
        }
        else
        {
            // Yes a transaction is required.
            RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
     
            boolean readOnly = readState == TxnReadState.TXN_READ_ONLY;
        
            RetryingTransactionCallback<Object> cb =  new RetryingTransactionCallback<Object>()
            {
                /**
                 * Perform a set of commands as a unit of transactional work.
                 *
                 * @return              Return the result of the unit of work
                 * @throws Throwable    This can be anything and will guarantee either a retry or a rollback
                 */
                public Object execute() throws IOException
                {
                    try
                    {
                        return executeInternal(sess, tree, command, null);
                    }
                    catch (IOException e)
                    {
                        // Ensure original checked IOExceptions get propagated
                        throw new PropagatingException(e);
                    }
                }
            };
        
            try
            {
                ret = helper.doInTransaction(cb, readOnly);
            }
            catch(PropagatingException pe)
            {
                if(command instanceof CompoundCommand)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("post error", pe);
                    }
                    
                    CompoundCommand c = (CompoundCommand)command;
                    // Error Callback Here ?
                    List<Command> commands = c.getPostErrorCommands();
                
                    if(commands != null)
                    {
                        for(Command c2 : commands)
                        {
                            try
                            {
                                executeInternal(sess, tree, c2, ret);
                            }
                            catch(Throwable t)
                            {
                                logger.warn("caught and ignored exception from error handler", t);
                                // Swallow exception from error handler.
                            }
                        }
                    }
                }
                
                // Unwrap checked exceptions
                throw (IOException) pe.getCause();
            }
        }
        
        /**
         * execute post commit commands.
         */
        if(command instanceof CompoundCommand)
        {
            logger.debug("post commit of compound command");
            CompoundCommand c = (CompoundCommand)command;
            List<Command> commands = c.getPostCommitCommands();
            
            if(commands != null)
            {
                for(Command c2 : commands)
                {
                    // TODO - what about exceptions from post commit?             
                    executeInternal(sess, tree, c2, ret);
                }
            }
        }
        
        return ret;
    }
    
    /**
     * @param sess
     * @param tree
     * @param command
     * @param result
     * @return
     * @throws IOException
     */
    private Object executeInternal(SrvSession sess, TreeConnection tree, Command command, Object result) throws IOException
    {
        if(command instanceof CompoundCommand)
        {
           Object ret = null;
           logger.debug("compound command received");
           CompoundCommand x = (CompoundCommand)command;
                  
           for(Command compoundPart : x.getCommands())
           {
               logger.debug("running part of compound command");
               Object val = executeInternal(sess, tree, compoundPart, result);
               if(val != null)
               {
                   // Return the value from the last command.
                   ret = val;
               }
           }
           return ret;
        }
        else if(command instanceof CreateFileCommand)
        {
            logger.debug("create file command");
            CreateFileCommand create = (CreateFileCommand)command;
            return repositoryDiskInterface.createFile(create.getRootNode(), create.getPath(), create.getAllocationSize());
        }
        else if(command instanceof DeleteFileCommand)
        {
            logger.debug("delete file command");
            DeleteFileCommand delete = (DeleteFileCommand)command;
            diskInterface.deleteFile(sess, tree, delete.getPath());
        }
        else if(command instanceof OpenFileCommand)
        {
            logger.debug("open file command");
            OpenFileCommand o = (OpenFileCommand)command;
            int openAction = FileAction.OpenIfExists;
            
            OpenFileMode mode = o.getMode();
            int jlanAccessMode = 0;
            switch (mode)
            {
                case ATTRIBUTES_ONLY:
                    jlanAccessMode = AccessMode.ReadOnly;
                    break;
                case READ_ONLY:
                    jlanAccessMode = AccessMode.ReadOnly;
                    break;
                case READ_WRITE:
                    jlanAccessMode = AccessMode.ReadWrite;
                    break;
                case DELETE:
                    // Don't care file is being deleted
                    jlanAccessMode = AccessMode.ReadOnly;
                    break;
                case WRITE_ONLY:
                    jlanAccessMode = AccessMode.WriteOnly;
                    break;
            }
            
            FileOpenParams params = new FileOpenParams(o.getPath(), openAction, jlanAccessMode, FileAttribute.NTNormal, 0);
          
            if(logger.isDebugEnabled())
            {
                int sharedAccess = params.getSharedAccess();
                String strSharedAccess = SharingMode.getSharingModeAsString(sharedAccess);
                    
                logger.debug("openFile:" + o.getPath() 
                + ", isDirectory: " + params.isDirectory()
                + ", isStream: " + params.isStream()
                + ", readOnlyAccess: " + params.isReadOnlyAccess()
                + ", readWriteAccess: " + params.isReadWriteAccess()
                + ", writeOnlyAccess:" +params.isWriteOnlyAccess()
                + ", attributesOnlyAccess:" +params.isAttributesOnlyAccess()
                + ", sequentialAccessOnly:" + params.isSequentialAccessOnly()
                + ", requestBatchOpLock:" +params.requestBatchOpLock()
                + ", requestExclusiveOpLock:" +params.requestExclusiveOpLock()  
                + ", isDeleteOnClose:" +params.isDeleteOnClose()
                + ", allocationSize:" + params.getAllocationSize()
                + ", sharedAccess: " + strSharedAccess
                );
            }

            return diskInterface.openFile(sess, tree, params);
        }
        else if(command instanceof CloseFileCommand)
        {
            logger.debug("close file command");
            CloseFileCommand c = (CloseFileCommand)command;
            repositoryDiskInterface.closeFile(c.getRootNodeRef(), c.getPath(), c.getNetworkFile());
        }
        else if(command instanceof ReduceQuotaCommand)
        {
            logger.debug("reduceQuota file command");
            ReduceQuotaCommand r = (ReduceQuotaCommand)command;
            repositoryDiskInterface.reduceQuota(sess, tree, r.getNetworkFile());
        }
        else if(command instanceof RenameFileCommand)
        {
            logger.debug("rename command");
            RenameFileCommand rename = (RenameFileCommand)command;
            diskInterface.renameFile(sess, tree, rename.getFromPath(), rename.getToPath());    
        }
        else if(command instanceof CopyContentCommand)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("Copy content command - copy content");
            }
            CopyContentCommand copy = (CopyContentCommand)command;
            repositoryDiskInterface.copyContent(copy.getRootNode(), copy.getFromPath(), copy.getToPath());
        }
        else if(command instanceof DoNothingCommand)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("Do Nothing Command - doing nothing");
            }
        }
        else if(command instanceof ResultCallback)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("Result Callback");
            }
            ResultCallback callback = (ResultCallback)command;
            callback.execute(result);
        }
        else if(command instanceof RemoveTempFileCommand)
        {
            RemoveTempFileCommand r = (RemoveTempFileCommand)command;
            if(logger.isDebugEnabled())
            {
                logger.debug("Remove Temp File:" + r.getNetworkFile());
            }
            File file = r.getNetworkFile().getFile();
            file.delete();
        }
        else if(command instanceof ReturnValueCommand)
        {
            ReturnValueCommand r = (ReturnValueCommand)command;
            if(logger.isDebugEnabled())
            {
                logger.debug("Return value");
            }
            return r.getReturnValue();
        }
        else if(command instanceof  RemoveNoContentFileOnError)
        {
            RemoveNoContentFileOnError r = (RemoveNoContentFileOnError)command;
            if(logger.isDebugEnabled())
            {
                logger.debug("Remove no content file on error");
            }
            repositoryDiskInterface.deleteEmptyFile(r.getRootNodeRef(), r.getPath());
        }
        

        return null;
    }


    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }


    public TransactionService getTransactionService()
    {
        return transactionService;
    }

    public void setRepositoryDiskInterface(RepositoryDiskInterface repositoryDiskInterface)
    {
        this.repositoryDiskInterface = repositoryDiskInterface;
    }

    public RepositoryDiskInterface getRepositoryDiskInterface()
    {
        return repositoryDiskInterface;
    }
    
    public void setDiskInterface(ExtendedDiskInterface diskInterface)
    {
        this.diskInterface = diskInterface;
    }

    public ExtendedDiskInterface getDiskInterface()
    {
        return diskInterface;
    }
    
    /**
     * A wrapper for checked exceptions to be passed through the retrying transaction handler.
     */
    protected static class PropagatingException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        /**
         * @param cause
         */
        public PropagatingException(Throwable cause)
        {
            super(cause);
        }        
    }

}
