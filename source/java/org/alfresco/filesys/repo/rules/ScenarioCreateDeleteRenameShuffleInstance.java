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

import org.alfresco.filesys.repo.rules.ScenarioLockedDeleteShuffleInstance.InternalState;
import org.alfresco.filesys.repo.rules.commands.CompoundCommand;
import org.alfresco.filesys.repo.rules.commands.CopyContentCommand;
import org.alfresco.filesys.repo.rules.commands.DeleteFileCommand;
import org.alfresco.filesys.repo.rules.commands.RenameFileCommand;
import org.alfresco.filesys.repo.rules.operations.CreateFileOperation;
import org.alfresco.filesys.repo.rules.operations.DeleteFileOperation;
import org.alfresco.filesys.repo.rules.operations.MoveFileOperation;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.alfresco.jlan.server.filesys.FileName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is an instance of a create, delete, rename shuffle" triggered by a create of a 
 * file matching a specified pattern.
 * <p>
 * a) New file created.   Typically with an obscure name.
 * b) Existing file deleted 
 * c) New file moved into place.
 * 
 * <p>
 * If this filter is active then this is what happens.
 * a) New file created.   New file created.
 * b) Existing file deleted.   File moved to temporary location instead.
 * c) Rename - Scenario fires 
 * - File moved back from temporary location
 * - Content updated.
 * - temporary file deleted
 */
public class ScenarioCreateDeleteRenameShuffleInstance implements ScenarioInstance
{
    private static Log logger = LogFactory.getLog(ScenarioCreateDeleteRenameShuffleInstance.class);
    
    enum InternalState 
    {
        NONE,
        ACTIVE
    }
       
    InternalState internalState = InternalState.NONE;
    
    private Date startTime = new Date();
    
    private String createName;
    private Ranking ranking;
    private boolean checkFilename=true;
    
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
                logger.debug("Instance timed out createName:" + createName);
                isComplete = true;
                return null;
            }
        }
        
        /**
         * Anti-pattern for all states - delete the file we are 
         * shuffling
         */
        if(createName != null)
        {
            if(operation instanceof DeleteFileOperation)
            {
                DeleteFileOperation d = (DeleteFileOperation)operation;
                if(d.getName().equals(createName))
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Anti-pattern : Shuffle file deleted createName:" + createName);
                    }
                    isComplete = true;
                    return null;
                }
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
                    logger.debug("entering ACTIVE state: " + createName);
                }
                internalState = InternalState.ACTIVE;
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
        
        case ACTIVE:
       
            /**
             * Looking for deletes and renames
             */
            
            /**
             * Looking for target file being deleted 
             * 
             * Need to intervene and replace delete with a rename to temp file.
             */              
            if(operation instanceof DeleteFileOperation)
            {
                DeleteFileOperation d = (DeleteFileOperation)operation;
                
                String deleteName = d.getName();
             
                /**
                 * For Mac 2011 powerpoint files - add an extra check based on the filename
                 * e.g FileA1 - delete FileA.
                 * For Mac 2011 excel files - check that file to delete is not temporary
                 */
                if(checkFilename)
                {
                    boolean isRightTarget = false;
                    int i = deleteName.lastIndexOf('.');
                    int j = createName.lastIndexOf('.');
                    
                    if (i > 0)
                    {
                        String deleteExt = deleteName.substring(i + 1, deleteName.length());
                        String createExt = (j > 0) ? createName.substring(j + 1, createName.length()) : "";
                        
                        if (deleteExt.startsWith("ppt") && createExt.startsWith("ppt"))
                        {
                            isRightTarget = (i < createName.length()) && deleteName.substring(0, i).equalsIgnoreCase(createName.substring(0, i));
                        }
                        else if (deleteExt.startsWith("xls") && createExt.isEmpty())
                        {
                            isRightTarget = !deleteName.startsWith("._") && !deleteName.startsWith("~$");
                        }
                    }
                    
                    if (isRightTarget)
                    {
                        logger.debug("check filenames - does match");
                    }
                    else
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("check filename patterns do not match - Ignore" + createName + deleteName);
                        }
                        return null;
                    }
                }
                
                if(logger.isDebugEnabled())
                {
                    logger.debug("got a delete : replace with rename createName:" + createName + "deleteName:" + deleteName);
                }
                
                String tempName = ".shuffle" + d.getName();
                
                deletes.put(d.getName(), tempName);
                
                String[] paths = FileName.splitPath(d.getPath());
                String currentFolder = paths[0];
                
                RenameFileCommand r1 = new RenameFileCommand(d.getName(), tempName, d.getRootNodeRef(), d.getPath(), currentFolder + "\\" + tempName);
                
                return r1;
            }
 
            /**
             * 
             */
            if(operation instanceof RenameFileOperation)
            {
                RenameFileOperation m = (RenameFileOperation)operation;
                
                String targetFile = m.getTo();
                
                if(deletes.containsKey(targetFile))
                {
                    String tempName = deletes.get(targetFile);
                    
                    String[] paths = FileName.splitPath(m.getToPath());
                    String currentFolder = paths[0];
                   
                    /**
                     * This is where the scenario fires.
                     * a) Rename the temp file back to the targetFile
                     * b) Copy content from moved file
                     * c) Delete rather than move file 
                     */  
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("scenario fires:" + createName);
                    }
                    ArrayList<Command> commands = new ArrayList<Command>();
                    
                    RenameFileCommand r1 = new RenameFileCommand(tempName, targetFile, m.getRootNodeRef(), currentFolder + "\\" + tempName, m.getToPath());
                    
                    CopyContentCommand copyContent = new CopyContentCommand(m.getFrom(), targetFile, m.getRootNodeRef(), m.getFromPath(), m.getToPath());
                    
                    DeleteFileCommand d1 = new DeleteFileCommand(m.getFrom(), m.getRootNodeRef(), m.getFromPath()); 

                    commands.add(r1);
                    commands.add(copyContent);
                    commands.add(d1);

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
        return "ScenarioShuffleInstance: createName:" + createName;
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
