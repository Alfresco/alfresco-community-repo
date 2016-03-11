package org.alfresco.module.org_alfresco_module_rm.action;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.service.cmr.action.ActionDefinition;

/**
 * Extended action definition interface.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public interface RecordsManagementActionDefinition extends ActionDefinition
{
    /**
     * @return  list of applicable file plan component kinds
     */
    Set<FilePlanComponentKind> getApplicableKinds();
}
