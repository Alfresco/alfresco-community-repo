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
package org.alfresco.web.ui.repo.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.repo.WebResources;

/**
 * @author Kevin Roast
 */
public class UISpaceSelector extends AbstractItemSelector
{
   // ------------------------------------------------------------------------------
   // Component Impl 
   
   public String getFamily()
   {
      return "org.alfresco.faces.SpaceSelector";
   }

   public String getDefaultLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "select_space_prompt");
   }
   
   public String getParentNodeId(FacesContext context)
   {
      String id = null;
      
      if (this.navigationId != null && this.navigationId.equals(Application.getCompanyRootId()) == false)
      {
         try
         {
            ChildAssociationRef parentRef = getNodeService(context).getPrimaryParent(
                  new NodeRef(Repository.getStoreRef(), this.navigationId));
            id = parentRef.getParentRef().getId();
         }
         catch (AccessDeniedException accessErr)
         {
            // cannot navigate to parent id will be null
         }
      }
      
      return id;
   }

   public Collection<NodeRef> getChildrenForNode(FacesContext context)
   {
      NodeRef nodeRef = new NodeRef(Repository.getStoreRef(), this.navigationId);
      List<ChildAssociationRef> allKids = getNodeService(context).getChildAssocs(nodeRef,
            ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
      DictionaryService dd = getDictionaryService(context);
      NodeService service = getNodeService(context);
      
      // filter out those children that are not spaces
      List<NodeRef> spaceKids = new ArrayList<NodeRef>(); 
      for (ChildAssociationRef ref : allKids)
      {
         if (dd.isSubClass(service.getType(ref.getChildRef()), ContentModel.TYPE_FOLDER) && 
             dd.isSubClass(service.getType(ref.getChildRef()), ContentModel.TYPE_SYSTEM_FOLDER) == false)
         {
            spaceKids.add(ref.getChildRef());
         }
      }
      
      return spaceKids;
   }

   public Collection<NodeRef> getRootChildren(FacesContext context)
   {
      NodeRef rootRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId());
      
      // get a child association reference back from the parent node to satisfy
      // the generic API we have in the abstract super class
      PermissionService ps = Repository.getServiceRegistry(context).getPermissionService();
      if (ps.hasPermission(rootRef, PermissionService.READ) != AccessStatus.ALLOWED)
      {
         // get the root space from the current user home instead
         String homeId = Application.getCurrentUser(context).getHomeSpaceId();
         rootRef = new NodeRef(Repository.getStoreRef(), homeId);
      }
      List<NodeRef> roots = new ArrayList<NodeRef>(1);
      roots.add(rootRef);
      
      return roots;
   }

   public String getItemIcon(FacesContext context, NodeRef ref)
   {
      String icon = (String)getNodeService(context).getProperty(ref, ApplicationModel.PROP_ICON);
      if (icon != null)
      {
         icon = "/images/icons/" + icon + "-16.gif";
      }
      else
      {
         icon = WebResources.IMAGE_SPACE;
      }
      return icon;
   }
}
