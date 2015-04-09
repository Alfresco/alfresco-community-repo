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

import java.io.Serializable;

import org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * This class is a POJO data type for a Classification Level.
 *
 * @author Neil Mc Erlean
 * @since 3.0
 */
public final class ClassificationLevel implements Serializable
{
    /** serial version uid */
    private static final long serialVersionUID = -3375064867090476422L;
    
    private final String id;
    private final String displayLabelKey;

    public ClassificationLevel(final String id, final String displayLabelKey)
    {
        RMParameterCheck.checkNotBlank("id", id);
        RMParameterCheck.checkNotBlank("displayLabelKey", displayLabelKey);
        this.id              = id;
        this.displayLabelKey = displayLabelKey;
    }

    /** Returns the unique identifier for this classification level. */
    public String getId() { return this.id; }

    /** Returns the localised (current locale) display label for this classification level. */
    public String getDisplayLabel() { return I18NUtil.getMessage(displayLabelKey); }

    @Override public String toString()
    {
        StringBuilder msg = new StringBuilder();
        msg.append(ClassificationLevel.class.getSimpleName())
           .append(":").append(id);

        return  msg.toString();
    }

    @Override public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassificationLevel that = (ClassificationLevel) o;

        return this.id.equals(that.id);
    }

    @Override public int hashCode() { return id.hashCode(); }
}
