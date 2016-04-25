/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.filesys.repo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

import org.alfresco.filesys.alfresco.ExtendedDiskInterface;
import org.alfresco.filesys.alfresco.RepositoryDiskInterface;
import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.filesys.repo.rules.commands.CloseFileCommand;
import org.alfresco.filesys.repo.rules.commands.CompoundCommand;
import org.alfresco.filesys.repo.rules.commands.CopyContentCommand;
import org.alfresco.filesys.repo.rules.commands.CreateFileCommand;
import org.alfresco.filesys.repo.rules.commands.DeleteFileCommand;
import org.alfresco.filesys.repo.rules.commands.DoNothingCommand;
import org.alfresco.filesys.repo.rules.commands.MoveFileCommand;
import org.alfresco.filesys.repo.rules.commands.OpenFileCommand;
import org.alfresco.filesys.repo.rules.commands.ReduceQuotaCommand;
import org.alfresco.filesys.repo.rules.commands.RemoveNoContentFileOnError;
import org.alfresco.filesys.repo.rules.commands.RemoveTempFileCommand;
import org.alfresco.filesys.repo.rules.commands.RenameFileCommand;
import org.alfresco.filesys.repo.rules.commands.RestoreFileCommand;
import org.alfresco.filesys.repo.rules.commands.ReturnValueCommand;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
                 * @throws IOException
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
                        logger.debug("error executing command :command" + command, pe);
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
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param command Command
     * @param result Object
     * @return Object
     * @throws IOException
     */
    private Object executeInternal(SrvSession sess, TreeConnection tree, Command command, Object result) throws IOException
    {
        FileFilterMode.setClient(ClientHelper.getClient(sess));
        try
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
                return repositoryDiskInterface.createFile(create.getRootNode(), create.getPath(), create.getAllocationSize(), create.isHidden());
            }
            else if(command instanceof RestoreFileCommand)
            {
                logger.debug("restore file command");
                RestoreFileCommand restore = (RestoreFileCommand)command;
                return repositoryDiskInterface.restoreFile(sess, tree, restore.getRootNode(), restore.getPath(), restore.getAllocationSize(), restore.getOriginalNodeRef());
            }
            else if(command instanceof DeleteFileCommand)
            {
                logger.debug("delete file command");
                DeleteFileCommand delete = (DeleteFileCommand)command;
                return repositoryDiskInterface.deleteFile2(sess, tree, delete.getRootNode(), delete.getPath());
            }
            else if(command instanceof OpenFileCommand)
            {
                logger.debug("open file command");
                OpenFileCommand o = (OpenFileCommand)command;
                
                OpenFileMode mode = o.getMode();
                return repositoryDiskInterface.openFile(sess, tree, o.getRootNodeRef(), o.getPath(), mode, o.isTruncate());
                
            }
            else if(command instanceof CloseFileCommand)
            {
                logger.debug("close file command");
                CloseFileCommand c = (CloseFileCommand)command;
                return repositoryDiskInterface.closeFile(tree, c.getRootNodeRef(), c.getPath(), c.getNetworkFile());
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
                
                repositoryDiskInterface.renameFile(rename.getRootNode(), rename.getFromPath(), rename.getToPath(), rename.isSoft(), false);
            }
            else if(command instanceof MoveFileCommand)
            {
                logger.debug("move command");
                MoveFileCommand move = (MoveFileCommand)command;
                repositoryDiskInterface.renameFile(move.getRootNode(), move.getFromPath(), move.getToPath(), false, move.isMoveAsSystem());
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
                boolean isDeleted = file.delete();
           
                if(!isDeleted)
                {          
                    logger.debug("unable to delete temp file:" + r.getNetworkFile() + ", closed="+ r.getNetworkFile().isClosed());
 
                    /*
                     * Unable to delete temporary file
                     * Could be a bug with the file handle not being closed, but yourkit does not
                     * find anything awry.
                     * There are reported Windows JVM bugs such as 4715154 ... 
                     */
                    FileOutputStream fos = new FileOutputStream(file);
                    FileChannel outChan = null;
                    try
                    {
                        outChan = fos.getChannel();
                        outChan.truncate(0);
                    }
                    catch (IOException e)
                    {
                        logger.debug("unable to clean up file", e);
                    }
                    finally
                    {
                        if(outChan != null)
                        {
                            try
                            {
                                outChan.close();
                            }
                            catch(IOException e){}
                        }
                        fos.close();
                    }
                }
                              
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
        }
        finally
        {
            FileFilterMode.clearClient();
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
         * @param cause Throwable
         */
        public PropagatingException(Throwable cause)
        {
            super(cause);
        }        
    }

}
