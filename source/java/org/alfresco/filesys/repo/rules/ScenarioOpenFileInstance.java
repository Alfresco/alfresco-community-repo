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
package org.alfresco.filesys.repo.rules;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.filesys.repo.OpenFileMode;
import org.alfresco.filesys.repo.ResultCallback;
import org.alfresco.filesys.repo.TempNetworkFile;
import org.alfresco.filesys.repo.rules.ScenarioInstance.Ranking;
import org.alfresco.filesys.repo.rules.ScenarioRenameShuffleInstance.InternalState;
import org.alfresco.filesys.repo.rules.commands.CallbackCommand;
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
import org.alfresco.filesys.repo.rules.operations.CloseFileOperation;
import org.alfresco.filesys.repo.rules.operations.CreateFileOperation;
import org.alfresco.filesys.repo.rules.operations.DeleteFileOperation;
import org.alfresco.filesys.repo.rules.operations.OpenFileOperation;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An open file scenario is ...
 * <p>
 * 1) open(readOnly)
 * 2) close(readOnly)
 * <p>
 * 1) open(readOnly)
 * 2) open(readWrite)
 * 3) close(readOnly)
 * 4  close(readWrite) updates the repo
 * <p>
 * 1) open(readOnly)
 * 2) open(readWrite)
 * 3) open(readWrite) - does nothing.   Increments Open Count.
 * 4) close(readWrite) - does nothing.   Decrements Open Count.
 * 5) close(readWrite) - updates the repo.
 * 6) close(readOnly) - closes read only
 * <p>
 * 1) open (readWrite)
 * 2) open (readOnly)   - file already open for read/write
 * 3) close
 * 4) close
 * 
 */
class ScenarioOpenFileInstance implements ScenarioInstance, DependentInstance, ScenarioInstanceRenameAware
{
    private static Log logger = LogFactory.getLog(ScenarioOpenFileInstance.class);
      
    private Date startTime = new Date();
    
    private String name;
    
    enum InternalState
    {
        NONE,
        OPENING,
        OPEN,
        ERROR
    } ;
    
    InternalState state = InternalState.NONE;
    
    /**
     * For each read only open file  
     */
    private NetworkFile fileHandleReadOnly;
    private int openReadOnlyCount = 0;
    
    /**
     * For each read/write open file
     */
    private NetworkFile fileHandleReadWrite;
    private int openReadWriteCount = 0;
    
    /**
     * Timeout in ms.  Default 30 seconds.
     */
    private long timeout = 30000;
    
    private boolean isComplete = false;
    
    private Ranking ranking = Ranking.HIGH;
        
    /**
     * Evaluate the next operation
     * @param operation
     */
    public Command evaluate(Operation operation)
    {                
        /**
         * Anti-pattern : timeout - this scenario does not timeout
         */
        //        Date now = new Date();
        //        if(now.getTime() > startTime.getTime() + getTimeout())
        //        {
        //            if(logger.isDebugEnabled())
        //            {
        //                logger.debug("Instance timed out");
        //            }
        //        }
        
        /**
         * Anti Pattern - Delete of the open file.
         */
        if(operation instanceof DeleteFileOperation)
        {
            DeleteFileOperation d = (DeleteFileOperation)operation;
            
            if(d.getName() == null)
            {
                return null;
            }
            
            if(name.equalsIgnoreCase(d.getName()))
            {
                logger.debug("Anti-Pattern - delete of the open file, scenario:" + this);
                isComplete = true;
                return null;
            }
        }
        
        switch (state)
        {
            case NONE:
                if(operation instanceof CreateFileOperation)
                {
                    CreateFileOperation c = (CreateFileOperation)operation;
                    name = c.getName();
                    
                    if(name != null)
                    {
                        state = InternalState.OPENING;
                        logger.debug("Create File name:" + name);
                        ArrayList<Command> commands = new ArrayList<Command>();
                        ArrayList<Command> postCommitCommands = new ArrayList<Command>();
                        ArrayList<Command> postErrorCommands = new ArrayList<Command>();
                        commands.add(new CreateFileCommand(c.getName(), c.getRootNodeRef(), c.getPath(), c.getAllocationSize(), c.isHidden()));
                        postCommitCommands.add(newOpenFileCallbackCommand());
                        postErrorCommands.add(newOpenFileErrorCallbackCommand());
                        return new CompoundCommand(commands, postCommitCommands, postErrorCommands);
                    }
                }
                else if(operation instanceof OpenFileOperation)
                {
                    OpenFileOperation o = (OpenFileOperation)operation;
                    name = o.getName();
                    if(name != null)
                    {
                        state = InternalState.OPENING;
                        logger.debug("Open File name:" + name);
                        ArrayList<Command> commands = new ArrayList<Command>();
                        commands.add(new OpenFileCommand(o.getName(), o.getMode(), o.isTruncate(), o.getRootNodeRef(), o.getPath()));
                        ArrayList<Command> postCommitCommands = new ArrayList<Command>();
                        ArrayList<Command> postErrorCommands = new ArrayList<Command>();
                        postCommitCommands.add(newOpenFileCallbackCommand());
                        postErrorCommands.add(newOpenFileErrorCallbackCommand());
                        return new CompoundCommand(commands, postCommitCommands, postErrorCommands);
                    }
                }
                
                // Scenario Not Started
                logger.debug("Scenario not started - no name");
                isComplete = true;
                return null;
                
            case OPENING:
                
                if(operation instanceof OpenFileOperation)
                {
                    OpenFileOperation o = (OpenFileOperation)operation;
                    
                    if(o.getName() == null)
                    {
                        return null;
                    }
                    
                    if(name.equalsIgnoreCase(o.getName()))
                    {
                        /**
                         * TODO What to do here - one thread is in the middle of 
                         * opening a file while another tries to open the same file 
                         * sleep for a bit? then check state again?  What happens if file 
                         * closes while sleeping.   For now log an error.
                         */
                        logger.error("Second open while in opening state. :" + name);
//                        isComplete = true;
//                        return null;  
                    }
                }
                
                /**
                 * Anti-pattern : timeout - is this needed ?
                 */
                Date now = new Date();
                if(now.getTime() > startTime.getTime() + getTimeout())
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Instance in OPENING STATE timed out name" + name);
                    }
                    isComplete = true; 
                }
                return null;
                
            case ERROR:
                
                logger.debug("Open has failed :" + name);
                isComplete = true;
                return null; 
                
            case OPEN:
                
                if(operation instanceof RenameFileOperation)
                {
                    RenameFileOperation r = (RenameFileOperation)operation;
                    if(r.getFrom() == null)
                    {
                        return null;
                    }
                    
                    if(name.equalsIgnoreCase(r.getFrom()))
                    {
                        logger.warn("rename of an open file");
                    }
                }

                if(operation instanceof CloseFileOperation)
                {
                    CloseFileOperation c = (CloseFileOperation)operation;
                    
                    if(c.getName() == null)
                    {
                        return null;
                    }
                    
                    if(name.equalsIgnoreCase(c.getName()))
                    {
                        NetworkFile file = c.getNetworkFile();
                        if(isReadOnly(file))
                        {
                            // Read Only File
                            if(openReadOnlyCount == 1)
                            {
                                if(logger.isDebugEnabled())
                                {
                                    logger.debug("Close of last read only file handle:" + this);
                                }
                              
                                openReadOnlyCount = 0;
                                
                                if(openReadWriteCount <= 0)
                                {
                                    if(logger.isDebugEnabled())
                                    {
                                        logger.debug("Scenario is complete:" + this);
                                    }
                                    isComplete=true;
                                }
                              
                                if (file instanceof TempNetworkFile)
                                {
                                    logger.debug("this is the last close of a temp read only file");
                                    ArrayList<Command> commands = new ArrayList<Command>();
                                    ArrayList<Command> postCommitCommands = new ArrayList<Command>();
                                 
                                    commands.add(new CloseFileCommand(c.getName(), file, c.getRootNodeRef(), c.getPath()));
                                    postCommitCommands.add(new RemoveTempFileCommand((TempNetworkFile)file));
                                    return new CompoundCommand(commands, postCommitCommands);   
                                }
                                else
                                {
                                    return new CloseFileCommand(c.getName(), file, c.getRootNodeRef(), c.getPath());
                                }
                            }
                            
                            if(logger.isDebugEnabled())
                            {
                                logger.debug("Only decrement count of read only file handle:" + this);
                            }
                            
                            openReadOnlyCount--;
                            
                            return new DoNothingCommand();
                        }
                        else
                        {
                            // This is a close of a Read Write File
                            // Read Only File
                            if(openReadWriteCount == 1)
                            {
                                if(logger.isDebugEnabled())
                                {
                                    logger.debug("Close of last read write file handle:" + this);
                                }
                              
                                openReadWriteCount = 0;
                                
                                if(openReadOnlyCount <= 0)
                                {
                                    if(logger.isDebugEnabled())
                                    {
                                        logger.debug("Scenario is complete:" + this);
                                    }
                                    isComplete=true;
                                }
                                
                                
                                //             
                                ArrayList<Command> commands = new ArrayList<Command>();
                                ArrayList<Command> postCommitCommands = new ArrayList<Command>();
                                ArrayList<Command> postErrorCommands = new ArrayList<Command>();
                                
                                commands.add(new CloseFileCommand(c.getName(), file, c.getRootNodeRef(), c.getPath()));
                                
                                //postErrorCommands.add(new RemoveNoContentFileOnError(c.getName(), c.getRootNodeRef(), c.getPath()));
                                
                                if(c.isDeleteOnClose())
                                {
                                    postCommitCommands.add(new ReduceQuotaCommand(c.getName(), file, c.getRootNodeRef(), c.getPath()));
                                }
                                
                                if (file instanceof TempNetworkFile)
                                { 
                                    postCommitCommands.add(new RemoveTempFileCommand((TempNetworkFile)file));
                                }

                                return new CompoundCommand(commands, postCommitCommands, postErrorCommands);  
                        
                            }
                            
                            if(logger.isDebugEnabled())
                            {
                                logger.debug("Only decrement count of read write file handle:" + this);
                            }
                            
                            openReadWriteCount--;
                            
                            return new DoNothingCommand();
                        }
                    }
                }
                else if(operation instanceof OpenFileOperation)
                {
                    OpenFileOperation o = (OpenFileOperation)operation;
                    
                    if(o.getName() == null)
                    {
                        return null;
                    }
                    
                    if(name != null && name.equalsIgnoreCase(o.getName()))
                    {
                        if(o.getMode() == OpenFileMode.READ_WRITE)
                        {    
                            // This is an open of a read write access
                            if(openReadWriteCount == 0)
                            {
                                logger.debug("Open first read/write from scenario:" + this);
                                ArrayList<Command> commands = new ArrayList<Command>();
                                commands.add(new OpenFileCommand(o.getName(), o.getMode(), o.isTruncate(), o.getRootNodeRef(), o.getPath()));
                                ArrayList<Command> postCommitCommands = new ArrayList<Command>();
                                postCommitCommands.add(newOpenFileCallbackCommand());
                                return new CompoundCommand(commands, postCommitCommands);
                            }
                            else
                            {
                                // TODO Need a permission check here and increment post check
                                openReadWriteCount++;
                                logger.debug("Return already open read/write file handle from scenario:" + this);
                                return new ReturnValueCommand(fileHandleReadWrite);
                            }
                        }
                        else
                        {
                            // This is an open for read only access
                            
                            if(openReadWriteCount > 0)
                            {
                                //however the file is already open for read/write
                                openReadWriteCount++;
                                logger.debug("Return already open read/write file handle from scenario:" + this);
                                return new ReturnValueCommand(fileHandleReadWrite);
                            }
                            
                            if(openReadOnlyCount == 0)
                            {
                                logger.debug("Open first read only from scenario:" + this);
                                ArrayList<Command> commands = new ArrayList<Command>();
                                commands.add(new OpenFileCommand(o.getName(), o.getMode(), o.isTruncate(), o.getRootNodeRef(), o.getPath()));
                                ArrayList<Command> postCommitCommands = new ArrayList<Command>();
                                postCommitCommands.add(newOpenFileCallbackCommand());
                                return new CompoundCommand(commands, postCommitCommands);
                            }
                            else
                            {
                                openReadOnlyCount++;
                                logger.debug("Return already open only file handle from scenario:" + this);
                                return new ReturnValueCommand(fileHandleReadOnly);
                            }
                        }
                    }
                }
          
                break;
                
          }
                
        return null;
    }
    
    @Override
    public boolean isComplete()
    {
        return isComplete;
    }
    
    public String toString()
    {
        return "ScenarioOpenFileInstance name:" + name;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public long getTimeout()
    {
        return timeout;
    }
    
    @Override
    public Ranking getRanking()
    {
        return ranking;
    }
    
    public void setRanking(Ranking ranking)
    {
        this.ranking = ranking;
    }
    
    public String getName()
    {
        return name;
    }
    
    /**
     * Called for open file.
     */
    private ResultCallback newOpenFileCallbackCommand()
    {
        return new ResultCallback()
        {
            @Override
            public void execute(Object result)
            {
                if(result instanceof NetworkFile)
                {
               
                    // Now update the state of this scenario - we have an open fileHandle
                    NetworkFile fileHandle = (NetworkFile)result;
                    
                    state = InternalState.OPEN;
                    
                    if(isReadOnly(fileHandle))
                    {
                        openReadOnlyCount++;
                        fileHandleReadOnly=fileHandle;
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("file opened read only:" + result + ", name:" + name);
                        }
                    }
                    else
                    {
                        openReadWriteCount++;
                        fileHandleReadWrite=fileHandle;
                        
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("file opened read write :" + result + ", name:" + name);
                        }
                    }
                }
            }

            @Override
            public TxnReadState getTransactionRequired()
            {
                return TxnReadState.TXN_NONE;
            }   
        };
    }
    
    /**
     * Called for open file error.
     */
    private ResultCallback newOpenFileErrorCallbackCommand()
    {
        return new ResultCallback()
        {
            @Override
            public void execute(Object result)
            {
                logger.debug("error handler - set state to error for name:" + name);
                isComplete = true;
                state = InternalState.ERROR;
            }

            @Override
            public TxnReadState getTransactionRequired()
            {
                return TxnReadState.TXN_NONE;
            }   
        };
    }

    
    private boolean isReadOnly(NetworkFile file)
    {
        return (file.getGrantedAccess() == NetworkFile.READONLY);
    }

    /* This openFileInstance knows about ScenarioDeleteRestore */
    @Override
    public Command win(List<ScenarioResult> results, Command command)
    {
        if(command instanceof CompoundCommand)
        {
            CompoundCommand c = (CompoundCommand)command; 
            for(ScenarioResult looser : results)
            {
                if(looser.scenario instanceof ScenarioDeleteRestoreInstance)
                {
                    Command l = looser.command; 
                    ArrayList<Command> commands = new ArrayList<Command>();
                    ArrayList<Command> postCommitCommands = new ArrayList<Command>();
                    ArrayList<Command> postErrorCommands = new ArrayList<Command>();
                    commands.add(l);
                    postCommitCommands.addAll(c.getPostCommitCommands());
                    postErrorCommands.addAll(c.getPostErrorCommands());
                    
                    logger.debug("returning merged high priority executor");
                    return new CompoundCommand(commands, postCommitCommands, postErrorCommands);
                }
                
                
                if(looser.scenario instanceof ScenarioDeleteRenameOrCreateInstance)
                {
                    
                    Command x = looser.command;
                    if(x instanceof CompoundCommand)
                    {
                        CompoundCommand l = (CompoundCommand)x;
                        
                        ArrayList<Command> commands = new ArrayList<Command>();
                        ArrayList<Command> postCommitCommands = new ArrayList<Command>();
                        ArrayList<Command> postErrorCommands = new ArrayList<Command>();
                        commands.addAll(c.getCommands());
                        postCommitCommands.addAll(c.getPostCommitCommands());
                        // Merge in the loosing post commit
                        postCommitCommands.addAll(l.getPostCommitCommands());
                        postErrorCommands.addAll(c.getPostErrorCommands());
                    
                        logger.debug("returning merged high priority executor");
                        return new CompoundCommand(commands, postCommitCommands, postErrorCommands);
                    }
                    else
                    {
                        ArrayList<Command> commands = new ArrayList<Command>();
                        ArrayList<Command> postCommitCommands = new ArrayList<Command>();
                        ArrayList<Command> postErrorCommands = new ArrayList<Command>();
                        commands.add(x);
                        postCommitCommands.addAll(c.getPostCommitCommands());
                        postErrorCommands.addAll(c.getPostErrorCommands());
                        
                        logger.debug("returning merged high priority executor");
                        return new CompoundCommand(commands, postCommitCommands, postErrorCommands);
                    }
                }
            }
        }
        // No change
        return command;
    }

    @Override
    public void notifyRename(Operation operation, Command command)
    {
        if(operation instanceof RenameFileOperation)
        {
            RenameFileOperation r = (RenameFileOperation)operation;
            if(r.getFrom() == null)
            {
                return;
            }
            
            if(name.equalsIgnoreCase(r.getFrom()))
            {
                if(logger.isWarnEnabled())
                {
                    logger.warn("rename of this scenario: to " + r.getTo());
                }
                
                name = r.getTo();
                
                if (fileHandleReadWrite != null)
                {
                    fileHandleReadWrite.setName(r.getTo());
                    fileHandleReadWrite.setFullName(r.getToPath());
                }
                
                if (fileHandleReadOnly != null)
                {
                    fileHandleReadOnly.setName(r.getTo());
                    fileHandleReadOnly.setFullName(r.getToPath());
                }
            }
        } 
    }
}

