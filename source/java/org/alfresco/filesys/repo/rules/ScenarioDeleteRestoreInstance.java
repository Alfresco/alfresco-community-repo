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
import java.util.HashMap;
import java.util.Map;


import org.alfresco.filesys.repo.ResultCallback;
import org.alfresco.filesys.repo.rules.commands.CompoundCommand;
import org.alfresco.filesys.repo.rules.commands.DeleteFileCommand;
import org.alfresco.filesys.repo.rules.commands.RestoreFileCommand;
import org.alfresco.filesys.repo.rules.operations.CreateFileOperation;
import org.alfresco.filesys.repo.rules.operations.DeleteFileOperation;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is an instance of a "delete restore shuffle" 
 * 
 * Triggered by a delete of a file followed by a recreate of that same file.
 * 
 * <p> First implemented for MacOS Lion Finder.
 * 
 * <p>
 * Sequence of operations.
 * a) File Deleted
 * b) File Created.  
 */
public class ScenarioDeleteRestoreInstance implements ScenarioInstance
{
    private static Log logger = LogFactory.getLog(ScenarioDeleteRestoreInstance.class);
    
    enum InternalState 
    {
        NONE,
        LOOKING_FOR_CREATE
    }
       
    InternalState internalState = InternalState.NONE;
    
    private Date startTime = new Date();
    
    private String deleteName;
    
    private NodeRef originalNodeRef;
    
    private Ranking ranking;
    
    /**
     * Timeout in ms.  Default 30 seconds.
     */
    private long timeout = 60000;
    
    private boolean isComplete;
    
    /**
     * Keep track of deletes that we substitute with a rename
     * could be more than one if scenarios overlap
     * 
     * From, TempFileName
     */
    private Map<String, String> deletes = new HashMap<String, String>();
    
    /**
     * Evaluate the next operation
     * @param operation Operation
     */
    public Command evaluate(Operation operation)
    {
               
        /**
         * Anti-pattern : timeout
         */
        Date now = new Date();
        if(now.getTime() > startTime.getTime() + getTimeout())
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("Instance timed out deleteName:" + deleteName);
                isComplete = true;
                return null;
            }
        }
        
        switch (internalState)
        {
        
        case NONE:
            
            /**
             * Looking for file being deleted deleted 

             */              
            if(operation instanceof DeleteFileOperation)
            {
                DeleteFileOperation d = (DeleteFileOperation)operation;
                
                deleteName = d.getName();
                               
                if(logger.isDebugEnabled())
                {
                    logger.debug("entering LOOKING_FOR_CREATE state: " + deleteName);
                }
                internalState = InternalState.LOOKING_FOR_CREATE;
                
                ArrayList<Command> commands = new ArrayList<Command>();
                ArrayList<Command> postCommitCommands = new ArrayList<Command>();
                ArrayList<Command> postErrorCommands = new ArrayList<Command>();
                commands.add(new DeleteFileCommand(d.getName(), d.getRootNodeRef(), d.getPath()));
                postCommitCommands.add(newDeleteFileCallbackCommand());
                
                return new CompoundCommand(commands, postCommitCommands, postErrorCommands);  
                
            }
            else
            {
                // anything else bomb out
                if(logger.isDebugEnabled())
                {
                    logger.debug("State error, expected a DELETE");
                }
                isComplete = true;
            }
            break;
            
        case LOOKING_FOR_CREATE:
            
            /**
             * Looking for a create operation of the deleted file
             */
            if(operation instanceof CreateFileOperation)
            {
                CreateFileOperation c = (CreateFileOperation)operation;
                
                if(c.getName().equalsIgnoreCase(deleteName))
                {
                    isComplete = true;
                    if(originalNodeRef != null)
                    {
                        logger.debug("Scenario fires:" + this);
                        return new RestoreFileCommand(c.getName(), c.getRootNodeRef(), c.getPath(), c.getAllocationSize(), originalNodeRef);
                    }
                    
                    return null;
                }
            }    
        }
        
        return null;
    }
    
    @Override
    public boolean isComplete()
    {
        return isComplete;
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
    
    public String toString()
    {
        return "ScenarioDeleteRestoreShuffleInstance:" + deleteName;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public long getTimeout()
    {
        return timeout;
    }
    
    /**
     * Called for delete file.
     */
    private ResultCallback newDeleteFileCallbackCommand()
    {
        return new ResultCallback()
        {
            @Override
            public void execute(Object result)
            {
                if(result instanceof NodeRef)
                {
                    logger.debug("got node ref of deleted node");
                    originalNodeRef = (NodeRef)result;
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
