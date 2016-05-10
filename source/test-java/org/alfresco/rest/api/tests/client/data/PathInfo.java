
package org.alfresco.rest.api.tests.client.data;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

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

        public String getName()
        {
            return name;
        }

        public NodeRef getId()
        {
            return id;
        }
    }
}
