/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.rendition;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This simple class is a struct containing a rendition node, its parent and its name.
 */
public class RenditionLocationImpl implements RenditionLocation
{
    private final NodeRef parentRef;
    private final NodeRef childRef;
    private final String childName;

    public RenditionLocationImpl(NodeRef parentRef, NodeRef childRef, String childName)
    {
        this.parentRef = parentRef;
        this.childRef = childRef;
        this.childName = childName;
    }

    public String getChildName()
    {
        return childName;
    }

    public NodeRef getParentRef()
    {
        return parentRef;
    }

    public NodeRef getChildRef()
    {
        return childRef;
    }
}
