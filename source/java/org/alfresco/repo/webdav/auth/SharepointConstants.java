package org.alfresco.repo.webdav.auth;

/**
 * A place to put Sharepoint specific authentication constants.
 * 
 * @author dward
 */
public interface SharepointConstants
{

    /** The session attribute under which sharepoint {@link AuthenticationDriver}s store their user objects. */
    public final static String USER_SESSION_ATTRIBUTE = "_vtiAuthTicket";

}
