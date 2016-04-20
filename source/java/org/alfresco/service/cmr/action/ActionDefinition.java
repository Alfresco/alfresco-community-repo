package org.alfresco.service.cmr.action;

import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.QName;

/**
 * Rule action interface.
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface ActionDefinition extends ParameterizedItemDefinition
{
    /**
     * Gets a list of the types that this action item is applicable for
     * 
     * @return  set of types never <tt>null</tt>
     */
    public Set<QName> getApplicableTypes();
    
    /**
     * Get whether the basic action definition supports action tracking
     * or not.  This can be overridden for each {@link Action#getTrackStatus() action}
     * but if not, this value is used.  Defaults to <tt>false</tt>.
     * 
     * @return      <tt>true</tt> to track action execution status or <tt>false</tt> (default)
     *              to do no action tracking
     * 
     * @since 3.4.1
     */
    public boolean getTrackStatus();
}
