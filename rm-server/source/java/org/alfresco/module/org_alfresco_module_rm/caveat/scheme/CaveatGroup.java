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
import java.util.List;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.alfresco.module.org_alfresco_module_rm.caveat.CaveatException.CaveatMarkNotFound;
import org.alfresco.module.org_alfresco_module_rm.util.CoreServicesExtras;
import org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * A group of related caveat marks and metadata describing how they are related.
 *
 * @author Tom Page
 * @since 2.4.a
 */
public class CaveatGroup implements Serializable
{
    /** Generated serial version id. */
    private static final long serialVersionUID = -8909291192015016370L;
    /** The unique id of the caveat group. */
    private final String id;
    /** The key to retrieve the I18n'ed display label for the caveat group. */
    private final String displayLabelKey;
    /** The key to retrieve the I18n'ed description of the caveat group. */
    private final String descriptionKey;
    /** The model property in which this caveat group will store marks on objects. */
    private final QName modelProperty;
    /** The relationship between marks in the group. */
    private final CaveatGroupType caveatGroupType;
    /** The marks that are contained in this group, ordered according to the list supplied in the constructor. */
    private final ImmutableMap<String, CaveatMark> caveatMarks;

    /**
     * Constructor for the caveat group.
     * <p>
     * This sets the group id of all the caveat marks that are supplied. It uses the
     * default keys for the display label and description.
     *
     * @param id The unique id of the caveat group.
     * @param caveatGroupType The relationship between marks in the group.
     * @param caveatMarks The marks that are contained in this group.
     */
    public CaveatGroup(String id, QName modelProperty, CaveatGroupType caveatGroupType, List<CaveatMark> caveatMarks)
    {
        this(id, null, null, modelProperty, caveatGroupType, caveatMarks);
    }

    /**
     * Constructor for the caveat group.
     * <p>
     * This sets the group id of all the caveat marks that are supplied.
     *
     * @param id The unique id of the caveat group.
     * @param displayLabelKey The key to retrieve the I18n'ed display label for the caveat group. If null then use the
     *            default label key.
     * @param descriptionKey The key to retrieve the I18n'ed description of the caveat group. If null then use the
     *            default description key.
     * @param caveatGroupType The relationship between marks in the group.
     * @param caveatMarks The marks that are contained in this group.
     */
    public CaveatGroup(String id, String displayLabelKey, String descriptionKey,
                       QName modelProperty, CaveatGroupType caveatGroupType,
                       List<CaveatMark> caveatMarks)
    {
        RMParameterCheck.checkNotBlank("id", id);
        ParameterCheck.mandatory("caveatGroupType", caveatGroupType);
        ParameterCheck.mandatoryCollection("caveatMarks", caveatMarks);

        if (StringUtils.isBlank(displayLabelKey))
        {
            displayLabelKey = DEFAULT_CAVEAT_PREFIX + id + ".label";
        }
        if (StringUtils.isBlank(descriptionKey))
        {
            descriptionKey = DEFAULT_CAVEAT_PREFIX + id + ".description";
        }

        this.id = id;
        this.displayLabelKey = displayLabelKey;
        this.descriptionKey = descriptionKey;
        this.modelProperty = modelProperty;
        this.caveatGroupType = caveatGroupType;
        for (CaveatMark caveatMark : caveatMarks)
        {
            caveatMark.setGroupId(id);
        }
        this.caveatMarks = immutableMapOf(caveatMarks);
    }

    /**
     * Create an immutable map from the supplied caveat marks.
     *
     * @param caveatMarks A list of the marks.
     * @return An map from group id to caveat group, with keys in the same order as the list.
     */
    private ImmutableMap<String, CaveatMark> immutableMapOf(List<CaveatMark> caveatMarks)
    {
        Builder<String, CaveatMark> builder = ImmutableMap.builder();
        for (CaveatMark caveatMark : caveatMarks)
        {
            builder.put(caveatMark.getId(), caveatMark);
        }
        return builder.build();
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

    /** Gets the content model property in which the caveat marks will be recorded. */
    public QName getModelProperty()
    {
        return modelProperty;
    }

    /**
     * Indicates how the marks in the caveat groups are related.
     */
    public CaveatGroupType getCaveatGroupType()
    {
        return caveatGroupType;
    }

    /**
     * Get the caveat marks in the order they were supplied.
     */
    public ImmutableCollection<CaveatMark> getCaveatMarks()
    {
        return caveatMarks.values();
    }

    /**
     * Indicates whether a mark exists in this caveat group or not.
     *
     * @param markId The identifier of the mark.
     */
    public boolean hasCaveatMark(String markId)
    {
        return caveatMarks.containsKey(markId);
    }

    /**
     * Get caveat mark by identifier.
     *
     * @param markId The identified of the mark.
     * @throws CaveatMarkNotFound If the supplied id does not match a mark in this group.
     */
    public CaveatMark getCaveatMark(String markId)
    {
        if (!hasCaveatMark(markId))
        {
            throw new CaveatMarkNotFound(markId);
        }
        return caveatMarks.get(markId);
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

        CaveatGroup that = (CaveatGroup) o;

        return this.id.equals(that.id);
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }
}
