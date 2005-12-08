/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.ui.repo.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.SpaceSelector";
   }

   /**
    * 
    * @see org.alfresco.web.ui.repo.component.AbstractItemSelector#getDefaultLabel()
    */
   public String getDefaultLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "select_space_prompt");
   }
   
   /**
    * Returns the parent id of the current space or null if the parent space is the company home space
    * 
    * @see org.alfresco.web.ui.repo.component.AbstractItemSelector#getParentNodeId(javax.faces.context.FacesContext)
    */
   public String getParentNodeId(FacesContext context)
   {
      String id = null;
      
      if (this.navigationId != null && this.navigationId.equals(Application.getCompanyRootId()) == false)
      {
         ChildAssociationRef parentRef = getNodeService(context).getPrimaryParent(
               new NodeRef(Repository.getStoreRef(), this.navigationId));
         id = parentRef.getParentRef().getId();
      }
      
      return id;
   }

   /**
    * Returns the child spaces of the current space
    * 
    * @see org.alfresco.web.ui.repo.component.AbstractItemSelector#getChildrenForNode(javax.faces.context.FacesContext)
    */
   public Collection<ChildAssociationRef> getChildrenForNode(FacesContext context)
   {
      NodeRef nodeRef = new NodeRef(Repository.getStoreRef(), this.navigationId);
      List<ChildAssociationRef> allKids = getNodeService(context).getChildAssocs(nodeRef);
      DictionaryService dd = getDictionaryService(context);
      NodeService service = getNodeService(context);
      
      // filter out those children that are not spaces
      List<ChildAssociationRef> spaceKids = new ArrayList<ChildAssociationRef>(); 
      for (ChildAssociationRef ref : allKids)
      {
         if (dd.isSubClass(service.getType(ref.getChildRef()), ContentModel.TYPE_FOLDER) && 
             dd.isSubClass(service.getType(ref.getChildRef()), ContentModel.TYPE_SYSTEM_FOLDER) == false)
         {
            spaceKids.add(ref);
         }
      }
      
      return spaceKids;
   }

   /**
    * Returns the current users home space
    * 
    * @see org.alfresco.web.ui.repo.component.AbstractItemSelector#getRootChildren(javax.faces.context.FacesContext)
    */
   public Collection<ChildAssociationRef> getRootChildren(FacesContext context)
   {
      // get the root space from the current user
      //String rootId = Application.getCurrentUser(context).getHomeSpaceId();
      NodeRef rootRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId());

      // get a child association reference back to the real repository root to satisfy
      // the generic API we have in the abstract super class
      ChildAssociationRef childRefFromRealRoot = getNodeService(context).getPrimaryParent(rootRef);
      List<ChildAssociationRef> roots = new ArrayList<ChildAssociationRef>(1);
      roots.add(childRefFromRealRoot);
                  
      return roots;
   }

   /**
    * @see org.alfresco.web.ui.repo.component.AbstractItemSelector#getItemIcon()
    */
   public String getItemIcon()
   {
      return WebResources.IMAGE_SPACE;
   }
}
