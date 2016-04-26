package org.alfresco.service.cmr.security;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;

/**
 * Evaluate public service entry conditions as defined in the security interceptors.
 * Decouples any understanding of the security model from asking can I invoke the method and expect it to work.
 * @author andyh
 *
 */
public interface PublicServiceAccessService
{
    /**
     * @param publicService - the name of the public service
     * @param method - the method call
     * @param args - the arguments to the method as you woud call the method
     * @return AccessStatus
     */
    @Auditable(parameters = { "publicService", "method" })
    public AccessStatus hasAccess(String publicService, String method, Object ... args);

}
