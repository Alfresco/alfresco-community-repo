package org.alfresco.repo.domain.schema.script;

/**
 * Defines a SQL script executor that executes a single SQL script.
 * 
 * @author Matt Ward
 */
public interface ScriptExecutor
{
    void executeScriptUrl(String scriptUrl) throws Exception;
}
