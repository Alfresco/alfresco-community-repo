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
package org.alfresco.web.bean;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.repo.ContentContext;
import org.alfresco.filesys.repo.ContentDiskInterface;
import org.alfresco.jlan.server.config.ServerConfigurationAccessor;
import org.alfresco.jlan.server.core.SharedDevice;
import org.alfresco.jlan.server.core.SharedDeviceList;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FilesystemsConfigSection;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.bean.search.SearchContext;
import org.alfresco.web.bean.spaces.CreateSpaceWizard;
import org.alfresco.web.bean.spaces.SpaceDetailsDialog;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.alfresco.web.config.ClientConfigElement;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.UIBreadcrumb;
import org.alfresco.web.ui.common.component.UIModeList;
import org.alfresco.web.ui.repo.component.IRepoBreadcrumbHandler;
import org.alfresco.web.ui.repo.component.shelf.UIShelf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean providing access and management of the various global navigation mechanisms
 * such as the My Home, Company Home, Guest Home toolbar shortcuts, breadcrumb and
 * the current node id and associated properties.
 * 
 * @author Kevin Roast
 */
public class NavigationBean implements Serializable
{
   private static final long serialVersionUID = -648110889585522227L;

   /** Public JSF Bean name */
   public static final String BEAN_NAME = "NavigationBean";
   
   private static Log logger = LogFactory.getLog(NavigationBean.class);

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
   
   protected NodeService getNodeService()
   {
      if (nodeService == null)
         nodeService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
      return nodeService;
   }

   /**
    * @param searchService The searchService to set.
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }
   
   protected SearchService getSearchService()
   {
      if (searchService == null)
         this.searchService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getSearchService();
      return searchService;
   }
   
   /**
    * @param namespaceService The namespaceService to set.
    */
   public void setNamespaceService(NamespaceService namespaceService)
   {
      this.namespaceService = namespaceService;
   }
   
   protected NamespaceService getNamespaceService()
   {
      if (namespaceService == null)
         this.namespaceService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNamespaceService();
      return namespaceService;
   }
   
   /**
    * @param ruleService The ruleService to use
    */
   public void setRuleService(RuleService ruleService)
   {
      this.ruleService = ruleService;
   }
   
   protected RuleService getRuleService()
   {
      if (ruleService == null)
         this.ruleService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getRuleService();
      return ruleService;
   }

   /**
    * @param serverConfiguration The serverConfiguration to set.
    */
   public void setServerConfiguration(ServerConfigurationAccessor serverConfiguration)
   {
      this.serverConfiguration = serverConfiguration;
   }
   
   protected ServerConfigurationAccessor getServerConfiguration()
   {
      if (serverConfiguration == null)
         this.serverConfiguration = (ServerConfigurationAccessor) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "fileServerConfiguration");
      return serverConfiguration;
   }

   /**
    * @param contentDiskDriver The contentDiskDriver to set.
    */
   public void setContentDiskDriver(ContentDiskInterface contentDiskDriver)
   {
      this.contentDiskDriver = contentDiskDriver;
   }
   
   protected ClientConfigElement getClientConfig()
   {
      if (clientConfig == null)
         this.clientConfig = Application.getClientConfig(FacesContext.getCurrentInstance());
      return clientConfig;
   }

   /**
    * @param preferences The UserPreferencesBean to set
    */
   public void setUserPreferencesBean(UserPreferencesBean preferences)
   {
      this.preferences = preferences;
   }
   
   /**
    * @param authService The AuthenticationService to set.
    */
   public void setAuthenticationService(MutableAuthenticationService authService)
   {
      this.authService = authService;
   }
   
   protected MutableAuthenticationService getAuthService()
   {
      if (authService == null)
         this.authService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAuthenticationService();
      return authService;
   }
   
   /**
    * @param permissionService The PermissionService to set.
    */
   public void setPermissionService(PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }
   
   protected PermissionService getPermissionService()
   {
      if (permissionService == null)
         this.permissionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPermissionService();
      return permissionService;
   }

   /**
    * @return the User object representing the current instance for this user 
    */
   public User getCurrentUser()
   {
      return Application.getCurrentUser(FacesContext.getCurrentInstance());
   }
   
   /**
    * @return true if the system is running within a JSR-168 portal container
    */
   public boolean getInPortalServer()
   {
      return Application.inPortalServer();
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
    * Return the width of the main work area depending on the visibility of the Shelf panel
    * 
    * @return width, which will either be "80%" or "100%"
    */
   public String getWorkAreaWidth()
   {
      return this.shelfExpanded ? "80%" : "100%";
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
    * @return Returns the toolbar Location - initially set from the user preferences.
    */
   public String getToolbarLocation()
   {
      if (this.toolbarLocation == null)
      {
         // if the toolbar location has not been set yet, try and get the
         // default via the user preferences object
         this.toolbarLocation = this.preferences.getStartLocation();
         
         // test that the user still has access to the specified location
         // the location will need to be reset if the user permissions are no longer valid
         if (NavigationBean.LOCATION_COMPANY.equals(this.toolbarLocation))
         {
            if (getCompanyHomeVisible() == false)
            {
               this.toolbarLocation = null;
            }
         }
         else if (NavigationBean.LOCATION_GUEST.equals(this.toolbarLocation))
         {
            if (getGuestHomeVisible() == false)
            {
               this.toolbarLocation = null;
            }
         }
         
         // if don't have a valid start location default to My Home
         if (this.toolbarLocation == null)
         {
            this.toolbarLocation = LOCATION_HOME;
            this.preferences.setStartLocation(this.toolbarLocation);
         }
      }
      
      return this.toolbarLocation;
   }
   
   /**
    * @param location  The toolbar Location to set.
    */
   public void setToolbarLocation(String location)
   {
      this.toolbarLocation = location;
   }
   
   /**
    * Process the selected toolbar location. Setup the breadcrumb with initial value and
    * setup the current node ID. This method can also perform the navigatin setup if requested.
    * 
    * @param location      Toolbar location constant
    * @param navigate      True to perform navigation, false otherwise
    */
   @SuppressWarnings("serial")
   public void processToolbarLocation(String location, boolean navigate)
   {
      this.toolbarLocation = location;
      
      FacesContext context = FacesContext.getCurrentInstance();
      if (LOCATION_COMPANY.equals(location))
      {
         List<IBreadcrumbHandler> elements = new ArrayList<IBreadcrumbHandler>(1);
         Node companyHome = getCompanyHomeNode();
         elements.add(new NavigationBreadcrumbHandler(companyHome.getNodeRef(), companyHome.getName()));
         setLocation(elements);
         setCurrentNodeId(companyHome.getId());
         
         if (s_logger.isDebugEnabled())
            s_logger.debug("Created breadcrumb for companyhome: " + elements);
         
         // inform registered beans that the current area has changed
         UIContextService.getInstance(FacesContext.getCurrentInstance()).areaChanged();
         
         // we need to force a navigation to refresh the browse screen breadcrumb
         if (navigate)
         {
            context.getApplication().getNavigationHandler().handleNavigation(context, null, OUTCOME_BROWSE);
         }
      }
      else if (LOCATION_HOME.equals(location))
      {
         List<IBreadcrumbHandler> elements = new ArrayList<IBreadcrumbHandler>(1);
         String homeSpaceId = Application.getCurrentUser(context).getHomeSpaceId();
         NodeRef homeSpaceRef = new NodeRef(Repository.getStoreRef(), homeSpaceId);
         
         if (this.clientConfig.getBreadcrumbMode().equals(ClientConfigElement.BREADCRUMB_LOCATION))
         {
            Repository.setupBreadcrumbLocation(context, this, elements, homeSpaceRef);
            
            if (s_logger.isDebugEnabled())
               s_logger.debug("Created breadcrumb location for userhome: " + elements);
         }
         else
         {
            String homeSpaceName = Repository.getNameForNode(this.getNodeService(), homeSpaceRef);
            elements.add(new NavigationBreadcrumbHandler(homeSpaceRef, homeSpaceName));
            
            if (s_logger.isDebugEnabled())
               s_logger.debug("Created breadcrumb path for userhome: " + elements);
         }
         
         setLocation(elements);
         setCurrentNodeId(homeSpaceRef.getId());
         
         // inform registered beans that the current area has changed
         UIContextService.getInstance(FacesContext.getCurrentInstance()).areaChanged();
         
         // we need to force a navigation to refresh the browse screen breadcrumb
         if (navigate)
         {
            context.getApplication().getNavigationHandler().handleNavigation(context, null, OUTCOME_BROWSE);
         }
      }
      else if (LOCATION_GUEST.equals(location))
      {
         List<IBreadcrumbHandler> elements = new ArrayList<IBreadcrumbHandler>(1);
         Node guestHome = getGuestHomeNode();
         
         if (this.clientConfig.getBreadcrumbMode().equals(ClientConfigElement.BREADCRUMB_LOCATION))
         {
            Repository.setupBreadcrumbLocation(context, this, elements, guestHome.getNodeRef());
            
            if (s_logger.isDebugEnabled())
               s_logger.debug("Created breadcrumb location for guesthome: " + elements);
         }
         else
         {
            elements.add(new NavigationBreadcrumbHandler(guestHome.getNodeRef(), guestHome.getName()));
            
            if (s_logger.isDebugEnabled())
               s_logger.debug("Created breadcrumb path for guesthome: " + elements);
         }
         
         setLocation(elements);
         setCurrentNodeId(guestHome.getId());
         
         // inform registered beans that the current area has changed
         UIContextService.getInstance(FacesContext.getCurrentInstance()).areaChanged();
         
         // we need to force a navigation to refresh the browse screen breadcrumb
         if (navigate)
         {
            context.getApplication().getNavigationHandler().handleNavigation(context, null, OUTCOME_BROWSE);
         }
      }
      else if (LOCATION_MYALFRESCO.equals(location))
      {
         // make sure we set a current node ID as some screens expect this
         if (getCurrentNodeId() == null)
         {
            String homeSpaceId = Application.getCurrentUser(context).getHomeSpaceId();
            NodeRef homeSpaceRef = new NodeRef(Repository.getStoreRef(), homeSpaceId);
            setCurrentNodeId(homeSpaceRef.getId());
         }
         
         // create a breadcrumb handler for this special case location (not a node)
         List<IBreadcrumbHandler> elements = new ArrayList<IBreadcrumbHandler>(1);
         elements.add(new IBreadcrumbHandler()
            {
               @SuppressWarnings("unchecked")
               public String navigationOutcome(UIBreadcrumb breadcrumb)
               {
                  setLocation( (List)breadcrumb.getValue() );
                  return OUTCOME_MYALFRESCO;
               };
               
               public String toString()
               {
                  return Application.getMessage(FacesContext.getCurrentInstance(), MSG_MYALFRESCO);
               };
            });
         
         if (s_logger.isDebugEnabled())
            s_logger.debug("Created breadcrumb for myalfresco: " + elements);
         
         setLocation(elements);
         
         // inform registered beans that the current area has changed
         UIContextService.getInstance(FacesContext.getCurrentInstance()).areaChanged();
         
         // we need to force a navigation to refresh the browse screen breadcrumb
         if (navigate)
         {
            context.getApplication().getNavigationHandler().handleNavigation(context, null, OUTCOME_MYALFRESCO);
         }
      }
      else
      {
         // handle outcomes to any other custom location
         context.getApplication().getNavigationHandler().handleNavigation(context, null, location);
      }
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
      if (this.helpUrl == null)
      {
         Descriptor serverDescriptor = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getDescriptorService().getServerDescriptor();
         // search / replace each available key occurrence in the template string
         // Note: server descriptor is looking for "version.major", "version.minor", etc.
         Pattern p = Pattern.compile("\\{(\\w+\\.?\\w+)\\}");
         Matcher m = p.matcher(helpUrl);
         boolean result = m.find();
         if (result)
         {
            StringBuffer sb = new StringBuffer();
            String value = null;
            do
            {
               value = serverDescriptor.getDescriptor(m.group(1));
               m.appendReplacement(sb, value != null ? value.toLowerCase() : m.group(1));
               result = m.find();
            } while (result);
            m.appendTail(sb);
            helpUrl = sb.toString();
         }

         this.helpUrl = helpUrl;
      }
   }
   
   /**
    * @return the number of rules associated with the current space
    */
   public int getRuleCount()
   {
       Node node = getCurrentNode();
       return (node != null ? this.getRuleService().countRules(node.getNodeRef()) : 0);
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
    * @return true if the current node has a custom view available
    */
   public boolean getHasCustomView()
   {
      return getHasWebscriptView() || getHasTemplateView();
   }
   
   /**
    * @return true if the current node has a Template based custom view available
    */
   public boolean getHasTemplateView()
   {
      Node node = getCurrentNode();
      if (node.hasAspect(ContentModel.ASPECT_TEMPLATABLE))
      {
         NodeRef templateRef = (NodeRef)node.getProperties().get(ContentModel.PROP_TEMPLATE);
         return (templateRef != null && this.getNodeService().exists(templateRef) &&
                 this.getPermissionService().hasPermission(templateRef, PermissionService.READ) == AccessStatus.ALLOWED);
      }
      return false;
   }
   
   /**
    * @return true if the current node has a Webscript based custom view available
    */
   public boolean getHasWebscriptView()
   {
      Node node = getCurrentNode();
      if (node.hasAspect(ContentModel.ASPECT_WEBSCRIPTABLE))
      {
         return (node.getProperties().get(ContentModel.PROP_WEBSCRIPT) != null);
      }
      return false;
   }
   
   /**
    * @return the NodeRef.toString() for the current node Template custom view if it has one 
    */
   public String getCurrentNodeTemplate()
   {
      NodeRef ref = (NodeRef)getCurrentNode().getProperties().get(ContentModel.PROP_TEMPLATE);
      return ref != null ? ref.toString() : null;
   }
   
   /**
    * @return the service url for the current node Webscript custom view if it has one 
    */
   public String getCurrentNodeWebscript()
   {
      return (String)getCurrentNode().getProperties().get(ContentModel.PROP_WEBSCRIPT);
   }
   
   /**
    * Returns a model for use by a template on a space Dashboard page.
    * 
    * @return model containing current current space info.
    */
   @SuppressWarnings("unchecked")
   public Map getTemplateModel()
   {
      HashMap model = new HashMap(2, 1.0f);
      
      model.put("space", getCurrentNode().getNodeRef());
      model.put(TemplateService.KEY_IMAGE_RESOLVER, 
            new TemplateImageResolver() 
            {
               public String resolveImagePathForName(String filename, FileTypeImageSize size)
               {
                  return FileTypeImageUtils.getFileTypeImage(FacesContext.getCurrentInstance(), filename, size);
               }
            });
      
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
            // handle the possibility that we have no current location
            // this is possible in cluster fail-over or due to a missing node
            // default back to the current toolbar root location
            this.processToolbarLocation(this.getToolbarLocation(), false);
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
            FacesContext fc = FacesContext.getCurrentInstance();
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  fc, ERROR_DELETED_FOLDER), new Object[] {this.currentNodeId}) );
            
            nodeRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId(fc));
            node = new Node(nodeRef);
            props = node.getProperties();
         }
         String icon = (String)props.get("app:icon");
         props.put("icon", icon != null ? icon : CreateSpaceWizard.DEFAULT_SPACE_ICON_NAME);
         Path path = node.getNodePath();
         
         // resolve CIFS network folder location for this node
         FilesystemsConfigSection filesysConfig = (FilesystemsConfigSection)getServerConfiguration().getConfigSection(FilesystemsConfigSection.SectionName); 
         DiskSharedDevice diskShare = null;
         
         SharedDeviceList shares = filesysConfig.getShares();
         Enumeration<SharedDevice> shareEnum = shares.enumerateShares();
         
         while (shareEnum.hasMoreElements() && diskShare == null)
         {
            SharedDevice curShare = shareEnum.nextElement();
            if (curShare.getContext() instanceof ContentContext)
            {
               diskShare = (DiskSharedDevice)curShare;
            }
         }
         
         if (diskShare != null)
         {
            ContentContext contentCtx = (ContentContext) diskShare.getContext();
            NodeRef rootNode = contentCtx.getRootNode();
            try
            {
               String cifsPath = Repository.getNamePath(this.getNodeService(), path, rootNode, "\\", "file:///" + getCIFSServerPath(diskShare));
               
               node.getProperties().put("cifsPath", cifsPath);
               node.getProperties().put("cifsPathLabel", cifsPath.substring(8));  // strip file:/// part
            }
            catch(AccessDeniedException ade)
            {
               node.getProperties().put("cifsPath", "");
               node.getProperties().put("cifsPathLabel","");  // strip file:/// part
            }
         }
         
         this.currentNode = node;
      }
      
      return this.currentNode;
   }
   
   /**
    * @return Boolean value according to CREATE_CHILDREN permission on the current node.
    */
   public boolean isCreateChildrenPermissionEnabled()
   {
       return getCurrentNode().hasPermission(PermissionService.CREATE_CHILDREN);
   }
   
   /**
    * @return Returns the breadcrumb handler elements representing the location path of the UI.
    */
   public List<IBreadcrumbHandler> getLocation()
   {
      if (this.location == null)
      {
         // get the initial location from the user preferences
         processToolbarLocation(getToolbarLocation(), false);
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
      return Repository.getIsGuest(FacesContext.getCurrentInstance());
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
   
   /**
    * @return Node representing the Company Home folder
    */
   public Node getCompanyHomeNode()
   {
      if (this.companyHomeNode == null)
      {
          FacesContext fc = FacesContext.getCurrentInstance();
          String companyRootId = Application.getCompanyRootId(fc);
          if (companyRootId != null)
          {
              NodeRef companyRootRef = new NodeRef(Repository.getStoreRef(), companyRootId);
              this.companyHomeNode = new Node(companyRootRef);
          }
      }
      return this.companyHomeNode;
   }
   
   /**
    * @return Node representing the Guest Home Space folder
    */
   public Node getGuestHomeNode()
   {
      if (this.guestHomeNode == null)
      {
         try
         {
            FacesContext fc = FacesContext.getCurrentInstance();
            String xpath = Application.getRootPath(fc) + "/" + Application.getGuestHomeFolderName(fc);
            List<NodeRef> guestHomeRefs = this.getSearchService().selectNodes(
                  this.getNodeService().getRootNode(Repository.getStoreRef()),
                  xpath, null, this.getNamespaceService(), false);
            if (guestHomeRefs.size() == 1)
            {
               this.guestHomeNode = new Node(guestHomeRefs.get(0));
            }
         }
         catch (InvalidNodeRefException err1)
         {
            // cannot continue if this occurs
         }
         catch (AccessDeniedException err2)
         {
            // cannot see node if this occurs
         }
      }
      return this.guestHomeNode;
   }
   
   /**
    * @return true if the Company home node is accessible to the current user
    */
   public boolean getCompanyHomeVisible()
   {
      try
      {
         Node companyHomeNode = getCompanyHomeNode();
         if (companyHomeNode != null)
         {
            return companyHomeNode.hasPermission(PermissionService.READ);
         }
         else
         {
            return false;
         }
      }
      catch (Throwable e)
      {
         if (logger.isDebugEnabled())
         {
            logger.debug("getCompanyHomeVisible failed", e);
         }
         return false;
      }
   }
   
   /**
    * @return true if the Guest home node is accessible to the current user
    */
   public boolean getGuestHomeVisible()
   {
      try
      {
         if (this.getAuthService().guestUserAuthenticationAllowed())
         {
            Node guestHome = getGuestHomeNode();
            return guestHome != null && guestHome.hasPermission(PermissionService.READ);
         }
         else
         {
            return false;
         }
      }
      catch (Throwable e)
      {
         if (logger.isDebugEnabled())
         {
            logger.debug("getGuestHomeVisible failed", e);
         }
         return false;
      }
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
         processToolbarLocation(location, true);
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
         StringBuilder buf = new StringBuilder(32);
         
         String serverName = this.getServerConfiguration().getServerName();
         if (serverName != null && serverName.length() != 0)
         {
            buf.append("\\\\");
            buf.append(serverName);
            
            // Check if there is a suffix to apply to the host name
            if (clientConfig != null && clientConfig.getCifsURLSuffix() != null)
            {
            	buf.append(clientConfig.getCifsURLSuffix());
            }
            
            buf.append("\\");
            buf.append(diskShare.getName());
         }
         
         this.cifsServerPath = buf.toString();
      }
      
      return this.cifsServerPath;
   }
   
   /**
    * @return true if the current space has an RSS feed applied
    */
   public boolean isRSSFeed()
   {
      return SpaceDetailsDialog.hasRSSFeed(getCurrentNode());
   }
   
   /**
    * @return RSS Feed URL for the current space
    */
   public String getRSSFeedURL()
   {
      return SpaceDetailsDialog.buildRSSFeedURL(getCurrentNode());
   }
   
   /**
    * @return true if User/Group admin is allowed by admin users
    */
   public boolean isAllowUserGroupAdmin()
   {
      return this.clientConfig.isUserGroupAdmin();
   }
   
   /**
    * @return true if users can configure their own settings in the User Console
    */
   public boolean isAllowUserConfig()
   {
      return this.clientConfig.getAllowUserConfig();
   }
   
   /**
    * @return true if a users can modify the password set against their authentication
    */
   public boolean isAllowUserChangePassword()
   {
       return this.authService.isAuthenticationMutable(this.authService.getCurrentUserName());
   }
   
   
   // ------------------------------------------------------------------------------
   // Helpers
   
   /**
    * Initialise default values from client configuration
    * 
    * Package visibility to allow LoginBean to re-init (for example, in context of tenant config)
    */
   /* package */ void initFromClientConfig()
   {
      this.clientConfig = Application.getClientConfig(FacesContext.getCurrentInstance());
      this.setHelpUrl(clientConfig.getHelpUrl());
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
      @SuppressWarnings("unchecked")
      public String navigationOutcome(UIBreadcrumb breadcrumb)
      {
         // set the current node to the specified top level node ID
         FacesContext fc = FacesContext.getCurrentInstance();
         setCurrentNodeId(ref.getId());
         setLocation( (List)breadcrumb.getValue() );
         
         // setup the dispatch context
         setupDispatchContext(new Node(ref));
         
         // inform any listeners that the current space has changed
         UIContextService.getInstance(FacesContext.getCurrentInstance()).spaceChanged();
         
         if (fc.getViewRoot().getViewId().equals(BrowseBean.BROWSE_VIEW_ID))
         {
            return null;
         }
         else
         {
            return OUTCOME_BROWSE;
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
   
   private static Log    s_logger = LogFactory.getLog(NavigationBean.class);
   
   /** constant values used by the toolbar location modelist control */
   public static final String LOCATION_COMPANY    = "companyhome";
   public static final String LOCATION_HOME       = "userhome";
   public static final String LOCATION_GUEST      = "guesthome";
   public static final String LOCATION_MYALFRESCO = "myalfresco";
   
   /** constant value representing the display lables for toolbar locations */
   public static final String MSG_MYALFRESCO = "my_alfresco";
   public static final String MSG_MYHOME = "my_home";
   public static final String MSG_COMPANYHOME = "company_home";
   public static final String MSG_GUESTHOME = "guest_home";
   
   private static final String OUTCOME_MYALFRESCO = "myalfresco";
   private static final String OUTCOME_BROWSE = "browse";
   
   private static final String ERROR_DELETED_FOLDER = "error_deleted_folder";
   
   /** The NodeService to be used by the bean */
   transient private NodeService nodeService;
   
   /** The SearchService to be used by the bean */
   transient private SearchService searchService;
   
   /** NamespaceService bean reference */
   transient private NamespaceService namespaceService;
   
   /** RuleService bean reference*/
   transient private RuleService ruleService;
   
   /** File server configuration reference */
   transient private ServerConfigurationAccessor serverConfiguration;
   
   /** CIFS content disk driver bean reference */
   protected ContentDiskInterface contentDiskDriver;
   
   /** Client configuration object */
   protected ClientConfigElement clientConfig = null;
   
   /** The user preferences bean reference */
   UserPreferencesBean preferences;
   
   /** The Authentication service bean reference */
   transient private MutableAuthenticationService authService;
   
   /** The PermissionService reference */
   transient private PermissionService permissionService;
   
   /** Cached path to our CIFS server and top level node DIR */
   private String cifsServerPath;
   
   /** Node Id we are using for UI context operations */
   private String currentNodeId;
   
   /** Node we are using for UI context operations */
   private Node currentNode = null;
   
   /** Node we are using for dispatching */
   private Node dispatchContext = null;
   
   /** Node representing the guest home */
   private Node guestHomeNode = null;
   
   /** Node representing the company home */
   private Node companyHomeNode = null;
   
   /** Current toolbar location */
   private String toolbarLocation = null;
   
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