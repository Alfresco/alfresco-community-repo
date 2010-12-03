/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.web.bean.ajax;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.app.servlet.ajax.InvokeCommand;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.repository.Repository;
import org.springframework.extensions.webscripts.json.JSONWriter;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean backing the ajax requests for the ajax based picker components.
 * 
 * @author Kevin Roast
 */
public class PickerBean implements Serializable
{
   private static final long serialVersionUID = 8950457520023294902L;
   
   private static final String MSG_CATEGORIES = "categories";
   private static final String MSG_TAGS = "tags";
   private static final String ID_URL = "url";
   private static final String ID_ICON = "icon";
   private static final String ID_CHILDREN = "children";
   private static final String ID_SELECTABLE = "selectable";
   private static final String ID_ISROOT = "isroot";
   private static final String ID_NAME = "name";
   private static final String ID_ID = "id";
   private static final String ID_PARENT = "parent";
   private static final String PARAM_PARENT = "parent";
   private static final String PARAM_CHILD = "child";
   private static final String PARAM_MIMETYPES = "mimetypes";
   
   private static final String FOLDER_IMAGE_PREFIX = "/images/icons/";
   
   private static Log logger = LogFactory.getLog(PickerBean.class);
   
   transient private CategoryService categoryService;
   transient private NodeService nodeService;
   transient private NodeService internalNodeService;
   transient private FileFolderService fileFolderService;
   
   
   /**
    * @param categoryService     The categoryService to set
    */
   public void setCategoryService(CategoryService categoryService)
   {
      this.categoryService = categoryService;
   }
   
   /**
    * @return the categoryService
    */
   private CategoryService getCategoryService()
   {
      //check for null in cluster environment
      if(categoryService == null)
      {
         categoryService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getCategoryService();
      }
      return categoryService;
   }

   /**
    * @param nodeService         The nodeService to set
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   /**
    * @return the nodeService
    */
   private NodeService getNodeService()
   {
      //check for null in cluster environment
      if (nodeService == null)
      {
         nodeService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
      }
      return nodeService;
   }
   
   /**
    * @param internalNodeService The internalNodeService to set.
    */
   public void setInternalNodeService(NodeService internalNodeService)
   {
      this.internalNodeService = internalNodeService;
   }
   
   /**
    * @return the internalNodeService
    */
   private NodeService getInternalNodeService()
   {
      //check for null in cluster environment
      if (internalNodeService == null)
      {
         internalNodeService = (NodeService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "nodeService");
      }
      return internalNodeService;
   }

   /**
    * @param fileFolderService   the FileFolderService to set
    */
   public void setFileFolderService(FileFolderService fileFolderService)
   {
      this.fileFolderService = fileFolderService;
   }
   
   /**
    * @return the fileFolderService
    */
   private FileFolderService getFileFolderService()
   {
      if (fileFolderService == null)
      {
         fileFolderService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getFileFolderService();
      }
      return fileFolderService;
   }

   /**
    * Return the JSON objects representing a list of categories.
    * 
    * IN: "parent" - null for root categories, else the parent noderef of the categories to retrieve.
    * 
    * The pseudo root node 'Categories' is not selectable.
    */
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
         String strParentRef = (String)params.get(PARAM_PARENT);
         if (strParentRef == null || strParentRef.length() == 0)
         {
            childRefs = this.getCategoryService().getRootCategories(
               Repository.getStoreRef(),
               ContentModel.ASPECT_GEN_CLASSIFIABLE);
         }
         else
         {
            parentRef = new NodeRef(strParentRef);
            childRefs = this.getCategoryService().getChildren(
                  parentRef,
                  CategoryService.Mode.SUB_CATEGORIES,
                  CategoryService.Depth.IMMEDIATE);
         }
         
         JSONWriter out = new JSONWriter(fc.getResponseWriter());
         out.startObject();
         out.startValue(ID_PARENT);
         out.startObject();
         if (parentRef == null)
         {
            out.writeNullValue(ID_ID);
            out.writeValue(ID_NAME, Application.getMessage(fc, MSG_CATEGORIES));
            out.writeValue(ID_ISROOT, true);
            out.writeValue(ID_SELECTABLE, false);
         }
         else
         {
            out.writeValue(ID_ID, strParentRef);
            out.writeValue(ID_NAME, Repository.getNameForNode(this.getInternalNodeService(), parentRef));
         }
         out.endObject();
         out.endValue();
         out.startValue(ID_CHILDREN);
         out.startArray();
         for (ChildAssociationRef ref : childRefs)
         {
            NodeRef nodeRef = ref.getChildRef();
            out.startObject();
            out.writeValue(ID_ID, nodeRef.toString());
            out.writeValue(ID_NAME, Repository.getNameForNode(this.getInternalNodeService(), nodeRef));
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
   
   /**
    * Return the JSON objects representing a list of cm:folder nodes.
    * 
    * IN: "parent" - noderef (can be null) of the parent to retrieve the child folder nodes for. Null is valid
    *        and specifies the Company Home root as the parent.
    * IN: "child" - non-null value of the child noderef to retrieve the siblings for - the parent value returned
    *        in the JSON response will be the parent of the specified child.
    * 
    * The 16x16 pixel folder icon path is output as the 'icon' property for each child folder. 
    */
   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_HTML)
   public void getTagNodes() throws Exception
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
         String strParentRef = (String)params.get(ID_PARENT);
         if (strParentRef == null || strParentRef.length() == 0)
         {
            childRefs = this.getCategoryService().getRootCategories(
               Repository.getStoreRef(),
               ContentModel.ASPECT_TAGGABLE);
         }
         else
         {
            parentRef = new NodeRef(strParentRef);
            childRefs = this.getCategoryService().getChildren(
                  parentRef,
                  CategoryService.Mode.SUB_CATEGORIES,
                  CategoryService.Depth.IMMEDIATE);
         }
         
         JSONWriter out = new JSONWriter(fc.getResponseWriter());
         out.startObject();
         out.startValue(ID_PARENT);
         out.startObject();
         if (parentRef == null)
         {
            out.writeNullValue(ID_ID);
            out.writeValue(ID_NAME, Application.getMessage(fc, MSG_TAGS));
            out.writeValue(ID_ISROOT, true);
            out.writeValue(ID_SELECTABLE, false);
         }
         else
         {
            out.writeValue(ID_ID, strParentRef);
            out.writeValue(ID_NAME, Repository.getNameForNode(this.getInternalNodeService(), parentRef));
         }
         out.endObject();
         out.endValue();
         out.startValue(ID_CHILDREN);
         out.startArray();
         for (ChildAssociationRef ref : childRefs)
         {
            NodeRef nodeRef = ref.getChildRef();
            out.startObject();
            out.writeValue(ID_ID, nodeRef.toString());
            out.writeValue(ID_NAME, Repository.getNameForNode(this.getInternalNodeService(), nodeRef));
            out.endObject();
         }
         out.endArray();
         out.endValue();
         out.endObject();
         
         tx.commit();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage("PickerBean exception in getTagNodes()", err);
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
         NodeRef companyHomeRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId(fc));
         
         NodeRef parentRef = null;
         Map params = fc.getExternalContext().getRequestParameterMap();
         String strChildRef = (String)params.get(PARAM_CHILD);
         if (strChildRef != null && strChildRef.length() != 0)
         {
            // TODO: check permission on the parent
            NodeRef childRef = new NodeRef(strChildRef);
            parentRef = this.getNodeService().getPrimaryParent(childRef).getParentRef();
         }
         else
         {
            // TODO: check permission on the parent
            String strParentRef = (String)params.get(PARAM_PARENT);
            if (strParentRef == null || strParentRef.length() == 0)
            {
               parentRef = companyHomeRef;
               strParentRef = parentRef.toString();
            }
            else
            {
               parentRef = new NodeRef(strParentRef);
            }
         }
         
         List<FileInfo> folders = this.getFileFolderService().listFolders(parentRef);
         
         JSONWriter out = new JSONWriter(fc.getResponseWriter());
         out.startObject();
         out.startValue(ID_PARENT);
         out.startObject();
         out.writeValue(ID_ID, parentRef.toString());
         out.writeValue(ID_NAME, Repository.getNameForNode(this.getInternalNodeService(), parentRef));
         if (parentRef.equals(companyHomeRef))
         {
            out.writeValue(ID_ISROOT, true);
         }
         out.endObject();
         out.endValue();
         out.startValue(ID_CHILDREN);
         out.startArray();
         
         // filter out those children that are not spaces
         for (FileInfo folder : folders)
         {
            out.startObject();
            out.writeValue(ID_ID, folder.getNodeRef().toString());
            out.writeValue(ID_NAME, (String)folder.getProperties().get(ContentModel.PROP_NAME));
            String icon = (String)folder.getProperties().get(ApplicationModel.PROP_ICON);
            out.writeValue(ID_ICON, FOLDER_IMAGE_PREFIX + (icon != null ? icon + "-16.gif" : BrowseBean.SPACE_SMALL_DEFAULT + ".gif"));
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
   
   /**
    * Return the JSON objects representing a list of cm:folder and cm:content nodes.
    * 
    * IN: "parent" - noderef (can be null) of the parent to retrieve the child nodes for. Null is valid
    *        and specifies the Company Home root as the parent.
    * IN: "child" - non-null value of the child noderef to retrieve the siblings for - the parent value returned
    *        in the JSON response will be the parent of the specified child.
    * IN: "mimetypes" (optional) - if set, a comma separated list of mimetypes to restrict the file list.
    * 
    * It is assumed that only files should be selectable, all cm:folder nodes will be marked with the
    * 'selectable:false' property. Therefore the parent (which is a folder) is not selectable.
    * 
    * The 16x16 pixel node icon path is output as the 'icon' property for each child, in addition each
    * cm:content node has an property of 'url' for content download. 
    */
   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_HTML)
   public void getFileFolderNodes() throws Exception
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
         tx.begin();
         
         DictionaryService dd = Repository.getServiceRegistry(fc).getDictionaryService();
         ContentService cs = Repository.getServiceRegistry(fc).getContentService();
         
         List<ChildAssociationRef> childRefs;
         NodeRef companyHomeRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId(fc));
         
         NodeRef parentRef = null;
         Map params = fc.getExternalContext().getRequestParameterMap();
         String strChildRef = (String)params.get(PARAM_CHILD);
         if (strChildRef != null && strChildRef.length() != 0)
         {
            // TODO: check permission on the parent
            NodeRef childRef = new NodeRef(strChildRef);
            parentRef = this.getNodeService().getPrimaryParent(childRef).getParentRef();
         }
         else
         {
            // TODO: check permission on the parent
            String strParentRef = (String)params.get(PARAM_PARENT);
            if (strParentRef == null || strParentRef.length() == 0)
            {
               parentRef = companyHomeRef;
               strParentRef = parentRef.toString();
            }
            else
            {
               parentRef = new NodeRef(strParentRef);
            }
         }
         
         // look for mimetype restriction parameter
         Set<String> mimetypes = null;
         String mimetypeParam = (String)params.get(PARAM_MIMETYPES);
         if (mimetypeParam != null && mimetypeParam.length() != 0)
         {
            // convert to a set of mimetypes to test each file against
            mimetypes = new HashSet<String>();
            for (StringTokenizer t = new StringTokenizer(mimetypeParam, ","); t.hasMoreTokens(); /**/)
            {
               mimetypes.add(t.nextToken());
            }
         }
         
         List<FileInfo> items = this.getFileFolderService().list(parentRef);
         
         JSONWriter out = new JSONWriter(fc.getResponseWriter());
         out.startObject();
         out.startValue(ID_PARENT);
         out.startObject();
         out.writeValue(ID_ID, parentRef.toString());
         out.writeValue(ID_NAME, Repository.getNameForNode(this.getInternalNodeService(), parentRef));
         if (parentRef.equals(companyHomeRef))
         {
            out.writeValue(ID_ISROOT, true);
         }
         out.writeValue(ID_SELECTABLE, false);
         out.endObject();
         out.endValue();
         out.startValue(ID_CHILDREN);
         out.startArray();
         
         for (FileInfo item : items)
         {
            if (dd.isSubClass(this.getInternalNodeService().getType(item.getNodeRef()), ContentModel.TYPE_FOLDER))
            {
               // found a folder
               out.startObject();
               out.writeValue(ID_ID, item.getNodeRef().toString());
               String name = (String)item.getProperties().get(ContentModel.PROP_NAME);
               out.writeValue(ID_NAME, name);
               String icon = (String)item.getProperties().get(ApplicationModel.PROP_ICON);
               out.writeValue(ID_ICON, FOLDER_IMAGE_PREFIX + (icon != null ? icon + "-16.gif" : BrowseBean.SPACE_SMALL_DEFAULT + ".gif"));
               out.writeValue(ID_SELECTABLE, false);
               out.endObject();
            }
            else
            {
               // must be a file
               boolean validFile = true;
               if (mimetypes != null)
               {
                  validFile = false;
                  ContentReader reader = cs.getReader(item.getNodeRef(), ContentModel.PROP_CONTENT);
                  if (reader != null)
                  {
                     String mimetype = reader.getMimetype();
                     validFile = (mimetype != null && mimetypes.contains(mimetype));
                  }
               }
               if (validFile)
               {
                  out.startObject();
                  out.writeValue(ID_ID, item.getNodeRef().toString());
                  String name = (String)item.getProperties().get(ContentModel.PROP_NAME);
                  out.writeValue(ID_NAME, name);
                  String icon = FileTypeImageUtils.getFileTypeImage(fc, name, FileTypeImageSize.Small);
                  out.writeValue(ID_ICON, icon);
                  out.writeValue(ID_URL, DownloadContentServlet.generateBrowserURL(item.getNodeRef(), name));
                  out.endObject();
               }
            }
         }
         
         out.endArray();
         out.endValue();
         out.endObject();
         
         tx.commit();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage("PickerBean exception in getFileFolderNodes()", err);
         fc.getResponseWriter().write("ERROR: " + err.getMessage());
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
   }
}