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
package org.alfresco.repo.transfer.manifest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;

import java.io.Serializable;

/**
 * Decorator to extend capabilities of TransferManifestNode
 *
 * @author Mark Rogers
 */
public class TransferManifestNodeHelper
{
    /**
     * Gets the primary parent association 
     * @param node the node to process
     * @return the primary parent association or null if this is a root node
     */
     public static ChildAssociationRef getPrimaryParentAssoc(TransferManifestNormalNode node)
     {
         List<ChildAssociationRef> assocs = node.getParentAssocs();

         for(ChildAssociationRef assoc : assocs)
         {
             if(assoc.isPrimary())
             {
                 return assoc;
             }
         }
         return null;
     }

     /**
      * Gets the content properties for a node
      * @param node the node to process
      * @return
      */
     public static Set<ContentData> getContentData(TransferManifestNormalNode node)
     {
         Set<ContentData> content = new HashSet<ContentData>();
         
         for(Serializable value : node.getProperties().values())
         {
             if(value instanceof ContentData)
             {
                 content.add((ContentData)value);
             }
         }
         
         return content;
     }
}
