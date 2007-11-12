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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;
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
   
   public static final String MSG_WARN_CANNOT_VIEW_TARGET_DETAILS = "warn_cannot_view_target_details";

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
      // and place them in a map keyed by the noderef of the child node
      if (this.originalAssocs == null)
      {
         this.originalAssocs = new LinkedHashMap<String, Object>();

         List assocs = (List)node.getAssociations().get(this.associationName);
         if (assocs != null)
         {
            Iterator iter = assocs.iterator();
            while (iter.hasNext())
            {
               AssociationRef assoc = (AssociationRef)iter.next();
               
               // add the association to the map
               this.originalAssocs.put(assoc.getTargetRef().toString(), assoc);
            }
         }
      }
      
      // get the map of added associations for this node and association type
      this.added = (Map)node.getAddedAssociations().get(this.associationName);
      if (added == null)
      {
         // if there aren't any added associations for 'associationName' create a map and add it
         added = new LinkedHashMap<String, Object>();
         node.getAddedAssociations().put(this.associationName, (Map)added);
      }
      
      // get the map of removed associations for this node and association type
      this.removed = (Map)node.getRemovedAssociations().get(this.associationName);
      if (removed == null)
      {
         // if there aren't any added associations for 'associationName' create a map and add it
         removed = new LinkedHashMap<String, Object>();
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
         if (removed.containsKey(assoc.getTargetRef().toString()) == false)
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
            NodeRef targetNode = assoc.getTargetRef();
            if (ContentModel.TYPE_PERSON.equals(nodeService.getType(targetNode)))
            {
               // if the node represents a person, show the username instead of the name
               out.write(Utils.encode(User.getFullName(nodeService, targetNode)));
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
               // use the standard cm:name property
            	
               // Fix AWC-1301
               String displayString = null;
               try
               {
            	   displayString = Repository.getDisplayPath(nodeService.getPath(targetNode)) + "/" + Repository.getNameForNode(nodeService, targetNode);
               }
               catch (AccessDeniedException ade)
               {
            	   displayString = Application.getMessage(context, MSG_WARN_CANNOT_VIEW_TARGET_DETAILS);
               }
            
               out.write(displayString);
            }
            out.write("</td></tr>");
         }
         
         out.write("</table>");
      }
   }
   
   /**
    * Updates the component and node state to reflect an association being removed 
    * 
    * @param node The node we are dealing with
    * @param targetRef The noderef of the child to remove
    */
   protected void removeTarget(Node node, String targetRef)
   {
      if (node != null && targetRef != null)
      {
         QName assocQName = Repository.resolveToQName(this.associationName);
         AssociationRef newAssoc = new AssociationRef(node.getNodeRef(), assocQName, new NodeRef(targetRef));
         
         // update the node so it knows to remove the association, but only if the association
         // was one of the original ones
         if (this.originalAssocs.containsKey(targetRef))
         {
            Map<String, AssociationRef> removed = node.getRemovedAssociations().get(this.associationName);
            removed.put(targetRef, newAssoc);
            
            if (logger.isDebugEnabled())
               logger.debug("Added association to " + targetRef + " to the removed list");
         }
         
         // if this association was previously added in this session it will still be
         // in the added list so remove it if it is
         Map<String, AssociationRef> added = node.getAddedAssociations().get(this.associationName);
         if (added.containsKey(targetRef))
         {
            added.remove(targetRef);
            
            if (logger.isDebugEnabled())
               logger.debug("Removed association to " + targetRef + " from the added list");
         }
      }
   }
   
   /**
    * Updates the component and node state to reflect an association being added 
    * 
    * @param node The node we are dealing with
    * @param toAdd The noderefs of the children to add
    */
   protected void addTarget(Node node, String[] toAdd)
   {
      if (node != null && toAdd != null && toAdd.length > 0)
      {
         for (int x = 0; x < toAdd.length; x++)
         {
            String targetRef = toAdd[x];   
            
            // update the node so it knows to add the association (if it wasn't there originally)
            if (this.originalAssocs.containsKey(targetRef) == false)
            {
               QName assocQName = Repository.resolveToQName(this.associationName);
               AssociationRef newAssoc = new AssociationRef(node.getNodeRef(), assocQName, 
                     new NodeRef(targetRef));
            
               Map<String, AssociationRef> added = node.getAddedAssociations().get(this.associationName);
               added.put(targetRef, newAssoc);
               
               if (logger.isDebugEnabled())
                  logger.debug("Added association to " + targetRef + " to the added list");
            }
            
            // if the association was previously removed and has now been re-added it
            // will still be in the "to be removed" list so remove it if it is
            Map<String, AssociationRef> removed = node.getRemovedAssociations().get(this.associationName);
            if (removed.containsKey(targetRef))
            {
               removed.remove(targetRef);
               
               if (logger.isDebugEnabled())
                  logger.debug("Removed association to " + targetRef + " from the removed list");
            }
         }
      }
   }
}
