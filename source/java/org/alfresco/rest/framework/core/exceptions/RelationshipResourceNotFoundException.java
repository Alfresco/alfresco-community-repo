package org.alfresco.rest.framework.core.exceptions;

/**
 * The addressed Relationship Resource was not found
 *
 * @author Gethin James
 */
public class RelationshipResourceNotFoundException extends NotFoundException
{
    private static final long serialVersionUID = 6780456990930538458L;
    public static String DEFAULT_MESSAGE_ID = "framework.exception.RelationshipNotFound";
    
    /**
     * The entity id param will be shown in the default error message.
     * @param msgId
     * @param entity
     */
    public RelationshipResourceNotFoundException(String entityId, String relationshipId)
    {
        super(DEFAULT_MESSAGE_ID, new String[] {entityId, relationshipId});
    }
}
