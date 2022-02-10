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

package org.alfresco.module.org_alfresco_module_rm.email;

/**
 * Custom EMail Mapping
 */
public class CustomMapping
{
    private String from;
    private String to;

    /**
     * Default constructor.
     */
    public CustomMapping()
    {
    }

    /**
     * Default constructor.
     * @param from
     * @param to
     */
    public CustomMapping(String from, String to)
    {
        this.from = from;
        this.to = to;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getFrom()
    {
        return from;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    public String getTo()
    {
        return to;
    }

    public int hashCode()
    {
        if(from != null && to != null)
        {
            return (from + to).hashCode();
        }
        else
        {
            return 1;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }

        final CustomMapping other = (CustomMapping) obj;

        if (!from.equals(other.getFrom()))
        {
            return false;
        }
        if (!to.equals(other.getTo()))
        {
            return false;
        }
        return true;
    }
}
