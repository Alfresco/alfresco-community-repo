 

package org.alfresco.module.org_alfresco_module_rm.action.constraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.repo.action.constraint.BaseParameterConstraint;

/**
 * Record type parameter constraint
 * 
 * @author Craig Tan
 * @since 2.1
 */
public class DispositionActionParameterConstraint extends BaseParameterConstraint
{
    /** Name constant */
    public static final String NAME = "rm-ac-record-types";
    
    private RecordsManagementActionService rmActionService;

    public void setRecordsManagementActionService(RecordsManagementActionService rmActionService)
    {
        this.rmActionService = rmActionService;
    }

    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getAllowableValues()
     */
    protected Map<String, String> getAllowableValuesImpl()
    {   
        List<RecordsManagementAction> rmActions = rmActionService.getDispositionActions();

        Map<String, String> result = new HashMap<String, String>(rmActions.size());
        for (RecordsManagementAction rmAction : rmActions)
        {
            result.put(rmAction.getName(), rmAction.getLabel());
        }        
        return result;
    }    
    
    
}
