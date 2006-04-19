/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.ui.repo.component.property;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component that allows associations to be edited 
 * i.e. new associations to be added, existing ones to be 
 * removed whilst following the rules in the data dictionary 
 * 
 * @author gavinc
 */
public class UIAssociationEditor extends BaseAssociationEditor
{
   private static final Log logger = LogFactory.getLog(UIAssociationEditor.class);

   // ------------------------------------------------------------------------------
   // Component implementation
      
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.AssociationEditor";
   }
   
   /**
    * @see org.alfresco.web.ui.repo.component.property.BaseAssociationEditor#populateAssocationMaps(org.alfresco.web.bean.repository.Node)
    */
   @SuppressWarnings("unchecked")
   protected void populateAssocationMaps(Node node)
   {
      // we need to remember the original set of associations (if there are any)
      // and place them in a map keyed by the id of the child node
      if (this.originalAssocs == null)
      {
         this.originalAssocs = new HashMap<String, Object>();

         List assocs = (List)node.getAssociations().get(this.associationName);
         if (assocs != null)
         {
            Iterator iter = assocs.iterator();
            while (iter.hasNext())
            {
               AssociationRef assoc = (AssociationRef)iter.next();
               
               // add the association to the map
               this.originalAssocs.put(assoc.getTargetRef().getId(), assoc);
            }
         }
      }
      
      // get the map of added associations for this node and association type
      this.added = (Map)node.getAddedAssociations().get(this.associationName);
      if (added == null)
      {
         // if there aren't any added associations for 'associationName' create a map and add it
         added = new HashMap<String, Object>();
         node.getAddedAssociations().put(this.associationName, (Map)added);
      }
      
      // get the map of removed associations for this node and association type
      this.removed = (Map)node.getRemovedAssociations().get(this.associationName);
      if (removed == null)
      {
         // if there aren't any added associations for 'associationName' create a map and add it
         removed = new HashMap<String, Object>();
         node.getRemovedAssociations().put(this.associationName, (Map)removed);
      }
   }

   /**
    * @see org.alfresco.web.ui.repo.component.property.BaseAssociationEditor#renderExistingAssociations(javax.faces.context.FacesContext, javax.faces.context.ResponseWriter, org.alfresco.service.cmr.repository.NodeService, boolean)
    */
   protected void renderExistingAssociations(FacesContext context, ResponseWriter out, 
         NodeService nodeService, boolean allowManyChildren) throws IOException
   {
      boolean itemsRendered = false;
      
      // show the associations from the original list if they are not in the removed list
      Iterator iter = this.originalAssocs.values().iterator();
      while (iter.hasNext())
      {
         AssociationRef assoc = (AssociationRef)iter.next();
         if (removed.containsKey(assoc.getTargetRef().getId()) == false)
         {
            renderExistingAssociation(context, out, nodeService, assoc.getTargetRef(), allowManyChildren);
            itemsRendered = true;
         }
      }
      
      // also show any associations added in this session
      iter = this.added.values().iterator();
      while (iter.hasNext())
      {
         AssociationRef assoc = (AssociationRef)iter.next();
         renderExistingAssociation(context, out, nodeService, assoc.getTargetRef(), allowManyChildren);
         itemsRendered = true;
      }
      
      // show the none selected message if no items were rendered
      if (itemsRendered == false && allowManyChildren == true)
      {
         renderNone(context, out);
      }
   }
   
   /**
    * @see org.alfresco.web.ui.repo.component.property.BaseAssociationEditor#renderReadOnlyAssociations(javax.faces.context.FacesContext, javax.faces.context.ResponseWriter, org.alfresco.service.cmr.repository.NodeService)
    */
   protected void renderReadOnlyAssociations(FacesContext context, ResponseWriter out, NodeService nodeService) throws IOException
   {
      if (this.originalAssocs.size() > 0)
      {
         out.write("<table cellspacing='0' cellpadding='2' border='0'>");
         
         Iterator iter = this.originalAssocs.values().iterator();
         while (iter.hasNext())
         {
            out.write("<tr><td>");
            AssociationRef assoc = (AssociationRef)iter.next();
            out.write(Repository.getDisplayPath(nodeService.getPath(assoc.getTargetRef())));
            out.write("/");
            out.write(Repository.getNameForNode(nodeService, assoc.getTargetRef()));
            out.write("</td></tr>");
         }
         
         out.write("</table>");
      }
   }
   
   /**
    * Updates the component and node state to reflect an association being removed 
    * 
    * @param node The node we are dealing with
    * @param targetId The id of the child to remove
    */
   protected void removeTarget(Node node, String targetId)
   {
      if (node != null && targetId != null)
      {
         QName assocQName = Repository.resolveToQName(this.associationName);
         AssociationRef newAssoc = new AssociationRef(node.getNodeRef(), assocQName, new NodeRef(Repository.getStoreRef(), targetId));
         
         // update the node so it knows to remove the association, but only if the association
         // was one of the original ones
         if (this.originalAssocs.containsKey(targetId))
         {
            Map<String, AssociationRef> removed = node.getRemovedAssociations().get(this.associationName);
            removed.put(targetId, newAssoc);
            
            if (logger.isDebugEnabled())
               logger.debug("Added association to " + targetId + " to the removed list");
         }
         
         // if this association was previously added in this session it will still be
         // in the added list so remove it if it is
         Map<String, AssociationRef> added = node.getAddedAssociations().get(this.associationName);
         if (added.containsKey(targetId))
         {
            added.remove(targetId);
            
            if (logger.isDebugEnabled())
               logger.debug("Removed association to " + targetId + " from the added list");
         }
      }
   }
   
   /**
    * Updates the component and node state to reflect an association being added 
    * 
    * @param node The node we are dealing with
    * @param childId The id of the child to add
    */
   protected void addTarget(Node node, String[] toAdd)
   {
      if (node != null && toAdd != null && toAdd.length > 0)
      {
         for (int x = 0; x < toAdd.length; x++)
         {
            String targetId = toAdd[x];   
            
            // update the node so it knows to add the association (if it wasn't there originally)
            if (this.originalAssocs.containsKey(targetId) == false)
            {
               QName assocQName = Repository.resolveToQName(this.associationName);
               AssociationRef newAssoc = new AssociationRef(node.getNodeRef(), assocQName, 
                     new NodeRef(Repository.getStoreRef(), targetId));
            
               Map<String, AssociationRef> added = node.getAddedAssociations().get(this.associationName);
               added.put(targetId, newAssoc);
               
               if (logger.isDebugEnabled())
                  logger.debug("Added association to " + targetId + " to the added list");
            }
            
            // if the association was previously removed and has now been re-added it
            // will still be in the "to be removed" list so remove it if it is
            Map<String, AssociationRef> removed = node.getRemovedAssociations().get(this.associationName);
            if (removed.containsKey(targetId))
            {
               removed.remove(targetId);
               
               if (logger.isDebugEnabled())
                  logger.debug("Removed association to " + targetId + " from the removed list");
            }
         }
      }
   }
}
