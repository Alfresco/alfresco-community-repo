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
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.config.JNDIConstants;
import org.alfresco.model.ApplicationModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.WebProject;
import org.alfresco.web.ui.repo.WebResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Selector component for folders within a Web Project.
 * 
 * @author gavinc
 */
public class UIWebProjectFolderSelector extends AbstractItemSelector
{
   private static final Log logger = LogFactory.getLog(UIWebProjectFolderSelector.class);
   
   // ------------------------------------------------------------------------------
   // Component Impl 
   
   public String getFamily()
   {
      return "org.alfresco.faces.WebProjectFolderSelector";
   }

   public String getDefaultLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "select_web_project_folder");
   }
   
   public String getParentNodeId(FacesContext context)
   {
      String id = null;
      
      if (this.navigationId != null && this.navigationId.startsWith("-1;"))
      {
         String rootPath = "-1;" + JNDIConstants.DIR_DEFAULT_WWW + ";" + JNDIConstants.DIR_DEFAULT_APPBASE +
               ";" + AVMUtil.DIR_ROOT;
         
         if (this.navigationId.equals(rootPath) == false)
         {
            // remove the last part of the path
            String parentPath = this.navigationId.substring(0, 
                     this.navigationId.lastIndexOf(';'));
            
            if (logger.isDebugEnabled())
               logger.debug("Parent of " + this.navigationId + " is: " + parentPath);
            
            id = parentPath;
         }
      }
      
      return id;
   }

   public Collection<NodeRef> getChildrenForNode(FacesContext context)
   {
      if (logger.isDebugEnabled())
         logger.debug("Getting children for: " + this.navigationId);
      
      List<NodeRef> folders = new ArrayList<NodeRef>(); 
      
      if (this.navigationId.startsWith("-1;"))
      {
         // if we are within the web project folder structure calculate the 
         // current path and get the children
         if (logger.isDebugEnabled())
            logger.debug("Getting children for path: " + this.navigationId + 
                     " in store: " + this.avmStore);
         
         // remove the -1; from the beginning of the path and change ; for /
         String translatedPath = this.navigationId.substring(3);
         String path = translatedPath.replace(';', '/');
         
         String avmPath = this.avmStore + ":/" + path;
         addChildrenForPath(context, avmPath, folders);
      }
      else
      {
         // get the root children for the sandbox of the current user
         WebProject webProject = new WebProject(new NodeRef(Repository.getStoreRef(), this.navigationId));
         this.avmStore = AVMUtil.buildUserMainStoreName(webProject.getStoreId(), 
                  Application.getCurrentUser(context).getUserName());
         
         if (logger.isDebugEnabled())
            logger.debug("Getting children for store: " + this.avmStore);
            
         String rootPath = AVMUtil.buildStoreWebappPath(this.avmStore, AVMUtil.DIR_ROOT);
         
         if (logger.isDebugEnabled())
            logger.debug("Root path for store: "+ rootPath);
         
         addChildrenForPath(context, rootPath, folders);
      }
      
      return folders;
   }

   public Collection<NodeRef> getRootChildren(FacesContext context)
   {
      // query for all nodes under the "Web Projects" folder in company home.
      FacesContext fc = FacesContext.getCurrentInstance();
      String xpath = Application.getRootPath(fc) + "/" + Application.getWebsitesFolderName(fc) + "/*";
      NodeRef rootNodeRef = getFastNodeService(fc).getRootNode(Repository.getStoreRef());
      NamespaceService resolver = Repository.getServiceRegistry(fc).getNamespaceService();
      SearchService searchService = Repository.getServiceRegistry(fc).getSearchService();
      List<NodeRef> nodes = searchService.selectNodes(rootNodeRef, xpath, null, resolver, false);
      
      // filter the web projects i.e. only show those available to the current user
      AVMService avmService = Repository.getServiceRegistry(context).getAVMService();
      PermissionService permissionService = Repository.getServiceRegistry(context).getPermissionService();
      String currentUserName = Application.getCurrentUser(context).getUserName();
      List<NodeRef> webProjects = new ArrayList<NodeRef>(nodes.size());
      for (NodeRef node : nodes)
      {
         // see if the user has AddChildren permission on the Web Project node
         if (permissionService.hasPermission(node, PermissionService.ADD_CHILDREN) == AccessStatus.ALLOWED)
         {
            // if they have AddChildren there is probably a sandbox but check to make
            // sure as it could have been deleted
            WebProject webProject = new WebProject(node);
            String storeName = AVMUtil.buildUserMainStoreName(webProject.getStoreId(), 
                     currentUserName);
            AVMStoreDescriptor storeDesc = avmService.getStore(storeName);
            if (storeDesc != null)
            {
               // if we found a store (sandbox) for the user add to the list
               webProjects.add(node);
            }
         }
      }
      
      return webProjects;
   }

   public String getItemIcon(FacesContext context, NodeRef ref)
   {
      String icon = (String)getFastNodeService(context).getProperty(ref, ApplicationModel.PROP_ICON);
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
   
   /**
    * Adds any child folders of the given path to the given list of children
    * 
    * @param context Faces context
    * @param path The path to get children of
    * @param children The list of child folders
    */
   protected void addChildrenForPath(FacesContext context, String path, List<NodeRef> children)
   {
      if (logger.isDebugEnabled())
         logger.debug("Retrieving children for path: " + path);
      
      // get directory listing for the given path and convert to NodeRef's
      AVMService avmService = Repository.getServiceRegistry(context).getAVMService();
      Map<String, AVMNodeDescriptor> nodes = avmService.getDirectoryListing(-1, path);
      for (String name : nodes.keySet())
      {
         AVMNodeDescriptor avmRef = nodes.get(name);
         NodeRef node = AVMNodeConverter.ToNodeRef(-1, avmRef.getPath());
            
         // only add folders
         if (avmRef.isDirectory())
         {
            children.add(node);
         }
      }
   }
}
