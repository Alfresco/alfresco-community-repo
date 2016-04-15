package org.alfresco.filesys.repo.rules;

import java.util.List;

import org.alfresco.filesys.repo.rules.ScenarioInstance.Ranking;

/**
 * The Bog Standard Instance.   This always executes.
 * 
 * @author mrogers
 */
public class ScenarioSimpleNonBuffered implements Scenario
{
    private ScenarioSimpleNonBufferedInstance instance = new ScenarioSimpleNonBufferedInstance();

    private Ranking ranking = Ranking.LOW;
    
    @Override
    public ScenarioInstance createInstance(final EvaluatorContext ctx, Operation operation)
    {
        /**
         * The bog standard scenario is always interested.
         */
        return instance;
    }
    
    public void setRanking(Ranking ranking)
    {
        this.ranking = ranking;
        instance.setRanking(ranking);
    }

    public Ranking getRanking()
    {
        return ranking;
    }
    
    public String toString()
    {
        return "ScenarioSimpleNonBuffered - default instance";
    }

}
