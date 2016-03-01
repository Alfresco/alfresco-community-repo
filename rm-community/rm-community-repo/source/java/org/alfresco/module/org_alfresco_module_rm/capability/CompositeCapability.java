 
package org.alfresco.module.org_alfresco_module_rm.capability;

import java.util.Set;

/**
 * Composite capability Interface.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public interface CompositeCapability extends Capability
{
    /**
     * Get set of child capabilities.
     * 
     * @return  {@link Set}<{@link Capability}> set of child capabilities.
     */
    Set<Capability> getCapabilities();
}
