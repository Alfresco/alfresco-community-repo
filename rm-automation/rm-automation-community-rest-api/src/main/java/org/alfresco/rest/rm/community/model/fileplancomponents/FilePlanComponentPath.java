/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.model.fileplancomponents;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO for FilePlanComponent path parameter
 * <br>
 * @author Kristijan Conkas
 * @since 2.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilePlanComponentPath
{
    private String name;
    private Boolean isComplete;
    private List<FilePlanComponentIdNamePair> elements;

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the isComplete
     */
    public Boolean isComplete()
    {
        return this.isComplete;
    }

    /**
     * @param isComplete the isComplete to set
     */
    public void setComplete(Boolean isComplete)
    {
        this.isComplete = isComplete;
    }

    /**
     * @return the elements
     */
    public List<FilePlanComponentIdNamePair> getElements()
    {
        return this.elements;
    }

    /**
     * @param elements the elements to set
     */
    public void setElements(List<FilePlanComponentIdNamePair> elements)
    {
        this.elements = elements;
    }
}
