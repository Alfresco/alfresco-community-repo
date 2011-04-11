/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.transfer.TransferVersion;

/**
 * 
 * @author mrogers
 *
 */
public interface TransferVersionChecker
{
    /**
     * Checks whether transfer is compatible between the two versions
     * @param from the version from
     * @param to the version to
     * @return boolean true the versions are compatible
     */
    boolean checkTransferVersions(TransferVersion from, TransferVersion to);

}
