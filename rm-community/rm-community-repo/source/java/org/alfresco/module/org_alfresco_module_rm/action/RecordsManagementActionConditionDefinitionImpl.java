package org.alfresco.module.org_alfresco_module_rm.action;

import org.alfresco.repo.action.ActionConditionDefinitionImpl;

/**
 * Records management condition definition implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class RecordsManagementActionConditionDefinitionImpl extends ActionConditionDefinitionImpl
                                                     implements RecordsManagementActionConditionDefinition
{
    /** Serial Version UID */
    private static final long serialVersionUID = -7599279732731533610L;

    /**
     * Default constructor.
     * 
     * @param name  name of the condition
     */
    public RecordsManagementActionConditionDefinitionImpl(String name)
    {
        super(name);
    }
}
