/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

import org.alfresco.filesys.repo.ResultCallback;
import org.alfresco.filesys.repo.rules.commands.*;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Date;

/**
 * This is an instance of a "multiple rename shuffle" triggered by rename of a file to a special pattern
 * file matching a specified pattern.
 *
 * a) Original file renamed to the temporary
 * b) Any operations with temporary (optional):
 *   b1) Temporary file renamed to other temporary
 *   b2) Temporary file deleted
 * c) Temporary file (maybe not the same, as it was at step 1) renamed to the original file
 * <p>
 * If this filter is active then this is what happens.
 * a) Temporary file created. Content copied from original file to temporary file.
 * b) Original file deleted (temporary).
 * c) any operations with temporary file
 * d) Original file restored. Content copied from temporary file to original file.
 *
 */
public class ScenarioMultipleRenameShuffleInstance implements ScenarioInstance
{
    private static Log logger = LogFactory.getLog(ScenarioMultipleRenameShuffleInstance.class);
    private NodeRef originalNodeRef;

    enum InternalState 
    {
        NONE,
        INITIALISED
    }
       
    InternalState internalState = InternalState.NONE;
    private Date startTime = new Date();
    private String originalName;
    private Ranking ranking;
    
    /**
     * Timeout in ms.  Default 30 seconds.
     */
    private long timeout = 30000;
    private boolean isComplete;

    /**
     * Evaluate the next operation
     * @param operation
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
                logger.debug("Instance timed out");

            }
            isComplete = true;
            return null;
        }
        

            switch (internalState)
            {
                case NONE:
                /**
                 * Looking for first rename O(original) to T(temporary)
                 */
                if(operation instanceof RenameFileOperation)
                {
                    RenameFileOperation r = (RenameFileOperation)operation;
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Got first rename - tracking rename: " + operation);
                    }
                    originalName = r.getFromPath();
                    ArrayList<Command> commands = new ArrayList<Command>();
                    ArrayList<Command> postCommitCommands = new ArrayList<Command>();
                    ArrayList<Command> postErrorCommands = new ArrayList<Command>();

                    CreateFileCommand c1 = new CreateFileCommand(r.getTo(), r.getRootNodeRef(), r.getToPath(), 0, true);
                    CopyContentCommand copyContent = new CopyContentCommand(r.getFrom(), r.getTo(), r.getRootNodeRef(), r.getFromPath(), r.getToPath());
                    DeleteFileCommand d1 = new DeleteFileCommand(r.getFrom(), r.getRootNodeRef(), r.getFromPath());
                    postCommitCommands.add(deleteFileCallbackCommand());

                    commands.add(c1);
                    commands.add(copyContent);
                    commands.add(d1);

                    internalState = InternalState.INITIALISED;

                    return new CompoundCommand(commands, postCommitCommands, postErrorCommands);
                }
                else
                {
                    // anything else bomb out
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("State error, expected a RENAME");
                    }
                    isComplete = true;
                }

                case INITIALISED:

                    /**
                     * Looking for last rename T(temporary) to O(original)
                     */
                    if (operation instanceof RenameFileOperation)
                    {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Tracking rename: " + operation);
                        }
                        RenameFileOperation r = (RenameFileOperation) operation;

                        if(r.getToPath().equalsIgnoreCase(originalName))
                        {
                            ArrayList<Command> commands = new ArrayList<Command>();
                            RestoreFileCommand r1 = new RestoreFileCommand(r.getTo(), r.getRootNodeRef(), r.getToPath(), 0, originalNodeRef);
                            CopyContentCommand copyContent = new CopyContentCommand(r.getFrom(), r1.getName(), r.getRootNodeRef(), r.getFromPath(), r1.getPath());
                            DeleteFileCommand d1 = new DeleteFileCommand(r.getFrom(), r.getRootNodeRef(), r.getFromPath());

                            commands.add(r1);
                            commands.add(copyContent);
                            commands.add(d1);
                            if(logger.isDebugEnabled())
                            {
                                logger.debug("Scenario complete");
                            }
                            isComplete = true;
                            return new CompoundCommand(commands);
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
        return "ScenarioMultipleRenameShuffleInstance: createName:" + originalName;
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
    private ResultCallback deleteFileCallbackCommand()
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
            public AlfrescoTransactionSupport.TxnReadState getTransactionRequired()
            {
                return AlfrescoTransactionSupport.TxnReadState.TXN_NONE;
            }
        };
    }
}
