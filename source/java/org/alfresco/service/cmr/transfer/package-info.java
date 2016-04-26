/**
 * Provides the public interface for the transfer service which can be used to 
 * transfer nodes from one repository to another.
 * <p>
 * TransferService provides the methods to transfer nodes from one instance of Alfresco to another.   The TransferTarget contains details of where to transfer to.   
 * The TransferDefinition contains details of what to transfer.
 * <p>
 * TransferEvents are produced by an ongoing transfer.   They can be use to monitor an in-flight transfer or build a user interface.
 * <p>
 * The NodeCrawler provides the ability to find a set of nodes to give to the transfer service.
 *
 * @see org.alfresco.service.cmr.transfer.TransferService
 * @since 3.3
 */
@PackageMarker
package org.alfresco.service.cmr.transfer;
import org.alfresco.util.PackageMarker;

