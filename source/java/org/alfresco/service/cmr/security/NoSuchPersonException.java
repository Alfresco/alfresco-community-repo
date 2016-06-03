package org.alfresco.service.cmr.security;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.security.person.PersonException;

/**
 * Thrown when a person doesn't exist and can't be created.
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public class NoSuchPersonException extends PersonException
{
    private static final long serialVersionUID = -8514361120995433997L;

    private final String userName;
    
    public NoSuchPersonException(String userName)
    {
        super(String.format("User does not exist and could not be created: %s", userName));
        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }
}
