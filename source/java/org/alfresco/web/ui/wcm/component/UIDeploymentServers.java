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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIParameter;
import javax.faces.component.UISelectBoolean;
import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.actions.AVMDeployWebsiteAction;
import org.alfresco.service.cmr.avm.deploy.DeploymentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.DeploymentServerConfig;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.PanelGenerator;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.repo.component.UIActions;
import org.springframework.util.comparator.CompoundComparator;

/**
 * JSF component that allows deployment servers to be added, edited and removed.
 * 
 * @author gavinc
 */
public class UIDeploymentServers extends UIInput
{
   private static final String MSG_LIVE_SERVER = "deploy_server_type_live";
   private static final String MSG_TEST_SERVER = "deploy_server_type_test";
   private static final String MSG_TYPE = "deploy_server_type";
   private static final String MSG_NAME = "deploy_server_name";
   private static final String MSG_GROUP = "deploy_server_group";
   private static final String MSG_ADAPTER_NAME = "deploy_server_adapter_name";
   private static final String MSG_HOST = "deploy_server_host";
   private static final String MSG_PORT = "deploy_server_port";
   private static final String MSG_USER = "deploy_server_username";
   private static final String MSG_PWD = "deploy_server_password";
   private static final String MSG_URL = "deploy_server_url";
   private static final String MSG_ALLOCATED = "deploy_server_allocated";
   private static final String MSG_SOURCE = "deploy_server_source_path";
   private static final String MSG_TARGET = "deploy_server_target_name";
   private static final String MSG_EXCLUDES = "deploy_server_excludes";
   private static final String MSG_INCLUDE_AUTO_DEPLOY = "deploy_server_include_auto_deploy";
   private static final String MSG_EDIT = "edit_deploy_server";
   private static final String MSG_DELETE = "delete_deploy_server";
   private static final String MSG_NO_DEPLOY_SERVERS = "no_deploy_servers";
   private static final String MSG_NO_DATA = "no_data";
   
   private List<DeploymentServerConfig> servers;
   private DeploymentServerConfig currentServer;
   private Boolean inAddMode;
   private String addType;
   
   private DeploymentService deploymentService;
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * Default constructor
    */
   public UIDeploymentServers()
   {
      setRendererType(null);
   }
   
   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.DeploymentServers";
   }

   @SuppressWarnings("unchecked")
   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.servers = (List<DeploymentServerConfig>)values[1];
      this.inAddMode = (Boolean)values[2];
      this.addType = (String)values[3];
      this.currentServer = (DeploymentServerConfig)values[4];
   }
   
   @Override
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[5];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.servers;
      values[2] = this.inAddMode;
      values[3] = this.addType;
      values[4] = this.currentServer;
      return values;
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      // clear previously generated children
      this.getChildren().clear();
      
      ResponseWriter out = context.getResponseWriter();
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
         tx.begin();
         
         String contextPath = context.getExternalContext().getRequestContextPath();
         out.write("\n<script type='text/javascript'>");
         out.write("var MSG_PORT_MUST_BE_NUMBER = '" + Application.getMessage(context, "port_must_be_number") + "'; \n");
         out.write("var MSG_HOST_WRONG_FORMAT = '" + Application.getMessage(context, "host_wrong_format") + "'; \n");
         out.write("</script>\n");
         		
         out.write("<script type='text/javascript' src='");
         out.write(contextPath);
         out.write("/scripts/ajax/deployment.js'></script>\n");
         out.write("<div class='deployConfig'>");
         
         List<DeploymentServerConfig> servers = getValue();
         
         // Sort the deployment servers by display group then display name      
         CompoundComparator comp = new CompoundComparator();
         comp.addComparator(new DeploymentServerConfigComparator(DeploymentServerConfig.PROP_GROUP));
         comp.addComparator(new DeploymentServerConfigComparator(DeploymentServerConfig.PROP_NAME));
         Collections.sort(servers, comp);
                           
         if (getInAddMode())
         {
            renderServerForm(context, out, null, false);
         }
         else
         {
            if (servers.size() == 0)
            {
               out.write("<div class='deployNoConfigServers'><img src='");
               out.write(contextPath);
               out.write("/images/icons/info_icon.gif' />&nbsp;");
               out.write(Application.getMessage(context, MSG_NO_DEPLOY_SERVERS));
               out.write("</div>");
            }
         }
         
         DeploymentServerConfig currentServer = getCurrentServer();
         String currentDisplayGroup = "";
         
         for (DeploymentServerConfig server: servers)
         {
            // Write the display group title if it is a new title
            String displayGroup = (String)server.getProperties().get(DeploymentServerConfig.PROP_GROUP);
            if(!currentDisplayGroup.equalsIgnoreCase(displayGroup)) 
            {
               // yes title has changed - write out the new displayGroup	
               out.write("<p class='mainSubTitle'>");
               out.write(Utils.encode(displayGroup));
               out.write("</p>");
               currentDisplayGroup = displayGroup;
            }
        	   
            if (currentServer != null && currentServer.getId().equals(server.getId()))
            {
               // This is the server in edit mode
               renderServerForm(context, out, server, true);
            }
            else
            {
               renderServer(context, out, server);
            }
         }
         
         out.write("</div>");
         
         out.write("\n<script type='text/javascript'>");
         out.write("YAHOO.util.Event.on(window, \"load\", Alfresco.checkDeployConfigPage);");
         if (currentServer != null)
         {
            out.write("var SCROLL_TO_SERVER_CONFIG_ID = '");
            out.write(currentServer.getId());
            out.write("';\n");
         }
         out.write("</script>\n");
         
         tx.commit();
      }
      catch (Throwable err)
      {
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         throw new RuntimeException(err);
      }
   }
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors
   
   /**
    * @return List of deployment servers to show 
    */
   @SuppressWarnings("unchecked")
   public List<DeploymentServerConfig> getValue()
   {
      ValueBinding vb = getValueBinding("value");
      if (vb != null)
      {
         this.servers = (List<DeploymentServerConfig>)vb.getValue(getFacesContext());
      }
      
      return this.servers;
   }
   
   /**
    * @param value The list of deployment servers to show 
    */
   public void setValue(List<DeploymentServerConfig> value)
   {
      this.servers = value;
   }
   
   /**
    * @param server The current deployment server being added or edited
    */
   public void setCurrentServer(DeploymentServerConfig server)
   {
      this.currentServer = server;
   }
   
   /**
    * @return Deployment server currently being edited or added 
    */
   @SuppressWarnings("unchecked")
   public DeploymentServerConfig getCurrentServer()
   {
      ValueBinding vb = getValueBinding("currentServer");
      if (vb != null)
      {
         this.currentServer = (DeploymentServerConfig)vb.getValue(getFacesContext());
      }
      
      return this.currentServer;
   }
   
   /**
    * @return true if the component should show a form to add a new server
    */
   public boolean getInAddMode()
   {
      ValueBinding vb = getValueBinding("inAddMode");
      if (vb != null)
      {
         this.inAddMode = (Boolean)vb.getValue(getFacesContext());
      }
      
      if (this.inAddMode == null)
      {
         this.inAddMode = Boolean.FALSE;
      }
      
      return this.inAddMode.booleanValue();
   }
   
   /**
    * @param inAddMode Determines whether a new server should be added
    */
   public void setInAddMode(boolean inAddMode)
   {
      this.inAddMode = new Boolean(inAddMode);
   }
   
   /**
    * @return The type of reciever server to add
    */
   public String getAddType()
   {
      ValueBinding vb = getValueBinding("addType");
      if (vb != null)
      {
         this.addType = (String)vb.getValue(getFacesContext());
      }
      
      if (this.addType == null)
      {
         this.addType = WCMAppModel.CONSTRAINT_FILEDEPLOY;
      }
      
      return this.addType;
   }
   
   /**
    * @param value The type of server receiver to add 
    */
   public void setAddType(String value)
   {
      this.addType = value;
   }
   
   // ------------------------------------------------------------------------------
   // Helpers
   
   protected void renderServer(FacesContext context, ResponseWriter out,
            DeploymentServerConfig server) throws IOException
   {
      String contextPath = context.getExternalContext().getRequestContextPath();
      ResourceBundle bundle = Application.getBundle(context);
      String noData = bundle.getString(MSG_NO_DATA);
      
      String serverName = (String)server.getProperties().get(DeploymentServerConfig.PROP_NAME);
      if (serverName == null || serverName.length() == 0)
      {
         serverName = AVMDeployWebsiteAction.calculateServerUri(server.getRepoProps());
      }
      
      out.write("<div class='deployConfigServer'>");
      PanelGenerator.generatePanelStart(out, contextPath, "lightstorm", "#eaeff2");
      out.write("<table width='100%'><tr><td><img class='deployConfigServerIcon' src='");
      out.write(contextPath);
      out.write("/images/icons/deploy_server_");
      out.write(Utils.encode(server.getDeployType()));
      out.write(".gif");
      out.write("' /></td><td width='100%'><span class='deployPanelServerName'>");
      out.write(Utils.encode(serverName));
      out.write("</span></td><td><div class='deployConfigServerActions'>");
      Utils.encodeRecursive(context, aquireEditServerAction(context, server.getId()));
      Utils.encodeRecursive(context, aquireDeleteServerAction(context, server.getId()));
      out.write("</div></td></tr>");
      out.write("<tr><td colspan='3'>");
      out.write("<table cellpadding='0' cellspacing='0'>");
      out.write("<tr><td width='100%'><table cellpadding='3' cellspacing='0' class='deployConfigServerDetailsLeftCol'>");
      
      if (WCMAppModel.CONSTRAINT_FILEDEPLOY.equals(server.getDeployType()))
      {
  
    	  out.write("<tr><td align='right'>");
    	  out.write(bundle.getString(MSG_ADAPTER_NAME));
    	  out.write(":</td><td>");
    	  if (server.getProperties().get(DeploymentServerConfig.PROP_ADAPTER_NAME) != null)
    	  {
    		  out.write((String)server.getProperties().get(DeploymentServerConfig.PROP_ADAPTER_NAME));
    	  }
    	  else
    	  {
    		  out.write(noData);
    	  }
    	  out.write("</td></tr>");
      }

      
      out.write("<tr><td align='right'>");
      out.write(bundle.getString(MSG_HOST));
      out.write(":</td><td>");
      if (server.getProperties().get(DeploymentServerConfig.PROP_HOST) != null)
      {
         out.write(Utils.encode((String)server.getProperties().get(DeploymentServerConfig.PROP_HOST)));
      }
      out.write("</td></tr>");
      
         
      out.write("<tr><td align='right'>");
      out.write(bundle.getString(MSG_PORT));
      out.write(":</td><td>");
      if (server.getProperties().get(DeploymentServerConfig.PROP_PORT) != null)
      {
         out.write(Utils.encode((String)server.getProperties().get(DeploymentServerConfig.PROP_PORT)));
      }
      else
      {
         out.write(noData);
      }
      out.write("</td></tr>");
      
      out.write("<tr><td align='right'>");
      out.write(bundle.getString(MSG_URL));
      out.write(":</td><td>");
      if (server.getProperties().get(DeploymentServerConfig.PROP_URL) != null)
      {
         out.write(Utils.encode((String)server.getProperties().get(DeploymentServerConfig.PROP_URL)));
      }
      else
      {
         out.write(noData);
      }
      out.write("</td></tr>");
      
      out.write("<tr><td align='right'>");
      out.write(bundle.getString(MSG_TYPE));
      out.write(":</td><td>");
      if (server.getProperties().get(DeploymentServerConfig.PROP_TYPE) != null)
      {
         String type = (String)server.getProperties().get(DeploymentServerConfig.PROP_TYPE);
         if (WCMAppModel.CONSTRAINT_LIVESERVER.equals(type))
         {
            out.write(Utils.encode(Application.getMessage(context, MSG_LIVE_SERVER)));
         }
         else if (WCMAppModel.CONSTRAINT_TESTSERVER.equals(type))
         {
            out.write(Utils.encode(Application.getMessage(context, MSG_TEST_SERVER)));
         }
      }
      out.write("</td></tr>");
      
      out.write("<tr><td align='right'>");
      out.write(bundle.getString(MSG_USER));
      out.write(":</td><td>");
      if (server.getProperties().get(DeploymentServerConfig.PROP_USER) != null)
      {
         out.write(Utils.encode((String)server.getProperties().get(DeploymentServerConfig.PROP_USER)));
      }
      else
      {
         out.write(noData);
      }
      out.write("</td></tr></table></td>"); 
      out.write("<td valign='top'><table cellpadding='3' cellspacing='0' class='deployConfigServerDetailsRightCol'>");
      
      out.write("<tr><td align='right'><nobr>");
      out.write(bundle.getString(MSG_SOURCE));
      out.write(":</nobr></td><td>");
      if (server.getProperties().get(DeploymentServerConfig.PROP_SOURCE_PATH) != null)
      {
         out.write(Utils.encode((String)server.getProperties().get(DeploymentServerConfig.PROP_SOURCE_PATH)));
      }
      else
      {
         out.write(noData);
      }
      out.write("</td></tr>");
      
      out.write("<tr><td align='right'><nobr>");
      out.write(bundle.getString(MSG_EXCLUDES));
      out.write(":</nobr></td><td>");
      if (server.getProperties().get(DeploymentServerConfig.PROP_EXCLUDES) != null)
      {
         out.write(Utils.encode((String)server.getProperties().get(DeploymentServerConfig.PROP_EXCLUDES)));
      }
      else
      {
         out.write(noData);
      }
      out.write("</td></tr>");
      
      if (WCMAppModel.CONSTRAINT_FILEDEPLOY.equals(server.getDeployType()))
      {
         out.write("<tr><td align='right'><nobr>");
         out.write(bundle.getString(MSG_TARGET));
         out.write(":</nobr></td><td>");
         if (server.getProperties().get(DeploymentServerConfig.PROP_TARGET_NAME) != null)
         {
            out.write(Utils.encode((String)server.getProperties().get(DeploymentServerConfig.PROP_TARGET_NAME)));
         }
         else
         {
            out.write(noData);
         }
         out.write("</td></tr>");
      }
      
      if (WCMAppModel.CONSTRAINT_LIVESERVER.equals(
          server.getProperties().get(DeploymentServerConfig.PROP_TYPE)))
      {
         out.write("<tr><td align='right'><nobr>");
         out.write(bundle.getString(MSG_INCLUDE_AUTO_DEPLOY));
         out.write(":</nobr></td><td>");
         if (server.getProperties().get(DeploymentServerConfig.PROP_ON_APPROVAL) != null)
         {
            Object obj = server.getProperties().get(DeploymentServerConfig.PROP_ON_APPROVAL);
            if (obj instanceof Boolean && ((Boolean)obj).booleanValue())
            {
               out.write(bundle.getString("yes"));
            }
            else
            {
               out.write(bundle.getString("no"));
            }
         }
         out.write("</td></tr>");
      }
      
      if (WCMAppModel.CONSTRAINT_TESTSERVER.equals(
          server.getProperties().get(DeploymentServerConfig.PROP_TYPE)))
      {
         out.write("<tr><td align='right'><nobr>");
         out.write(Utils.encode(bundle.getString(MSG_ALLOCATED)));
         out.write(":</nobr></td><td>");
         if (server.getProperties().get(DeploymentServerConfig.PROP_ALLOCATED_TO) != null)
         {
            String allocatedToTip = (String)server.getProperties().get(
                     DeploymentServerConfig.PROP_ALLOCATED_TO);
            out.write("<span title='");
            out.write(Utils.encode(allocatedToTip));
            out.write("'><nobr>");
            out.write(bundle.getString("yes"));
            out.write("&nbsp;(");
            if (AVMUtil.isWorkflowStore(allocatedToTip))
            {
               out.write(bundle.getString("review_sandbox"));
            }
            else
            {
               String username = AVMUtil.getUserName(allocatedToTip);
               out.write(Utils.encode(username));
            }
            out.write(")</nobr></span>");
         }
         else
         {
            out.write(bundle.getString("no"));
         }
         out.write("</td></tr>");
      }
      
      out.write("</table></td></tr></table></td></tr></table>");
      PanelGenerator.generatePanelEnd(out, contextPath, "lightstorm");
      out.write("</div>");
   }
   
   @SuppressWarnings("unchecked")
   protected void renderServerForm(FacesContext context, ResponseWriter out,
            DeploymentServerConfig server, boolean edit) throws IOException
   {
      String contextPath = context.getExternalContext().getRequestContextPath();
      ResourceBundle bundle = Application.getBundle(context);
         
      out.write("<div class='deployConfigServer'");
      if (edit)
      {
         out.write(" id='");
         out.write(server.getId());
         out.write("'");
      }
      out.write(">");
      PanelGenerator.generatePanelStart(out, contextPath, "lightstorm", "#eaeff2");
      out.write("<table width='100%'><tr><td><img class='deployConfigServerIcon' src='");
      out.write(contextPath);
      out.write("/images/icons/deploy_server_");
      if (edit)
      {
         out.write(Utils.encode(server.getDeployType()));
      }
      else
      {
         out.write(getAddType());
      }
      out.write(".gif' /></td>");
      out.write("<td width='100%'><span class='mainSubTitle'>");
      if (edit)
      {
         if (WCMAppModel.CONSTRAINT_ALFDEPLOY.equals(server.getDeployType()))
         {
            out.write(bundle.getString("edit_alf_deploy_server_info"));
         }
         else
         {
            out.write(bundle.getString("edit_file_deploy_server_info"));
         }
      }
      else
      {
         if (WCMAppModel.CONSTRAINT_ALFDEPLOY.equals(getAddType()))
         {
            out.write(bundle.getString("add_alf_deploy_server_info"));
         }
         else
         {
            out.write(bundle.getString("add_file_deploy_server_info"));
         }
      }
      out.write("</span></td></tr>");
      out.write("<tr><td colspan='2'><table class='deployConfigServerForm'>");
      
      // create the server type drop down
      out.write("<tr><td align='right'>");
      out.write(bundle.getString(MSG_TYPE));
      out.write(":</td><td>");
      UIComponent type = context.getApplication().createComponent(
               UISelectOne.COMPONENT_TYPE);
      FacesHelper.setupComponentId(context, type, "deployServerType");
      type.getAttributes().put("styleClass", "inputField");
      type.getAttributes().put("onchange", 
               "javascript:Alfresco.deployServerTypeChanged();");
      ValueBinding vbType = context.getApplication().createValueBinding(
            "#{WizardManager.bean.editedDeployServerProperties." + 
            DeploymentServerConfig.PROP_TYPE + "}");
      type.setValueBinding("value", vbType);
      UISelectItems itemsComponent = (UISelectItems)context.getApplication().
               createComponent(UISelectItems.COMPONENT_TYPE);
      List<SelectItem> items = new ArrayList<SelectItem>(2);
      
      items.add(new SelectItem(WCMAppModel.CONSTRAINT_LIVESERVER, 
               Application.getMessage(context, MSG_LIVE_SERVER)));
      items.add(new SelectItem(WCMAppModel.CONSTRAINT_TESTSERVER, 
               Application.getMessage(context, MSG_TEST_SERVER)));
      
      itemsComponent.setValue(items);
      type.getChildren().add(itemsComponent);
      this.getChildren().add(type);
      Utils.encodeRecursive(context, type);
      out.write("</td></tr>");
      
      // create the server display name field
      out.write("<tr><td align='right'>");
      out.write(bundle.getString(MSG_NAME));
      out.write(":</td><td>");
      UIComponent name = context.getApplication().createComponent(
               UIInput.COMPONENT_TYPE);
      FacesHelper.setupComponentId(context, name, null);
      name.getAttributes().put("styleClass", "inputField");
      ValueBinding vbName = context.getApplication().createValueBinding(
            "#{WizardManager.bean.editedDeployServerProperties." + 
            DeploymentServerConfig.PROP_NAME + "}");
      name.setValueBinding("value", vbName);
      this.getChildren().add(name);
      Utils.encodeRecursive(context, name);
      out.write("</td></tr>");
      
      // create the display group name field
      out.write("<tr><td align='right'>");
      out.write(bundle.getString(MSG_GROUP));
      out.write(":</td><td>");
      UIComponent group = context.getApplication().createComponent(
               UIInput.COMPONENT_TYPE);
      FacesHelper.setupComponentId(context, group, null);
      group.getAttributes().put("styleClass", "inputField");
      ValueBinding vbGroup = context.getApplication().createValueBinding(
            "#{WizardManager.bean.editedDeployServerProperties." + 
            DeploymentServerConfig.PROP_GROUP + "}");
      group.setValueBinding("value", vbGroup);
      this.getChildren().add(group);
      Utils.encodeRecursive(context, group);
      out.write("</td></tr>");

      if (!edit && WCMAppModel.CONSTRAINT_FILEDEPLOY.equals(getAddType() ) ||
         (edit && WCMAppModel.CONSTRAINT_FILEDEPLOY.equals(server.getDeployType())))
      {
    	  // for an FSR create the protocol adapter field
    	  out.write("<tr><td align='right'>");
    	  out.write(bundle.getString(MSG_ADAPTER_NAME));
    	  out.write(":</td><td>");
    	  
          UIComponent adapterName = context.getApplication().createComponent(
                  UISelectOne.COMPONENT_TYPE);
    	  FacesHelper.setupComponentId(context, adapterName, "deploy_server_adapter_name");
    	  adapterName.getAttributes().put("styleClass", "inputField");
    	  
    	  ValueBinding vbAdapterName = context.getApplication().createValueBinding(
            "#{WizardManager.bean.editedDeployServerProperties." + 
            DeploymentServerConfig.PROP_ADAPTER_NAME + "}");
    	  adapterName.setValueBinding("value", vbAdapterName);
    	  
    	  UISelectItems adaptersComponent = (UISelectItems)context.getApplication().
          	createComponent(UISelectItems.COMPONENT_TYPE);
    	  
    	  DeploymentService dep = getDeploymentService();
    	  if(dep == null) 
    	  {
        	  List<SelectItem> adapters = new ArrayList<SelectItem>(1);
        	  adapters.add(new SelectItem("default", "Default")); 
        	  adaptersComponent.setValue(adapters);
    	  }
    	  else
    	  {
    		  Set<String> adapterNames = dep.getAdapterNames();
        	  List<SelectItem> adapters = new ArrayList<SelectItem>(adapterNames.size());
        	  for(String aname : adapterNames)
        	  {
        		  adapters.add(new SelectItem(aname, aname)); 
        	  }
        	  adaptersComponent.setValue(adapters);
    	  }
    	     	  
    	  adapterName.getChildren().add(adaptersComponent);
    	  this.getChildren().add(adapterName);
    	  Utils.encodeRecursive(context, adapterName);
    	  
    	  out.write("</td></tr>");
      }
      
      // create the server host field
      out.write("<tr><td align='right'>");
      out.write(bundle.getString(MSG_HOST));
      out.write(":</td><td>");
      UIComponent host = context.getApplication().createComponent(
               UIInput.COMPONENT_TYPE);
      FacesHelper.setupComponentId(context, host, "deployServerHost");
      
      host.getAttributes().put("styleClass", "inputField");
      host.getAttributes().put("onkeyup", 
               "javascript:Alfresco.checkDeployConfigButtonState();");
      host.getAttributes().put("onchange", 
               "javascript:Alfresco.checkDeployConfigButtonState();");
      
      ValueBinding vbHost = context.getApplication().createValueBinding(
               "#{WizardManager.bean.editedDeployServerProperties." + 
               DeploymentServerConfig.PROP_HOST + "}");
      host.setValueBinding("value", vbHost);
      this.getChildren().add(host);
      Utils.encodeRecursive(context, host);
      out.write("</td><td><img src='");
      out.write(contextPath);
      out.write("/images/icons/required_field.gif' title='");
      out.write(bundle.getString("required_field"));
      out.write("'/></td></tr>");
      
      // create the server port field
      out.write("<tr><td align='right'>");
      out.write(bundle.getString(MSG_PORT));
      out.write(":</td><td>");
      UIComponent port = context.getApplication().createComponent(
               UIInput.COMPONENT_TYPE);
      FacesHelper.setupComponentId(context, port, "deployServerPort");
      port.getAttributes().put("styleClass", "inputField");
      port.getAttributes().put("onkeyup", 
      			"javascript:Alfresco.checkDeployConfigButtonState();");
      port.getAttributes().put("onchange", 
      			"javascript:Alfresco.checkDeployConfigButtonState();");
      
      ValueBinding vbPort = context.getApplication().createValueBinding(
            "#{WizardManager.bean.editedDeployServerProperties." + 
            DeploymentServerConfig.PROP_PORT + "}");
      port.setValueBinding("value", vbPort);
      this.getChildren().add(port);
      Utils.encodeRecursive(context, port);
      out.write("</td><td><img src='");
      out.write(contextPath);
      out.write("/images/icons/required_field.gif' title='");
      out.write(bundle.getString("required_field"));
      out.write("'/></td></tr>");
      
      // create the server url field
      out.write("<tr><td align='right'>");
      out.write(bundle.getString(MSG_URL));
      out.write(":</td><td>");
      UIComponent url = context.getApplication().createComponent(
               UIInput.COMPONENT_TYPE);
      FacesHelper.setupComponentId(context, url, null);
      url.getAttributes().put("styleClass", "inputField");
      ValueBinding vbUrl = context.getApplication().createValueBinding(
            "#{WizardManager.bean.editedDeployServerProperties." + 
            DeploymentServerConfig.PROP_URL + "}");
      url.setValueBinding("value", vbUrl);
      this.getChildren().add(url);
      Utils.encodeRecursive(context, url);
      out.write("</td></tr>");
        
      // create the server username field
      out.write("<tr><td align='right'>");
      out.write(bundle.getString(MSG_USER));
      out.write(":</td><td>");
      UIComponent username = context.getApplication().createComponent(
               UIInput.COMPONENT_TYPE);
      FacesHelper.setupComponentId(context, username, null);
      username.getAttributes().put("styleClass", "inputField");
      ValueBinding vbUser = context.getApplication().createValueBinding(
            "#{WizardManager.bean.editedDeployServerProperties." + 
            DeploymentServerConfig.PROP_USER + "}");
      username.setValueBinding("value", vbUser);
      this.getChildren().add(username);
      Utils.encodeRecursive(context, username);
      out.write("</td></tr>");
      
      // create the server password field
      out.write("<tr><td align='right'>");
      out.write(bundle.getString(MSG_PWD));
      out.write(":</td><td>");
      UIComponent pwd = context.getApplication().createComponent(
               UIInput.COMPONENT_TYPE);
      FacesHelper.setupComponentId(context, pwd, null);
      pwd.setRendererType("javax.faces.Secret");
      pwd.getAttributes().put("styleClass", "inputField");
      pwd.getAttributes().put("redisplay", true);
      ValueBinding vbPwd = context.getApplication().createValueBinding(
            "#{WizardManager.bean.editedDeployServerProperties." + 
            DeploymentServerConfig.PROP_PASSWORD + "}");
      pwd.setValueBinding("value", vbPwd);
      this.getChildren().add(pwd);
      Utils.encodeRecursive(context, pwd);
      out.write("</td></tr>");
      

      
      // create the source path field
      out.write("<tr><td align='right'>");
      out.write(bundle.getString(MSG_SOURCE));
      out.write(":</td><td>");
      UIComponent source = context.getApplication().createComponent(
               UIInput.COMPONENT_TYPE);
      FacesHelper.setupComponentId(context, source, null);
      source.getAttributes().put("styleClass", "inputField");
      ValueBinding vbSource = context.getApplication().createValueBinding(
            "#{WizardManager.bean.editedDeployServerProperties." + 
            DeploymentServerConfig.PROP_SOURCE_PATH + "}");
      source.setValueBinding("value", vbSource);
      this.getChildren().add(source);
      Utils.encodeRecursive(context, source);
      out.write("</td></tr>");
      
      // create the excludes field
      out.write("<tr><td align='right'>");
      out.write(bundle.getString(MSG_EXCLUDES));
      out.write(":</td><td>");
      UIComponent excludes = context.getApplication().createComponent(
               UIInput.COMPONENT_TYPE);
      FacesHelper.setupComponentId(context, excludes, null);
      excludes.getAttributes().put("styleClass", "inputField");
      ValueBinding vbExcludes = context.getApplication().createValueBinding(
            "#{WizardManager.bean.editedDeployServerProperties." + 
            DeploymentServerConfig.PROP_EXCLUDES + "}");
      excludes.setValueBinding("value", vbExcludes);
      this.getChildren().add(excludes);
      Utils.encodeRecursive(context, excludes);
      out.write("</td></tr>");
      
      if ((edit == false && WCMAppModel.CONSTRAINT_FILEDEPLOY.equals(getAddType())) ||
          (edit && WCMAppModel.CONSTRAINT_FILEDEPLOY.equals(server.getDeployType())))
      {
         // create the target field
         out.write("<tr><td align='right'>");
         out.write(bundle.getString(MSG_TARGET));
         out.write(":</td><td>");
         UIComponent target = context.getApplication().createComponent(
                  UIInput.COMPONENT_TYPE);
         FacesHelper.setupComponentId(context, target, null);
         target.getAttributes().put("styleClass", "inputField");
         ValueBinding vbTarget = context.getApplication().createValueBinding(
               "#{WizardManager.bean.editedDeployServerProperties." + 
               DeploymentServerConfig.PROP_TARGET_NAME + "}");
         target.setValueBinding("value", vbTarget);
         this.getChildren().add(target);
         Utils.encodeRecursive(context, target);
         out.write("</td></tr>");
      }
      
      // create the auto deploy checkbox
      out.write("<tr><td align='right'></td><td>");
      UIComponent auto = context.getApplication().createComponent(
               UISelectBoolean.COMPONENT_TYPE);
      FacesHelper.setupComponentId(context, auto, "autoDeployCheckbox");
      ValueBinding vbAuto = context.getApplication().createValueBinding(
            "#{WizardManager.bean.editedDeployServerProperties." + 
            DeploymentServerConfig.PROP_ON_APPROVAL + "}");
      auto.setValueBinding("value", vbAuto);
      this.getChildren().add(auto);
      Utils.encodeRecursive(context, auto);
      out.write("<span id='autoDeployLabel'>&nbsp;");
      out.write(bundle.getString(MSG_INCLUDE_AUTO_DEPLOY));
      out.write("</td></tr>");
      
      // create and add the cancel button
      out.write("<tr><td colspan='2' align='right'>");
      UICommand cancelButton = (UICommand)context.getApplication().createComponent(
               UICommand.COMPONENT_TYPE);
      FacesHelper.setupComponentId(context, cancelButton, null);
      cancelButton.setValue(bundle.getString("cancel"));
      MethodBinding cancelBinding = context.getApplication().createMethodBinding(
                  "#{WizardManager.bean.cancelDeploymentServerConfig}", new Class[] {});
      cancelButton.setAction(cancelBinding);
      this.getChildren().add(cancelButton);
      Utils.encodeRecursive(context, cancelButton);
      out.write("&nbsp;&nbsp;");
      
      if (edit)
      {
         // create the done button
         UICommand saveButton = (UICommand)context.getApplication().createComponent(
                  UICommand.COMPONENT_TYPE);
         FacesHelper.setupComponentId(context, saveButton, "deployActionButton");
         saveButton.setValue(bundle.getString("save"));
         MethodBinding saveBinding = context.getApplication().createMethodBinding(
                     "#{WizardManager.bean.saveDeploymentServerConfig}", new Class[] {});
         saveButton.setAction(saveBinding);
         this.getChildren().add(saveButton);
         Utils.encodeRecursive(context, saveButton);
         
      }
      else
      {
         // create the add button
         UICommand addButton = (UICommand)context.getApplication().createComponent(
                  UICommand.COMPONENT_TYPE);
         FacesHelper.setupComponentId(context, addButton, "deployActionButton");
         addButton.setValue(bundle.getString("add"));
         MethodBinding addBinding = context.getApplication().createMethodBinding(
                     "#{WizardManager.bean.addDeploymentServerConfig}", new Class[] {});
         addButton.setAction(addBinding);
         this.getChildren().add(addButton);
         Utils.encodeRecursive(context, addButton);
         out.write("</td></tr>");
      }
      
      // finish off tables and div
      out.write("</td></tr></table></td></tr></table>");
      PanelGenerator.generatePanelEnd(out, contextPath, "lightstorm");
      out.write("</div>");
   }
   
   @SuppressWarnings("unchecked")
   protected UIActionLink aquireEditServerAction(FacesContext context, String serverId)
   {
      UIActionLink action = null;
      String actionId = "edit_" + serverId;
      
      // try find the action as a child of this component
      for (UIComponent component : (List<UIComponent>)getChildren())
      {
         if (actionId.equals(component.getId()))
         {
            action = (UIActionLink)component;
            break;
         }
      }
      
      if (action == null)
      {
         // create the action and add as a child component
         javax.faces.application.Application facesApp = context.getApplication();
         action = (UIActionLink)facesApp.createComponent(UIActions.COMPONENT_ACTIONLINK);
         action.setId(actionId);
         action.setValue(Application.getMessage(context, MSG_EDIT));
         action.setImage("/images/icons/edit_icon.gif");
         action.setShowLink(false);
         action.setActionListener(facesApp.createMethodBinding(
               "#{WizardManager.bean.editDeploymentServerConfig}", 
               UIActions.ACTION_CLASS_ARGS));
         
         // add server id param
         UIParameter param = (UIParameter)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
         param.setId(actionId + "_1");
         param.setName("id");
         param.setValue(serverId);
         action.getChildren().add(param);
         
         this.getChildren().add(action);
      }
      
      return action;
   }
   
   @SuppressWarnings("unchecked")
   protected UIActionLink aquireDeleteServerAction(FacesContext context, String serverId)
   {
      UIActionLink action = null;
      String actionId = "delete_" + serverId;
      
      // try find the action as a child of this component
      for (UIComponent component : (List<UIComponent>)getChildren())
      {
         if (actionId.equals(component.getId()))
         {
            action = (UIActionLink)component;
            break;
         }
      }
      
      if (action == null)
      {
         // create the action and add as a child component
         javax.faces.application.Application facesApp = context.getApplication();
         action = (UIActionLink)facesApp.createComponent(UIActions.COMPONENT_ACTIONLINK);
         action.setId(actionId);
         action.setValue(Application.getMessage(context, MSG_DELETE));
         action.setImage("/images/icons/delete.gif");
         action.setShowLink(false);
         action.setActionListener(facesApp.createMethodBinding(
               "#{WizardManager.bean.deleteDeploymentServerConfig}", 
               UIActions.ACTION_CLASS_ARGS));
         
         // add server id param
         UIParameter param = (UIParameter)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
         param.setId(actionId + "_1");
         param.setName("id");
         param.setValue(serverId);
         action.getChildren().add(param);
         
         this.getChildren().add(action);
      }
      
      return action;
   }
   
   /**
    * @return Options for the type of deployment server i.e. test or live
    */
   public List<UIListItem> getDeployServerTypes()
   {
      List<UIListItem> items = new ArrayList<UIListItem>(2);
      
      UIListItem live = new UIListItem();
      live.setValue(WCMAppModel.CONSTRAINT_LIVESERVER);
      live.setLabel(Application.getMessage(FacesContext.getCurrentInstance(), MSG_LIVE_SERVER));
      
      UIListItem test = new UIListItem();
      test.setValue(WCMAppModel.CONSTRAINT_TESTSERVER);
      test.setLabel(Application.getMessage(FacesContext.getCurrentInstance(), MSG_TEST_SERVER));
      
      items.add(live);
      items.add(test);
      
      return items;
   }
   
   protected DeploymentService getDeploymentService()
   {
      if (deploymentService == null)
      {
         deploymentService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getDeploymentService();
      }
      return deploymentService;
   }
   
}
