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
 * Provide access to commonly used lists of templates.
 * <p>
 * The lists are cached for a small period of time to help performance in the client,
 * as generally the contents of the template folders are not changed frequently.
 * 
 * @author Kevin Roast
 */
public class TemplateSupportBean
{
   /** "no selection" marker for SelectItem lists */
   public static final String NO_SELECTION = "none";
   
   /** NodeService instance */
   private NodeService nodeService;
   
   /** The SearchService instance */
   private SearchService searchService;
   
   /** cache of content templates that last 10 seconds - enough for a couple of page refreshes */
   private ExpiringValueCache<List<SelectItem>> contentTemplates = new ExpiringValueCache<List<SelectItem>>(1000*10);
   
   /** cache of email templates that last 30 seconds - enough for a few page refreshes */
   private ExpiringValueCache<List<SelectItem>> emailTemplates = new ExpiringValueCache<List<SelectItem>>(1000*30);
   
   
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
         
         templates = selectTemplateNodes(fc, xpath);
         
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
         
         templates = selectTemplateNodes(fc, xpath);
         
         emailTemplates.put(templates);
      }
      
      return templates;
   }

   /**
    * @param context    FacesContext
    * @param xpath      XPath to the template nodes to select
    * 
    * @return List of SelectItem object from the template nodes found at the XPath
    */
   private List<SelectItem> selectTemplateNodes(FacesContext fc, String xpath)
   {
      List<SelectItem> templates = null;
      
      try
      {
         NodeRef rootNodeRef = this.nodeService.getRootNode(Repository.getStoreRef());
         NamespaceService resolver = Repository.getServiceRegistry(fc).getNamespaceService();
         List<NodeRef> results = this.searchService.selectNodes(rootNodeRef, xpath, null, resolver, false);
         
         templates = new ArrayList<SelectItem>(results.size() + 1);
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
                     templates.add(new SelectItem(childNode.getId(), childNode.getName()));
                  }
               }
            }
            
            // make sure the list is sorted by the label
            QuickSort sorter = new QuickSort(templates, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
            sorter.sort();
         }
      }
      catch (AccessDeniedException accessErr)
      {
         // ignore the result if we cannot access the root
      }
      
      // add an entry (at the start) to instruct the user to select a template
      if (templates == null)
      {
         templates = new ArrayList<SelectItem>(1);
      }
      templates.add(0, new SelectItem(NO_SELECTION, Application.getMessage(FacesContext.getCurrentInstance(), "select_a_template")));
      
      return templates;
   }
}
