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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.filesys.repo.rules.ScenarioInstance.Ranking;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The "Vi" shuffle is a sequence where a file is moved out of the way
 * and then a new copy of the file put into place.
 * 
 * a) Rename File to File~
 * b) Create File
 *
 */
public class ScenarioRenameShuffle implements Scenario
{
    private static Log logger = LogFactory.getLog(ScenarioRenameShuffle.class);

    /**
     * The regex pattern of a create that will trigger a new instance of
     * the scenario.
     */
    private Pattern pattern;
    private String strPattern;
    
    private long timeout = 30000;
    
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
                    logger.debug("New Scenario Rename Shuffle Instance strPattern:" + pattern);
                }
                ScenarioRenameShuffleInstance instance = new ScenarioRenameShuffleInstance();
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
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        this.strPattern = pattern;
    }

    public String getPattern()
    {
        return strPattern;
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
}
