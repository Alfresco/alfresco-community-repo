/**
 * Provides the implementation of the transfer requsite which is used by the transfer service.
 * <p>
 * XMLTransferRequsiteWriter writes the transfer requsite.  XMLTransferRequsiteReader reads the transfer requsite and calls the
 * TransferRequsiteProcessor as the read progresses.    These classes are designed to stream content through, processing each node at a time, 
 * and not hold a large data objects in memory.
 * @since 3.4
 */
@PackageMarker
package org.alfresco.repo.transfer.requisite;
import org.alfresco.util.PackageMarker;
