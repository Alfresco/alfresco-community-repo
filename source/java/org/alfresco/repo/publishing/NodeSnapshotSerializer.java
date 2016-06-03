
package org.alfresco.repo.publishing;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.publishing.NodeSnapshot;

/**
 * @author Brian
 * @author Nick Smith
 *
 * @since 4.0
 */
public interface NodeSnapshotSerializer
{
    void serialize(Collection<NodeSnapshot> snapshots, OutputStream output) throws Exception;
    
    List<NodeSnapshot> deserialize(InputStream input) throws Exception;
}
