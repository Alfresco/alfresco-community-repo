/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.module.org_alfresco_module_rm.caveat.scheme;

import org.alfresco.module.org_alfresco_module_rm.util.CoreServicesExtras;

/**
 * A marking from a caveat group that can be applied to content to restrict access to users who have a corresponding
 * mark.
 *
 * @author Tom Page
 * @since 2.4.a
 */
public class CaveatMark
{
    /** The id of the group that this mark belongs to. */
    private String groupId;
    /** The id for this mark. */
    private String id;
    /** The I18N key for the display label of this mark. */
    private String displayLabelKey;

    /**
     * Constructor for the caveat group.
     *
     * @param groupId The id of the group that this mark belongs to.
     * @param id The id for this mark.
     * @param displayLabelKey The I18N key for the display label of this mark.
     */
    public CaveatMark(String groupId, String id, String displayLabelKey)
    {
        this.groupId = groupId;
        this.id = id;
        this.displayLabelKey = displayLabelKey;
    }

    /**
     * Constructor for a caveat group that does not yet belong to a group.
     *
     * @param id The id for this mark.
     * @param displayLabelKey The I18N key for the display label of this mark.
     */
    public CaveatMark(String id, String displayLabelKey)
    {
        this.id = id;
        this.displayLabelKey = displayLabelKey;
    }

    /**
     * Get the identifier for the group that this mark belongs to.
     */
    public String getGroupId()
    {
        return groupId;
    }

    /**
     * Set the identifier of the group that this mark belongs to.
     */
    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    /**
     * Get the identifier for the mark.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Get the display label for the mark.
     */
    public String getDisplayLabel()
    {
        return CoreServicesExtras.getI18NMessageOrKey(displayLabelKey);
    }
}
