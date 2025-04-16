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

import java.util.List;

/**
 * Representation of a path info
 *
 * @author janv
 */
public class PathInfo
{
    private String name;
    private Boolean isComplete;
    private String relativePath;
    private List<ElementInfo> elements;

    public PathInfo()
    {}

    public PathInfo(String name, Boolean isComplete, List<ElementInfo> elements)
    {
        this.name = name;
        this.isComplete = isComplete;
        this.elements = elements;
    }

    public String getName()
    {
        return name;
    }

    public Boolean getIsComplete()
    {
        return isComplete;
    }

    public List<ElementInfo> getElements()
    {
        return elements;
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    public void setRelativePath(String relativePath)
    {
        this.relativePath = relativePath;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(120);
        sb.append("PathInfo [name=").append(name)
                .append(", isComplete=").append(isComplete)
                .append(", elements=").append(elements)
                .append(']');
        return sb.toString();
    }

    public static class ElementInfo
    {

        private String id;
        private String name;
        private String nodeType;
        private List<String> aspectNames;

        public ElementInfo()
        {}

        public ElementInfo(String id, String name, String nodeType, List<String> aspectNames)
        {
            this.id = id;
            this.name = name;
            this.nodeType = nodeType;
            this.aspectNames = aspectNames;
        }

        public String getName()
        {
            return name;
        }

        public String getId()
        {
            return id;
        }

        public String getNodeType()
        {
            return nodeType;
        }

        public List<String> getAspectNames()
        {
            return aspectNames;
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder(250);
            sb.append("PathElement [id=").append(id)
                    .append(", name=").append(name)
                    .append(", nodeType=").append(nodeType)
                    .append(", aspectNames=").append(aspectNames)
                    .append(']');
            return sb.toString();
        }
    }
}
