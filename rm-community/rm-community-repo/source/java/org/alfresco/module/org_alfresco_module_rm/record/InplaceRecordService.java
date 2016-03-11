package org.alfresco.module.org_alfresco_module_rm.record;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Inplace Record Service Interface.
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public interface InplaceRecordService
{
    /**
     * Hides a record within a collaboration site
     *
     * @param nodeRef   The record which should be hidden
     */
    void hideRecord(NodeRef nodeRef);

    /**
     * Moves a record within a collaboration site
     *
     * @param nodeRef                The record which should be moved
     * @param targetNodeRef          The target node reference where it should be moved to
     */
    void moveRecord(NodeRef nodeRef, NodeRef targetNodeRef);
}
