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
 * This is an instance of a "temp delete shuffle" triggered by a delete of a file matching 
 * a newly created file in a temporary directory.
 * 
 * <p> First implemented for TextEdit from MacOS Lion
 * 
 * <p>
 * Sequence of operations.
 * a) Temporary Directory Created
 * b) Temporary file created in temporary directory.  
 * c) Target file deleted
 * d) Temp file moved in place of target file.
 * e) Temporary directory deleted.
 * <p>
 * If this filter is active then this is what happens.
 * a) Temp file created - in another folder.
 * b) Existing file deleted. Scenario kicks in to rename rather than delete.
 * c) New file moved into place (X to Y). Scenario kicks in 
 *   1) renames file from step c
 *   2) copies content from temp file to target file 
 *   3) deletes temp file.
 * d) Clean up scenario.
 */
public class ScenarioTempDeleteShuffleInstance implements ScenarioInstance
{
    private static Log logger = LogFactory.getLog(ScenarioTempDeleteShuffleInstance.class);
    
    enum InternalState 
    {
        NONE,
        DELETE_SUBSTITUTED,   // Scenario has intervened and renamed rather than delete
        MOVED
    }
       
    InternalState internalState = InternalState.NONE;
    
    private Date startTime = new Date();
    
    private String lockName;
    
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
                logger.debug("Instance timed out lockName:" + lockName);
                isComplete = true;
                return null;
            }
        }
        
        switch (internalState)
        {
        
        case NONE:
            
            /**
             * Looking for target file being deleted 
             * 
             * Need to intervene and replace delete with a rename to temp file.
             */              
            if(operation instanceof DeleteFileOperation)
            {
                DeleteFileOperation d = (DeleteFileOperation)operation;
                
                
                if(logger.isDebugEnabled())
                {
                    logger.debug("entering DELETE_SUBSTITUTED state: " + lockName);
                }
                
                String tempName = ".shuffle" + d.getName();
                
                deletes.put(d.getName(), tempName);
                
                String[] paths = FileName.splitPath(d.getPath());
                String currentFolder = paths[0];
                
                RenameFileCommand r1 = new RenameFileCommand(d.getName(), tempName, d.getRootNodeRef(), d.getPath(), currentFolder + "\\" + tempName);
                
                internalState = InternalState.DELETE_SUBSTITUTED;
                
                return r1;

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
            
        case DELETE_SUBSTITUTED:
            
            /**
             * Looking for a move operation of the deleted file
             */
            if(operation instanceof MoveFileOperation)
            {
                MoveFileOperation m = (MoveFileOperation)operation;
                
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
                    logger.debug("scenario fires");
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
        return "ScenarioTempDeleteShuffleInstance:" + lockName;
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
