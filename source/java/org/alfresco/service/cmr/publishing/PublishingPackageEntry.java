
package org.alfresco.service.cmr.publishing;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @since 4.0
 */
public interface PublishingPackageEntry
{
    /**
     * Retrieve the identifier of the node that this publishing package entry
     * relates to
     * 
     * @return A NodeRef object that identifies the node that this publishing
     *         package entry relates to
     */
    NodeRef getNodeRef();

    /**
     * Retrieve the snapshot of the node that is held as the payload of this
     * publishing package entry. The snapshot is taken when the containing
     * publishing package is placed on the publishing queue IF this is a
     * "publish" entry as opposed to an "unpublish" entry. No snapshot is taken
     * for an unpublish entry.
     * 
     * @return The snapshot of the node that this publishing package entry
     *         relates to if this is a "publish" entry (
     *         <code>null</node> if this is an "unpublish" entry). The snapshot is taken when
     * the containing publishing package is placed on the publishing queue, so if this operation is called before that point
     * then it will return <code>null</code>.
     */
    NodeSnapshot getSnapshot();

    /**
     * Determine if this entry relates to a publish request or an unpublish
     * request
     * 
     * @return <code>true</code> if this entry relates to a publish request and
     *         <code>false</code> if it relates to an unpublish request
     */
    boolean isPublish();
}
