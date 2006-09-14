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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.alfresco.web.ui.common.PanelGenerator;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.repo.component.UIActions;
import org.alfresco.web.ui.wcm.WebResources;
import org.springframework.web.jsf.FacesContextUtils;

import sun.swing.UIAction;

/**
 * @author Kevin Roast
 */
public class UIUserSandboxes extends SelfRenderingComponent
{
   private static final String MSG_USERNAME = "username";
   private static final String MSG_NAME = "name";
   private static final String MSG_DESCRIPTION = "description";
   private static final String MSG_MODIFIED = "modified_date";
   private static final String MSG_ACTIONS = "actions";
   
   /** website to show sandboxes for */
   private NodeRef value;
   
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
            String mainStore = storeRoot + '-' + username + AVMConstants.STORE_MAIN;
            
            // check it exists before we render the view
            if (avmService.getAVMStore(mainStore) != null)
            {
               // for each user sandbox, generate an outer panel table
               PanelGenerator.generatePanelStart(out,
                     context.getExternalContext().getRequestContextPath(),
                     "white",
                     "white");
               
               // components for the current username, preview, browse and modified items inner list
               out.write("<table cellspacing=2 cellpadding=2 border=0 width=100%><tr><td>");
               out.write(Utils.buildImageTag(context, WebResources.IMAGE_SANDBOX_32, 32, 32, ""));
               out.write("</td><td width=100%>");
               out.write("<b>");
               out.write(bundle.getString(MSG_USERNAME));
               out.write(":</b>&nbsp;");
               out.write(username); // TODO: convert to full name?
               out.write("</td><td><nobr>");
               
               // direct actions for a sandbox
               Utils.encodeRecursive(context, aquireAction(
                     context, "sandbox_preview", "/images/icons/preview_website.gif"));
               out.write("&nbsp;&nbsp;");
               Utils.encodeRecursive(context, aquireAction(
                     context, "sandbox_create", "/images/icons/new_content.gif"));
               out.write("&nbsp;&nbsp;");
               Utils.encodeRecursive(context, aquireAction(
                     context, "sandbox_browse", "/images/icons/space_small.gif"));
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
               out.write("&nbsp;<b>Modified Items (3)</b>");
               if (this.expandedPanels.contains(username))
               {
                  out.write("<div style='padding:2px'></div>");
                  out.write("<table cellspacing=2 cellpadding=2 border=0 width=100%>");
                  
                  // header row
                  out.write("<tr align=left><th width=16></th><th>");
                  out.write(bundle.getString(MSG_NAME));
                  out.write("</th><th>");
                  out.write(bundle.getString(MSG_DESCRIPTION));
                  out.write("</th><th>");
                  out.write(bundle.getString(MSG_MODIFIED));
                  out.write("</th><th>");
                  out.write(bundle.getString(MSG_ACTIONS));
                  out.write("</th></tr>");
                  
                  // row per modified doc item
                  // TODO: add modified items list for this sandbox user
                  out.write("<tr><td width=16>(O)</td><td>");
                  out.write("Some document.html");
                  out.write("</td><td>");
                  out.write("A description would go here");
                  out.write("</td><td>");
                  out.write("01-01-2006 11:58am");
                  out.write("</td><td>");
                  // TODO: add UI actions for this item
                  out.write("(P)&nbsp;(E)&nbsp;(T)&nbsp;(D)");
                  out.write("</td></tr>");
                  
                  out.write("</table>");
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
   
   private UIActionLink aquireAction(FacesContext fc, String name, String icon)
   {
      UIActionLink action = findAction(name);
      if (action == null)
      {
         action = createAction(fc, name, icon);
      }
      return action;
   }
   
   private UIActionLink findAction(String name)
   {
      UIActionLink action = null;
      String actionId = getId() + name;
      for (UIComponent component : (List<UIComponent>)getChildren())
      {
         if (actionId.equals(component.getId()))
         {
            action = (UIActionLink)component;
            break;
         }
      }
      return action;
   }
   
   private UIActionLink createAction(FacesContext fc, String name, String icon)
   {
      javax.faces.application.Application facesApp = fc.getApplication();
      UIActionLink control = (UIActionLink)facesApp.createComponent(UIActions.COMPONENT_ACTIONLINK);
      
      control.setRendererType(UIActions.RENDERER_ACTIONLINK);
      control.setId(getId() + name);
      control.setValue(Application.getMessage(fc, name));
      control.setShowLink(false);
      control.setImage(icon);
      
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
