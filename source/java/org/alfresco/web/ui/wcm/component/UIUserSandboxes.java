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
package org.alfresco.web.ui.wcm.component;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.transaction.UserTransaction;

import org.alfresco.config.JNDIConstants;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.wcm.asset.AssetInfo;
import org.alfresco.wcm.asset.AssetInfoImpl;
import org.alfresco.wcm.sandbox.SandboxConstants;
import org.alfresco.wcm.sandbox.SandboxInfo;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.alfresco.wcm.webproject.WebProjectService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.bean.wcm.AVMBrowseBean;
import org.alfresco.web.bean.wcm.AVMNode;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.DeploymentUtil;
import org.alfresco.web.bean.wcm.WebProject;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.forms.Form;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.PanelGenerator;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIMenu;
import org.alfresco.web.ui.common.converter.ByteSizeConverter;
import org.alfresco.web.ui.repo.component.UIActions;
import org.alfresco.web.ui.wcm.WebResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.ui.common.ConstantMethodBinding;
import org.springframework.extensions.webscripts.ui.common.component.SelfRenderingComponent;

/**
 * Component responsible for rendering the list of user sandboxes for a web project.
 * <p>
 * The list of users attached to the supplied Web Project noderef (must be of type
 * wcm:avmfolder) are iterated and the various AVM services used to provide the list
 * of modified files for each user in turn. Actions are rendering with appropriate
 * permission evaluators next to each item. The status of workflows in progress are
 * also checked against items when building the list of actions.
 * <p>
 * Multi-select functionality is provided for specific actions.
 * 
 * @author Kevin Roast
 */
public class UIUserSandboxes extends SelfRenderingComponent implements Serializable
{
   private static final long serialVersionUID = 362170364310941059L;

   private static Log logger = LogFactory.getLog(UIUserSandboxes.class);
   
   private static final String ACT_FIND_FORM_CONTENT = "find_form_content";
   private static final String ACT_CREATE_FORM_CONTENT = "create_form_content";
   private static final String ACT_SANDBOX_REVERTSELECTED = "sandbox_revertselected";
   private static final String ACT_SANDBOX_SUBMITSELECTED = "sandbox_submitselected";
   private static final String ACT_SANDBOX_BROWSE = "sandbox_browse";
   private static final String ACT_SANDBOX_REVERTALL = "sandbox_revertall";
   private static final String ACT_SANDBOX_SUBMITALL = "sandbox_submitall";
   private static final String ACT_SANDBOX_PREVIEW = "sandbox_preview";
   private static final String ACT_SANDBOX_ICON = "sandbox_icon";
   private static final String ACT_REMOVE_SANDBOX = "sandbox_remove";
   private static final String ACT_SANDBOX_REFRESH = "sandbox_refresh";
   private static final String ACT_SANDBOX_DEPLOY = "sandbox_deploy";
   private static final String ACT_SANDBOX_DEPLOY_REPORT = "deployment_report_action";
   private static final String ACT_SANDBOX_RELEASE_SERVER = "sandbox_release_test_server";
   
   private static final String ACTIONS_FILE = "avm_file_modified";
   private static final String ACTIONS_FOLDER = "avm_folder_modified";
   private static final String ACTIONS_DELETED = "avm_deleted_modified";
   
   private static final String COMPONENT_ACTIONS = "org.alfresco.faces.Actions";
   
   private static final String PANEL_MODIFIED = "_items";
   private static final String PANEL_FORMS = "_forms";
   
   private static final String MSG_MODIFIED_ITEMS = "modified_items";
   private static final String MSG_CONTENT_FORMS = "content_forms";
   private static final String MSG_SIZE = "size";
   private static final String MSG_CREATED = "created_date";
   private static final String MSG_USERNAME = "sandbox_user";
   private static final String MSG_NAME = "name";
   private static final String MSG_TITLE = "title";
   private static final String MSG_DESCRIPTION = "description";
   private static final String MSG_MODIFIED = "modified_date";
   private static final String MSG_ACTIONS = "actions";
   private static final String MSG_DELETED_ITEM = "avm_node_deleted";
   private static final String MSG_SELECTED = "selected";
   private static final String MSG_NO_MODIFIED_ITEMS = "sandbox_no_modified_items";
   private static final String MSG_NO_WEB_FORMS = "sandbox_no_web_forms";
   private static final String MSG_MY_SANDBOX = "sandbox_my_sandbox";
   private static final String MSG_COUNT_CONFLICTED_ITEMS="count_conflicted_items";
   private static final String MSG_REVERT_ALL_CONFLICTS="revert_all_conflicts";
   
   private static final String REQUEST_FORM_REF = "formref";
   private static final String REQUEST_PREVIEW_REF = "prevhref";
   private static final String REQUEST_UPDATE_TEST_SERVER = "updatetestserver";
   
   private static final String SPACE_ICON = "/images/icons/" + BrowseBean.SPACE_SMALL_DEFAULT + ".gif";
   private static final String CONFLICTED_ICON = "/images/icons/conflict-16.gif";
   
   public static final String PARAM_FORM_NAME = "form-name";
   
   private static final String SCRIPT_MULTISELECT =
                        "<script>function _sb_select(obj) {" + 
                        "var p = obj.value + '_';" + 
                        "var b = document.getElementsByTagName('input');" + 
                        "for (var i=0; i<b.length; i++) {" + 
                        "  if (b[i].value.indexOf(p, 0) != -1) {" + 
                        "    b[i].checked = obj.checked;\r\n" + 
                        "  }" + 
                        "}" + 
                        "}</script>";

   /** website to show sandboxes for */
   private NodeRef value;
   
   /** webapp to filter list by */
   private String webapp;
   
   /** cached converter instance */
   transient private ByteSizeConverter sizeConverter = null;
   
   /** set of exanded user panels */
   private Set<String> expandedPanels = new HashSet<String>();
   
   /** map of users to modified item nodes - used for multi-select action lookup */
   private Map<Integer, String> userToRowLookup = new HashMap<Integer, String>(8, 1.0f);
   private Map<String, Integer> rowToUserLookup = new HashMap<String, Integer>(8, 1.0f);
   private Map<String, List<AVMNodeDescriptor>> userNodes = new HashMap<String, List<AVMNodeDescriptor>>(8, 1.0f);
   
   private String[] checkedItems = null;
   
   /** transient list of available web forms */
   private List<Form> forms = null;
   
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.UserSandboxes";
   }
   
   @SuppressWarnings("unchecked")
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.value = (NodeRef)values[1];
      this.expandedPanels = (Set)values[2];
      this.userToRowLookup = (Map)values[3];
      this.rowToUserLookup = (Map)values[4];
      this.userNodes = (Map)values[5];
      this.checkedItems = (String[])values[6];
   }
   
   public Object saveState(FacesContext context)
   {
      return new Object[]
      {
         // standard component attributes are saved by the super class
         super.saveState(context),
         this.value,
         this.expandedPanels,
         this.userToRowLookup,
         this.rowToUserLookup,
         this.userNodes,
         this.checkedItems
      };
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#getRendersChildren()
    */
   public boolean getRendersChildren()
   {
      return true;
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#encodeChildren(javax.faces.context.FacesContext)
    */
   public void encodeChildren(FacesContext context) throws IOException
   {
      // the child components are rendered explicitly during the encodeBegin()
   }

   /**
    * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
    */
   @SuppressWarnings("unchecked")
   public void decode(FacesContext context)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      Map valuesMap = context.getExternalContext().getRequestParameterValuesMap();
      
      // detect if Modified Items or Available Content Forms panel has been expanded/collapsed
      String fieldId = getClientId(context) + PANEL_FORMS;
      String value = (String)requestMap.get(fieldId);
      if (value == null || value.length() == 0)
      {
         fieldId = getClientId(context) + PANEL_MODIFIED;
         value = (String)requestMap.get(fieldId);
      }
      if (value != null && value.length() != 0)
      {
         // expand/collapse the specified users panel
         if (this.expandedPanels.contains(value) == true)
         {
            // collapse by removing from expanded list 
            this.expandedPanels.remove(value);
         }
         else
         {
            // add to expanded panel set
            this.expandedPanels.add(value);
         }
      }
      
      // store the list of checked items for multi-select action context
      this.checkedItems = (String[])valuesMap.get(getClientId(context));
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   @SuppressWarnings("unchecked")
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      
      this.rowToUserLookup.clear();
      this.userToRowLookup.clear();
      this.userNodes.clear();
      this.forms = null;
      
      ResourceBundle bundle = Application.getBundle(context);
      SandboxService sbService = getSandboxService(context);
      WebProjectService wpService = getWebProjectService(context);
      NodeService nodeService = getNodeService(context);
      PermissionService permissionService = getPermissionService(context);
      AVMBrowseBean avmBrowseBean = (AVMBrowseBean)FacesHelper.getManagedBean(context, AVMBrowseBean.BEAN_NAME);
      boolean showAllSandboxes = avmBrowseBean.getShowAllSandboxes();
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         NodeRef websiteRef = getValue();
         if (websiteRef == null)
         {
            throw new IllegalArgumentException("Website NodeRef must be specified.");
         }
         String storeRoot = (String)nodeService.getProperty(websiteRef, WCMAppModel.PROP_AVMSTORE);
         
         // find out the current user role in the web project
         User currentUser = Application.getCurrentUser(context);
         String currentUserName = currentUser.getUserName();
         String currentUserRole = wpService.getWebUserRole(websiteRef, currentUserName);
         
         // sort the user list alphabetically and insert the current user at the top of the list
         List<UserRoleWrapper> userRoleWrappers;
         if (showAllSandboxes)
         {
            Map<String, String> userRoles = null;
            if (currentUserRole.equals(WCMUtil.ROLE_CONTENT_MANAGER) || currentUserRole.equals(WCMUtil.ROLE_CONTENT_PUBLISHER))
            {
                Map<String, String> allUserRoles = wpService.listWebUsers(websiteRef);
                
                WebProjectInfo wpInfo = wpService.getWebProject(websiteRef);
                List<SandboxInfo> sbInfos = sbService.listSandboxes(wpInfo.getStoreId());
                
                userRoles = new HashMap<String, String>(sbInfos.size());
                
                // Note: currently displays author sandboxes only
                for (SandboxInfo sbInfo : sbInfos)
                {
                    if (sbInfo.getSandboxType().equals(SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN))
                    {
                        userRoles.put(sbInfo.getName(), allUserRoles.get(sbInfo.getName()));
                    }
                }
            }
            else
            {
                userRoles = new HashMap<String, String>(1);
                userRoles.put(currentUserName, currentUserRole);
            }
            
            userRoleWrappers = buildSortedUserRoles(nodeService, currentUserName, userRoles);
         }
         else
         {
            userRoleWrappers = buildCurrentUserRole(wpService, websiteRef, currentUserName);
         }
         
         // determine whether the deploy action should be shown
         boolean deployServersConfigured = false;
         List<NodeRef> deployToServers = Repository.getServiceRegistry(context).getDeploymentService().findTestDeploymentServers(websiteRef, false);
         if (deployToServers != null && deployToServers.size() > 0)
         {
            deployServersConfigured = true;
         }
         
         // output a javascript function we need for multi-select functionality
         out.write(SCRIPT_MULTISELECT);
         
         // walk the list of users who have a sandbox in the website
         int index = 0;
         for (UserRoleWrapper wrapper : userRoleWrappers)
         {
            String username = wrapper.UserName;
            String userrole = wrapper.UserRole;
            
            // create the lookup value of sandbox index to username 
            this.userToRowLookup.put(index, username);
            this.rowToUserLookup.put(username, index);
            
            // build the name of the main store for this user (user sandbox id)
            String mainStore = AVMUtil.buildUserMainStoreName(storeRoot, username);
            
            // check it exists before we render the view
            if (sbService.getSandbox(mainStore) != null)
            {
               // check the permissions on this store for the current user
               if (logger.isDebugEnabled())
                     logger.debug("Checking user role to view store: " + mainStore);
               
               if ((showAllSandboxes &&
                    (currentUserName.equals(username) ||
                     WCMUtil.ROLE_CONTENT_MANAGER.equals(currentUserRole) ||
                     WCMUtil.ROLE_CONTENT_PUBLISHER.equals(currentUserRole))) ||
                   showAllSandboxes == false)
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Building sandbox view for user store: " + mainStore);
                  
                  // determine if the sandbox has an allocated test server for deployment
                  List<NodeRef> testServers = DeploymentUtil.findAllocatedTestServers(mainStore);
                  boolean hasAllocatedTestServer = (!testServers.isEmpty());
                  
                  // determine if there are any previous deployment attempts
                  List<NodeRef> deployAttempts = DeploymentUtil.findDeploymentAttempts(mainStore);
                  boolean hasPreviousDeployments = (deployAttempts.size() > 0);
                  
                  // for each user sandbox, generate an outer panel table
                  PanelGenerator.generatePanelStart(out,
                        context.getExternalContext().getRequestContextPath(),
                        "innerwhite",
                        "white");
                  
                  // components for the current username, preview, browse and modified items inner list
                  out.write("<table cellspacing=2 cellpadding=2 border=0 width=100%><tr><td>");
                  // show the icon for the sandbox as a clickable browse link image
                  // this is currently identical to the sandbox_browse action as below
                  UIActionLink browseAction = aquireAction(
                        context, mainStore, username, ACT_SANDBOX_ICON, WebResources.IMAGE_USERSANDBOX_32,
                        "#{AVMBrowseBean.setupSandboxAction}", "browseSandbox");
                  browseAction.setShowLink(false);
                  Utils.encodeRecursive(context, browseAction);
                  out.write("</td><td width=100%>");
                  if (wrapper.IsCurrentUser == false)
                  {
                     out.write("<b>");
                     out.write(bundle.getString(MSG_USERNAME));
                     out.write(":</b>&nbsp;");
                     out.write(Utils.encode(username));
                  }
                  else
                  {
                     out.write("<b>");
                     out.write(bundle.getString(MSG_MY_SANDBOX));
                     out.write("</b>");
                  }
                  out.write(" (");
                  out.write(bundle.getString(userrole));
                  out.write(")</td><td><table cellpadding='4' cellspacing='0'><tr><td><nobr>");
                  
                  // Direct actions for a sandbox...
                  
                  // Browse Sandbox
                  Utils.encodeRecursive(context, aquireAction(
                        context, mainStore, username, ACT_SANDBOX_BROWSE, "/images/icons/space_small.gif",
                        "#{AVMBrowseBean.setupSandboxAction}", "browseSandbox"));
                  out.write("</nobr></td><td><nobr>");
                  
                  // Preview Website
                  String websiteUrl = AVMUtil.getPreviewURI(mainStore, JNDIConstants.DIR_DEFAULT_WWW_APPBASE + '/' + getWebapp());
                  Map requestMap = context.getExternalContext().getRequestMap();
                  requestMap.put(REQUEST_PREVIEW_REF, websiteUrl);
                  Utils.encodeRecursive(context, aquireAction(
                        context, mainStore, username, ACT_SANDBOX_PREVIEW, "/images/icons/preview_website.gif",
                        null, null, "#{" + REQUEST_PREVIEW_REF + "}", null));
                  requestMap.remove(REQUEST_PREVIEW_REF);
                  out.write("</nobr></td><td><nobr>");
                                    
                  // Submit All Items
                  Utils.encodeRecursive(context, aquireAction(
                        context, mainStore, username, ACT_SANDBOX_SUBMITALL, "/images/icons/submit_all.gif",
                        "#{AVMBrowseBean.setupAllItemsAction}", "dialog:submitSandboxItems"));
                  out.write("</nobr></td><td><nobr>");
                  
                  // Revert All Items
                  Utils.encodeRecursive(context, aquireAction(
                        context, mainStore, username, ACT_SANDBOX_REVERTALL, "/images/icons/revert_all.gif",
                        "#{AVMBrowseBean.setupAllItemsAction}", "dialog:revertAllItems"));
                  out.write("</nobr></td><td><nobr>");
                  
                  // More Actions menu
                  UIMenu menu = findMenu(mainStore);
                  if (menu == null)
                  {
                     // create the menu, then the actions
                     menu = createMenu(context, mainStore);
                     
                     // add the menu to this component
                     this.getChildren().add(menu);
                  }
                  
                  // clear current menu actions then add relevant ones
                  menu.getChildren().clear();
                  
                  // Deploy action
                  if (deployServersConfigured)
                  {
                     Map<String, String> dialogParams = new HashMap<String, String>(6);
                     dialogParams.put("store", mainStore);
                     dialogParams.put("username", username);
                     requestMap.put(REQUEST_UPDATE_TEST_SERVER, Boolean.toString(hasAllocatedTestServer));
                     dialogParams.put("updateTestServer", "#{" + REQUEST_UPDATE_TEST_SERVER + "}");
                     UIActionLink deploy = createAction(context, mainStore, username, 
                              ACT_SANDBOX_DEPLOY, "/images/icons/deploy.gif",
                              "#{DialogManager.setupParameters}", "dialog:deployWebsite", 
                              null, dialogParams, false);
                     menu.getChildren().add(deploy);
                  }

                  // View deployment report action
                  if (hasPreviousDeployments)
                  {
                     UIActionLink reports = createAction(context, mainStore, username, 
                              ACT_SANDBOX_DEPLOY_REPORT, "/images/icons/deployment_report.gif", 
                              "#{DialogManager.setupParameters}", "dialog:viewDeploymentReport",
                              null, null, false);
                     menu.getChildren().add(reports);
                  }
                  
                  // Release Test Server action
                  if (hasAllocatedTestServer)
                  {
                     UIActionLink releaseServer = createAction(context, mainStore, username, 
                              ACT_SANDBOX_RELEASE_SERVER, "/images/icons/release_server.gif", 
                              "#{DialogManager.setupParameters}", "dialog:releaseTestServer", 
                              null, null, false);
                     menu.getChildren().add(releaseServer);
                  }
                  
                  // Refresh Sandbox action
                  UIActionLink refresh = createAction(context, mainStore, username, 
                           ACT_SANDBOX_REFRESH, "/images/icons/reset.gif",
                           "#{AVMBrowseBean.refreshSandbox}", null, null, null, false);
                  menu.getChildren().add(refresh);

                  // Delete Sandbox action
                  if (WCMUtil.ROLE_CONTENT_MANAGER.equals(currentUserRole))
                  {
                     UIActionLink delete = createAction(context, mainStore, username, 
                              ACT_REMOVE_SANDBOX, "/images/icons/delete_sandbox.gif",
                              "#{AVMBrowseBean.setupSandboxAction}", "dialog:deleteSandbox",
                              null, null, false);
                     menu.getChildren().add(delete);
                  }
                  
                  // render the menu
                  Utils.encodeRecursive(context, menu);
                  
                  out.write("</nobr></td></tr></table></td></tr>");
                  
                  // modified items panel
                  out.write("<tr><td></td><td colspan=2>");
                  String panelImage = WebResources.IMAGE_COLLAPSED;
                  if (this.expandedPanels.contains(username + PANEL_MODIFIED))
                  {
                     panelImage = WebResources.IMAGE_EXPANDED;
                  }
                  out.write(Utils.buildImageTag(context, panelImage, 11, 11, "",
                        Utils.generateFormSubmit(context, this, getClientId(context) + PANEL_MODIFIED, username + PANEL_MODIFIED)));
                  out.write("&nbsp;<b>");
                  out.write(bundle.getString(MSG_MODIFIED_ITEMS));
                  out.write("</b>");
                  
                  if (this.expandedPanels.contains(username + PANEL_MODIFIED))
                  {
                     out.write("<div style='padding:2px'></div>");
   
                     // list the modified docs for this sandbox user
                     renderUserFiles(context, out, username, storeRoot, index);
                  }
                  out.write("</td></tr>");
                  
                  // content forms panel
                  if (permissionService.hasPermission(
                       AVMNodeConverter.ToNodeRef(-1, AVMUtil.buildSandboxRootPath(mainStore)),
                       PermissionService.ADD_CHILDREN) == AccessStatus.ALLOWED)
                  {
                      out.write("<tr style='padding-top:4px'><td></td><td colspan=2>");
                      panelImage = WebResources.IMAGE_COLLAPSED;
                      if (this.expandedPanels.contains(username + PANEL_FORMS))
                      {
                         panelImage = WebResources.IMAGE_EXPANDED;
                      }
                      out.write(Utils.buildImageTag(context, panelImage, 11, 11, "",
                            Utils.generateFormSubmit(context, this, getClientId(context) + PANEL_FORMS, username + PANEL_FORMS)));
                      out.write("&nbsp;<b>");
                      out.write(bundle.getString(MSG_CONTENT_FORMS));
                      out.write("</b>");
                      if (this.expandedPanels.contains(username + PANEL_FORMS))
                      {
                         out.write("<div style='padding:2px'></div>");
                         
                         // list the content forms for this sandbox user
                         renderContentForms(context, out, websiteRef, username, storeRoot);
                      }
                      out.write("</td></tr>");
                  }
                  out.write("</table>");
                  
                  // end the outer panel for this sandbox
                  PanelGenerator.generatePanelEnd(out,
                        context.getExternalContext().getRequestContextPath(),
                        "innerwhite");
                  
                  // spacer row
                  if (index++ < userRoleWrappers.size() - 1)
                  {
                     out.write("<div style='padding:4px'></div>");
                  }
               }
            }
         }
         
         tx.commit();
      }
      catch (Throwable err)
      {
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         throw new RuntimeException(err);
      }
   }
   
   /**
    * Build a sorted list of objects representing the users of the website.
    * <p>
    * User role data and the current user is also stored. The current user sandbox
    * is inserted at the top of the list if present. 
    */
   private static List<UserRoleWrapper> buildSortedUserRoles(
         NodeService nodeService, String currentUser, Map<String, String> userRoles)
   {
      // build a list of wrappers to hold the fields we need for each user and role
      UserRoleWrapper currentUserWrapper = null;
      List<UserRoleWrapper> wrappers = new LinkedList<UserRoleWrapper>();
      for (Map.Entry<String, String> userRole : userRoles.entrySet())
      {
         String username = userRole.getKey();
         String userrole = userRole.getValue();
         
         UserRoleWrapper wrapper = new UserRoleWrapper(username, userrole);
         
         if (currentUser.equals(username))
         {
            wrapper.IsCurrentUser = true;
            currentUserWrapper = wrapper;
         }
         else
         {
            wrappers.add(wrapper);
         }
      }
      
      // sort list by username
      QuickSort sorter = new QuickSort(wrappers, "UserName", true, IDataContainer.SORT_CASEINSENSITIVE);
      sorter.sort();
      
      // if present, insert the current user to the top of the list
      if (currentUserWrapper != null)
      {
         wrappers.add(0, currentUserWrapper);
      }
      
      return wrappers;
   }
   
   /**
    * Build a list containing one item representing the current user role for the website.
    */
   private static List<UserRoleWrapper> buildCurrentUserRole(
         WebProjectService wpService, NodeRef webProjectRef, String username)
   {
      // build a list of wrappers to hold the fields we need for each user and role
      List<UserRoleWrapper> wrappers = new ArrayList<UserRoleWrapper>(0);
      String userrole = wpService.getWebUserRole(webProjectRef, username);
      if (userrole != null)
      {
         wrappers = new ArrayList<UserRoleWrapper>(1);

         UserRoleWrapper wrapper = new UserRoleWrapper(username, userrole);
         wrapper.IsCurrentUser = true;
         wrappers.add(0, wrapper);
      }
      return wrappers;
   }

   /**
    * Render the list of user modified files/folders in the layered sandbox area.
    * 
    * @param fc         FacesContext
    * @param out        ResponseWriter
    * @param username   The username to render the modified files for
    * @param index      Index of the sandbox in the list of sandboxes
    * @param storeRoot  Root name of the store containing the users sandbox
    * 
    * @throws IOException
    */
   private void renderUserFiles(
         FacesContext fc, ResponseWriter out, String username, String storeRoot, int index)
         throws IOException
   {
      PermissionService permissionService = getPermissionService(fc);
      SandboxService sandboxService = getSandboxService(fc);
      
      DateFormat df = Utils.getDateTimeFormat(fc);
      ResourceBundle bundle = Application.getBundle(fc);
      
      // compare user sandbox to staging sandbox - filter by current webapp, include deleted items
      String userStore = AVMUtil.buildUserMainStoreName(storeRoot, username);
      String userStorePath = AVMUtil.buildStoreWebappPath(userStore, getWebapp());
      String stagingStore = AVMUtil.buildStagingStoreName(storeRoot);
      String stagingStorePath = AVMUtil.buildStoreWebappPath(stagingStore, getWebapp());
      
      long start = System.currentTimeMillis();
      
      List<AssetInfo> assets = sandboxService.listChangedWebApp(userStore, getWebapp(), true);
      
      if (logger.isDebugEnabled())
      {
          logger.debug("List "+assets.size()+" changes webapp in "+(System.currentTimeMillis()-start)+" msecs");
      }
      
      if (assets.size() != 0)
      {
         start = System.currentTimeMillis();
          
         // output confict header, only if conflicts exist
         int diffCount = 0;
         for (AssetInfo asset : assets)
         {
            if (asset.getDiffCode() == AVMDifference.CONFLICT)
            {
               diffCount++;
            }
         }
          
         if (diffCount > 0)
         {
            out.write("<table cellspacing=\"0\" cellpadding=\"2\"");
            out.write(" class='conflictItemsList' width=\"100%\"");
            out.write(">");
            out.write("<tr width=\"100%\">");
            out.write("<td width=\"100%\">");
      
            out.write(Utils.buildImageTag(fc, CONFLICTED_ICON, 16, 16, "", null, "-25%"));
            out.write("&nbsp;");
            out.write(MessageFormat.format(bundle.getString(MSG_COUNT_CONFLICTED_ITEMS), diffCount));
          
            out.write("</td>");
            out.write("<td>");
            Utils.encodeRecursive(fc, createRevertAllItemsButton(fc, bundle, username, userStorePath, stagingStorePath));
            out.write("</td>");
            out.write("</tr>");
            out.write("</table>");
            out.write("<div style='padding:2px'></div>");
         }
          
         // info we need to calculate preview paths for assets
         int rootPathIndex = AVMUtil.buildSandboxRootPath(userStore).length();
         
         // get the UIActions component responsible for rendering context related user actions
         // TODO: we may need a component per user instance? (or use evaluators for roles...)
         UIActions uiFileActions = aquireUIActions(ACTIONS_FILE, userStore);
         UIActions uiFolderActions = aquireUIActions(ACTIONS_FOLDER, userStore);
         UIActions uiDeletedActions = aquireUIActions(ACTIONS_DELETED, userStore);
         
         String id = getClientId(fc);
         
         // TODO - refactor to AssetInfo and getSelected calls ... etc
         List<AVMNodeDescriptor> nodes = new ArrayList<AVMNodeDescriptor>(assets.size());
         for (AssetInfo asset : assets)
         {
             // TODO
             nodes.add(((AssetInfoImpl)asset).getAVMNodeDescriptor());
         }
         // store lookup of username to list of modified nodes
         this.userNodes.put(username, nodes);
         
         // output the table of modified items
         // TODO: apply tag style - removed hardcoded
         out.write("<table class='modifiedItemsList' cellspacing=0 cellpadding=2 border=0 width=100%>");
         
         // output multi-select actions for this user
         out.write("<tr><td colspan=8>");
         out.write(bundle.getString(MSG_SELECTED));
         out.write(":&nbsp;&nbsp;");
         NodeRef userStoreRef = AVMNodeConverter.ToNodeRef(-1, AVMUtil.buildSandboxRootPath(userStore));
         if (permissionService.hasPermission(userStoreRef, PermissionService.WRITE) == AccessStatus.ALLOWED ||
             permissionService.hasPermission(userStoreRef, PermissionService.ADD_CHILDREN) == AccessStatus.ALLOWED)
         {
            Utils.encodeRecursive(fc, aquireAction(
                  fc, userStore, username, ACT_SANDBOX_SUBMITSELECTED, "/images/icons/submit_all.gif",
                  "#{AVMBrowseBean.setupSandboxAction}", "dialog:submitSandboxItems"));
            out.write("&nbsp;&nbsp;");
            Utils.encodeRecursive(fc, aquireAction(
                  fc, userStore, username, ACT_SANDBOX_REVERTSELECTED, "/images/icons/revert_all.gif",
                  "#{AVMBrowseBean.setupSandboxAction}", "dialog:revertSelectedItems"));
         }
         out.write("</td></tr>");
         
         // header row
         out.write("<tr align=left><th>");
         // multi-select checkbox
         out.write("<input type='checkbox' value='");
         out.write(Integer.toString(index));
         out.write("' onclick='");
         out.write("javascript:_sb_select(this);");
         out.write("'></th><th></th><th></th><th></th><th>");
         out.write(bundle.getString(MSG_NAME));
         out.write("</th><th>");
         out.write(bundle.getString(MSG_CREATED));
         out.write("</th><th>");
         out.write(bundle.getString(MSG_MODIFIED));
         out.write("</th><th>");
         out.write(bundle.getString(MSG_SIZE));
         out.write("</th><th>");
         out.write(bundle.getString(MSG_ACTIONS));
         out.write("</th></tr>");
         
         // move conflicts to top of list
         if (diffCount > 0)
         {
	         List<AssetInfo> conflicted = new ArrayList<AssetInfo>();
	         
	         for(Iterator<AssetInfo> it = assets.iterator(); it.hasNext();)
	         {
	             AssetInfo diff = (AssetInfo) it.next();
	        	 if (diff.getDiffCode() == AVMDifference.CONFLICT)
	        	 {
	        		 conflicted.add(diff);
	        		 it.remove();
	        	 }
	         }
	         assets.addAll(0, conflicted);
         }
         
         // output each of the modified files as a row in the table
         int rowIndex = 0;
         for (AssetInfo node : assets)
         {
            // TODO: different display cases for diff.getDifferenceCode()?
            boolean isGhost = node.isDeleted();
            String sourcePath = node.getAvmPath();
            
            // output multi-select checkbox
            out.write("<tr");
            if (node.getDiffCode() == AVMDifference.CONFLICT)
            {
            	out.write(" class='conflictItem'");
            }
            out.write("><td><input type='checkbox' name='");
            out.write(id);
            out.write("' id='");
            out.write(id);
            out.write("' value='");
            // the value is a username index followed by a node lookup index
            out.write(Integer.toString(index) + '_' + Integer.toString(rowIndex++));
            out.write("'></td>");
            
            if (isGhost == false)
            {
               // icon and name of the file/folder - files are clickable to see the content
               String name = node.getName();
               String linkPrefix =
                     "<a href=\"" +
                     fc.getExternalContext().getRequestContextPath() +
                     DownloadContentServlet.generateBrowserURL(AVMNodeConverter.ToNodeRef(-1, sourcePath), name) +
                     "\" target='new'>";
               
               if (node.isFile())
               {
            	   out.write("<td width=16>");
            	   if (node.getDiffCode() == AVMDifference.CONFLICT)
                   {
            		   out.write(Utils.buildImageTag(fc, CONFLICTED_ICON, 16, 16, ""));
                   }
                   out.write("</td><td width=20>");
            	   UIAVMLockIcon lockIcon = (UIAVMLockIcon)fc.getApplication().createComponent(UIAVMLockIcon.ALFRESCO_FACES_AVMLOCKICON);
                   lockIcon.setId("avmlock_" + Integer.toString(rowIndex));
                   lockIcon.setValue(node);
                   Utils.encodeRecursive(fc, lockIcon);
                   out.write("</td><td width=16>");
                   out.write(linkPrefix);
                   out.write(Utils.buildImageTag(fc, FileTypeImageUtils.getFileTypeImage(fc, name, true), ""));
                   out.write("</a>");
                   out.write("</td><td>");
                   out.write(linkPrefix);
                   out.write(Utils.encode(name));
                   out.write("</a>");
               }
               else
               {
                  out.write("<td width=16></td>");
                  out.write("<td width=20></td>");
            	  out.write("<td width=16>");
                  out.write(Utils.buildImageTag(fc, SPACE_ICON, 16, 16, ""));
                  out.write("</td><td>");
                  out.write(Utils.encode(name));
               }
               out.write("</td><td>");
               
               // created date
               out.write(df.format(node.getCreatedDate()));
               out.write("</td><td>");
               
               // modified date
               out.write(df.format(node.getModifiedDate()));
               out.write("</td><td>");
               
               // build node context required for actions
               AVMNode avmNode = new AVMNode(node);
               String assetPath = sourcePath.substring(rootPathIndex);
               String previewUrl = AVMUtil.getPreviewURI(userStore, JNDIConstants.DIR_DEFAULT_WWW_APPBASE + assetPath);
               avmNode.getProperties().put("previewUrl", previewUrl);
               avmNode.getProperties().put("avmDiff", node.getDiffCode());
               
               // size of files
               if (node.isFile())
               {
                  out.write(getSizeConverter().getAsString(fc, this, node.getFileSize()));
                  out.write("</td><td>");
               
                  // add UI actions for this item
                  uiFileActions.setContext(avmNode);
                  Utils.encodeRecursive(fc, uiFileActions);
               }
               else
               {
                  out.write("</td><td>");
               
                  // add UI actions for this item
                  uiFolderActions.setContext(avmNode);
                  Utils.encodeRecursive(fc, uiFolderActions);
               }
            }
            else
            {
               // must have been deleted from this sandbox - show as ghosted
               String name = node.getName();
               out.write("<td width=16></td>");
               out.write("<td width=20></td>");
               out.write("<td width=16>");
               if (node.isFile() && node.isDeleted())
               {
                  out.write(Utils.buildImageTag(fc, FileTypeImageUtils.getFileTypeImage(fc, name, true), ""));
                  out.write("</td><td style='color:#aaaaaa'>");
                  out.write(Utils.encode(name) + " [" + bundle.getString(MSG_DELETED_ITEM) + "]");
                  out.write("</a>");
               }
               else
               {
                  out.write(Utils.buildImageTag(fc, SPACE_ICON, 16, 16, ""));
                  out.write("</td><td style='color:#aaaaaa'>");
                  out.write(Utils.encode(name) + " [" + bundle.getString(MSG_DELETED_ITEM) + "]");
               }
               out.write("</td><td style='color:#aaaaaa'>");
               
               // created date
               out.write(df.format(node.getCreatedDate()));
               out.write("</td><td style='color:#aaaaaa'>");
               
               // modified date
               out.write(df.format(node.getModifiedDate()));
               out.write("</td><td style='color:#aaaaaa'>");
               
               // size of files
               if (node.isFile())
               {
                  out.write(getSizeConverter().getAsString(fc, this, node.getFileSize()));
               }
               out.write("</td><td style='color:#aaaaaa'>");
               
               // deleted UI actions for this item
               uiDeletedActions.setContext(new AVMNode(node));
               Utils.encodeRecursive(fc, uiDeletedActions);
            }
            out.write("</td></tr>");
         }
         
         // end table
         out.write("</table>");
         
         if (logger.isDebugEnabled())
         {
             logger.debug("Wrote table in "+(System.currentTimeMillis()-start)+" msecs");
         }
      }
      else
      {
         // output "no modified files found" message
         out.write("<div style='padding-left:16px'><i>");
         out.write(bundle.getString(MSG_NO_MODIFIED_ITEMS));
         out.write("</i></div>");
      }
   }
   
   /**
    * Render the list of content forms available for this sandbox.
    * 
    * @param fc         FacesContext
    * @param out        ResponseWriter
    * 
    * @throws IOException
    */
   @SuppressWarnings("unchecked")
   private void renderContentForms(
         FacesContext fc, ResponseWriter out, NodeRef websiteRef, String username, String storeRoot)
         throws IOException
   {
      Map requestMap = fc.getExternalContext().getRequestMap();
      String userStorePrefix = AVMUtil.buildUserMainStoreName(storeRoot, username);
      
      // only need to collect the list of forms once per render
      // TODO: execute permission evaluations on a per user basis against each form?
      if (this.forms == null)
      {
         this.forms = new WebProject(websiteRef).getForms();
      }
      
      ResourceBundle bundle = Application.getBundle(fc);
      if (this.forms.size() != 0)
      {
         // output the table of available forms
         // TODO: apply tag style - removed hardcoded
         out.write("<table class='modifiedItemsList' cellspacing='2' cellpadding='1' border='0' width='100%'>");
         
         // header row
         out.write("<tr align='left'><th>");
         out.write(bundle.getString(MSG_NAME));
         out.write("</th><th>");
         out.write(bundle.getString(MSG_TITLE));
         out.write("</th><th>");
         out.write(bundle.getString(MSG_DESCRIPTION));
         out.write("</th><th>");
         out.write(bundle.getString(MSG_ACTIONS));
         out.write("</th></tr>");
         
         for (Form f : this.forms)
         {
            out.write("<tr><td>");
            String name = f.getName();
            out.write(name != null ? Utils.encode(name) : "");
            out.write("</td><td>");
            String title= f.getTitle();
            out.write(title != null ? Utils.encode(title) : "");
            out.write("</td><td>");
            String desc = f.getDescription();
            out.write(desc != null ? Utils.encode(desc) : "");
            out.write("</td><td>");
            
            // set the form-id into the request scope for actions data binding
            requestMap.put(REQUEST_FORM_REF, f);
            
            // actions
            UIActionLink action = findAction(ACT_CREATE_FORM_CONTENT, userStorePrefix);
            if (action == null)
            {
               // create content action passes the ID of the Form to uses
               Map<String, String> params = new HashMap<String, String>(4, 1.0f);
               // setup a data-binding param for the Form ID
               params.put(PARAM_FORM_NAME, "#{" + REQUEST_FORM_REF + ".name}");
               params.put("username", username);
               params.put("store", userStorePrefix);
               action = createAction(fc,
                                     userStorePrefix,
                                     username,
                                     ACT_CREATE_FORM_CONTENT,
                                     "/images/icons/new_content.gif",
                                     "#{AVMBrowseBean.createFormContent}",
                                     null, null, params, true);
            }
            Utils.encodeRecursive(fc, action);
            
            out.write("&nbsp;&nbsp;");
            
            action = findAction(ACT_FIND_FORM_CONTENT, userStorePrefix);
            if (action == null)
            {
               // create content action passes the ID of the Form to uses
               Map<String, String> params = new HashMap<String, String>(4, 1.0f);
               // setup a data-binding param for the Form ID
               params.put(PARAM_FORM_NAME, "#{" + REQUEST_FORM_REF + ".name}");
               params.put("username", username);
               params.put("store", userStorePrefix);
               action = createAction(fc,
                                     userStorePrefix,
                                     username,
                                     ACT_FIND_FORM_CONTENT,
                                     "/images/icons/search_icon.gif",
                                     "#{AVMBrowseBean.searchFormContent}",
                                     "browseSandbox",
                                     null, params, true);
            }
            Utils.encodeRecursive(fc, action);
            
            requestMap.remove(REQUEST_FORM_REF);
            
            out.write("</td></tr>");
         }
         
         out.write("</table>");
      }
      else
      {
         // output "no web forms" message
         out.write("<div style='padding-left:16px'><i>");
         out.write(bundle.getString(MSG_NO_WEB_FORMS));
         out.write("</i></div>");
      }
   }
   
   /**
    * @return Byte size converter
    */
   private ByteSizeConverter getSizeConverter()
   {
      if (this.sizeConverter == null)
      {
         this.sizeConverter = new ByteSizeConverter();
      }
      return this.sizeConverter;
   }
   
   /**
    * Aquire the UIActions component for the specified action group ID.
    * Search for the component in the child list or create as needed. 
    * 
    * @param id      ActionGroup id of the UIActions component
    * 
    * @return UIActions component
    */
   @SuppressWarnings("unchecked")
   private UIActions aquireUIActions(String id, String store)
   {
      UIActions uiActions = null;
      String componentId = id + '_' + FacesHelper.makeLegalId(store);
      if (logger.isDebugEnabled())
         logger.debug("Find UIActions component id: " + componentId);
      for (UIComponent component : (List<UIComponent>)getChildren())
      {
         if (componentId.equals(component.getId()))
         {
            if (logger.isDebugEnabled())
               logger.debug("...found UIActions component id: " + componentId);
            uiActions = (UIActions)component;
            break;
         }
      }
      if (uiActions == null)
      {
         if (logger.isDebugEnabled())
               logger.debug("...creating UIActions component id: " + componentId);
         javax.faces.application.Application facesApp = FacesContext.getCurrentInstance().getApplication();
         uiActions = (UIActions)facesApp.createComponent(COMPONENT_ACTIONS);
         uiActions.setShowLink(false);
         uiActions.getAttributes().put("styleClass", "inlineAction");
         uiActions.setId(componentId);
         uiActions.setParent(this);
         uiActions.setValue(id);
         
         this.getChildren().add(uiActions);
      }
      return uiActions;
   }
   
   /**
    * Aquire a UIActionLink component for the specified action
    * 
    * @param fc               FacesContext
    * @param store            Root store name for the user sandbox
    * @param username         Username of the user for the action
    * @param name             Action name - will be used for I18N message lookup
    * @param icon             Icon to display for the action
    * @param actionListener   Actionlistener for the action
    * @param outcome          Navigation outcome for the action
    * 
    * @return UIActionLink component
    */
   private UIActionLink aquireAction(FacesContext fc, String store, String username,
         String name, String icon, String actionListener, String outcome)
   {
      return aquireAction(fc, store, username, name, icon, actionListener, outcome, null, null);
   }
   
   private HtmlCommandButton createRevertAllItemsButton(FacesContext fc, ResourceBundle bundle, String name, String userStorePath, String stagingStorePath)
   {
	   javax.faces.application.Application facesApp = fc.getApplication();
	   String id = "revert_all_conflict" + new Date().getTime() + FacesHelper.makeLegalId(name);
	   HtmlCommandButton cb = (HtmlCommandButton) facesApp.createComponent(HtmlCommandButton.COMPONENT_TYPE);
	   cb.setId(id);
	   cb.setValue(bundle.getString(MSG_REVERT_ALL_CONFLICTS));
	   cb.setActionListener(facesApp.createMethodBinding(
			   "#{AVMBrowseBean.revertAllConflict}", UIActions.ACTION_CLASS_ARGS));
	   
	   UIParameter param = (UIParameter)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
       param.setId(id + "_1");
       param.setName("userStorePath");
       param.setValue(userStorePath);
       cb.getChildren().add(param);
       param = (UIParameter)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
       param.setId(id + "_2");
       param.setName("stagingStorePath");
       param.setValue(stagingStorePath);
       cb.getChildren().add(param);
	   
	   this.getChildren().add(cb);
	   return cb;
   }
   
   /**
    * Aquire a UIActionLink component for the specified action
    * 
    * @param fc               FacesContext
    * @param store            Root store name for the user sandbox
    * @param username         Username of the user for the action
    * @param name             Action name - will be used for I18N message lookup
    * @param icon             Icon to display for the action
    * @param actionListener   Actionlistener for the action
    * @param outcome          Navigation outcome for the action
    * @param url              HREF URL for the action
    * @param params           Parameters name/values for the action listener args
    * 
    * @return UIActionLink component
    */
   private UIActionLink aquireAction(FacesContext fc, String store, String username, String name,
         String icon, String actionListener, String outcome, String url, Map<String, String> params)
   {
      UIActionLink action = findAction(name, store);
      if (action == null)
      {
         action = createAction(fc, store, username, name, icon, actionListener, 
                               outcome, url, params, true);
      }
      return action;
   }
   
   /**
    * Locate a child UIActionLink component by name.
    * 
    * @param name       Of the action component to find
    * @param store      Store the action component is tied to
    * @param username   Username of the user owner of the action
    * 
    * @return UIActionLink component if found, else null if not created yet
    */
   @SuppressWarnings("unchecked")
   private UIActionLink findAction(String name, String store)
   {
      UIActionLink action = null;
      String actionId = name + '_' + FacesHelper.makeLegalId(store);
      if (logger.isDebugEnabled())
         logger.debug("Finding action Id: " + actionId);
      for (UIComponent component : (List<UIComponent>)getChildren())
      {
         if (actionId.equals(component.getId()))
         {
            action = (UIActionLink)component;
            if (logger.isDebugEnabled())
               logger.debug("...found action Id: " + actionId);
            break;
         }
      }
      return action;
   }
   
   /**
    * Create a UIActionLink child component.
    * 
    * @param fc               FacesContext
    * @param store            Root store name for the user sandbox
    * @param username         Username of the user for the action
    * @param name             Action name - will be used for I18N message lookup
    * @param icon             Icon to display for the action
    * @param actionListener   Actionlistener for the action
    * @param outcome          Navigation outcome for the action
    * @param url              HREF URL for the action
    * @param params           Parameters name/values for the action listener args
    * @param addAsChild       true to add the action as a child of this component
    * 
    * @return UIActionLink child component
    */
   @SuppressWarnings("unchecked")
   private UIActionLink createAction(FacesContext fc, String store, String username, String name,
         String icon, String actionListener, String outcome, String url, Map<String, String> params,
         boolean addAsChild)
   {
      javax.faces.application.Application facesApp = fc.getApplication();
      UIActionLink control = (UIActionLink)facesApp.createComponent(UIActions.COMPONENT_ACTIONLINK);
      
      String id = name + '_' + FacesHelper.makeLegalId(store);
      if (logger.isDebugEnabled())
         logger.debug("...creating action Id: " + id);
      control.setRendererType(UIActions.RENDERER_ACTIONLINK);
      control.setId(id);
      control.setValue(Application.getMessage(fc, name));
      //control.setShowLink(icon != null ? false : true);
      control.setImage(icon);
      
      if (actionListener != null)
      {
         control.setActionListener(facesApp.createMethodBinding(
               actionListener, UIActions.ACTION_CLASS_ARGS));
         
         // add the store and username as default action listener parameters
         if (params == null)
         {
            UIParameter param = (UIParameter)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
            param.setId(id + "_1");
            param.setName("store");
            param.setValue(store);
            control.getChildren().add(param);
            param = (UIParameter)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
            param.setId(id + "_2");
            param.setName("username");
            param.setValue(username);
            control.getChildren().add(param);
         }
         else
         {
            // if a specific set of parameters are supplied, then add them instead
            int idIndex = 1;
            for (String key : params.keySet())
            {
               UIParameter param = (UIParameter)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
               param.setId(id + '_' + Integer.toString(idIndex++));
               param.setName(key);
               String value = params.get(key);
               if (value.startsWith("#{") == true)
               {
                  ValueBinding vb = facesApp.createValueBinding(value);
                  param.setValueBinding("value", vb);
               }
               else
               {
                  param.setValue(params.get(key));
               }
               control.getChildren().add(param);
            }
         }
      }
      if (outcome != null)
      {
         control.setAction(new ConstantMethodBinding(outcome));
      }
      if (url != null)
      {
         if (url.startsWith("#{") == true)
         {
            ValueBinding vb = facesApp.createValueBinding(url);
            control.setValueBinding("href", vb);
         }
         else
         {
            control.setHref(url);
         }
         control.setTarget("new");
      }
      
      if (addAsChild)
      {
         this.getChildren().add(control);
      }
      
      return control;
   }
   
   /**
    * Locate a child UIMenu component by name.
    * 
    * @param store      Store the action component is tied to
    * 
    * @return UIMenu component if found, else null if not created yet
    */
   @SuppressWarnings("unchecked")
   private UIMenu findMenu(String store)
   {
      UIMenu menu = null;
      String menuId = "menu_" + FacesHelper.makeLegalId(store);
      
      if (logger.isDebugEnabled())
         logger.debug("Finding action Id: " + menuId);
      
      for (UIComponent component : (List<UIComponent>)getChildren())
      {
         if (menuId.equals(component.getId()))
         {
            menu = (UIMenu)component;
            
            if (logger.isDebugEnabled())
               logger.debug("...found action Id: " + menuId);
            
            break;
         }
      }
      
      return menu;
   }
   
   /**
    * Creates a menu component to hold the 'more actions' for a sandbox
    * 
    * @param context FacesContext
    * @param store The store to create the menu for
    * @return The UIMenu component (with no children)
    */
   @SuppressWarnings("unchecked")
   private UIMenu createMenu(FacesContext context, String store)
   {
      UIMenu menu = (UIMenu)context.getApplication().createComponent("org.alfresco.faces.Menu");
      
      String id = "menu_" + FacesHelper.makeLegalId(store);
      menu.setId(id);
      menu.setLabel(Application.getMessage(context, "more_actions") + " ");
      menu.getAttributes().put("itemSpacing", 4);
      menu.getAttributes().put("image", "/images/icons/menu.gif");
      menu.getAttributes().put("menuStyleClass", "moreActionsMenu");
      menu.getAttributes().put("style", "white-space:nowrap; margin-left: 4px; margin-right: 6px;");
      
      return menu;
   }
   
   private WebProjectService getWebProjectService(FacesContext fc)
   {
      return Repository.getServiceRegistry(fc).getWebProjectService();
   }
   
   private NodeService getNodeService(FacesContext fc)
   {
      return Repository.getServiceRegistry(fc).getNodeService();
   }
   
   private PermissionService getPermissionService(FacesContext fc)
   {
      return Repository.getServiceRegistry(fc).getPermissionService();
   }
   
   private SandboxService getSandboxService(FacesContext fc)
   {
      return Repository.getServiceRegistry(fc).getSandboxService();
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors

   /**
    * Returns the NodeRef to the website to show the sandboxes for
    *
    * @return The website NodeRef instance
    */
   public NodeRef getValue()
   {
      ValueBinding vb = getValueBinding("value");
      if (vb != null)
      {
         this.value = (NodeRef)vb.getValue(getFacesContext());
      }
      
      return this.value;
   }
   
   /**
    * Sets the NodeRef to the website to show the sandboxes for
    *
    * @param value   The NodeRef to the website to show the sandboxes for
    */
   public void setValue(NodeRef value)
   {
      this.value = value;
   }
   
   /**
    * @return Returns the webapp to filter file list by
    */
   public String getWebapp()
   {
      ValueBinding vb = getValueBinding("webapp");
      if (vb != null)
      {
         this.webapp = (String)vb.getValue(getFacesContext());
      }
      
      return this.webapp;
   }

   /**
    * @param webapp  The webapp to filter file list by
    */
   public void setWebapp(String webapp)
   {
      this.webapp = webapp;
   }

   /**
    * Get the selected nodes for a specified sandbox user
    * 
    * @param username   User in the user sandbox list
    * 
    * @return List of AVMNodeDescriptor object representing the selected items
    */
   public List<AVMNodeDescriptor> getSelectedNodes(String username)
   {
      return getSelectedNodes(username, this.rowToUserLookup.get(username));
   }
   
   /**
    * Get the selected nodes for a specified sandbox index
    * 
    * @param sandbox    Index of sandbox in the user sandbox list
    * 
    * @return List of AVMNodeDescriptor object representing the selected items
    */
   public List<AVMNodeDescriptor> getSelectedNodes(int sandbox)
   {
      return getSelectedNodes(this.userToRowLookup.get(sandbox), sandbox);
   }
   
   private List<AVMNodeDescriptor> getSelectedNodes(String username, int sandbox)
   {
      List<AVMNodeDescriptor> nodes = null;
      
      if (username != null && this.checkedItems != null)
      {
         List<AVMNodeDescriptor> paths = this.userNodes.get(username);
         if (paths != null)
         {
            nodes = new ArrayList<AVMNodeDescriptor>(paths.size());
            String sandboxPrefix = Integer.toString(sandbox) + '_';
            
            // check against the selected items
            for (int i=0; i<this.checkedItems.length; i++)
            {
               String value = checkedItems[i];
               if (value.startsWith(sandboxPrefix))
               {
                  int pathIndex = Integer.valueOf(value.substring(sandboxPrefix.length()));
                  nodes.add(paths.get(pathIndex));
               }
            }
         }
      }
      
      return nodes;
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class representing a user of the website and their role.
    */
   public static class UserRoleWrapper
   {
      UserRoleWrapper(String username, String userrole)
      {
         UserName = username;
         UserRole = userrole;
      }
      
      /**
       * Public accessor to user by sorting class
       * 
       * @return User Name
       */
      public String getUserName()
      {
         return UserName;
      }
      
      String UserName;
      String UserRole;
      boolean IsCurrentUser = false;
   }
}
