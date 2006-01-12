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
package org.alfresco.web.bean.preview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;

/**
 * Backing bean for the Preview Document in Template action page
 * 
 * @author Kevin Roast
 */
public abstract class BasePreviewBean
{
   private static final String NO_SELECTION = "none";

   /** BrowseBean instance */
   protected BrowseBean browseBean;
   
   /** NodeService instance */
   protected NodeService nodeService;
   
   /** The SearchService instance */
   protected SearchService searchService;
   
   /** The NavigationBean bean reference */
   protected NavigationBean navigator;
   
   protected NodeRef template;
   
   
   /**
    * @param nodeService The nodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * @param browseBean The BrowseBean to set.
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
   }
   
   /**
    * @param searchService The searchService to set.
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }
   
   /**
    * @param navigator The NavigationBean to set.
    */
   public void setNavigator(NavigationBean navigator)
   {
      this.navigator = navigator;
   }
   
   /**
    * Returns the node this bean is currently working with
    * 
    * @return The current Node
    */
   public abstract Node getNode();
   
   /**
    * Returns the id of the current node
    * 
    * @return The id
    */
   public String getId()
   {
      return getNode().getId();
   }
   
   /**
    * Returns the name of the current node
    * 
    * @return Name of the current node
    */
   public String getName()
   {
      return getNode().getName();
   }
   
   /**
    * @return the list of available Content Templates that can be applied to the current document.
    */
   public SelectItem[] getTemplates()
   {
      // TODO: could cache this last for say 1 minute before requerying
      // get the template from the special Content Templates folder
      FacesContext context = FacesContext.getCurrentInstance();
      String xpath = Application.getRootPath(context) + "/" + 
            Application.getGlossaryFolderName(context) + "/" +
            Application.getContentTemplatesFolderName(context) + "//*";
      NodeRef rootNodeRef = this.nodeService.getRootNode(Repository.getStoreRef());
      NamespaceService resolver = Repository.getServiceRegistry(context).getNamespaceService();
      List<NodeRef> results = this.searchService.selectNodes(rootNodeRef, xpath, null, resolver, false);
      
      List<SelectItem> templates = new ArrayList<SelectItem>(results.size() + 1);
      if (results.size() != 0)
      {
         DictionaryService dd = Repository.getServiceRegistry(context).getDictionaryService();
         for (NodeRef ref : results)
         {
            Node childNode = new Node(ref);
            if (dd.isSubClass(childNode.getType(), ContentModel.TYPE_CONTENT))
            {
               templates.add(new SelectItem(childNode.getId(), childNode.getName()));
            }
         }
         
         // make sure the list is sorted by the label
         QuickSort sorter = new QuickSort(templates, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
      }
      
      // add an entry (at the start) to instruct the user to select a template
      templates.add(0, new SelectItem(NO_SELECTION, Application.getMessage(FacesContext.getCurrentInstance(), "select_a_template")));
      
      return templates.toArray(new SelectItem[templates.size()]);
   }
   
   /**
    * Returns a model for use by the template on the Preview page.
    * 
    * @return model containing current document/space info.
    */
   public abstract Map getTemplateModel();
   
   /** Template Image resolver helper */
   protected TemplateImageResolver imageResolver = new TemplateImageResolver()
   {
       public String resolveImagePathForName(String filename, boolean small)
       {
           return Utils.getFileTypeImage(filename, small);
       }
   };

   /**
    * @return the current template as a full NodeRef
    */
   public NodeRef getTemplateRef()
   {
      return this.template;
   }
   
   /**
    * @return Returns the template Id.
    */
   public String getTemplate()
   {
      return (this.template != null ? this.template.getId() : null);
   }

   /**
    * @param template The template Id to set.
    */
   public void setTemplate(String template)
   {
      if (template != null && template.equals(NO_SELECTION) == false)
      {
         this.template = new NodeRef(Repository.getStoreRef(), template);
      }
   }
   
   private int findNextPreviewNode(List<Node> nodes, int start)
   {
      // search from start to end of list
      for (int i=start; i<nodes.size(); i++)
      {
         Node next = nodes.get(i);
         if (next.hasAspect(ContentModel.ASPECT_TEMPLATABLE))
         {
            return i;
         }
      }
      // search from zero index to start - 1 (to skip original node)
      for (int i=0; i<start - 1; i++)
      {
         Node next = nodes.get(i);
         if (next.hasAspect(ContentModel.ASPECT_TEMPLATABLE))
         {
            return i;
         }
      }
      return -1;
   }
   
   private int findPrevPreviewNode(List<Node> nodes, int start)
   {
      // search from start to beginning of list
      for (int i=start; i>=0; i--)
      {
         Node next = nodes.get(i);
         if (next.hasAspect(ContentModel.ASPECT_TEMPLATABLE))
         {
            return i;
         }
      }
      // end of list to start + 1 (to skip original node)
      for (int i=nodes.size() - 1; i>start; i--)
      {
         Node next = nodes.get(i);
         if (next.hasAspect(ContentModel.ASPECT_TEMPLATABLE))
         {
            return i;
         }
      }
      return -1;
   }
}
