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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.filesys.repo.rules.ScenarioInstance.Ranking;
import org.alfresco.filesys.repo.rules.operations.CreateFileOperation;
import org.alfresco.filesys.repo.rules.operations.OpenFileOperation;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Open File Scenario is a sequence of multiple openFile operations
 * 
 * Only on the last close does the repo get closed.  Open Files in the middle
 * share the same file handle.
 * 
 * For example:
 * 
 * 1) open(readOnly)
 * 2) open(readWrite)
 * 3) open(readOnly) - does nothing.
 * 4) close - does nothing
 * 5) close - does nothing
 * 6) close - updates the repo
 */
public class ScenarioOpenFile implements Scenario
{
    private static Log logger = LogFactory.getLog(ScenarioOpenFile.class);

    private String pattern;
    
    private long timeout = 300000;
    
    @Override
    public ScenarioInstance createInstance(final List<ScenarioInstance> currentInstances, Operation operation)
    {
       /**
         * This scenario is triggered by an open or create of a new file
         */
        if(operation instanceof CreateFileOperation)
        {   
            CreateFileOperation c = (CreateFileOperation)operation;
            if(c.getName() == null)
            {
                logger.debug("c.getName is null! - scenario not active");
                return null;
            }
            
            if(c.getName().matches(pattern))
            {
                
                if(checkScenarioActive(c.getName(),currentInstances))
                {
                    logger.debug("scenario already active for name" + c.getName());
                    return null;
                }
                
                if(logger.isDebugEnabled())
                {
                    logger.debug("New Open File Instance for CreateFileOperation:" + c);
                }
                
                ScenarioOpenFileInstance instance = new ScenarioOpenFileInstance();
                instance.setTimeout(timeout);
                instance.setRanking(ranking);
                return instance;
            }
        }
        
        if(operation instanceof OpenFileOperation)
        {   
            
            OpenFileOperation o = (OpenFileOperation)operation;
            
            if(o.getName() == null)
            {
                logger.debug("o.getName is null! - scenario not active");
                return null;
            }
            
            if(o.getName().matches(pattern))
            {
                if(checkScenarioActive(o.getName(),currentInstances))
                {
                    logger.debug("scenario already active for name" + o.getName());
                    return null;
                }
                
                if(logger.isDebugEnabled())
                {
                    logger.debug("New Open File Instance for OpenFileOperation:" + o);
                }
         
                ScenarioOpenFileInstance instance = new ScenarioOpenFileInstance();
                instance.setTimeout(timeout);
                instance.setRanking(ranking);
                return instance;
            }
        }
        
        // No not interested.
        return null;
   
    }
    
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public String getPattern()
    {
        return pattern;
    }    
    
    private Ranking ranking = Ranking.HIGH;
    
    public void setRanking(Ranking ranking)
    {
        this.ranking = ranking;
    }

    public Ranking getRanking()
    {
        return ranking;
    }

    /**
     * Check whether there is already an instance of the ScenarioOpenFile for the file
     */
    private boolean checkScenarioActive(String name, final List<ScenarioInstance> currentInstances)
    {
        for(ScenarioInstance instance: currentInstances)
        {
            if(instance instanceof ScenarioOpenFileInstance)
            {
                ScenarioOpenFileInstance i = (ScenarioOpenFileInstance)instance;
                if(i.getName().equalsIgnoreCase(name));
                {
                     return true;
                }
                
            }
        }
        return false;
    }
}
