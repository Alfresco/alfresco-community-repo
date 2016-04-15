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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.filesys.repo.rules.ScenarioInstance.Ranking;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A double rename shuffle
 * 
 * a) Existing file renamed out of the way.  X.fm to X.backup.fm
 * b) New file moved renamed into place. X.fm.C29
 * 
 * Scenario is triggered by the first rename matching a pattern.
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
    private Pattern interimPattern;
    private String strInterimPattern;
    private boolean deleteBackup;
    private boolean moveAsSystem;
    
    private long timeout = 30000;
    
    private Ranking ranking = Ranking.HIGH;
    
    @Override
    public ScenarioInstance createInstance(final EvaluatorContext ctx, Operation operation)
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
                    logger.debug("New Scenario Double Rename Shuffle Instance strPattern:" + pattern + " matches" + r.getTo());
                }
                ScenarioDoubleRenameShuffleInstance instance = new ScenarioDoubleRenameShuffleInstance();
                instance.setTimeout(timeout);
                instance.setRanking(ranking);
                instance.setDeleteBackup(deleteBackup);
                instance.setMoveAsSystem(moveAsSystem);
                if (interimPattern != null)
                {
                    instance.setInterimPattern(interimPattern);
                }
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

    public void setDeleteBackup(boolean deleteBackup)
    {
        this.deleteBackup = deleteBackup;
    }

    public boolean isDeleteBackup()
    {
        return deleteBackup;
    }
    
    public boolean isMoveAsSystem()
    {
        return moveAsSystem;
    }

    public void setMoveAsSystem(boolean retryAsSystem)
    {
        this.moveAsSystem = retryAsSystem;
    }

    public void setInterimPattern(String intermediateMovePattern)
    {
        if (null != intermediateMovePattern)
        {
            this.interimPattern = Pattern.compile(intermediateMovePattern, Pattern.CASE_INSENSITIVE);
            this.strInterimPattern = intermediateMovePattern;
        }
    }

    public String getInterimPattern()
    {
        return this.strInterimPattern;
    }
}
