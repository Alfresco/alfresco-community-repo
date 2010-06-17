/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.node;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Object to keep hold of a node and it's parent associations.
 * 
 * @author David Ward
 * @author Derek Hulley
 * @since 3.4
 */
/* package */ class ParentAssocsInfo implements Serializable
{
    private static final long serialVersionUID = -2167221525380802365L;
    
    private static final Log logger = LogFactory.getLog(ParentAssocsInfo.class);
    
    private static Set<Long> warnedDuplicateParents = new HashSet<Long>(3);

    private final boolean isRoot;
    private final boolean isStoreRoot;
    private final Long primaryAssocId;
    private final Map<Long, ChildAssocEntity> parentAssocsById;

    /**
     * Constructor to provide clean initial version of a Node's parent association
     */
    ParentAssocsInfo(boolean isRoot, boolean isStoreRoot, ChildAssocEntity parent)
    {
        this(isRoot, isStoreRoot, Collections.singletonList(parent));
    }
    /**
     * Constructor to provide clean initial version of a Node's parent associations
     */
    ParentAssocsInfo(boolean isRoot, boolean isStoreRoot, List<? extends ChildAssocEntity> parents)
    {
        this.isRoot = isRoot;
        this.isStoreRoot = isStoreRoot;
        Long primaryAssocId = null;
        // Build map of child associations
        Map<Long, ChildAssocEntity> parentAssocsById = new HashMap<Long, ChildAssocEntity>(5);
        for (ChildAssocEntity parentAssoc : parents)
        {
            Long parentAssocId = parentAssoc.getId();
            // Populate the results
            parentAssocsById.put(parentAssocId, parentAssoc);
            // Primary
            if (parentAssoc.isPrimary())
            {
                if (primaryAssocId == null)
                {
                    primaryAssocId = parentAssocId;
                }
                else
                {
                    // Warn about the duplicates
                    synchronized (warnedDuplicateParents)
                    {
                        Long childNodeId = parentAssoc.getChildNode().getId();
                        boolean added = warnedDuplicateParents.add(childNodeId);
                        if (added)
                        {
                            logger.warn(
                                    "Multiple primary associations: \n" +
                                    "   Node:         " + childNodeId + "\n" +
                                    "   Associations: " + parents);
                        }
                    }
                }
            }
        }
        this.primaryAssocId = primaryAssocId;
        // Protect the map from accidental modification
        this.parentAssocsById = Collections.unmodifiableMap(parentAssocsById);
    }

    /**
     * Private constructor used to copy existing values.
     */
    private ParentAssocsInfo(
            boolean isRoot,
            boolean isStoreRoot,
            Map<Long, ChildAssocEntity> parentAssocsById,
            Long primaryAssocId)
    {
        this.isRoot = isRoot;
        this.isStoreRoot = isStoreRoot;
        this.parentAssocsById = Collections.unmodifiableMap(parentAssocsById);
        this.primaryAssocId = primaryAssocId;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ParentAssocsInfo ")
               .append("[isRoot=").append(isRoot)
               .append(", isStoreRoot=").append(isStoreRoot)
               .append(", parentAssocsById=").append(parentAssocsById)
               .append(", primaryAssocId=").append(primaryAssocId)
               .append("]");
        return builder.toString();
    }

    public boolean isRoot()
    {
        return isRoot;
    }

    public boolean isStoreRoot()
    {
        return isStoreRoot;
    }

    public Map<Long, ChildAssocEntity> getParentAssocs()
    {
        return parentAssocsById;
    }
    
    public ChildAssocEntity getPrimaryParentAssoc()
    {
        return (primaryAssocId != null) ? parentAssocsById.get(primaryAssocId) : null;
    }
    
    public ParentAssocsInfo changeIsRoot(boolean isRoot)
    {
        return new ParentAssocsInfo(isRoot, this.isRoot, parentAssocsById, primaryAssocId);
    }

    public ParentAssocsInfo changeIsStoreRoot(boolean isStoreRoot)
    {
        return new ParentAssocsInfo(this.isRoot, isStoreRoot, parentAssocsById, primaryAssocId);
    }

    public ParentAssocsInfo addAssoc(Long assocId, ChildAssocEntity parentAssoc)
    {
        Map<Long, ChildAssocEntity> parentAssocs = new HashMap<Long, ChildAssocEntity>(parentAssocsById);
        parentAssocs.put(parentAssoc.getId(), parentAssoc);
        return new ParentAssocsInfo(isRoot, isStoreRoot, parentAssocs, primaryAssocId);
    }

    public ParentAssocsInfo removeAssoc(Long assocId)
    {
        Map<Long, ChildAssocEntity> parentAssocs = new HashMap<Long, ChildAssocEntity>(parentAssocsById);
        parentAssocs.remove(assocId);
        return new ParentAssocsInfo(isRoot, isStoreRoot, parentAssocs, primaryAssocId);
    }
}
