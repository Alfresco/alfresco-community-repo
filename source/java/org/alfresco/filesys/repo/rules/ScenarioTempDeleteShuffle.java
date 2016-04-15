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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.filesys.repo.rules.ScenarioInstance.Ranking;
import org.alfresco.filesys.repo.rules.operations.CreateFileOperation;
import org.alfresco.filesys.repo.rules.operations.DeleteFileOperation;
import org.alfresco.filesys.repo.rules.operations.MoveFileOperation;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.util.MaxSizeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A temp delete shuffle.
 * 
 * Files are created in a temporary directory 
 * and then a delete and move.
 */
public class ScenarioTempDeleteShuffle implements Scenario
{
    private static Log logger = LogFactory.getLog(ScenarioTempDeleteShuffle.class);

    protected final static String SCENARIO_KEY = "org.alfresco.filesys.repo.rules.ScenarioTempDeleteShuffle";
    
    /**
     * The regex pattern of a create that will identify a temporary directory.
     */
    private Pattern tempDirPattern;
    private String strTempDirPattern;

    /**
     * The regex pattern of a create that will trigger a new instance of
     * the scenario.
     */    
    private Pattern pattern;
    private String strPattern;
 
    
    private long timeout = 30000;
    
    private Ranking ranking = Ranking.HIGH;
    
    @Override
    public ScenarioInstance createInstance(final EvaluatorContext ctx, Operation operation)
    {
        /**
         * This scenario is triggered by a delete of a file matching
         * the pattern
         */
        if(operation instanceof CreateFileOperation)
        {
            CreateFileOperation c = (CreateFileOperation)operation;
            
            // check whether file is below .TemporaryItems 
            String path = c.getPath();
            
            // if path contains .TemporaryItems
            Matcher d = tempDirPattern.matcher(path);
            if(d.matches())
            {
                logger.debug("pattern matches temp dir folder so this is a new create in a temp dir");
                Matcher m = pattern.matcher(c.getName());
                if(m.matches())
                {
                    // and how to lock - since we are already have one lock on the scenarios/folder here
                    // this is a potential deadlock and synchronization bottleneck
                    Map<String, String> createdTempFiles = (Map<String,String>)ctx.getSessionState().get(SCENARIO_KEY);
                    
                    if(createdTempFiles == null)
                    {
                        synchronized(ctx.getSessionState())
                        {
                            logger.debug("created new temp file map and added it to the session state");
                            createdTempFiles = (Map<String,String>)ctx.getSessionState().get(SCENARIO_KEY);
                            if(createdTempFiles == null)
                            {
                                createdTempFiles = Collections.synchronizedMap(new MaxSizeMap<String, String>(5, false));
                                ctx.getSessionState().put(SCENARIO_KEY, createdTempFiles);
                            }
                        }                        
                    }
                    createdTempFiles.put(c.getName(), c.getName());
                
                    // TODO - Return a different scenario instance here ???
                    // So it can time out and have anti-patterns etc?
                }
            }
        }
        
        if (operation instanceof MoveFileOperation)
        {
            MoveFileOperation mf = (MoveFileOperation)operation;
            
            // check whether file is below .TemporaryItems 
            String path = mf.getFromPath();
            
            // if path contains .TemporaryItems
            Matcher d = tempDirPattern.matcher(path);
            if(d.matches())
            {
                logger.debug("pattern matches temp dir folder so this is a new create in a temp dir");
                Matcher m = pattern.matcher(mf.getFrom());
                if(m.matches())
                {
                    // and how to lock - since we are already have one lock on the scenarios/folder here
                    // this is a potential deadlock and synchronization bottleneck
                    Map<String, String> createdTempFiles = (Map<String,String>)ctx.getSessionState().get(SCENARIO_KEY);
                    
                    if(createdTempFiles == null)
                    {
                        synchronized(ctx.getSessionState())
                        {
                            logger.debug("created new temp file map and added it to the session state");
                            createdTempFiles = (Map<String,String>)ctx.getSessionState().get(SCENARIO_KEY);
                            if(createdTempFiles == null)
                            {
                                createdTempFiles = Collections.synchronizedMap(new MaxSizeMap<String, String>(5, false));
                                ctx.getSessionState().put(SCENARIO_KEY, createdTempFiles);
                            }
                        }                        
                    }
                    createdTempFiles.remove(mf.getFrom());
                
                    // TODO - Return a different scenario instance here ???
                    // So it can time out and have anti-patterns etc?
                }
            }
        }
        
        if(operation instanceof DeleteFileOperation)
        {
            DeleteFileOperation c = (DeleteFileOperation)operation;
            
            Matcher m = pattern.matcher(c.getName());
            if(m.matches())
            {
                Map<String, String> createdTempFiles = (Map<String,String>)ctx.getSessionState().get(SCENARIO_KEY);
                
                if(createdTempFiles != null)
                {
                    if(createdTempFiles.containsKey(c.getName()))
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("New Scenario Temp Delete Shuffle Instance:" + c.getName());
                        }
                
                        ScenarioTempDeleteShuffleInstance instance = new ScenarioTempDeleteShuffleInstance() ;
                        instance.setTimeout(timeout);
                        instance.setRanking(ranking);
                        return instance;
                    }
                }
            }
        }
        
        // No not interested.
        return null;
   
    }

    public void setPattern(String pattern)
    {
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        this.strPattern = pattern;
    }
    
    public String getPattern()
    {
        return this.strPattern;
    }
    
    public void setTempDirPattern(String tempDirPattern)
    {
        this.tempDirPattern = Pattern.compile(tempDirPattern, Pattern.CASE_INSENSITIVE);
        this.strTempDirPattern = tempDirPattern;
    }
    
    public String getTempDirPattern()
    {
        return this.strTempDirPattern;
    }
    
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public void setRanking(Ranking ranking)
    {
        this.ranking = ranking;
    }

    public Ranking getRanking()
    {
        return ranking;
    }
}
