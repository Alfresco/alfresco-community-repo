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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.filesys.repo.rules.ScenarioInstance.Ranking;
import org.alfresco.util.PropertyCheck;

/**
 * The Rule Evaluator evaluates the operation and returns details of the commands to implement those operations.
 * <p>
 * It is configured with a list of scenarios which act as factories for scenario instances.
 */
public class RuleEvaluatorImpl implements RuleEvaluator
{
    private static Log logger = LogFactory.getLog(RuleEvaluatorImpl.class);

    /**
     * The evaluator context, one for each folder
     */
    private class EvaluatorContextImpl implements EvaluatorContext
    {
        Map<String, Object> sessionState;

        EvaluatorContextImpl(Map<String, Object> sessionState)
        {
            this.sessionState = sessionState;
        }

        /**
         * Current instances of scenarios
         */
        private List<ScenarioInstance> currentScenarioInstances = new ArrayList<ScenarioInstance>();

        @Override
        public List<ScenarioInstance> getScenarioInstances()
        {
            return currentScenarioInstances;
        }

        @Override
        public Map<String, Object> getSessionState()
        {
            return sessionState;
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
     * 
     * @param operation
     *            the operation to be evaluated
     * @return the command to execute that operation
     */
    public Command evaluate(EvaluatorContext context, Operation operation)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("evaluate:" + operation);
        }

        /**
         * For each scenario, do we need to create a new scenario instance for the specified operation ?
         */
        List<ScenarioResult> results = new ArrayList<ScenarioResult>(5);

        // currentScenarioInstances needs to be protected for concurrency.
        synchronized (context.getScenarioInstances())
        {
            for (Scenario scenario : scenarios)
            {
                ScenarioInstance instance = scenario.createInstance(context, operation);
                if (instance != null)
                {
                    context.getScenarioInstances().add(instance);
                }
            }

            /**
             * For each active scenario.
             */
            Iterator<ScenarioInstance> i = context.getScenarioInstances().iterator();

            while (i.hasNext())
            {

                ScenarioInstance scenario = i.next();
                if (logger.isDebugEnabled())
                {
                    logger.debug("evaluating:" + scenario + " operation: " + operation);
                }
                Command executor = scenario.evaluate(operation);
                if (executor != null)
                {
                    results.add(new ScenarioResult(scenario, executor));

                }
                if (scenario.isComplete())
                {
                    // That scenario is no longer active.
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Scenario is complete:" + scenario);
                    }
                    i.remove();
                }
            }
        } // End of syncronized block

        // results contains the results of the evaluator

        Map<Ranking, ScenarioResult> executors = new HashMap<Ranking, ScenarioResult>();

        // HOW to arbitrate between many scenario executors
        // Idea : Scenarios have rankings.
        for (ScenarioResult result : results)
        {
            executors.put(result.scenario.getRanking(), result);
        }

        ScenarioResult ex = executors.get(Ranking.HIGH);
        if (ex != null)
        {
            if (ex.scenario instanceof DependentInstance)
            {
                DependentInstance di = (DependentInstance) ex.scenario;
                for (ScenarioResult looser : results)
                {
                    if (ex != looser)
                    {
                        Command c = di.win(results, ex.command);
                        logger.debug("returning merged high priority executor");

                        return c;
                    }
                }
            }

            logger.debug("returning high priority executor");
            return ex.command;
        }
        ex = executors.get(Ranking.MEDIUM);
        if (ex != null)
        {
            logger.debug("returning medium priority executor");
            return ex.command;
        }
        ex = executors.get(Ranking.LOW);
        if (ex != null)
        {
            logger.debug("returning low priority executor");
            return ex.command;
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
    public EvaluatorContext createContext(Map<String, Object> sessionState)
    {
        EvaluatorContextImpl impl = new EvaluatorContextImpl(sessionState);

        return impl;
    }

    @Override
    public void notifyRename(EvaluatorContext context, Operation operation,
            Command command)
    {
        // currentScenarioInstances needs to be protected for concurrency.
        synchronized (context.getScenarioInstances())
        {
            /**
             * For each active scenario.
             */
            Iterator<ScenarioInstance> i = context.getScenarioInstances().iterator();

            while (i.hasNext())
            {
                ScenarioInstance scenario = i.next();
                if (scenario instanceof ScenarioInstanceRenameAware)
                {
                    ScenarioInstanceRenameAware awareScenario = (ScenarioInstanceRenameAware) scenario;
                    awareScenario.notifyRename(operation, command);
                }

            }
        }

    }
}
