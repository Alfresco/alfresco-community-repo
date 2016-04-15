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

import org.alfresco.service.cmr.transfer.TransferVersion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is an implementation of TransferVersionChecker.
 * 
 * It allows transfer to the same edition/major/minor but ignores revision.
 */
public class TransferVersionCheckerImpl implements TransferVersionChecker
{
    private static Log logger = LogFactory.getLog(TransferVersionCheckerImpl.class);
    
    public boolean checkTransferVersions(TransferVersion from, TransferVersion to)
    {
        logger.debug("checkTransferVersions from:" + from + ", to:" + to);
        
        if(from == null || to == null || to.getEdition() == null || to.getVersionMajor() == null || to.getVersionMinor() == null)
        {
            return false;
        }
        
        if(!from.getEdition().equalsIgnoreCase(to.getEdition()))
        {
            return false;
        }
        
        if(!from.getVersionMajor().equalsIgnoreCase(to.getVersionMajor()))
        {
            return false;
        }
        
        if(!from.getVersionMinor().equalsIgnoreCase(to.getVersionMinor()))
        {
            return false;
        }
        
        // ignore revisions
        
        return true;
    }

}
