/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.rest.api.tests.client.data;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Representation of a path info (initially for client tests for File Folder API)
 *
 * @author janv
 */
public class PathInfo
{
    private String name;
    private Boolean isComplete;
    private List<ElementInfo> elements;

    public PathInfo()
    {
    }

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

    public static class ElementInfo
    {
        private NodeRef id;
        private String name;

        public ElementInfo()
        {
        }

        public ElementInfo(NodeRef id, String name)
        {
            this.id = id;
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public NodeRef getId()
        {
            return id;
        }

        public void expected(Object o)
        {
            assertTrue(o instanceof ElementInfo);

            ElementInfo other = (ElementInfo) o;
            assertEquals(id, other.getName());
            assertEquals(name, other.getName());
        }
    }

    public void expected(Object o)
    {
        assertTrue(o instanceof PathInfo);

        PathInfo other = (PathInfo) o;

        assertEquals(getIsComplete(), other.getIsComplete());
        assertEquals(getName(), other.getName());

        int idx = 0;
        for (ElementInfo element : elements)
        {
            ElementInfo otherElement = other.getElements().get(idx);

            assertEquals("Expected: "+element.getId()+", actual: "+otherElement.getId(), element.getId(), otherElement.getId());
            assertEquals("Expected: "+element.getName()+", actual: "+otherElement.getName(), element.getName(), otherElement.getName());
            idx++;
        }
    }
}
