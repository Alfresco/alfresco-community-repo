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
package org.alfresco.web.ui.wcm.component;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.alfresco.web.bean.wcm.AVMNode;
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
 * @author Kevin Roast
 */
public class UIUserSandboxes extends SelfRenderingComponent
{
   private static Log logger = LogFactory.getLog(UIUserSandboxes.class);
   
   private static final String ACTIONS_FILE = "avm_file_modified";
   private static final String ACTIONS_FOLDER = "avm_folder_modified";
   private static final String ACTIONS_DELETED = "avm_deleted_modified";
   
   private static final String COMPONENT_ACTIONS = "org.alfresco.faces.Actions";
   
   private static final String MSG_MODIFIED_ITEMS = "modified_items";
   private static final String MSG_SIZE = "size";
   private static final String MSG_CREATED = "created_date";
   private static final String MSG_USERNAME = "username";
   private static final String MSG_NAME = "name";
   private static final String MSG_DESCRIPTION = "description";
   private static final String MSG_MODIFIED = "modified_date";
   private static final String MSG_ACTIONS = "actions";
   private static final String MSG_DELETED_ITEM = "avm_node_deleted";
   
   private static final String SPACE_ICON = "/images/icons/" + BrowseBean.SPACE_SMALL_DEFAULT + ".gif";
   
   /** website to show sandboxes for */
   private NodeRef value;
   
   private ByteSizeConverter sizeConverter = null;
   
   private Set<String> expandedPanels = new HashSet<String>();
   
   
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
   }
   
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[3];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.value;
      values[2] = this.expandedPanels;
      return values;
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
      String fieldId = getClientId(context);
      String value = (String)requestMap.get(fieldId);
      
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
      
      ResourceBundle bundle = Application.getBundle(context);
      AVMService avmService = getAVMService(context);
      NodeService nodeService = getNodeService(context);
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
         tx.begin();
         
         NodeRef websiteRef = getValue();
         if (value == null)
         {
            throw new IllegalArgumentException("Website NodeRef must be specified.");
         }
         String storeRoot = (String)nodeService.getProperty(websiteRef, ContentModel.PROP_AVMSTORE);
         
         // find the list of users who have a sandbox in the website
         List<String> users = (List<String>)nodeService.getProperty(websiteRef, ContentModel.PROP_USERSANDBOXES);
         for (int i=0; i<users.size(); i++)
         {
            String username = users.get(i);
            
            // build the name of the main store for the user
            String mainStore = AVMConstants.buildAVMUserMainStoreName(storeRoot, username);
            
            // check it exists before we render the view
            if (avmService.getAVMStore(mainStore) != null)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Building sandbox view for user store: " + mainStore);
               
               // for each user sandbox, generate an outer panel table
               PanelGenerator.generatePanelStart(out,
                     context.getExternalContext().getRequestContextPath(),
                     "white",
                     "white");
               
               // components for the current username, preview, browse and modified items inner list
               out.write("<table cellspacing=2 cellpadding=2 border=0 width=100%><tr><td>");
               // show the icon for the sandbox as a clickable browse link image
               // this is currently identical to the sandbox_browse action as below
               Utils.encodeRecursive(context, aquireAction(
                     context, mainStore, username, "sandbox_icon", WebResources.IMAGE_USERSANDBOX_32,
                     "#{AVMBrowseBean.setupSandboxAction}", "browseSandbox", null));
               out.write("</td><td width=100%>");
               out.write("<b>");
               out.write(bundle.getString(MSG_USERNAME));
               out.write(":</b>&nbsp;");
               out.write(username); // TODO: convert to full name?
               out.write("</td><td><nobr>");
               
               // direct actions for a sandbox
               String sandboxUrl = AVMConstants.buildAVMStoreUrl(mainStore);
               Utils.encodeRecursive(context, aquireAction(
                     context, mainStore, username, "sandbox_preview", "/images/icons/preview_website.gif",
                     null, null, sandboxUrl));
               out.write("&nbsp;");
               
               Utils.encodeRecursive(context, aquireAction(
                     context, mainStore, username, "sandbox_create", "/images/icons/new_content.gif",
                     "#{AVMBrowseBean.setupSandboxAction}", "wizard:createWebContent", null));
               out.write("&nbsp;");
               
               Utils.encodeRecursive(context, aquireAction(
                     context, mainStore, username, "sandbox_submitall", "/images/icons/submit.gif",
                     "#{AVMBrowseBean.submitAll}", null, null));
               out.write("&nbsp;");
               
               Utils.encodeRecursive(context, aquireAction(
                     context, mainStore, username, "sandbox_browse", "/images/icons/space_small.gif",
                     "#{AVMBrowseBean.setupSandboxAction}", "browseSandbox", null));
               out.write("</nobr></td></tr>");
               
               // modified items panel
               out.write("<tr><td></td><td colspan=2>");
               String panelImage = WebResources.IMAGE_COLLAPSED;
               if (this.expandedPanels.contains(username))
               {
                  panelImage = WebResources.IMAGE_EXPANDED;
               }
               out.write(Utils.buildImageTag(context, panelImage, 11, 11, "",
                     Utils.generateFormSubmit(context, this, getClientId(context), username)));
               out.write("&nbsp;<b>");
               out.write(bundle.getString(MSG_MODIFIED_ITEMS));
               out.write("</b>");
               if (this.expandedPanels.contains(username))
               {
                  out.write("<div style='padding:2px'></div>");

                  // list the modified docs for this sandbox user
                  renderUserFiles(context, out, username, storeRoot);
               }
               out.write("</td></tr></table>");
               
               // end the outer panel for this sandbox
               PanelGenerator.generatePanelEnd(out,
                     context.getExternalContext().getRequestContextPath(),
                     "white");
               
               // spacer row
               if (i < users.size() - 1)
               {
                  out.write("<div style='padding:4px'></div>");
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
    * Render the list of user modified files/folders in the layered sandbox area.
    * 
    * @param fc         FacesContext
    * @param out        ResponseWriter
    * @param username   The username to render the modified files for
    * @param storeRoot  Root name of the store containing the users sandbox
    * 
    * @throws IOException
    */
   private void renderUserFiles(FacesContext fc, ResponseWriter out, String username, String storeRoot)
      throws IOException
   {
      AVMSyncService avmSyncService = getAVMSyncService(fc);
      AVMService avmService = getAVMService(fc);
      
      DateFormat df = Utils.getDateTimeFormat(fc);
      ResourceBundle bundle = Application.getBundle(fc);
      
      // build the paths to the stores to compare
      String userStorePrefix = AVMConstants.buildAVMUserMainStoreName(storeRoot, username);
      String userStore = userStorePrefix + ":/";
      String stagingStore = AVMConstants.buildAVMStagingStoreName(storeRoot) + ":/";
      
      // get the UIActions component responsible for rendering context related user actions
      // TODO: we may need a component per user instance? (or use evaluators for roles...)
      UIActions uiFileActions = aquireUIActions(ACTIONS_FILE, userStorePrefix);
      UIActions uiFolderActions = aquireUIActions(ACTIONS_FOLDER, userStorePrefix);
      UIActions uiDeletedActions = aquireUIActions(ACTIONS_DELETED, userStorePrefix);
      
      // use the sync service to get the list of diffs between the stores
      List<AVMDifference> diffs = avmSyncService.compare(-1, userStore, -1, stagingStore);
      if (diffs.size() != 0)
      {
         // output the table of modified items
         out.write("<table class='modifiedItemsList' cellspacing=2 cellpadding=2 border=0 width=100%>");
         
         // header row
         out.write("<tr align=left><th width=16></th><th>");
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
         for (AVMDifference diff : diffs)
         {
            // TODO: display cases for diff.getDifferenceCode()?
            String sourcePath = diff.getSourcePath();
            AVMNodeDescriptor node = avmService.lookup(-1, sourcePath);
            if (node != null)
            {
               // icon and name of the file/folder - files are clickable to see the content
               String name = node.getName();
               String linkPrefix =
                     "<a href=\"" +
                     fc.getExternalContext().getRequestContextPath() +
                     DownloadContentServlet.generateBrowserURL(AVMNodeConverter.ToNodeRef(-1, sourcePath), name) +
                     "\" target='new'>";
               out.write("<tr><td width=16>");
               if (node.isFile())
               {
                  out.write(linkPrefix);
                  out.write(Utils.buildImageTag(fc, Utils.getFileTypeImage(fc, name, true), ""));
                  out.write("</a></td><td>");
                  out.write(linkPrefix);
                  out.write(name);
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
               
               // size of files
               if (node.isFile())
               {
                  out.write(getSizeConverter().getAsString(fc, this, node.getLength()));
                  out.write("</td><td>");
               
                  // add UI actions for this item
                  uiFileActions.setContext(new AVMNode(node));
                  Utils.encodeRecursive(fc, uiFileActions);
               }
               else
               {
                  out.write("</td><td>");
               
                  // add UI actions for this item
                  uiFolderActions.setContext(new AVMNode(node));
                  Utils.encodeRecursive(fc, uiFolderActions);
               }
               out.write("</td></tr>");
            }
            else
            {
               // must have been deleted from this sandbox - show ghosted
               AVMNodeDescriptor ghost = avmService.lookup(-1, diff.getSourcePath(), true);
               if (ghost != null)
               {
                  // icon and name of the file/folder - files are clickable to see the content
                  String name = ghost.getName();
                  out.write("<tr><td width=16>");
                  if (ghost.isFile())
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
                  out.write(df.format(new Date(ghost.getCreateDate())));
                  out.write("</td><td style='color:#aaaaaa'>");
                  
                  // modified date
                  out.write(df.format(new Date(ghost.getModDate())));
                  out.write("</td><td style='color:#aaaaaa'>");
                  
                  // size of files
                  if (ghost.isFile())
                  {
                     out.write(getSizeConverter().getAsString(fc, this, ghost.getLength()));
                  }
                  out.write("</td><td style='color:#aaaaaa'>");
                  
                  // deleted UI actions for this item
                  uiDeletedActions.setContext(new AVMNode(ghost, true));
                  Utils.encodeRecursive(fc, uiDeletedActions);
                  
                  out.write("</td></tr>");
               }
            }
         }
         
         // end table
         out.write("</table>");
      }
      else
      {
         // TODO: output "no modified files found" message
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
    * @param url              HREF URL for the action
    * 
    * @return UIActionLink component
    */
   private UIActionLink aquireAction(FacesContext fc, String store, String username,
         String name, String icon, String actionListener, String outcome, String url)
   {
      UIActionLink action = findAction(name, store, username);
      if (action == null)
      {
         action = createAction(fc, store, username, name, icon, actionListener, outcome, url);
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
   private UIActionLink findAction(String name, String store, String username)
   {
      UIActionLink action = null;
      String actionId = name + '_' + store;
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
    * 
    * @return UIActionLink child component
    */
   private UIActionLink createAction(FacesContext fc, String store, String username,
         String name, String icon, String actionListener, String outcome, String url)
   {
      javax.faces.application.Application facesApp = fc.getApplication();
      UIActionLink control = (UIActionLink)facesApp.createComponent(UIActions.COMPONENT_ACTIONLINK);
      
      String id = name + '_' + store;
      if (logger.isDebugEnabled())
         logger.debug("...creating action Id: " + id);
      control.setRendererType(UIActions.RENDERER_ACTIONLINK);
      control.setId(id);
      control.setValue(Application.getMessage(fc, name));
      control.setShowLink(false);
      control.setImage(icon);
      
      if (actionListener != null)
      {
         control.setActionListener(facesApp.createMethodBinding(
               actionListener, UIActions.ACTION_CLASS_ARGS));
         
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
      if (outcome != null)
      {
         control.setAction(new ConstantMethodBinding(outcome));
      }
      if (url != null)
      {
         control.setHref(url);
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
}
