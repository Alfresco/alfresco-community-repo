/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.transfer;

import java.util.List;

import org.alfresco.repo.transfer.manifest.TransferManifestProcessor;
import org.alfresco.repo.transfer.requisite.TransferRequsiteWriter;
import org.alfresco.service.cmr.transfer.TransferReceiver;

/**
 * @author brian
 *
 * This is a factory class for the processors of the transfer manifest file.
 */
public interface ManifestProcessorFactory
{
    /**
     * The requisite processor
     * @param receiver TransferReceiver
     * @param transferId String
     * @param out TransferRequsiteWriter
     * @return the requisite processor
     */
    TransferManifestProcessor getRequsiteProcessor(TransferReceiver receiver, String transferId, TransferRequsiteWriter out);
    
    /**
     * The commit processors
     * @param receiver TransferReceiver
     * @param transferId String
     * @return the requsite processor
     */
    List<TransferManifestProcessor> getCommitProcessors(TransferReceiver receiver, String transferId);
}
