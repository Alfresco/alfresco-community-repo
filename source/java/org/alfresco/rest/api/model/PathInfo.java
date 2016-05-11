
package org.alfresco.rest.api.model;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Representation of a path info
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

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(120);
        sb.append("PathInfo [name='").append(name)
                    .append(", isComplete=").append(isComplete)
                    .append(", elements=").append(elements)
                    .append(']');
        return sb.toString();
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

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder(250);
            sb.append("PathElement [id=").append(id)
                        .append(", name='").append(name)
                        .append(']');
            return sb.toString();
        }
    }
}
