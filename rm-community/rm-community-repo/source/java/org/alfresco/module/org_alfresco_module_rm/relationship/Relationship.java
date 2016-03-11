package org.alfresco.module.org_alfresco_module_rm.relationship;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Interface representing the relationship
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public interface Relationship
{
    /**
     * Gets the unique name of the relationship
     *
     * @return The unique name of the relationship
     */
    String getUniqueName();

    /**
     * Gets the source of the relationship
     *
     * @return The source of the relationship
     */
    NodeRef getSource();

    /**
     * Gets the target of the relationship
     *
     * @return The target of the relationship
     */
    NodeRef getTarget();
}
