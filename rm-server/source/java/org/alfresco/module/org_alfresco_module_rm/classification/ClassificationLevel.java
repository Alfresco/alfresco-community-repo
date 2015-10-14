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

import org.alfresco.module.org_alfresco_module_rm.caveat.scheme.CaveatMark;
import org.alfresco.module.org_alfresco_module_rm.util.CoreServicesExtras;
import org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck;
import org.alfresco.util.ParameterCheck;

/**
 * This class is a POJO data type for a Classification Level.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public final class ClassificationLevel implements ClassificationSchemeEntity
{
    /** serial version uid */
    private static final long serialVersionUID = -3375064867090476422L;

    private CaveatMark caveatMark;
    private String id;
    private String displayLabelKey;

    public ClassificationLevel(final CaveatMark caveatMark)
    {
        ParameterCheck.mandatory("caveatMark", caveatMark);
        this.caveatMark = caveatMark;
    }

    public ClassificationLevel(final String id, final String displayLabelKey)
    {
        RMParameterCheck.checkNotBlank("id", id);
        RMParameterCheck.checkNotBlank("displayLabelKey", displayLabelKey);
        this.id              = id;
        this.displayLabelKey = displayLabelKey;
    }

    /** Returns the unique identifier for this classification level. */
    public String getId()
    {
        if (caveatMark == null)
        {
            return this.id;
        }
        return caveatMark.getId();
    }

    /** Returns the key for the display label. */
    public String getDisplayLabelKey()
    {
        if (caveatMark == null)
        {
            return displayLabelKey;
        }
        return caveatMark.getDisplayLabelKey();
    }

    /**
     * Returns the localised (current locale) display label for this classification level. If no translation is found
     * then return the key instead.
     */
    public String getDisplayLabel()
    {
        if (caveatMark == null)
        {
            return CoreServicesExtras.getI18NMessageOrKey(displayLabelKey);
        }
        return caveatMark.getDisplayLabel();
    }

    @Override public String toString()
    {
        StringBuilder msg = new StringBuilder();
        msg.append(ClassificationLevel.class.getSimpleName())
           .append(":").append(getId());

        return msg.toString();
    }

    @Override public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassificationLevel that = (ClassificationLevel) o;

        return this.getId().equals(that.getId());
    }

    @Override public int hashCode() { return getId().hashCode(); }
}
