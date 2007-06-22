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
package org.alfresco.web.ui.wcm.component;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.transaction.UserTransaction;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.NameMatcher;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.AVMNode;
import org.alfresco.web.bean.wcm.WebProject;
import org.alfresco.web.config.ClientConfigElement;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.forms.Form;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.ConstantMethodBinding;
import org.alfresco.web.ui.common.PanelGenerator;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.converter.ByteSizeConverter;
import org.alfresco.web.ui.repo.component.UIActions;
import org.alfresco.web.ui.wcm.WebResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

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
public class UIUserSandboxes extends SelfRenderingComponent
{
   private static Log logger = LogFactory.getLog(UIUserSandboxes.class);
   
   private static final String ACT_CREATE_FORM_CONTENT = "create_form_content";
   private static final String ACT_SANDBOX_REVERTSELECTED = "sandbox_revertselected";
   private static final String ACT_SANDBOX_SUBMITSELECTED = "sandbox_submitselected";
   private static final String ACT_SANDBOX_CHECK_LINKS = "check_links";
   private static final String ACT_SANDBOX_BROWSE = "sandbox_browse";
   private static final String ACT_SANDBOX_REVERTALL = "sandbox_revertall";
   private static final String ACT_SANDBOX_SUBMITALL = "sandbox_submitall";
   private static final String ACT_SANDBOX_PREVIEW = "sandbox_preview";
   private static final String ACT_SANDBOX_ICON = "sandbox_icon";
   private static final String ACT_REMOVE_SANDBOX = "sandbox_remove";
   private static final String ACT_SANDBOX_REFRESH = "sandbox_refresh";
   
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
   private static final String MSG_DESCRIPTION = "description";
   private static final String MSG_MODIFIED = "modified_date";
   private static final String MSG_ACTIONS = "actions";
   private static final String MSG_DELETED_ITEM = "avm_node_deleted";
   private static final String MSG_SELECTED = "selected";
   private static final String MSG_NO_MODIFIED_ITEMS = "sandbox_no_modified_items";
   private static final String MSG_NO_WEB_FORMS = "sandbox_no_web_forms";
   private static final String MSG_MY_SANDBOX = "sandbox_my_sandbox";
   
   private static final String REQUEST_FORM_REF = "formref";
   private static final String REQUEST_PREVIEW_REF = "prevhref";
   
   private static final String SPACE_ICON = "/images/icons/" + BrowseBean.SPACE_SMALL_DEFAULT + ".gif";
   
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
   private ByteSizeConverter sizeConverter = null;
   
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
      AVMService avmService = getAVMService(context);
      NodeService nodeService = getNodeService(context);
      PermissionService permissionService = getPermissionService(context);
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
         tx.begin();
         
         NodeRef websiteRef = getValue();
         if (websiteRef == null)
         {
            throw new IllegalArgumentException("Website NodeRef must be specified.");
         }
         String storeRoot = (String)nodeService.getProperty(websiteRef, WCMAppModel.PROP_AVMSTORE);
         
         // find out the current user role in the web project
         List<ChildAssociationRef> userInfoRefs = nodeService.getChildAssocs(
            websiteRef, WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
         User currentUser = Application.getCurrentUser(context);
         String currentUserName = currentUser.getUserName();
         String currentUserRole = getWebProjectUserRole(nodeService, websiteRef, currentUser, userInfoRefs);
         
         // sort the user list alphabetically and insert the current user at the top of the list 
         List<UserRoleWrapper> userRoleWrappers = buildSortedUserRoles(nodeService, currentUserName, userInfoRefs);
         
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
            
            // build the name of the main store for this user
            String mainStore = AVMUtil.buildUserMainStoreName(storeRoot, username);
            
            // check it exists before we render the view
            if (avmService.getStore(mainStore) != null)
            {
               // check the permissions on this store for the current user
               if (logger.isDebugEnabled())
                     logger.debug("Checking user role to view store: " + mainStore);
               if (currentUserName.equals(username) ||
                   AVMUtil.ROLE_CONTENT_MANAGER.equals(currentUserRole) ||
                   AVMUtil.ROLE_CONTENT_PUBLISHER.equals(currentUserRole))
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Building sandbox view for user store: " + mainStore);
                  
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
                     out.write(username);
                  }
                  else
                  {
                     out.write("<b>");
                     out.write(bundle.getString(MSG_MY_SANDBOX));
                     out.write("</b>");
                  }
                  out.write(" (");
                  out.write(bundle.getString(userrole));
                  out.write(")</td><td><nobr>");
                  
                  // Direct actions for a sandbox...
                  Map<String, String> params = new HashMap<String, String>(3);
                  params.put("store", mainStore);
                  params.put("username", username);
                  params.put("webapp", this.getWebapp());
                  params.put("mode", "runReport");
                  params.put("compareToStaging", "true");
                  Utils.encodeRecursive(context, aquireAction(
                        context, mainStore, username, ACT_SANDBOX_CHECK_LINKS, "/images/icons/run_link_validation.gif",
                        "#{DialogManager.setupParameters}", "dialog:linkValidation", null, params));
                  out.write("&nbsp;&nbsp;");
                  
                  // Browse Sandbox
                  Utils.encodeRecursive(context, aquireAction(
                        context, mainStore, username, ACT_SANDBOX_BROWSE, "/images/icons/space_small.gif",
                        "#{AVMBrowseBean.setupSandboxAction}", "browseSandbox"));
                  out.write("&nbsp;&nbsp;");
                  
                  // Preview Website
                  String websiteUrl = AVMUtil.buildWebappUrl(mainStore, getWebapp());
                  Map requestMap = context.getExternalContext().getRequestMap();
                  requestMap.put(REQUEST_PREVIEW_REF, websiteUrl);
                  Utils.encodeRecursive(context, aquireAction(
                        context, mainStore, username, ACT_SANDBOX_PREVIEW, "/images/icons/preview_website.gif",
                        null, null, "#{" + REQUEST_PREVIEW_REF + "}", null));
                  requestMap.remove(REQUEST_PREVIEW_REF);
                  out.write("&nbsp;&nbsp;");
                  
                  // Refresh Sandbox
                  Utils.encodeRecursive(context, aquireAction(
                        context, mainStore, username, ACT_SANDBOX_REFRESH, "/images/icons/reset.gif",
                        "#{AVMBrowseBean.refreshSandbox}", null));
                  out.write("&nbsp;&nbsp;");
                  
                  // Submit All Items
                  // NOTE: removed for 2.0 final
                  /*Utils.encodeRecursive(context, aquireAction(
                        context, mainStore, username, ACT_SANDBOX_SUBMITALL, "/images/icons/submit_all.gif",
                        "#{AVMBrowseBean.setupAllItemsAction}", "dialog:submitSandboxItems"));
                  out.write("&nbsp;&nbsp;");*/
                  
                  // Revert All Items
                  Utils.encodeRecursive(context, aquireAction(
                        context, mainStore, username, ACT_SANDBOX_REVERTALL, "/images/icons/revert_all.gif",
                        "#{AVMBrowseBean.setupAllItemsAction}", "dialog:revertAllItems"));
                  out.write("&nbsp;&nbsp;");
                  
                  // Delete Sandbox
                  if (AVMUtil.ROLE_CONTENT_MANAGER.equals(currentUserRole))
                  {
                     Utils.encodeRecursive(context, aquireAction(
                           context, mainStore, username, ACT_REMOVE_SANDBOX, "/images/icons/delete_sandbox.gif",
                           "#{AVMBrowseBean.setupSandboxAction}", "dialog:deleteSandbox"));
                  }
                  
                  out.write("</nobr></td></tr>");
                  
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
                  if (index++ < userInfoRefs.size() - 1)
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
         NodeService nodeService, String currentUser, List<ChildAssociationRef> userInfoRefs)
   {
      // build a list of wrappers to hold the fields we need for each user and role
      UserRoleWrapper currentUserWrapper = null;
      List<UserRoleWrapper> wrappers = new LinkedList<UserRoleWrapper>();
      for (ChildAssociationRef ref : userInfoRefs)
      {
         NodeRef userInfoRef = ref.getChildRef();
         String username = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
         String userrole = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);
         
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
    * @return the role of this user in the current Web Project, or null for no assigned role
    */
   private static String getWebProjectUserRole(
         NodeService nodeService, NodeRef websiteRef, User currentUser, List<ChildAssociationRef> userInfoRefs)
   {
      String userrole = null;
      
      if (currentUser.isAdmin())
      {
         // fake the Content Manager role for an admin user
         userrole = AVMUtil.ROLE_CONTENT_MANAGER;
      }
      else
      {
         for (ChildAssociationRef ref : userInfoRefs)
         {
            NodeRef userInfoRef = ref.getChildRef();
            String username = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
            String role = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);
            if (currentUser.getUserName().equals(username))
            {
               userrole = role;
               break;
            }
         }
      }
      
      return userrole;
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
      AVMSyncService avmSyncService = getAVMSyncService(fc);
      AVMService avmService = getAVMService(fc);
      PermissionService permissionService = getPermissionService(fc);
      
      DateFormat df = Utils.getDateTimeFormat(fc);
      ResourceBundle bundle = Application.getBundle(fc);
      
      // build the paths to the stores to compare - filter by current webapp
      String userStore = AVMUtil.buildUserMainStoreName(storeRoot, username);
      String userStorePath = AVMUtil.buildStoreWebappPath(userStore, getWebapp());
      String stagingStore = AVMUtil.buildStagingStoreName(storeRoot);
      String stagingStorePath = AVMUtil.buildStoreWebappPath(stagingStore, getWebapp());
      
      // use the sync service to get the list of diffs between the stores
      NameMatcher matcher = (NameMatcher)FacesContextUtils.getRequiredWebApplicationContext(fc).getBean(
            "globalPathExcluder");
      List<AVMDifference> diffs = avmSyncService.compare(-1, userStorePath, -1, stagingStorePath, matcher);
      if (diffs.size() != 0)
      {
         // info we need to calculate preview paths for assets
         String dns = AVMUtil.lookupStoreDNS(userStore);
         int rootPathIndex = AVMUtil.buildSandboxRootPath(userStore).length();
         ClientConfigElement config = Application.getClientConfig(fc);
         
         // get the UIActions component responsible for rendering context related user actions
         // TODO: we may need a component per user instance? (or use evaluators for roles...)
         UIActions uiFileActions = aquireUIActions(ACTIONS_FILE, userStore);
         UIActions uiFolderActions = aquireUIActions(ACTIONS_FOLDER, userStore);
         UIActions uiDeletedActions = aquireUIActions(ACTIONS_DELETED, userStore);
         
         String id = getClientId(fc);
         
         // store lookup of username to list of modified nodes
         List<AVMNodeDescriptor> nodes = new ArrayList<AVMNodeDescriptor>(diffs.size());
         this.userNodes.put(username, nodes);
         
         // output the table of modified items
         // TODO: apply tag style - removed hardcoded
         out.write("<table class='modifiedItemsList' cellspacing=2 cellpadding=1 border=0 width=100%>");
         
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
         out.write("'></th><th width=16></th><th>");
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
         
         // output each of the modified files as a row in the table
         int rowIndex = 0;
         for (AVMDifference diff : diffs)
         {
            // TODO: different display cases for diff.getDifferenceCode()?
            boolean isGhost = false;
            String sourcePath = diff.getSourcePath();
            AVMNodeDescriptor node = avmService.lookup(-1, sourcePath);
            if (node == null)
            {
               // may have been deleted from this sandbox - which is a ghost node
               node = avmService.lookup(-1, diff.getSourcePath(), true);
               isGhost = true;
            }
            
            // handle missing node case by skipping the row rendering
            if (node == null)
            {
               continue;
            }
            
            // save reference to this node for multi-select action lookup later
            nodes.add(node);
            
            // output multi-select checkbox
            out.write("<tr><td><input type='checkbox' name='");
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
               out.write("<td width=16>");
               if (node.isFile())
               {
                  out.write(linkPrefix);
                  out.write(Utils.buildImageTag(fc, Utils.getFileTypeImage(fc, name, true), ""));
                  out.write("</a></td><td>");
                  out.write(linkPrefix);
                  out.write(name);
                  UIAVMLockIcon lockIcon = (UIAVMLockIcon)fc.getApplication().createComponent(
                        UIAVMLockIcon.ALFRESCO_FACES_AVMLOCKICON);
                  lockIcon.setId("lock_" + FacesHelper.makeLegalId(name));
                  lockIcon.setValue(sourcePath);
                  Utils.encodeRecursive(fc, lockIcon);
                  out.write("</a>");
               }
               else
               {
                  out.write(Utils.buildImageTag(fc, SPACE_ICON, 16, 16, ""));
                  out.write("</td><td>");
                  out.write(name);
               }
               out.write("</td><td>");
               
               // created date
               out.write(df.format(new Date(node.getCreateDate())));
               out.write("</td><td>");
               
               // modified date
               out.write(df.format(new Date(node.getModDate())));
               out.write("</td><td>");
               
               // build node context required for actions
               AVMNode avmNode = new AVMNode(node);
               String assetPath = sourcePath.substring(rootPathIndex);
               String previewUrl = AVMUtil.buildAssetUrl(
                     assetPath, config.getWCMDomain(), config.getWCMPort(), dns);
               avmNode.getProperties().put("previewUrl", previewUrl);
               
               // size of files
               if (node.isFile())
               {
                  out.write(getSizeConverter().getAsString(fc, this, node.getLength()));
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
               out.write("<td width=16>");
               if (node.isDeletedFile())
               {
                  out.write(Utils.buildImageTag(fc, Utils.getFileTypeImage(fc, name, true), ""));
                  out.write("</td><td style='color:#aaaaaa'>");
                  out.write(name + " [" + bundle.getString(MSG_DELETED_ITEM) + "]");
                  out.write("</a>");
               }
               else
               {
                  out.write(Utils.buildImageTag(fc, SPACE_ICON, 16, 16, ""));
                  out.write("</td><td style='color:#aaaaaa'>");
                  out.write(name + " [" + bundle.getString(MSG_DELETED_ITEM) + "]");
               }
               out.write("</td><td style='color:#aaaaaa'>");
               
               // created date
               out.write(df.format(new Date(node.getCreateDate())));
               out.write("</td><td style='color:#aaaaaa'>");
               
               // modified date
               out.write(df.format(new Date(node.getModDate())));
               out.write("</td><td style='color:#aaaaaa'>");
               
               // size of files
               if (node.isFile())
               {
                  out.write(getSizeConverter().getAsString(fc, this, node.getLength()));
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
   private void renderContentForms(
         FacesContext fc, ResponseWriter out, NodeRef websiteRef, String username, String storeRoot)
         throws IOException
   {
      NodeService nodeService = getNodeService(fc);
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
         out.write(bundle.getString(MSG_DESCRIPTION));
         out.write("</th><th>");
         out.write(bundle.getString(MSG_ACTIONS));
         out.write("</th></tr>");
         
         for (Form f : this.forms)
         {
            out.write("<tr><td>");
            String title = (String)f.getTitle();
            out.write(title != null ? title : "");
            out.write("</td><td>");
            String desc = (String)f.getDescription();
            out.write(desc != null ? desc : "");
            out.write("</td><td><nobr>");
            // actions
            UIActionLink action = findAction(ACT_CREATE_FORM_CONTENT, userStorePrefix);
            if (action == null)
            {
               // create content action passes the ID of the Form to uses
               Map<String, String> params = new HashMap<String, String>(3, 1.0f);
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
                                     null, 
                                     null, 
                                     params);
            }
            // set the form-id into the request scope for data binding
            requestMap.put(REQUEST_FORM_REF, f);
            Utils.encodeRecursive(fc, action);
            requestMap.remove(REQUEST_FORM_REF);
            out.write("</nobr></td></tr>");
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
   private UIActions aquireUIActions(String id, String store)
   {
      UIActions uiActions = null;
      String componentId = id + '_' + store;
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
         action = createAction(fc, store, username, name, icon, actionListener, outcome, url, params);
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
    * @param icon             Icon to display for the actio n
    * @param actionListener   Actionlistener for the action
    * @param outcome          Navigation outcome for the action
    * @param url              HREF URL for the action
    * @param params           Parameters name/values for the action listener args
    * 
    * @return UIActionLink child component
    */
   private UIActionLink createAction(FacesContext fc, String store, String username, String name,
         String icon, String actionListener, String outcome, String url, Map<String, String> params)
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
      
      this.getChildren().add(control);
      
      return control;
   }
   
   private AVMService getAVMService(FacesContext fc)
   {
      return (AVMService)FacesContextUtils.getRequiredWebApplicationContext(fc).getBean("AVMService");
   }
   
   private NodeService getNodeService(FacesContext fc)
   {
      return Repository.getServiceRegistry(fc).getNodeService();
   }
   
   private AVMSyncService getAVMSyncService(FacesContext fc)
   {
      return (AVMSyncService)FacesContextUtils.getRequiredWebApplicationContext(fc).getBean("AVMSyncService");
   }
   
   private PermissionService getPermissionService(FacesContext fc)
   {
      return Repository.getServiceRegistry(fc).getPermissionService();
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
