 
package org.alfresco.module.org_alfresco_module_rm.action;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.repo.action.ActionDefinitionImpl;

/**
 * Extended action definition implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class RecordsManagementActionDefinitionImpl extends ActionDefinitionImpl implements RecordsManagementActionDefinition
{
    /** generated serial version id */
    private static final long serialVersionUID = -5226538434707253206L;

    /** Applicable kinds */
    private Set<FilePlanComponentKind> applicableKinds;
    
    /**
     * Default constructor.
     * 
     * @param name  action definition name
     */
    public RecordsManagementActionDefinitionImpl(String name)
    {
        super(name);
    }

    /**
     * @param applicableKinds   applicable kinds
     */
    public void setApplicableKinds(Set<FilePlanComponentKind> applicableKinds)
    {
        this.applicableKinds = applicableKinds;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionDefinition#getApplicableKinds()
     */
    @Override
    public Set<FilePlanComponentKind> getApplicableKinds()
    {
        return applicableKinds;
    }    
}
