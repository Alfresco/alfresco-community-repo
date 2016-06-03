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
      * @return Set<ContentData>
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
