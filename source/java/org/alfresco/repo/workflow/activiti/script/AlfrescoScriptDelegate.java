
package org.alfresco.repo.workflow.activiti.script;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.alfresco.service.cmr.repository.ScriptService;

/**
 * A {@link JavaDelegate} that executes a script against the {@link ScriptService}.
 * 
 * The script that is executed can be set using field 'script'. A non-default 
 * script-processor can be set in the field 'scriptProcessor'. Optionally, you can run 
 * the script as a different user than the default by setting the field 'runAs'. 
 * By default, the user this script as the current logged-in user. If no user is 
 * currently logged in (eg. flow triggered by timer) the system user will be used instead.
 * 
 * @author Frederik Heremans
 * @since 4.0
 */
public class AlfrescoScriptDelegate extends DelegateExecutionScriptBase implements JavaDelegate 
{
    @Override
    public void execute(DelegateExecution execution) throws Exception 
    {
        runScript(execution);
    }
}
