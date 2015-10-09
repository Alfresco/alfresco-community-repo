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

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.alfresco.module.org_alfresco_module_rm.caveat.CaveatException.CaveatMarkNotFound;
import org.alfresco.module.org_alfresco_module_rm.util.CoreServicesExtras;

/**
 * A group of related caveat marks and metadata describing how they are related.
 *
 * @author Tom Page
 * @since 2.4.a
 */
public class CaveatGroup
{
    /** The unique id of the caveat group. */
    private String id;
    /** The key to retrieve the I18n'ed display label for the caveat group. */
    private String displayLabelKey;
    /** The key to retrieve the I18n'ed description of the caveat group. */
    private String descriptionKey;
    /** The relationship between marks in the group. */
    private CaveatGroupType caveatGroupType;
    /** The marks that are contained in this group. */
    private ImmutableList<CaveatMark> caveatMarks;

    /**
     * Constructor for the caveat group. This sets the group id of all the caveat marks that are supplied.
     *
     * @param id The unique id of the caveat group.
     * @param displayLabelKey The key to retrieve the I18n'ed display label for the caveat group.
     * @param descriptionKey The key to retrieve the I18n'ed description of the caveat group.
     * @param caveatGroupType The relationship between marks in the group.
     * @param caveatMarks The marks that are contained in this group.
     */
    public CaveatGroup(String id, String displayLabelKey, String descriptionKey, CaveatGroupType caveatGroupType,
                List<CaveatMark> caveatMarks)
    {
        this.id = id;
        this.displayLabelKey = displayLabelKey;
        this.descriptionKey = descriptionKey;
        this.caveatGroupType = caveatGroupType;
        this.caveatMarks = ImmutableList.copyOf(caveatMarks);
        for (CaveatMark caveatMark : caveatMarks)
        {
            caveatMark.setGroupId(id);
        }
    }

    /**
     * Gets the unique id of the caveat group.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Gets the I18n'ed display label for the caveat group.
     */
    public String getDisplayLabel()
    {
        return CoreServicesExtras.getI18NMessageOrKey(displayLabelKey);
    }

    /**
     * Gets the I18n'ed description of the caveat group.
     */
    public String getDescription()
    {
        return CoreServicesExtras.getI18NMessageOrKey(descriptionKey);
    }

    /**
     * Indicates how the marks in the caveat groups are related.
     */
    public CaveatGroupType getCaveatGroupType()
    {
        return caveatGroupType;
    }

    /**
     * Get caveat marks in ordered list with first being the most inclusive.
     */
    public ImmutableList<CaveatMark> getCaveatMarks()
    {
        return caveatMarks;
    }

    /**
     * Indicates whether a mark exists in this caveat group or not.
     *
     * @param The identifier of the mark.
     */
    public boolean hasCaveatMark(String markId)
    {
        for (CaveatMark caveatMark : caveatMarks)
        {
            if (caveatMark.getId().equals(markId)) { return true; }
        }
        return false;
    }

    /**
     * Get caveat mark by identifier.
     *
     * @param markId The identified of the mark.
     * @throws CaveatMarkNotFound If the supplied id does not match a mark in this group.
     */
    public CaveatMark getCaveatMark(String markId)
    {
        for (CaveatMark caveatMark : caveatMarks)
        {
            if (caveatMark.getId().equals(markId)) { return caveatMark; }
        }
        throw new CaveatMarkNotFound(markId);
    }
}
