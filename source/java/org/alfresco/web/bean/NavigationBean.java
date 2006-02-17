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

import org.alfresco.config.ConfigService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.CIFSServer;
import org.alfresco.filesys.server.filesys.DiskSharedDevice;
import org.alfresco.filesys.smb.server.repo.ContentContext;
import org.alfresco.filesys.smb.server.repo.ContentDiskInterface;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.bean.wizard.NewSpaceWizard;
import org.alfresco.web.config.ClientConfigElement;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.UIBreadcrumb;
import org.alfresco.web.ui.common.component.UIModeList;
import org.alfresco.web.ui.repo.component.IRepoBreadcrumbHandler;
import org.alfresco.web.ui.repo.component.shelf.UIShelf;
import org.apache.log4j.Logger;

/**
 * @author Kevin Roast
 */
public class NavigationBean
{
   /**
    * Default constructor
    */
   public NavigationBean()
   {
      initFromClientConfig();
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters
   
   /**
    * @param nodeService The nodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * @param searchService The searchService to set.
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }
   
   /**
    * @param namespaceService The namespaceService to set.
    */
   public void setNamespaceService(NamespaceService namespaceService)
   {
      this.namespaceService = namespaceService;
   }
   
   /**
    * @param cifsServer The cifsServer to set.
    */
   public void setCifsServer(CIFSServer cifsServer)
   {
      this.cifsServer = cifsServer;
   }

   /**
    * @param contentDiskDriver The contentDiskDriver to set.
    */
   public void setContentDiskDriver(ContentDiskInterface contentDiskDriver)
   {
      this.contentDiskDriver = contentDiskDriver;
   }
   
   /**
    * @return the User object representing the current instance for this user 
    */
   public User getCurrentUser()
   {
      return Application.getCurrentUser(FacesContext.getCurrentInstance());
   }

   /**
    * Return the expanded state of the Shelf panel wrapper component
    * 
    * @return the expanded state of the Shelf panel wrapper component
    */
   public boolean isShelfExpanded()
   {
      return this.shelfExpanded;
   }
   
   /**
    * Set the expanded state of the Shelf panel wrapper component
    * 
    * @param expanded      true to expanded the Shelf panel area, false to hide it
    */
   public void setShelfExpanded(boolean expanded)
   {
      this.shelfExpanded = expanded;
   }
   
   /**
    * @return Returns the array containing the expanded state of the shelf items
    */
   public boolean[] getShelfItemExpanded()
   {
      return this.shelfItemExpanded;
   }

   /**
    * @param shelfItemExpanded The array containing the expanded state of the shelf items
    */
   public void setShelfItemExpanded(boolean[] shelfItemExpanded)
   {
      this.shelfItemExpanded = shelfItemExpanded;
   }
   
   /**
    * @return Returns the toolbar Location.
    */
   public String getToolbarLocation()
   {
      return this.toolbarLocation;
   }
   
   /**
    * @param toolbarLocation  The toolbar Location to set.
    */
   public void setToolbarLocation(String toolbarLocation)
   {
      this.toolbarLocation = toolbarLocation;
   }
   
   /**
    * @return Returns the helpUrl.
    */
   public String getHelpUrl()
   {
      return this.helpUrl;
   }

   /**
    * @param helpUrl The helpUrl to set.
    */
   public void setHelpUrl(String helpUrl)
   {
      this.helpUrl = helpUrl;
   }
   
   /**
    * @return Returns the search context object if any.
    */
   public SearchContext getSearchContext()
   {
      return this.searchContext;
   }
   
   /**
    * @param searchContext    The search context object to set or null to clear search.
    */
   public void setSearchContext(SearchContext searchContext)
   {
      this.searchContext = searchContext;
      
      UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
   }
   
   /**
    * @return Returns the currently browsing node Id.
    */
   public String getCurrentNodeId()
   {
      return this.currentNodeId;
   }

   /**
    * Set the node Id of the current folder/space container node.
    * <p>
    * Setting this value causes the UI to update and display the specified node as current.
    * 
    * @param currentNodeId    The currently browsing node Id.
    */
   public void setCurrentNodeId(String currentNodeId)
   {
      if (s_logger.isDebugEnabled())
         s_logger.debug("Setting current node id to: " + currentNodeId);
      
      if (currentNodeId == null)
      {
         throw new AlfrescoRuntimeException("Can not set the current node id to null");
      }
      
      // set the current Node Id for our UI context operations
      this.currentNodeId = currentNodeId;
      
      // clear other context that is based on or relevant to the Node id
      this.currentNode = null;
      this.searchContext = null;
      
      // inform any interested beans that the UI needs updating after this change 
      UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
      
      // clear current node context after the notify - this is to ensure that if any delegates
      // performed operations on the current node, that we have fresh data for the next View
      this.currentNode = null;
   }
   
   /**
    * @return true if the current node has a template view available
    */
   public boolean getCurrentNodeHasTemplate()
   {
      boolean templateView = false;
      Node node = getCurrentNode();
      if (node.hasAspect(ContentModel.ASPECT_TEMPLATABLE))
      {
         NodeRef templateRef = (NodeRef)node.getProperties().get(ContentModel.PROP_TEMPLATE);
         try
         {
            templateView = (templateRef != null && this.nodeService.exists(templateRef));
         }
         catch (AccessDeniedException err)
         {
            // default to false if no access to template
         }
      }
      return templateView;
   }
   
   /**
    * @return the NodeRef.toString() for the current node template view if it has one set 
    */
   public String getCurrentNodeTemplate()
   {
      String strRef = null;
      if (getCurrentNodeHasTemplate() == true)
      {
         strRef = getCurrentNode().getProperties().get(ContentModel.PROP_TEMPLATE).toString();
      }
      return strRef;
   }
   
   /**
    * Returns a model for use by a template on a space Dashboard page.
    * 
    * @return model containing current current space info.
    */
   public Map getTemplateModel()
   {
      HashMap model = new HashMap(1, 1.0f);
      
      FacesContext fc = FacesContext.getCurrentInstance();
      TemplateNode spaceNode = new TemplateNode(getCurrentNode().getNodeRef(), Repository.getServiceRegistry(fc),
            new TemplateImageResolver() {
               public String resolveImagePathForName(String filename, boolean small) {
                  return Utils.getFileTypeImage(filename, small);
               }
            });
      model.put("space", spaceNode);
      
      return model;
   }
   
   /**
    * Clear state so that the current node properties cache for the next time they are requested
    */
   public void resetCurrentNodeProperties()
   {
      this.currentNode = null;
   }
   
   /**
    * @return The Map of properties for the current Node. 
    */
   public Map<String, Object> getNodeProperties()
   {
      return getCurrentNode().getProperties();
   }
   
   /**
    * @return The current Node object for UI context operations
    */
   public Node getCurrentNode()
   {
      if (this.currentNode == null)
      {
         if (this.currentNodeId == null)
         {
            throw new AlfrescoRuntimeException("Cannot retrieve current Node if NodeId is null!");
         }
         
         if (s_logger.isDebugEnabled())
            s_logger.debug("Caching properties for node id: " + this.currentNodeId);
         
         NodeRef nodeRef;
         Node node;
         Map<String, Object> props;
         try
         {
            // build a node which components on the JSP page can bind too
            nodeRef = new NodeRef(Repository.getStoreRef(), this.currentNodeId);
            node = new Node(nodeRef);
            
            // early init properties for this node (by getProperties() call)
            // resolve icon in-case one has not been set
            props = node.getProperties();
         }
         catch (InvalidNodeRefException refErr)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  FacesContext.getCurrentInstance(), ERROR_DELETED_FOLDER), new Object[] {this.currentNodeId}) );
            
            nodeRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId());
            node = new Node(nodeRef);
            props = node.getProperties();
         }
         String icon = (String)props.get("app:icon");
         props.put("icon", icon != null ? icon : NewSpaceWizard.SPACE_ICON_DEFAULT);
         Path path = this.nodeService.getPath(nodeRef);
         
         // resolve CIFS network folder location for this node
         
         DiskSharedDevice diskShare = cifsServer.getConfiguration().getPrimaryFilesystem();
         
         if (diskShare != null)
         {
             ContentContext contentCtx = (ContentContext) diskShare.getContext();
             NodeRef rootNode = contentCtx.getRootNode();
             String cifsPath = Repository.getNamePath(this.nodeService, path, rootNode, "\\", "file:///" + getCIFSServerPath(diskShare));
              
             node.getProperties().put("cifsPath", cifsPath);
             node.getProperties().put("cifsPathLabel", cifsPath.substring(8));  // strip file:/// part
         }
         
         this.currentNode = node;
      }
      
      return this.currentNode;
   }

   /**
    * @return Returns the breadcrumb handler elements representing the location path of the UI.
    */
   public List<IBreadcrumbHandler> getLocation()
   {
      if (this.location == null)
      {
         // set the current node to the users Home Space Id if one has not already been set
         NodeRef homeSpaceRef;
         List<IBreadcrumbHandler> elements = new ArrayList<IBreadcrumbHandler>(1);
         if (getCurrentNodeId() == null)
         {
            User user = Application.getCurrentUser(FacesContext.getCurrentInstance());
            homeSpaceRef = new NodeRef(Repository.getStoreRef(), user.getHomeSpaceId());
         }
         else
         {
            homeSpaceRef = new NodeRef(Repository.getStoreRef(), getCurrentNodeId());
         }
         
         // set initial node ID
         setCurrentNodeId(homeSpaceRef.getId());
         
         // setup the breadcrumb with the same initial location
         String homeSpaceName = Repository.getNameForNode(this.nodeService, homeSpaceRef);
         elements.add(new NavigationBreadcrumbHandler(homeSpaceRef, homeSpaceName));
         setLocation(elements);
      }
      
      return this.location;
   }
   
   /**
    * @param location      The UI location representation to set.
    */
   public void setLocation(List<IBreadcrumbHandler> location)
   {
      this.location = location;
   }
   
   /**
    * @return true if we are currently the special Guest user
    */
   public boolean getIsGuest()
   {
      return Application.getCurrentUser(FacesContext.getCurrentInstance()).getUserName().equals(PermissionService.GUEST);
   }
   
   /**
    * Sets up the dispatch context so that the navigation handler knows
    * what object is being acted upon
    * 
    * @param node The node to be added to the dispatch context
    */
   public void setupDispatchContext(Node node)
   {
      this.dispatchContext = node;
   }
   
   /**
    * Resets the dispatch context
    */
   public void resetDispatchContext()
   {
      this.dispatchContext = null;
   }
   
   /**
    * Returns the node currently set in the dispatch context
    * 
    * @return The node being dispatched or null if there is no 
    *         dispatch context
    */
   public Node getDispatchContextNode()
   {
      return this.dispatchContext;
   }
   
   
   // ------------------------------------------------------------------------------
   // Navigation action event handlers
   
   /**
    * Action handler to toggle the expanded state of the shelf.
    * The panel component wrapping the shelf area of the UI is value bound to the shelfExpanded property.
    */
   public void toggleShelf(ActionEvent event)
   {
      this.shelfExpanded = !this.shelfExpanded;
   }
   
   /**
    * Action handler called after a Shelf Group has had its expanded state toggled by the user
    */
   public void shelfGroupToggled(ActionEvent event)
   {
      UIShelf.ShelfEvent shelfEvent = (UIShelf.ShelfEvent)event;
      this.shelfItemExpanded[shelfEvent.Index] = shelfEvent.Expanded;
   }
   
   /**
    * Action to change the toolbar location
    * Currently this will changed the location from Company to the users Home space
    */
   public void toolbarLocationChanged(ActionEvent event)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      try
      {
         UIModeList locationList = (UIModeList)event.getComponent();
         String location = locationList.getValue().toString();
         setToolbarLocation(location);
         
         if (LOCATION_COMPANY.equals(location))
         {
            List<IBreadcrumbHandler> elements = new ArrayList<IBreadcrumbHandler>(1);
            NodeRef companyRootRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId());
            String companySpaceName = Repository.getNameForNode(this.nodeService, companyRootRef);
            elements.add(new NavigationBreadcrumbHandler(companyRootRef, companySpaceName));
            setLocation(elements);
            setCurrentNodeId(companyRootRef.getId());
         }
         else if (LOCATION_HOME.equals(location))
         {
            List<IBreadcrumbHandler> elements = new ArrayList<IBreadcrumbHandler>(1);
            String homeSpaceId = Application.getCurrentUser(context).getHomeSpaceId();
            NodeRef homeSpaceRef = new NodeRef(Repository.getStoreRef(), homeSpaceId);
            String homeSpaceName = Repository.getNameForNode(this.nodeService, homeSpaceRef);
            elements.add(new NavigationBreadcrumbHandler(homeSpaceRef, homeSpaceName));
            setLocation(elements);
            setCurrentNodeId(homeSpaceRef.getId());
         }
         
         // we need to force a navigation to refresh the browse screen breadcrumb
         context.getApplication().getNavigationHandler().handleNavigation(context, null, "browse");
      }
      catch (InvalidNodeRefException refErr)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NOHOME), Application.getCurrentUser(context).getHomeSpaceId()), refErr );
      }
      catch (Exception err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
      }
   }
   
   /**
    * @param diskShare Filesystem shared device
    * @return CIFS server path as network style string label
    */
   public String getCIFSServerPath(DiskSharedDevice diskShare)
   {
      if (this.cifsServerPath == null)
      {
         StringBuilder buf = new StringBuilder(24);
         
         String serverName = this.cifsServer.getConfiguration().getServerName();
         if (serverName != null && serverName.length() != 0)
         {
            buf.append("\\\\")
               .append(serverName)
               .append("\\");
            buf.append(diskShare.getName());
         }
         
         this.cifsServerPath = buf.toString();
      }
      
      return this.cifsServerPath;
   }
   
   
   // ------------------------------------------------------------------------------
   // Private helpers
   
   /**
    * Initialise default values from client configuration
    */
   private void initFromClientConfig()
   {
      this.clientConfig = Application.getClientConfig(FacesContext.getCurrentInstance());
      this.helpUrl = clientConfig.getHelpUrl();
      this.shelfExpanded = clientConfig.isShelfVisible();
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class to handle breadcrumb interaction for top-level navigation pages
    */
   public class NavigationBreadcrumbHandler implements IRepoBreadcrumbHandler
   {
      private static final long serialVersionUID = 4833194653193016638L;
      
      /**
       * Constructor
       * 
       * @param label      Element label
       */
      public NavigationBreadcrumbHandler(NodeRef ref, String label)
      {
         this.label = label;
         this.ref = ref;
      }
      
      /**
       * @see java.lang.Object#toString()
       */
      public String toString()
      {
         return this.label;
      }

      /**
       * @see org.alfresco.web.ui.common.component.IBreadcrumbHandler#navigationOutcome(org.alfresco.web.ui.common.component.UIBreadcrumb)
       */
      public String navigationOutcome(UIBreadcrumb breadcrumb)
      {
         // set the current node to the specified top level node ID
         FacesContext fc = FacesContext.getCurrentInstance();
         setCurrentNodeId(ref.getId());
         setLocation( (List)breadcrumb.getValue() );
         
         // setup the dispatch context
         setupDispatchContext(new Node(ref));
         
         if (fc.getViewRoot().getViewId().equals(BrowseBean.BROWSE_VIEW_ID))
         {
            return null;
         }
         else
         {
            return "browse";
         }
      }
      
      public NodeRef getNodeRef()
      {
         return this.ref;
      }
      
      private String label;
      private NodeRef ref;
   }

   
   // ------------------------------------------------------------------------------
   // Private data
   
   private static Logger s_logger = Logger.getLogger(NavigationBean.class);
   
   /** constant values used by the toolbar location modelist control */
   private static final String LOCATION_COMPANY = "company";
   private static final String LOCATION_HOME = "home";
   
   private static final String ERROR_DELETED_FOLDER = "error_deleted_folder";
   
   /** The NodeService to be used by the bean */
   protected NodeService nodeService;
   
   /** The SearchService to be used by the bean */
   protected SearchService searchService;
   
   /** NamespaceService bean reference */
   protected NamespaceService namespaceService;
   
   /** CIFSServer bean reference */
   protected CIFSServer cifsServer;
   
   /** CIFS content disk driver bean reference */
   protected ContentDiskInterface contentDiskDriver;
   
   /** Client configuration object */
   protected ClientConfigElement clientConfig = null;
   
   /** Cached path to our CIFS server and top level node DIR */
   private String cifsServerPath;
   
   /** Node Id we are using for UI context operations */
   private String currentNodeId;
   
   /** Node we are using for UI context operations */
   private Node currentNode = null;
   
   /** Node we are using for dispatching */
   private Node dispatchContext = null;
   
   /** Current toolbar location */
   private String toolbarLocation = LOCATION_HOME;
   
   /** Search context object we are currently using or null for no search */
   private SearchContext searchContext;
   
   /** expanded state of the Shelf panel wrapper component */
   private boolean shelfExpanded = true;
   
   /** expanded state of the Shelf item components */
   private boolean[] shelfItemExpanded = new boolean[] {true, true, true, false, false};
   
   /** list of the breadcrumb handler elements representing the location path of the UI */
   private List<IBreadcrumbHandler> location = null;
   
   /** The client Help file url */
   private String helpUrl;
}
