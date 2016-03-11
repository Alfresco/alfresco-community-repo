package org.alfresco.module.org_alfresco_module_rm.script.hold;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Hold POJO
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public class Hold
{
    /** Hold name */
    private String name;

    /** Hold node reference */
    private NodeRef nodeRef;

    /**
     * Constructor
     *
     * @param name The name of the hold
     * @param nodeRef The {@link NodeRef} of the hold
     */
    public Hold(String name, NodeRef nodeRef)
    {
        this.name = name;
        this.nodeRef = nodeRef;
    }

    /**
     * Gets the hold name
     *
     * @return The name of the hold
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Gets the hold node reference
     *
     * @return The {@link NodeRef} of the hold
     */
    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }
}
