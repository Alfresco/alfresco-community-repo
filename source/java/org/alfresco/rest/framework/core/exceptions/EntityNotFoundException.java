package org.alfresco.rest.framework.core.exceptions;

/**
 * The addressed entity was not found
 *
 * @author Gethin James
 */
public class EntityNotFoundException extends NotFoundException
{
    private static final long serialVersionUID = -1198595000441207734L;
    public static String DEFAULT_MESSAGE_ID = "framework.exception.EntityNotFound";
    
    /**
     * The entity id param will be shown in the default error message.
     * @param msgId
     * @param entity
     */
    public EntityNotFoundException(String entityId)
    {
        super(DEFAULT_MESSAGE_ID, new String[] {entityId});
    }
}
