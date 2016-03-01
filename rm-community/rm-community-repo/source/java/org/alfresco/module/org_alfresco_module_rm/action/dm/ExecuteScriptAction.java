 
package org.alfresco.module.org_alfresco_module_rm.action.dm;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ScriptActionExecuter;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;

/**
 * Executes a JavaScript
 * 
 * Note:  This is a 'normal' dm action, rather than a records management action.
 * 
 * @author Craig Tan
 */
public class ExecuteScriptAction extends ScriptActionExecuter
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_SCRIPTREF, DataTypeDefinition.NODE_REF, true,
                getParamDisplayLabel(PARAM_SCRIPTREF), false, "rm-ac-scripts"));
    }

}
