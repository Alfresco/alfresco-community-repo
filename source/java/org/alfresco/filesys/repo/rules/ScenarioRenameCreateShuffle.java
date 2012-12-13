package org.alfresco.filesys.repo.rules;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.filesys.repo.rules.ScenarioInstance.Ranking;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ScenarioRenameCreateShuffle implements Scenario
{
    private static Log logger = LogFactory.getLog(ScenarioRenameCreateShuffle.class);

    /**
     * The regex pattern of a create that will trigger a new instance of
     * the scenario.
     */
    private Pattern pattern;
    private String strPattern;
    
    private long timeout = 30000;
    
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
                    logger.debug("New Scenario Rename Shuffle Create Instance strPattern:" + pattern);
                }
                ScenarioRenameCreateShuffleInstance instance = new ScenarioRenameCreateShuffleInstance();
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


