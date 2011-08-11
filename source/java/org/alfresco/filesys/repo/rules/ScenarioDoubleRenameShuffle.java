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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.filesys.repo.rules.ScenarioInstance.Ranking;
import org.alfresco.filesys.repo.rules.operations.CreateFileOperation;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A double rename shuffle
 * 
 * a) Existing file renamed out of the way.  X.fm to X.backup.fm
 * b) New file moved renamed into place. X.fm.C29
 */
public class ScenarioDoubleRenameShuffle implements Scenario
{
    private static Log logger = LogFactory.getLog(ScenarioDoubleRenameShuffle.class);

    /**
     * The regex pattern of a create that will trigger a new instance of
     * the scenario.
     */
    private Pattern pattern;
    private String strPattern;
    
    
    private long timeout = 30000;
    
    private Ranking ranking = Ranking.HIGH;
    
    @Override
    public ScenarioInstance createInstance(final List<ScenarioInstance> currentInstances, Operation operation)
    {
        /**
         * This scenario is triggered by a rename of a file matching
         * the pattern
         */
        if(operation instanceof RenameFileOperation)
        {          
            RenameFileOperation r = (RenameFileOperation)operation;
            
            Matcher m = pattern.matcher(r.getTo());
            if(m.matches())
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("New Scenario Double Rename Shuffle Instance strPattern:" + pattern + " matches" + r.getTo() );
                }
                ScenarioDoubleRenameShuffleInstance instance = new ScenarioDoubleRenameShuffleInstance();
                instance.setTimeout(timeout);
                instance.setRanking(ranking);
                return instance;
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
