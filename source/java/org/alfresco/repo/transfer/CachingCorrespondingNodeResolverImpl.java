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

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author brian
 * 
 */
public class CachingCorrespondingNodeResolverImpl implements CorrespondingNodeResolver
{
    private static final Log log = LogFactory.getLog(CachingCorrespondingNodeResolverImpl.class);
    
    private Map<NodeRef, ResolvedParentChildPair> cache = new HashMap<NodeRef, ResolvedParentChildPair>(359);
    private CorrespondingNodeResolver delegateResolver;

    public CachingCorrespondingNodeResolverImpl()
    {

    }

    /**
     * @param delegateResolver CorrespondingNodeResolver
     */
    public CachingCorrespondingNodeResolverImpl(CorrespondingNodeResolver delegateResolver)
    {
        super();
        this.delegateResolver = delegateResolver;
    }

    public ResolvedParentChildPair resolveCorrespondingNode(NodeRef sourceNodeRef, ChildAssociationRef primaryAssoc,
            Path parentPath)
    {
        
        ResolvedParentChildPair result = cache.get(sourceNodeRef);

        if (result != null)
        {
            if (log.isDebugEnabled()) 
            {
                log.debug("Found fully-resolved entry in cache for node " + sourceNodeRef);
            }
            return result;
        }

        result = delegateResolver.resolveCorrespondingNode(sourceNodeRef, primaryAssoc, parentPath);

        //If we have fully resolved the parent and child nodes then stick it in the cache...
        if (result.resolvedChild != null && result.resolvedParent != null)
        {
            cache.put(sourceNodeRef, result);
        }
        return result;
    }

    /**
     * @param delegateResolver the delegateResolver to set
     */
    public void setDelegateResolver(CorrespondingNodeResolver delegateResolver)
    {
        this.delegateResolver = delegateResolver;
    }
}
