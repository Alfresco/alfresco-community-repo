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

import org.alfresco.filesys.repo.rules.ScenarioInstance.Ranking;
import org.alfresco.filesys.repo.rules.commands.CompoundCommand;
import org.alfresco.filesys.repo.rules.commands.CopyContentCommand;
import org.alfresco.filesys.repo.rules.commands.DeleteFileCommand;
import org.alfresco.filesys.repo.rules.commands.RenameFileCommand;
import org.alfresco.filesys.repo.rules.operations.CreateFileOperation;
import org.alfresco.filesys.repo.rules.operations.DeleteFileOperation;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.alfresco.jlan.server.filesys.FileName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A rename scenario is ...
 * 
 * a) Rename File to File~
 * b) Create File
 * c) Delete File~
 * 
 * This rule will kick in and copy the content and then switch the two file over. 
 * 
 */
class ScenarioRenameShuffleInstance implements ScenarioInstance
{
    private static Log logger = LogFactory.getLog(ScenarioRenameShuffleInstance.class);
      
    private Date startTime = new Date();
    
    /**
     * Timeout in ms.  Default 30 seconds.
     */
    private long timeout = 30000;
    
    private boolean isComplete = false;
    
    private Ranking ranking = Ranking.HIGH;
    
    enum InternalState
    {
        NONE,
        INITIALISED,
        LOOK_FOR_DELETE
    } ;
    
    InternalState state = InternalState.NONE;
    
    String from;
    String to;
    
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
        }
        
        switch (state)
        {
            case NONE:
                if(operation instanceof RenameFileOperation)
                {
                    logger.debug("New scenario initialised");
                    RenameFileOperation r = (RenameFileOperation)operation;
                    this.from = r.getFrom();
                    this.to = r.getTo();
                    state = InternalState.INITIALISED;
                }
                break;
                
            case INITIALISED:
                
                if(operation instanceof CreateFileOperation)
                {
                    CreateFileOperation c = (CreateFileOperation)operation;
                    if(from.equals(c.getName()))
                    {
                        logger.debug("transition to LOOK_FOR_DELETE");
                       
                        state = InternalState.LOOK_FOR_DELETE;
                    }
                }
                break;
                
            case LOOK_FOR_DELETE:
                if(operation instanceof DeleteFileOperation)
                {
                    DeleteFileOperation d = (DeleteFileOperation)operation;
                    if(to.equals(d.getName()))
                    {
                        logger.debug("Rename shuffle complete - fire!");
                       
                        String[] paths = FileName.splitPath(d.getPath());
                        String oldFolder = paths[0];
                        
                        /**
                         * Shuffle is as follows
                         * a) Copy content from File to File~
                         * b) Delete File
                         * c) Rename File~ to File
                         */
                        ArrayList<Command> commands = new ArrayList<Command>();
                        CopyContentCommand copyContent = new CopyContentCommand(from, to, d.getRootNodeRef(), oldFolder + "\\" + from, oldFolder + "\\" + to);
                        RenameFileCommand r1 = new RenameFileCommand(to, from, d.getRootNodeRef(), oldFolder + "\\" + to, oldFolder + "\\" + from);
                        DeleteFileCommand d1 = new DeleteFileCommand(from, d.getRootNodeRef(), oldFolder + "\\" + from); 
   
                        commands.add(copyContent);
                        commands.add(d1);
                        commands.add(r1);
                     
                        logger.debug("Scenario complete");
                        isComplete = true;
                        return new CompoundCommand(commands);
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
    
    public String toString()
    {
        return "ScenarioRenameShuffleInstance from:" + from + " to:" + to;
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

}

