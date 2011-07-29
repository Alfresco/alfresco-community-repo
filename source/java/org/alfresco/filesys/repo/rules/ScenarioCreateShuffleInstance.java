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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.filesys.repo.rules.commands.CompoundCommand;
import org.alfresco.filesys.repo.rules.commands.CopyContentCommand;
import org.alfresco.filesys.repo.rules.commands.RenameFileCommand;
import org.alfresco.filesys.repo.rules.operations.CreateFileOperation;
import org.alfresco.filesys.repo.rules.operations.DeleteFileOperation;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.alfresco.jlan.server.filesys.FileName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is an instance of a "classic shuffle" triggered by a create of a 
 * file matching a specified pattern.
 * <p>
 * a) New file created.   Typically with an obscure name.
 * b) Existing file moved out of the way
 * c) New file moved into place.
 * d) Old file deleted.
 * 
 * <p>
 * If this filter is active then this is what happens.
 * a) New file created.   New file created (X).
 * b) Existing file moved out of the way (Y to Z).   Raname tracked.
 * c) New file moved into place (X to Y).   Scenario kicks in to change commands.
 * d) Old file deleted.
 */
public class ScenarioCreateShuffleInstance implements ScenarioInstance
{
    private static Log logger = LogFactory.getLog(ScenarioCreateShuffleInstance.class);
    
    enum InternalState 
    {
        NONE,
        RENAME,
        DELETE
    }
       
    InternalState internalState = InternalState.NONE;
    
    private Date startTime = new Date();
    
    private String createName;
    private String move1;
    private String move2;
    private Ranking ranking;
    
    /**
     * Timeout in ms.  Default 30 seconds.
     */
    private long timeout = 30000;
    
    private boolean isComplete;
    
    /**
     * Keep track of re-names
     */
    private Map<String, String>renames = new HashMap<String, String>();
    
    /**
     * Evaluate the next operation
     * @param operation
     */
    public Command evaluate(Operation operation)
    {
        
//        /**
//         * Anti-pattern for all states - delete the file we are 
//         * shuffling
//         */
//        if(createName != null)
//        {
//            if(operation instanceof DeleteFileOperation)
//            {
//                DeleteFileOperation d = (DeleteFileOperation)operation;
//                if(d.getName().equals(createName))
//                {
//                    logger.debug("Anti-pattern : Shuffle file deleted");
//                    isComplete = true;
//                    return null;
//                }
//            }
//        }
        
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
        
        switch (internalState)
        {
        case NONE:
            // Looking for a create transition
            if(operation instanceof CreateFileOperation)
            {
                CreateFileOperation c = (CreateFileOperation)operation;
                this.createName = c.getName();
                if(logger.isDebugEnabled())
                {
                    logger.debug("entering RENAME state: " + createName);
                }
                internalState = InternalState.RENAME;
                return null;
            }
            else
            {
                // anything else bomb out
                if(logger.isDebugEnabled())
                {
                    logger.debug("State error, expected a CREATE");
                }
                isComplete = true;
            }
            break;
        
        case RENAME:
            
            /**
             * Looking for two renames X(createName) to Y(middle) to Z(end)
             */              
            if(operation instanceof RenameFileOperation)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("Tracking rename: " + operation);
                }
                RenameFileOperation r = (RenameFileOperation)operation;
                renames.put(r.getFrom(), r.getTo());
            
                // Now see if this rename makes a pair.
                String middle = renames.get(createName);
                if(middle != null)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Got second rename" );
                    }
                
                    String end = renames.get(middle);
                
                    if(end != null)
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("Got two renames " );
                        }
                        this.move1 = middle;
                        this.move2 = end;
                    
                       
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("entering DELETE state");
                        }
                    
                        internalState = InternalState.DELETE;
                    
                        /**
                         * This shuffle reverses the rename out of the way and then copies the 
                         * content only.   Finally it moves the temp file into place for the subsequent 
                         * delete.
                         * a) Rename Z to Y (Reverse previous move)
                         * b) Copy Content from X to Y
                         * c) Rename X to Z (move temp file out to old location)
                         */
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("Go and shuffle! createName:" + createName + " move1 " + move1 + " move2 " + move2);
                        }
                        
                        String[] paths = FileName.splitPath(r.getFromPath());
                        String oldFolder = paths[0];
                        // String oldFile = paths[1];
           
                        ArrayList<Command> commands = new ArrayList<Command>();
                        RenameFileCommand r1 = new RenameFileCommand(end, middle, r.getRootNodeRef(), oldFolder + "\\" + end, oldFolder + "\\" + middle);
                        CopyContentCommand copyContent = new CopyContentCommand(createName, move1, r.getRootNodeRef(), oldFolder + "\\" + createName, oldFolder + "\\" + middle);
                        RenameFileCommand r2 = new RenameFileCommand(createName, end, r.getRootNodeRef(), oldFolder + "\\" + createName, oldFolder + "\\" + end); 
                        
                        commands.add(r1);
                        commands.add(copyContent);
                        commands.add(r2);
                    
                        return new CompoundCommand(commands);
                    }
                }
            }
            
            break;
            
        case DELETE:
            
            /**
             * Looking for a delete of the destination
             */
            if(operation instanceof DeleteFileOperation)
            {
                DeleteFileOperation d = (DeleteFileOperation)operation;
                if(d.getName().equals(move2))
                {
                    logger.debug("Scenario complete");
                    isComplete = true;
//                    ArrayList<Command> commands = new ArrayList<Command>();
//                    // missing stuff here
//                    return new CompoundCommand(commands);
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
        return "ScenarioShuffleInstance:" + createName;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public long getTimeout()
    {
        return timeout;
    }
}
