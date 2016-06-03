
package org.alfresco.filesys.repo.rules;

public class ScenarioResult
{
    public ScenarioResult(ScenarioInstance scenario, Command command)
    {
        this.scenario = scenario;
        this.command = command;
    }
    ScenarioInstance scenario;
    Command command;
};

