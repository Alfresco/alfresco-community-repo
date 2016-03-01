 
package org.alfresco.module.org_alfresco_module_rm.patch.v23;

import org.alfresco.module.org_alfresco_module_rm.bootstrap.RecordContributorsGroupBootstrapComponent;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;

/**
 * RM v2.3 patch that creates the record contributors group.
 *
 * @author Roy Wetherall
 * @since 2.3
 */
public class RMv23RecordContributorsGroupPatch extends AbstractModulePatch
{
    /** record contributors group bootstrap component */
    private RecordContributorsGroupBootstrapComponent recordContributorsGroupBootstrapComponent;
    
    /**
     * @param recordContributorsGroupBootstrapComponent record contributors group bootstrap component
     */
    public void setRecordContributorsGroupBootstrapComponent(RecordContributorsGroupBootstrapComponent recordContributorsGroupBootstrapComponent)
    {
        this.recordContributorsGroupBootstrapComponent = recordContributorsGroupBootstrapComponent;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch#applyInternal()
     */
    @Override
    public void applyInternal()
    {
        // create record contributors group
        recordContributorsGroupBootstrapComponent.createRecordContributorsGroup();
    }
    
}
