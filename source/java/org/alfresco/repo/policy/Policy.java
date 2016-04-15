package org.alfresco.repo.policy;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Marker interface for representing a Policy.
 * 
 * @author David Caruana
 */
@AlfrescoPublicApi
public interface Policy
{
    /**
     * mandatory static field on a <tt>Policy</tt> that can be overridden in
     * derived policies
     */
    static String NAMESPACE = NamespaceService.ALFRESCO_URI;
    
    /**
     * Argument Configuration
     */
    public enum Arg
    {
        KEY,
        START_VALUE,
        END_VALUE
    }
        
}
