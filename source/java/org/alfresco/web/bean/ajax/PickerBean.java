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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.bean.ajax;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.ajax.InvokeCommand;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean backing the ajax requests for the ajax based picker components.
 * 
 * @author Kevin Roast
 */
public class PickerBean
{
   private static Log logger = LogFactory.getLog(PickerBean.class);
   
   private CategoryService categoryService;
   private NodeService nodeService;
   private NodeService internalNodeService;
   private FileFolderService fileFolderService;
   
   
   /**
    * @param categoryService     The categoryService to set
    */
   public void setCategoryService(CategoryService categoryService)
   {
      this.categoryService = categoryService;
   }
   
   /**
    * @param nodeService         The nodeService to set
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   /**
    * @param internalNodeService The internalNodeService to set.
    */
   public void setInternalNodeService(NodeService internalNodeService)
   {
      this.internalNodeService = internalNodeService;
   }
   
   /**
    * @param fileFolderService   the FileFolderService to set
    */
   public void setFileFolderService(FileFolderService fileFolderService)
   {
      this.fileFolderService = fileFolderService;
   }
   
   
   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_HTML)
   public void getCategoryNodes() throws Exception
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
         tx.begin();
         
         Collection<ChildAssociationRef> childRefs;
         NodeRef parentRef = null;
         Map params = fc.getExternalContext().getRequestParameterMap();
         String strParentRef = (String)params.get("parent");
         if (strParentRef == null || strParentRef.length() == 0)
         {
            childRefs = this.categoryService.getRootCategories(
               Repository.getStoreRef(),
               ContentModel.ASPECT_GEN_CLASSIFIABLE);
         }
         else
         {
            parentRef = new NodeRef(strParentRef);
            childRefs = this.categoryService.getChildren(
                  parentRef,
                  CategoryService.Mode.SUB_CATEGORIES,
                  CategoryService.Depth.IMMEDIATE);
         }
         
         JSONWriter out = new JSONWriter(fc.getResponseWriter());
         out.startObject();
         out.startValue("parent");
         out.startObject();
         if (parentRef == null)
         {
            out.writeNullValue("id");
            out.writeValue("name", "Categories");
         }
         else
         {
            out.writeValue("id", strParentRef);
            out.writeValue("name", Repository.getNameForNode(this.internalNodeService, parentRef));
         }
         out.endObject();
         out.endValue();
         out.startValue("children");
         out.startArray();
         for (ChildAssociationRef ref : childRefs)
         {
            NodeRef nodeRef = ref.getChildRef();
            out.startObject();
            out.writeValue("id", nodeRef.toString());
            out.writeValue("name", Repository.getNameForNode(this.internalNodeService, nodeRef));
            out.endObject();
         }
         out.endArray();
         out.endValue();
         out.endObject();
         
         tx.commit();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage("PickerBean exception in getCategoryRootNodes()", err);
         fc.getResponseWriter().write("ERROR: " + err.getMessage());
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
   }
   
   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_HTML)
   public void getFolderNodes() throws Exception
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
         tx.begin();
         
         List<ChildAssociationRef> childRefs;
         NodeRef parentRef = null;
         Map params = fc.getExternalContext().getRequestParameterMap();
         String strParentRef = (String)params.get("parent");
         if (strParentRef == null || strParentRef.length() == 0)
         {
            parentRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId(fc));
         }
         else
         {
            parentRef = new NodeRef(strParentRef);
         }
         List<FileInfo> folders = this.fileFolderService.listFolders(parentRef);
         
         JSONWriter out = new JSONWriter(fc.getResponseWriter());
         out.startObject();
         out.startValue("parent");
         out.startObject();
         if (strParentRef == null || strParentRef.length() == 0)
         {
            out.writeNullValue("id");
            out.writeValue("name", Repository.getNameForNode(this.internalNodeService, parentRef));
         }
         else
         {
            out.writeValue("id", strParentRef);
            out.writeValue("name", Repository.getNameForNode(this.internalNodeService, parentRef));
         }
         out.endObject();
         out.endValue();
         out.startValue("children");
         out.startArray();
         
         // filter out those children that are not spaces
         for (FileInfo folder : folders)
         {
            out.startObject();
            out.writeValue("id", folder.getNodeRef().toString());
            out.writeValue("name", (String)folder.getProperties().get(ContentModel.PROP_NAME));
            String icon = (String)folder.getProperties().get(ApplicationModel.PROP_ICON);
            out.writeValue("icon", (icon != null ? icon + "-16.gif" : BrowseBean.SPACE_SMALL_DEFAULT + ".gif"));
            out.endObject();
         }
         
         out.endArray();
         out.endValue();
         out.endObject();
         
         tx.commit();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage("PickerBean exception in getFolderNodes()", err);
         fc.getResponseWriter().write("ERROR: " + err.getMessage());
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
   }
}