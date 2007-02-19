/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.web.ui.repo.component.property;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component that allows child associations to be edited 
 * i.e. new associations to be added, existing ones to be 
 * removed whilst following the rules in the data dictionary 
 * 
 * @author gavinc
 */
public class UIChildAssociationEditor extends BaseAssociationEditor
{
   private static final Log logger = LogFactory.getLog(UIChildAssociationEditor.class);
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.ChildAssociationEditor";
   }
   
   /**
    * @see org.alfresco.web.ui.repo.component.property.BaseAssociationEditor#populateAssocationMaps(org.alfresco.web.bean.repository.Node)
    */
   @SuppressWarnings("unchecked")
   protected void populateAssocationMaps(Node node)
   {
      // we need to remember the original set of associations (if there are any)
      // and place them in a map keyed by the noderef of the child node
      if (this.originalAssocs == null)
      {
         this.originalAssocs = new HashMap<String, Object>();

         List assocs = (List)node.getChildAssociations().get(this.associationName);
         if (assocs != null)
         {
            Iterator iter = assocs.iterator();
            while (iter.hasNext())
            {
               ChildAssociationRef assoc = (ChildAssociationRef)iter.next();
               
               // add the association to the map
               this.originalAssocs.put(assoc.getChildRef().toString(), assoc);
            }
         }
      }
      
      // get the map of added associations for this node and association type
      this.added = (Map)node.getAddedChildAssociations().get(this.associationName);
      if (added == null)
      {
         // if there aren't any added associations for 'associationName' create a map and add it
         added = new HashMap<String, Object>();
         node.getAddedChildAssociations().put(this.associationName, (Map)added);
      }
      
      // get the map of removed associations for this node and association type
      this.removed = (Map)node.getRemovedChildAssociations().get(this.associationName);
      if (removed == null)
      {
         // if there aren't any added associations for 'associationName' create a map and add it
         removed = new HashMap<String, Object>();
         node.getRemovedChildAssociations().put(this.associationName, (Map)removed);
      }
   }
   
   /**
    * @see org.alfresco.web.ui.repo.component.property.BaseAssociationEditor#renderExistingAssociations(javax.faces.context.FacesContext, javax.faces.context.ResponseWriter, org.alfresco.service.cmr.repository.NodeService, boolean)
    */
   protected void renderExistingAssociations(FacesContext context, ResponseWriter out, 
         NodeService nodeService, boolean allowMany) throws IOException
   {
      boolean itemsRendered = false;
      
      // show the associations from the original list if they are not in the removed list
      Iterator iter = this.originalAssocs.values().iterator();
      while (iter.hasNext())
      {
         ChildAssociationRef assoc = (ChildAssociationRef)iter.next();
         if (removed.containsKey(assoc.getChildRef().toString()) == false)
         {
            renderExistingAssociation(context, out, nodeService, assoc.getChildRef(), allowMany);
            itemsRendered = true;
         }
      }
      
      // also show any associations added in this session
      iter = this.added.values().iterator();
      while (iter.hasNext())
      {
         ChildAssociationRef assoc = (ChildAssociationRef)iter.next();
         renderExistingAssociation(context, out, nodeService, assoc.getChildRef(), allowMany);
         itemsRendered = true;
      }
      
      // show the none selected message if no items were rendered
      if (itemsRendered == false && allowMany == true)
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
            ChildAssociationRef assoc = (ChildAssociationRef)iter.next();
            NodeRef targetNode = assoc.getChildRef();
            // if the node represents a person, show the username instead of the name
            if (ContentModel.TYPE_PERSON.equals(nodeService.getType(targetNode)))
            {
               out.write(User.getFullName(nodeService, targetNode));
            }
            else if (ContentModel.TYPE_AUTHORITY_CONTAINER.equals(nodeService.getType(targetNode)))
            {
               // if the node represents a group, show the group name instead of the name
               int offset = PermissionService.GROUP_PREFIX.length();
               String group = (String)nodeService.getProperty(targetNode, 
                     ContentModel.PROP_AUTHORITY_NAME);
               out.write(group.substring(offset));
            }
            else
            {
               out.write(Repository.getDisplayPath(nodeService.getPath(targetNode)));
               out.write("/");
               out.write(Repository.getNameForNode(nodeService, targetNode));
            }
            
            out.write("</tr></td>");
         }
         
         out.write("</table>");
      }
   }

   /**
    * @see org.alfresco.web.ui.repo.component.property.BaseAssociationEditor#removeTarget(org.alfresco.web.bean.repository.Node, java.lang.String)
    */
   protected void removeTarget(Node node, String childRef)
   {
      if (node != null && childRef != null)
      {
         QName assocQName = Repository.resolveToQName(this.associationName);
         ChildAssociationRef childAssoc = new ChildAssociationRef(assocQName, 
            node.getNodeRef(), assocQName, new NodeRef(childRef));
         
         // update the node so it knows to remove the association, but only if the association
         // was one of the original ones
         if (this.originalAssocs.containsKey(childRef))
         {
            Map<String, ChildAssociationRef> removed = node.getRemovedChildAssociations().get(this.associationName);
            removed.put(childRef, childAssoc);
            
            if (logger.isDebugEnabled())
               logger.debug("Added association to " + childRef + " to the removed list");
         }
         
         // if this association was previously added in this session it will still be
         // in the added list so remove it if it is
         Map<String, ChildAssociationRef> added = node.getAddedChildAssociations().get(this.associationName);
         if (added.containsKey(childRef))
         {
            added.remove(childRef);
            
            if (logger.isDebugEnabled())
               logger.debug("Removed association to " + childRef + " from the added list");
         }
      }
   }
   
   /**
    * @see org.alfresco.web.ui.repo.component.property.BaseAssociationEditor#addTarget(org.alfresco.web.bean.repository.Node, java.lang.String[])
    */
   protected void addTarget(Node node, String[] toAdd)
   {
      if (node != null && toAdd != null && toAdd.length > 0)
      {
         for (int x = 0; x < toAdd.length; x++)
         {
            String childRef = toAdd[x];
            
            // update the node so it knows to add the association
            if (this.originalAssocs.containsKey(childRef) == false)
            {
               QName assocQName = Repository.resolveToQName(this.associationName);
               ChildAssociationRef childAssoc = new ChildAssociationRef(assocQName, 
                     node.getNodeRef(), assocQName, new NodeRef(childRef));
            
               Map<String, ChildAssociationRef> added = node.getAddedChildAssociations().get(this.associationName);
               added.put(childRef, childAssoc);
            
               if (logger.isDebugEnabled())
                  logger.debug("Added association to " + childRef + " to the added list");
            }
            
            // if the association was previously removed and has now been re-added it
            // will still be in the "to be removed" list so remove it if it is
            Map<String, ChildAssociationRef> removed = node.getRemovedChildAssociations().get(this.associationName);
            if (removed.containsKey(childRef))
            {
               removed.remove(childRef);
               
               if (logger.isDebugEnabled())
                  logger.debug("Removed association to " + childRef + " from the removed list");
            }
         }
      }
   }
}
