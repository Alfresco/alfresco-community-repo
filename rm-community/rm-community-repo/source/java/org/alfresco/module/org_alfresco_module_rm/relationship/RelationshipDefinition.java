package org.alfresco.module.org_alfresco_module_rm.relationship;

/**
 * Interface representing the relationship definition
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public interface RelationshipDefinition
{
    /**
     * Gets the unique name of the relationship definition
     *
     * @return The unique name of the relationship definition
     */
    String getUniqueName();

    /**
     * Gets the type of the relationship definition
     *
     * @return The type of the relationship definition
     */
    RelationshipType getType();

    /**
     * Gets the display name of the relationship definition
     *
     * @return The display name of the relationship definition
     */
    RelationshipDisplayName getDisplayName();
}
