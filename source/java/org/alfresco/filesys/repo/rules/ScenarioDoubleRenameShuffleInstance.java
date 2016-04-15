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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.filesys.repo.rules.commands.CompoundCommand;
import org.alfresco.filesys.repo.rules.commands.CopyContentCommand;
import org.alfresco.filesys.repo.rules.commands.DeleteFileCommand;
import org.alfresco.filesys.repo.rules.commands.MoveFileCommand;
import org.alfresco.filesys.repo.rules.commands.RenameFileCommand;
import org.alfresco.filesys.repo.rules.operations.MoveFileOperation;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.alfresco.jlan.server.filesys.FileName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is an instance of a "double rename shuffle" triggered by rename of a file to a special pattern
 * file matching a specified pattern.  (*.backup.fm)
 * 
 * a) Existing file moved out of the way. X.fm to X.backup.fm
 * b) New file moved into place. X.fm.C29 X.fm
 * <p>
 * If this filter is active then this is what happens.
 * a) Existing file moved out of the way (Y to Z).   Raname tracked.
 * b) New file moved into place (X to Y).   
 * 
 * Scenario kicks in to change commands.
 */
public class ScenarioDoubleRenameShuffleInstance implements ScenarioInstance
{
    private static Log logger = LogFactory.getLog(ScenarioDoubleRenameShuffleInstance.class);
    
    enum InternalState 
    {
        NONE,
        RENAME1,
        RENAME2
    }
       
    InternalState internalState = InternalState.NONE;
    
    private Date startTime = new Date();
    
    private String fileMiddle;
    private String fileFrom;
    private String fileEnd;
    
    private Ranking ranking;
    private boolean deleteBackup;
    private boolean moveAsSystem;
    private Pattern interimPattern;
    
    /**
     * Timeout in ms.  Default 30 seconds.
     */
    private long timeout = 30000;
    
    private boolean isComplete;
    private String folderMiddle;
    private String folderEnd;
    
    /**
     * Keep track of re-names
     */
    private Map<String, String> renames = new HashMap<String, String>();
    
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
             * Looking for first rename Y(middle) to Z(end)
             */              
            if(operation instanceof RenameFileOperation)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("Got first rename - tracking rename: " + operation);
                }
                RenameFileOperation r = (RenameFileOperation)operation;
                fileMiddle = r.getFrom();
                fileEnd = r.getTo();
                
                String[] paths = FileName.splitPath(r.getFromPath());
                folderMiddle = paths[0];
                
                String[] paths2 = FileName.splitPath(r.getToPath());
                folderEnd = paths2[0];
                
                internalState = InternalState.RENAME1;
                
                return  new RenameFileCommand(r.getFrom(), r.getTo(), r.getRootNodeRef(), r.getFromPath(), r.getToPath());   
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
            
              
        case RENAME1:
            
            /**
             * Looking for the second of two renames X(createName) to Y(middle) to Z(end)
             */              
            if(operation instanceof RenameFileOperation)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("Tracking rename: " + operation);
                }
                RenameFileOperation r = (RenameFileOperation)operation;
            
                // Now see if this rename makes a pair
                if(fileMiddle.equalsIgnoreCase(r.getTo()))
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Got second rename" );
                    }
                    
                    fileFrom = r.getFrom();
                    
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
                        logger.debug("Go and shuffle! fromName:" + fileFrom + " middle: " + fileMiddle + " end: " + fileEnd);
                    }
                        
                    String[] paths = FileName.splitPath(r.getFromPath());
                    String oldFolder = paths[0];
           
                    ArrayList<Command> commands = new ArrayList<Command>();
                    
                    RenameFileCommand r1 = new RenameFileCommand(fileEnd, fileMiddle, r.getRootNodeRef(), oldFolder + "\\" + fileEnd, oldFolder + "\\" + fileMiddle);
                    commands.add(r1);
                    CopyContentCommand copyContent = new CopyContentCommand(fileFrom, fileMiddle, r.getRootNodeRef(), oldFolder + "\\" + fileFrom, oldFolder + "\\" + fileMiddle);
                    commands.add(copyContent);
                    if(deleteBackup)
                    {
                        logger.debug("deleteBackup option turned on");
                        DeleteFileCommand d1 = new DeleteFileCommand(oldFolder, r.getRootNodeRef(), oldFolder + "\\" + fileFrom);
                        commands.add(d1);
                    }
                    else
                    {
                        RenameFileCommand r2 = new RenameFileCommand(fileFrom, fileEnd, r.getRootNodeRef(), oldFolder + "\\" + fileFrom, oldFolder + "\\" + fileEnd);
                        commands.add(r2);
                    }   
                    
                    /**
                     * TODO - we may need to copy a new node for the backup and delete the temp node.
                     * It depends if we care about the contents of the Backup file.
                     */
                    
                    isComplete = true;
                    return new CompoundCommand(commands);                                        
                }
            }

            if(operation instanceof MoveFileOperation)
            {
                if(logger.isDebugEnabled())
                {
                    logger.info("Tracking rename: " + operation);
                }
                MoveFileOperation r = (MoveFileOperation)operation;
            
                // Now see if this rename makes a pair
                if(fileMiddle.equalsIgnoreCase(r.getTo()))
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Got second rename" );
                    }
                    
                    fileFrom = r.getFrom();
                    
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
                        logger.debug("Go and shuffle! fromName:" + fileFrom + " middle: " + fileMiddle + " end: " + fileEnd);
                    }
                        
                    String[] paths = FileName.splitPath(r.getFromPath());
                    String oldFolder = paths[0];
           
                    ArrayList<Command> commands = new ArrayList<Command>();
                    
                    RenameFileCommand r1 = new RenameFileCommand(fileEnd, fileMiddle, r.getRootNodeRef(), folderEnd + "\\" + fileEnd, folderMiddle + "\\" + fileMiddle);
                    commands.add(r1);
                    CopyContentCommand copyContent = new CopyContentCommand(fileFrom, fileMiddle, r.getRootNodeRef(), oldFolder + "\\" + fileFrom, folderMiddle + "\\" + fileMiddle);
                    commands.add(copyContent);
                    if(deleteBackup)
                    {
                        logger.debug("deleteBackup option turned on");
                        DeleteFileCommand d1 = new DeleteFileCommand(oldFolder, r.getRootNodeRef(), oldFolder + "\\" + fileFrom);
                        commands.add(d1);
                    }
                    else
                    {
                       MoveFileCommand m1 = new MoveFileCommand(fileFrom, fileEnd, r.getRootNodeRef(), oldFolder + "\\" + fileFrom, folderEnd + "\\" + fileEnd, isMoveAsSystem());
                       commands.add(m1);
                    }
                    /**
                     * TODO - we may need to copy a new node for the backup and delete the temp node.
                     * It depends if we care about the contents of the Backup file.
                     */
                    
                    isComplete = true;
                    return new CompoundCommand(commands);
                }
                else
                {
                    if ((interimPattern != null))
                    {
                        // ALF-16257: temporary Word file moved from .TemporaryItems
                        Matcher m = interimPattern.matcher(r.getFromPath());
                        if(m.matches() && r.getFrom().equals(r.getTo()))
                        {
                            if(logger.isDebugEnabled())
                            {
                                logger.debug("Got system move from temporary folder: " + r.getFrom() + " to " + r.getToPath());
                            }
                            return  new MoveFileCommand(r.getFrom(), r.getTo(), r.getRootNodeRef(), r.getFromPath(), r.getToPath(), true);
                        }
                    }
                }
            }
                
            break;
        }
        
        return null;
    }
    
    public boolean isMoveAsSystem()
    {
        return moveAsSystem;
    }

    public void setMoveAsSystem(boolean moveAsSystem)
    {
        this.moveAsSystem = moveAsSystem;
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
        return "ScenarioDoubleRename:" + fileMiddle;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public void setDeleteBackup(boolean deleteBackup)
    {
        this.deleteBackup = deleteBackup;
    }

    public boolean isDeleteBackup()
    {
        return deleteBackup;
    }

    public void setInterimPattern(Pattern interimPattern)
    {
        this.interimPattern = interimPattern;
    }
}
