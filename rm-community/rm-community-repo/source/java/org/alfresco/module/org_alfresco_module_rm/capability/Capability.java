package org.alfresco.module.org_alfresco_module_rm.capability;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * Capability Interface.
 *
 * @author andyh
 * @author Roy Wetherall
 */
public interface Capability
{
    /**
     * Does this capability apply to this nodeRef?
     * @param nodeRef
     * @return
     */
    AccessStatus hasPermission(NodeRef nodeRef);

    /**
     *
     * @param nodeRef
     * @return
     */
    int hasPermissionRaw(NodeRef nodeRef);

    /**
     * Evaluates the capability.
     *
     * @param nodeRef
     * @return
     */
    int evaluate(NodeRef nodeRef);

    /**
     * Evaluates the capability, taking into account a target.
     *
     * @param source    source node reference
     * @param target    target node reference
     * @return int      permission value
     */
    int evaluate(NodeRef source, NodeRef target);

    /**
     * Indicates whether this is a private capability or not.  Private capabilities are used internally, otherwise
     * they are made available to the user to assign to roles.
     *
     * @return  boolean true if private, false otherwise
     */
    boolean isPrivate();

    /**
     * Get the name of the capability
     *
     * @return  String  capability name
     */
    String getName();

    /**
     * Get the title of the capability
     *
     * @return  String  capability title
     */
    String getTitle();

    /**
     * Get the description of the capability
     *
     * @return  String  capability description
     */
    String getDescription();

    /**
     * Gets the group of a capability
     *
     * @return Group capability group
     */
    Group getGroup();

    /**
     * Gets the index of a capability
     *
     * @return int capability index
     */
    int getIndex();
}
