/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
