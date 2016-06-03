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
