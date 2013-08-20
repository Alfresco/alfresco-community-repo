/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
 * @see org.alfresco.service.cmr.transferTransferService
 * @since 3.3
 */
@PackageMarker
package org.alfresco.service.cmr.transfer;
import org.alfresco.util.PackageMarker;

