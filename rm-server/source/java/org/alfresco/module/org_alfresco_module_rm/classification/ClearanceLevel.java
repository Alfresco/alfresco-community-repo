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
package org.alfresco.module.org_alfresco_module_rm.classification;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck;
import org.alfresco.util.ParameterCheck;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * A POJO to represent a security clearance level. This wraps a {@link ClassificationLevel} and will often have the same
 * display text as well. The main exception is that the clearance level corresponding to "Unclassified" is "No Clearance".
 *
 * @author tpage
 */
public class ClearanceLevel
{
    /** The highest classification level that can be accessed by users with this clearance. */
    private final ClassificationLevel highestClassificationLevel;
    /** The key for the display label of this security clearance. */
    private final String displayLabelKey;

    /**
     * Constructor.
     *
     * @param highestClassificationLevel The highest classification level that can be accessed by users with this clearance.
     * @param displayLabelKey The key for the display label of this security clearance.
     */
    public ClearanceLevel(ClassificationLevel highestClassificationLevel, String displayLabelKey)
    {
        ParameterCheck.mandatory("highestClassificationLevel", highestClassificationLevel);
        RMParameterCheck.checkNotBlank("displayLabelKey", displayLabelKey);
        this.highestClassificationLevel = highestClassificationLevel;
        this.displayLabelKey = displayLabelKey;
    }

    /** Return the highest classification level that can be accessed by users with this clearance. */
    public ClassificationLevel getHighestClassificationLevel() { return this.highestClassificationLevel; }

    /**
     * Returns the localised (current locale) display label for this clearance level. If no translation is found
     * then return the key instead.
     */
    public String getDisplayLabel()
    {
        String message = I18NUtil.getMessage(displayLabelKey);
        return (isNotBlank(message) ? message : displayLabelKey);
    }

    @Override public String toString()
    {
        StringBuilder msg = new StringBuilder();
        msg.append(ClassificationLevel.class.getSimpleName())
           .append(":").append(highestClassificationLevel.getId());

        return  msg.toString();
    }

    @Override public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClearanceLevel that = (ClearanceLevel) o;

        return this.highestClassificationLevel.equals(that.highestClassificationLevel);
    }

    @Override public int hashCode() { return highestClassificationLevel.hashCode(); }
}
