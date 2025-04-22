/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.site.SiteVisibility;

/**
 * Class representing a site update API operation.
 *
 * @author Matt Ward
 * @since 5.2
 */
public class SiteUpdate implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String title;
    private String description;
    private SiteVisibility visibility;

    private Map<String, Boolean> setFields = new HashMap<>(3);

    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String VISIBILITY = "visibility";

    public SiteUpdate()
    {}

    public SiteUpdate(String title, String description, SiteVisibility visibility)
    {
        setTitle(title);
        setDescription(description);
        setVisibility(visibility);
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
        setFields.put(TITLE, true);
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
        setFields.put(DESCRIPTION, true);
    }

    public SiteVisibility getVisibility()
    {
        return visibility;
    }

    public void setVisibility(SiteVisibility visibility)
    {
        this.visibility = visibility;
        setFields.put(VISIBILITY, true);
    }

    public boolean wasSet(String fieldName)
    {
        Boolean b = setFields.get(fieldName);
        return (b != null ? b : false);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SiteUpdate that = (SiteUpdate) o;

        if (title != null ? !title.equals(that.title) : that.title != null)
            return false;
        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        return visibility == that.visibility;

    }

    @Override
    public int hashCode()
    {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (visibility != null ? visibility.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "SiteUpdate{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", visibility=" + visibility +
                '}';
    }
}
