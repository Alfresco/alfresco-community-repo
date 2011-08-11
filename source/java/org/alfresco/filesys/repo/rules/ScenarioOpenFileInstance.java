/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.filesys.repo.rules;

import java.util.ArrayList;
import java.util.Date;

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
 * 
 */
class ScenarioOpenFileInstance implements ScenarioInstance
{
    private static Log logger = LogFactory.getLog(ScenarioOpenFileInstance.class);
      
    private Date startTime = new Date();
    
    private String name;
    
    enum InternalState
    {
        NONE,
        OPEN
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
                    ArrayList<Command> commands = new ArrayList<Command>();
                    ArrayList<Command> postCommitCommands = new ArrayList<Command>();
                    commands.add(new CreateFileCommand(c.getName(), c.getRootNodeRef(), c.getPath()));
                    postCommitCommands.add(newOpenFileCallbackCommand());
                    return new CompoundCommand(commands, postCommitCommands);
                }
                else if(operation instanceof OpenFileOperation)
                {
                    OpenFileOperation o = (OpenFileOperation)operation;
                    name = o.getName();
                    ArrayList<Command> commands = new ArrayList<Command>();
                    commands.add(new OpenFileCommand(o.getName(), o.getMode(), o.isTruncate(), o.getRootNodeRef(), o.getPath()));
                    ArrayList<Command> postCommitCommands = new ArrayList<Command>();
                    postCommitCommands.add(newOpenFileCallbackCommand());
                    return new CompoundCommand(commands, postCommitCommands);
                }
                
                // Scenario Not Started
                isComplete = true;
                return null;
                
            case OPEN:
                
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
                        if(file.isReadOnly())
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
                                
                                postErrorCommands.add(new RemoveNoContentFileOnError(c.getName(), c.getRootNodeRef(), c.getPath()));
                                
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
                    
                    if(name.equalsIgnoreCase(o.getName()))
                    {
                        if(o.getMode() == OpenFileMode.WRITE)
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
                                openReadWriteCount++;
                                logger.debug("Return already open read/write file handle from scenario:" + this);
                                return new ReturnValueCommand(fileHandleReadWrite);
                            }
                        }
                        else
                        {
                            // This is an open for read only access
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
                    
                    if(fileHandle.isReadOnly())
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
    

}

