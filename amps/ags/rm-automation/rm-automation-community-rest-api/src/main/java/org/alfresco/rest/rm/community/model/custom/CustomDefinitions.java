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
package org.alfresco.rest.rm.community.model.custom;

/**
 * List of existing records management custom references.
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public enum CustomDefinitions
{
    ATTACHMENT("Attachment"),
    MESSAGE("Message"),
    NEXT_VERSION("Next Version"),
    RENDITION("Rendition");
    /**
     * The name of custom reference.
     */
    private String definition;

    /**
     * Private constructor.
     */
    CustomDefinitions(String definition)
    {
        this.definition = definition;
    }

    /**
     * Get the name of the custom reference.
     *
     * @return The value of custom reference.
     */
    public String getDefinition()
    {
        return definition;
    }
}
