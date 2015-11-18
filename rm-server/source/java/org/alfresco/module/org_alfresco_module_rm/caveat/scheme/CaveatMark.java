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

import static org.alfresco.module.org_alfresco_module_rm.caveat.CaveatConstants.DEFAULT_CAVEAT_PREFIX;

import java.io.Serializable;

import org.alfresco.module.org_alfresco_module_rm.util.CoreServicesExtras;
import org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck;
import org.apache.commons.lang3.StringUtils;

/**
 * A marking from a caveat group that can be applied to content to restrict access to users who have a corresponding
 * mark.
 *
 * @author Tom Page
 * @since 2.4.a
 */
public class CaveatMark implements Serializable
{
    /** Generated serial version id. */
    private static final long serialVersionUID = 2805846540946220526L;
    /** The id of the group that this mark belongs to. */
    private String groupId;
    /** The id for this mark. */
    private final String id;
    /** The I18N key for the display label of this mark. */
    private String displayLabelKey;

    /**
     * Constructor for the caveat group.
     *
     * @param id The id for this mark.
     * @param displayLabelKey The I18N key for the display label of this mark.
     * @param groupId The id of the group that this mark belongs to. If null is supplied then this can be set later.
     */
    public CaveatMark(String id, String displayLabelKey, String groupId)
    {
        RMParameterCheck.checkNotBlank("id", id);

        this.id = id;
        this.displayLabelKey = displayLabelKey;
        if (!StringUtils.isBlank(groupId))
        {
            // Set the group id, and also set the displayLabelKey to the default value if possible (and not already set).
            setGroupId(groupId);
        }
    }

    /**
     * Constructor for a caveat group that does not yet belong to a group.
     *
     * @param id The id for this mark.
     * @param displayLabelKey The I18N key for the display label of this mark. If a key is not supplied then the default
     *            display label key is used instead.
     */
    public CaveatMark(String id, String displayLabelKey)
    {
        this(id, displayLabelKey, null);
    }

    /**
     * Constructor for a caveat group that does not yet belong to a group.
     * <p>
     * This uses the default display label key.
     *
     * @param id The id for this mark.
     */
    public CaveatMark(String id)
    {
        this(id, null);
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
     * <p>
     * If no display label key has been provided up to this point then also create a default value.
     */
    public void setGroupId(String groupId)
    {
        RMParameterCheck.checkNotBlank("groupId", groupId);

        this.groupId = groupId;

        if (StringUtils.isBlank(displayLabelKey))
        {
            displayLabelKey = DEFAULT_CAVEAT_PREFIX + groupId + ".mark." + id + ".label";
        }
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

    /**
     * Get the display label key for the mark.
     */
    public String getDisplayLabelKey()
    {
        return displayLabelKey;
    }

    @Override
    public String toString()
    {
        StringBuilder msg = new StringBuilder();
        msg.append(this.getClass().getSimpleName())
           .append(":").append(id);

        return msg.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CaveatMark that = (CaveatMark) o;

        return this.id.equals(that.id);
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }
}
