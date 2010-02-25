/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
