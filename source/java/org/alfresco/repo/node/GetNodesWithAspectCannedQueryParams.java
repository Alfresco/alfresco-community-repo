/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.node;

import java.util.Set;

import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * GetNodesWithAspectCannedQuery CQ parameters - for query context and filtering
 *
 * @author Nick Burch
 * @since 4.1
 */
public class GetNodesWithAspectCannedQueryParams
{
    private StoreRef storeRef;
    
    private Set<QName> aspectQNames = null;
    
    public GetNodesWithAspectCannedQueryParams(
            StoreRef storeRef,
            Set<QName> aspectQNames)
    {
        this.storeRef = storeRef;

        if (aspectQNames != null && !aspectQNames.isEmpty()) 
        { 
            this.aspectQNames = aspectQNames; 
        }
        else
        {
            throw new IllegalArgumentException("At least one Aspect must be given");
        }
    }
    
    public StoreRef getStoreRef()
    {
        return storeRef;
    }
    
    public Set<QName> getAspectQNames()
    {
        return aspectQNames;
    }
}
