package org.alfresco.repo.security.authentication.external;

import javax.servlet.http.HttpServletRequest;

/**
 * An interface for objects capable of extracting an externally authenticated user ID from an HTTP request.
 * 
 * @author dward
 */
public interface RemoteUserMapper
{
   /**
    * Gets an externally authenticated user ID from an HTTP request.
    * 
    * @param request
    *           the request
    * @return the user ID or <code>null</code> if the user is unauthenticated
    */
   public String getRemoteUser(HttpServletRequest request);
}
