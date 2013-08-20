/**
 * Provides the implementation of the transfer manifest which is used by the transfer service.
 * <p>
 * XMLTransferManifestWriter writes the transfer manifest.  XMLTransferManifestReader reads the transfer manifest and calls the
 * TransferManifestProcessor as the read progresses.    These classes are designed to stream content through, processing each node at a time, and not hold a large data objects in memory.
 * @since 3.4
 */
@PackageMarker
package org.alfresco.repo.transfer.manifest;
import org.alfresco.util.PackageMarker;
