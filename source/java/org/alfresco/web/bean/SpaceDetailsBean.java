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

import java.text.MessageFormat;
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
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.Utils.URLMode;
import org.alfresco.web.ui.common.component.UIActionLink;

/**
 * Back bean provided access to the details of a Space
 * 
 * @author Kevin Roast
 */
public class SpaceDetailsBean
{
   private static final String OUTCOME_RETURN = "showSpaceDetails";

   /** BrowseBean instance */
   private BrowseBean browseBean;
   
   /** The NavigationBean bean reference */
   private NavigationBean navigator;
   
   /** PermissionService bean reference */
   private PermissionService permissionService;
   
   /** NodeServuce bean reference */
   private NodeService nodeService;
   
   /** Selected template Id */
   private String template;
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * Sets the BrowseBean instance to use to retrieve the current Space
    * 
    * @param browseBean BrowseBean instance
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
   }
   
   /**
    * @param navigator The NavigationBean to set.
    */
   public void setNavigator(NavigationBean navigator)
   {
      this.navigator = navigator;
   }
   
   /**
    * @param nodeService   The NodeService to set
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   /**
    * @param permissionService      The PermissionService to set.
    */
   public void setPermissionService(PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }
   
   /**
    * Returns the Space this bean is currently representing
    * 
    * @return The Space Node
    */
   public Node getSpace()
   {
      return this.browseBean.getActionSpace();
   }
   
   /**
    * Returns the id of the current space
    * 
    * @return The id
    */
   public String getId()
   {
      return getSpace().getId();
   }
   
   /**
    * Returns the name of the current space
    * 
    * @return Name of the current space
    */
   public String getName()
   {
      return getSpace().getName();
   }
   
   /**
    * Returns the WebDAV URL for the current space
    * 
    * @return The WebDAV url
    */
   public String getWebdavUrl()
   {
      return Utils.generateURL(FacesContext.getCurrentInstance(), getSpace(), URLMode.WEBDAV);
   }

   /**
    * Returns the URL to access the details page for the current space
    * 
    * @return The bookmark URL
    */
   public String getBookmarkUrl()
   {
      return Utils.generateURL(FacesContext.getCurrentInstance(), getSpace(), URLMode.SHOW_DETAILS);
   }
   
   /**
    * Returns the CIFS path for the current space
    * 
    * @return The CIFS path
    */
   public String getCifsPath()
   {
      return Utils.generateURL(FacesContext.getCurrentInstance(), getSpace(), URLMode.CIFS);
   }
   
   /**
    * Return the Alfresco NodeRef URL for the current space
    * 
    * @return the Alfresco NodeRef URL
    */
   public String getNodeRefUrl()
   {
      return getSpace().getNodeRef().toString();
   }

   /**
    * @return Returns the template Id.
    */
   public String getTemplate()
   {
      // return current template if it exists
      NodeRef ref = (NodeRef)getSpace().getProperties().get(ContentModel.PROP_TEMPLATE);
      return ref != null ? ref.getId() : this.template;
   }

   /**
    * @param template The template Id to set.
    */
   public void setTemplate(String template)
   {
      this.template = template;
   }
   
   /**
    * @return true if the current document has the 'templatable' aspect applied and
    *         references a template that currently exists in the system.
    */
   public boolean isTemplatable()
   {
      NodeRef templateRef = (NodeRef)getSpace().getProperties().get(ContentModel.PROP_TEMPLATE);
      return (getSpace().hasAspect(ContentModel.ASPECT_TEMPLATABLE) &&
              templateRef != null && nodeService.exists(templateRef));
   }
   
   /**
    * @return String of the NodeRef for the dashboard template used by the space if any 
    */
   public String getTemplateRef()
   {
      NodeRef ref = (NodeRef)getSpace().getProperties().get(ContentModel.PROP_TEMPLATE);
      return ref != null ? ref.toString() : null;
   }
   
   /**
    * Returns a model for use by a template on the Space Details page.
    * 
    * @return model containing current current space info.
    */
   @SuppressWarnings("unchecked")
   public Map getTemplateModel()
   {
      HashMap model = new HashMap(1, 1.0f);
      
      FacesContext fc = FacesContext.getCurrentInstance();
      TemplateNode spaceNode = new TemplateNode(getSpace().getNodeRef(), Repository.getServiceRegistry(fc),
            new TemplateImageResolver() {
               public String resolveImagePathForName(String filename, boolean small) {
                  return Utils.getFileTypeImage(filename, small);
               }
            });
      model.put("space", spaceNode);
      
      return model;
   }
   
   /**
    * @return the list of available Content Templates that can be applied to the current document.
    */
   public SelectItem[] getTemplates()
   {
      // get the template from the special Content Templates folder
      FacesContext context = FacesContext.getCurrentInstance();
      String xpath = Application.getRootPath(context) + "/" + 
            Application.getGlossaryFolderName(context) + "/" +
            Application.getContentTemplatesFolderName(context) + "//*";
      NodeRef rootNodeRef = this.nodeService.getRootNode(Repository.getStoreRef());
      NamespaceService resolver = Repository.getServiceRegistry(context).getNamespaceService();
      List<NodeRef> results = Repository.getServiceRegistry(context).getSearchService().selectNodes(
            rootNodeRef, xpath, null, resolver, false);
      
      List<SelectItem> templates = new ArrayList<SelectItem>(results.size());
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
      
      return templates.toArray(new SelectItem[templates.size()]);
   }
   
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Action handler to apply the selected Template and Templatable aspect to the current Space
    */
   public String applyTemplate()
   {
      try
      {
         // apply the templatable aspect if required 
         if (getSpace().hasAspect(ContentModel.ASPECT_TEMPLATABLE) == false)
         {
            this.nodeService.addAspect(getSpace().getNodeRef(), ContentModel.ASPECT_TEMPLATABLE, null);
         }
         
         // get the selected template from the Template Picker
         NodeRef templateRef = new NodeRef(Repository.getStoreRef(), this.template);
         
         // set the template NodeRef into the templatable aspect property
         this.nodeService.setProperty(getSpace().getNodeRef(), ContentModel.PROP_TEMPLATE, templateRef); 
         
         // reset space details for next refresh of details page
         getSpace().reset();
      }
      catch (Exception e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
      }
      return OUTCOME_RETURN;
   }
   
   /**
    * Action handler to remove a dashboard template from the current Space
    */
   public String removeTemplate()
   {
      try
      {
         // clear template property
         this.nodeService.setProperty(getSpace().getNodeRef(), ContentModel.PROP_TEMPLATE, null);
         this.nodeService.removeAspect(getSpace().getNodeRef(), ContentModel.ASPECT_TEMPLATABLE);
         
         // reset space details for next refresh of details page
         getSpace().reset();
      }
      catch (Exception e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
      }
      return OUTCOME_RETURN;
   }
   
   /**
    * Navigates to next item in the list of Spaces
    */
   public void nextItem(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         List<Node> nodes = this.browseBean.getNodes();
         if (nodes.size() > 1)
         {
            // perform a linear search - this is slow but stateless
            // otherwise we would have to manage state of last selected node
            // this gets very tricky as this bean is instantiated once and never
            // reset - it does not know when the document has changed etc.
            for (int i=0; i<nodes.size(); i++)
            {
               if (id.equals(nodes.get(i).getId()) == true)
               {
                  Node next;
                  // found our item - navigate to next
                  if (i != nodes.size() - 1)
                  {
                     next = nodes.get(i + 1);
                  }
                  else
                  {
                     // handle wrapping case
                     next = nodes.get(0);
                  }
                  
                  // prepare for showing details for this node
                  this.browseBean.setupSpaceAction(next.getId(), false);
               }
            }
        }
      }
   }
   
   /**
    * Navigates to the previous item in the list Spaces
    */
   public void previousItem(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         List<Node> nodes = this.browseBean.getNodes();
         if (nodes.size() > 1)
         {
            // see above
            for (int i=0; i<nodes.size(); i++)
            {
               if (id.equals(nodes.get(i).getId()) == true)
               {
                  Node previous;
                  // found our item - navigate to previous
                  if (i != 0)
                  {
                     previous = nodes.get(i - 1);
                  }
                  else
                  {
                     // handle wrapping case
                     previous = nodes.get(nodes.size() - 1);
                  }
                  
                  // show details for this node
                  this.browseBean.setupSpaceAction(previous.getId(), false);
               }
            }
         }
      }
   }
   
   /**
    * Action handler to clear the current Space properties before returning to the browse screen,
    * as the user may have modified the properties! 
    */
   public String closeDialog()
   {
      this.navigator.resetCurrentNodeProperties();
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }
}
