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

package org.alfresco.module.org_alfresco_module_rm.caveat;

import java.util.Arrays;

public class RMConstraintInfo
{
    private String name;
    private String title;
    private boolean caseSensitive;
    private String[] allowedValues;

    public void setName(String name)
    {
        this.name = name;
    }
    public String getName()
    {
        return name;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    public String getTitle()
    {
        return title;
    }
    public void setCaseSensitive(boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }
    public boolean isCaseSensitive()
    {
        return caseSensitive;
    }
    public void setAllowedValues(String[] values)
    {
        this.allowedValues = values.clone();
    }
    public String[] getAllowedValues()
    {
        return allowedValues;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("RMConstraintInfo [name=");
        builder.append(this.name);
        builder.append(", title=");
        builder.append(this.title);
        builder.append(", caseSensitive=");
        builder.append(this.caseSensitive);
        builder.append(", allowedValues=");
        builder.append(Arrays.toString(this.allowedValues));
        builder.append("]");
        return builder.toString();
    }

}
