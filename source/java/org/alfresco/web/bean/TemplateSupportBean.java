/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.bean;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.ExpiringValueCache;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;

/**
 * Provide access to commonly used lists of templates and script files.
 * <p>
 * The lists are cached for a small period of time to help performance in the client,
 * as generally the contents of the template folders are not changed frequently.
 * 
 * @author Kevin Roast
 */
public class TemplateSupportBean
{
   private static final String MSG_SELECT_TEMPLATE = "select_a_template";
   private static final String MSG_SELECT_SCRIPT = "select_a_script";
   
   /** "no selection" marker for SelectItem lists */
   public static final String NO_SELECTION = "none";
   
   /** NodeService instance */
   private NodeService nodeService;
   
   /** The SearchService instance */
   private SearchService searchService;
   
   /** cache of content templates that lasts 30 seconds - enough for a couple of page refreshes */
   private ExpiringValueCache<List<SelectItem>> contentTemplates = new ExpiringValueCache<List<SelectItem>>(1000*30);
   
   /** cache of email templates that lasts 30 seconds - enough for a few page refreshes */
   private ExpiringValueCache<List<SelectItem>> emailTemplates = new ExpiringValueCache<List<SelectItem>>(1000*30);
   
   /** cache of RSS templates that lasts 30 seconds - enough for a few page refreshes */
   private ExpiringValueCache<List<SelectItem>> rssTemplates = new ExpiringValueCache<List<SelectItem>>(1000*30);
   
   /** cache of JavaScript files that lasts 30 seconds - enough for a few page refreshes */ 
   private ExpiringValueCache<List<SelectItem>> scriptFiles = new ExpiringValueCache<List<SelectItem>>(1000*30);
   
   
   /**
    * @param nodeService The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   /**
    * @param searchService The SearchService to set.
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }
   
   /**
    * @return the list of available Content Templates that can be applied to the current document.
    */
   public List<SelectItem> getContentTemplates()
   {
      List<SelectItem> templates = contentTemplates.get();
      if (templates == null)
      {
         // get the template from the special Content Templates folder
         FacesContext fc = FacesContext.getCurrentInstance();
         String xpath = Application.getRootPath(fc) + "/" + 
               Application.getGlossaryFolderName(fc) + "/" +
               Application.getContentTemplatesFolderName(fc) + "//*";
         
         templates = selectDictionaryNodes(fc, xpath, MSG_SELECT_TEMPLATE);
         
         contentTemplates.put(templates);
      }
      
      return templates;
   }
   
   /**
    * @return the list of available Email Templates.
    */
   public List<SelectItem> getEmailTemplates()
   {
      List<SelectItem> templates = emailTemplates.get();
      if (templates == null)
      {
         // get the template from the special Email Templates folder
         FacesContext fc = FacesContext.getCurrentInstance();
         String xpath = Application.getRootPath(fc) + "/" + 
               Application.getGlossaryFolderName(fc) + "/" +
               Application.getEmailTemplatesFolderName(fc) + "//*";
         
         templates = selectDictionaryNodes(fc, xpath, MSG_SELECT_TEMPLATE);
         
         emailTemplates.put(templates);
      }
      
      return templates;
   }
   
   /**
    * @return the list of available RSS Templates.
    */
   public List<SelectItem> getRSSTemplates()
   {
      List<SelectItem> templates = rssTemplates.get();
      if (templates == null)
      {
         // get the template from the special Email Templates folder
         FacesContext fc = FacesContext.getCurrentInstance();
         String xpath = Application.getRootPath(fc) + "/" + 
               Application.getGlossaryFolderName(fc) + "/" +
               Application.getRSSTemplatesFolderName(fc) + "//*";
         
         templates = selectDictionaryNodes(fc, xpath, MSG_SELECT_TEMPLATE);
         
         rssTemplates.put(templates);
      }
      
      return templates;
   }
   
   /**
    * @return the list of available JavaScript files that can be applied to the current document.
    */
   public List<SelectItem> getScriptFiles()
   {
      List<SelectItem> scripts = this.scriptFiles.get();
      if (scripts == null)
      {
         // get the scripts from the special Scripts folder
         FacesContext fc = FacesContext.getCurrentInstance();
         String xpath = Application.getRootPath(fc) + "/" + 
               Application.getGlossaryFolderName(fc) + "/" +
               Application.getScriptsFolderName(fc) + "//*";
         
         scripts = selectDictionaryNodes(fc, xpath, MSG_SELECT_SCRIPT);
         
         scriptFiles.put(scripts);
      }
      
      return scripts;
   }

   /**
    * @param context    FacesContext
    * @param xpath      XPath to the nodes to select
    * 
    * @return List of SelectItem wrapper objects for the nodes found at the XPath
    */
   private List<SelectItem> selectDictionaryNodes(FacesContext fc, String xpath, String noSelectionLabel)
   {
      List<SelectItem> wrappers = null;
      
      try
      {
         NodeRef rootNodeRef = this.nodeService.getRootNode(Repository.getStoreRef());
         NamespaceService resolver = Repository.getServiceRegistry(fc).getNamespaceService();
         List<NodeRef> results = this.searchService.selectNodes(rootNodeRef, xpath, null, resolver, false);
         
         wrappers = new ArrayList<SelectItem>(results.size() + 1);
         if (results.size() != 0)
         {
            DictionaryService dd = Repository.getServiceRegistry(fc).getDictionaryService();
            for (NodeRef ref : results)
            {
               if (this.nodeService.exists(ref) == true)
               {
                  Node childNode = new Node(ref);
                  if (dd.isSubClass(childNode.getType(), ContentModel.TYPE_CONTENT))
                  {
                     wrappers.add(new SelectItem(childNode.getId(), childNode.getName()));
                  }
               }
            }
            
            // make sure the list is sorted by the label
            QuickSort sorter = new QuickSort(wrappers, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
            sorter.sort();
         }
      }
      catch (AccessDeniedException accessErr)
      {
         // ignore the result if we cannot access the root
      }
      
      // add an entry (at the start) to instruct the user to select an item
      if (wrappers == null)
      {
         wrappers = new ArrayList<SelectItem>(1);
      }
      wrappers.add(0, new SelectItem(NO_SELECTION, Application.getMessage(FacesContext.getCurrentInstance(), noSelectionLabel)));
      
      return wrappers;
   }
}
