
package org.alfresco.repo.workflow.activiti.listener;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.pvm.delegate.ExecutionListenerExecution;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.script.DelegateExecutionScriptBase;
import org.alfresco.service.cmr.repository.ScriptService;

/**
 * An {@link ExecutionListener} that runs a script against the {@link ScriptService}.
 * 
 * The script that is executed can be set using field 'script'. A non-default 
 * script-processor can be set in the field 'scriptProcessor'. Optionally, you can run 
 * the script as a different user than the default by setting the field 'runAs'. 
 * By default, the user this script as the current logged-in user. If no user is 
 * currently logged in (eg. flow triggered by timer) the system user will be used instead.
 * 
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class ScriptExecutionListener extends DelegateExecutionScriptBase implements ExecutionListener
{
    private static final String DELETED_FLAG = "deleted";
    private static final String CANCELLED_FLAG = "cancelled";

    @Override
    public void notify(DelegateExecution execution) throws Exception 
    {
        runScript(execution);
    }
    
    @Override
    protected Map<String, Object> getInputMap(DelegateExecution execution,
                String runAsUser) 
    {
        Map<String, Object> scriptModel =  super.getInputMap(execution, runAsUser);

        ExecutionListenerExecution listenerExecution = (ExecutionListenerExecution) execution;

        // Add deleted/cancelled flags
        boolean cancelled = false;
        boolean deleted = false;
        
        if (ActivitiConstants.DELETE_REASON_DELETED.equals(listenerExecution.getDeleteReason()))
        {
            deleted = true;
        } 
        else if (ActivitiConstants.DELETE_REASON_CANCELLED.equals(listenerExecution.getDeleteReason()))
        {
            cancelled = true;
        }
        scriptModel.put(DELETED_FLAG, deleted);
        scriptModel.put(CANCELLED_FLAG, cancelled);
        
        return scriptModel;
    }
}
