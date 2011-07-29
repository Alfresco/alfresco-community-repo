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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.filesys.repo.rules.ScenarioInstance.Ranking;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Rule Evaluator evaluates the operation and returns 
 * details of the commands to implement those operations.
 * <p>
 * It is configured with a list of scenarios.
 */
public class RuleEvaluatorImpl implements RuleEvaluator
{
    private static Log logger = LogFactory.getLog(RuleEvaluatorImpl.class);
    
    /**
     * The evaluator context
     */
    private class EvaluatorContextImpl implements EvaluatorContext
    {
        /**
         * Current instances of scenarios
         */
        private List<ScenarioInstance> currentScenarioInstances = new ArrayList<ScenarioInstance>();

        @Override
        public List<ScenarioInstance> getScenarioInstances()
        {
            return currentScenarioInstances;
        }     
    } 
    
    public void init()
    {
        PropertyCheck.mandatory(this, "scenarios", scenarios);
    }
     
    /**
     * The scenarios contained within this RuleEvaluator
     */
    private List<Scenario> scenarios;
    
    /**
     * Evaluate the scenarios against the current operation
     * @param operation the operation to be evaluated
     */
    public Command evaluate(EvaluatorContext context, Operation operation)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("evaluate:" + operation);
        }
        
         
        /**
         * For each scenario, do we need to create a new scenario 
         * instance for the specified operation ?
         */   
        // currentScenarioInstances needs to be protected for concurrency.       
        synchronized (context.getScenarioInstances())
        {
            for(Scenario scenario : scenarios)
            {
                ScenarioInstance instance = scenario.createInstance(context.getScenarioInstances(), operation);
                if(instance != null)
                {
                    context.getScenarioInstances().add(instance);
                }
            }

            /**
             * For each active scenario.
             */
            Iterator<ScenarioInstance> i = context.getScenarioInstances().iterator();

            Map<Ranking, Command> executors = new HashMap<Ranking, Command>();

            while(i.hasNext())
            {

                ScenarioInstance scenario = i.next();
                if(logger.isDebugEnabled())
                {
                    logger.debug("evaluating:" + scenario + " operation: " +operation );
                }
                Command executor = scenario.evaluate(operation);
                if(executor != null)
                {
                    executors.put(scenario.getRanking(), executor);
                }
                if(scenario.isComplete())
                {
                    // That scenario is no longer active.
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Scenario is complete:" + scenario);
                    }
                    i.remove();
                }
            }

            // HOW to arbitrate between many scenario executors
            // Idea : Scenarios have rankings.
            Command ex = executors.get(Ranking.HIGH);
            if (ex != null) 
            {
                logger.debug("returning high priority executor");
                return ex;
            }
            ex = executors.get(Ranking.MEDIUM);
            if (ex != null) 
            {
                logger.debug("returning medium priority executor");
                return ex;
            }
            ex = executors.get(Ranking.LOW);
            if (ex != null) 
            {
                logger.debug("returning low priority executor");
                return ex;
            }
        }

        return null;
    }

    public void setScenarios(List<Scenario> scenarios)
    {
        this.scenarios = scenarios;
    }

    public List<Scenario> getScenarios()
    {
        return scenarios;
    }

    @Override
    public EvaluatorContext createContext()
    {
        return new EvaluatorContextImpl();
    } 
   
}
