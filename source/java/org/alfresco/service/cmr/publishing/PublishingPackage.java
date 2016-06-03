package org.alfresco.service.cmr.publishing;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @since 4.0
 */
public interface PublishingPackage
{
    /**
     * Retrieve the collection of publishing package entries contained by this publishing package.
     * @return The collection of publishing package entries. Never <code>null</code>.
     */
    Collection<PublishingPackageEntry> getEntries();
    
    /**
     * Returns a {@link Map} from the {@link NodeRef} to be published/unpublished to the corresponding {@link PublishingPackageEntry}.
     * @return the {@link Map} of Publishing Package Entries.
     */
    Map<NodeRef,PublishingPackageEntry> getEntryMap();
    
    /**
     * @return a {@link Set} of all the {@link NodeRef}s to be published.
     */
    Set<NodeRef> getNodesToPublish();

    /**
     * @return a {@link Set} of all the {@link NodeRef}s to be unpublished.
     */
    Set<NodeRef> getNodesToUnpublish();
}
