package org.alfresco.module.org_alfresco_module_rm.relationship;

import static org.alfresco.util.ParameterCheck.mandatory;
import static org.alfresco.util.ParameterCheck.mandatoryString;

/**
 * Relationship definition implementation
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RelationshipDefinitionImpl implements RelationshipDefinition
{
    /** The unique name of the relationship definition */
    private String uniqueName;

    /** The type of the relationship definition */
    private RelationshipType type;

    /** The display name of the relationship definition */
    private RelationshipDisplayName displayName;

    /**
     * Constructor for creating a relationship definition
     *
     * @param uniqueName The unique name of the relationship definition
     * @param type The type of the relationship definition
     * @param displayName The display name of the relationship definition
     */
    public RelationshipDefinitionImpl(String uniqueName, RelationshipType type, RelationshipDisplayName displayName)
    {
        mandatoryString("uniqueName", uniqueName);
        mandatory("type", type);
        mandatory("displayName", displayName);

        setUniqueName(uniqueName);
        setType(type);
        setDisplayName(displayName);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDefinition#getUniqueName()
     */
    @Override
    public String getUniqueName()
    {
        return this.uniqueName;
    }

    /**
     * Sets the name of the relationship definition
     *
     * @param uniqueName The name of the relationship definition
     */
    private void setUniqueName(String uniqueName)
    {
        this.uniqueName = uniqueName;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDefinition#getType()
     */
    @Override
    public RelationshipType getType()
    {
        return this.type;
    }

    /**
     * Sets the type of the relationship definition
     *
     * @param type The type of the relationship definition
     */
    private void setType(RelationshipType type)
    {
        this.type = type;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDefinition#getDisplayName()
     */
    @Override
    public RelationshipDisplayName getDisplayName()
    {
        return this.displayName;
    }

    /**
     * Sets the display name of the relationship definition
     *
     * @param displayName The display name of the relationship definition
     */
    private void setDisplayName(RelationshipDisplayName displayName)
    {
        this.displayName = displayName;
    }
}
