
package org.alfresco.service.cmr.publishing;

/**
 * An interface that enables the use of the Visitor pattern on {@link NodePublishStatus} objects.
 * 
 * @author Brian
 * @since 4.0
 */
public interface NodePublishStatusVisitor<T>
{
    T accept(NodePublishStatusNotPublished status);
    T accept(NodePublishStatusOnQueue status);
    T accept(NodePublishStatusPublished status);
    T accept(NodePublishStatusPublishedAndOnQueue status);
}
