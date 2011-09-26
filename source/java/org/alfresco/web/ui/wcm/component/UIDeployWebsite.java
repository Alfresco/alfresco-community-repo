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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.transaction.UserTransaction;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.actions.AVMDeployWebsiteAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.DeploymentMonitor;
import org.alfresco.web.bean.wcm.DeploymentServerConfig;
import org.alfresco.web.bean.wcm.DeploymentUtil;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.comparator.CompoundComparator;

/**
 * JSF component that allows a user to select which servers to deploy a
 * website to and provides monitoring of the deployments selected
 * 
 * @author gavinc
 */
public class UIDeployWebsite extends UIInput
{
   protected NodeRef webProjectRef;
   protected Integer snapshotVersion = -1;
   protected Boolean monitorDeployment;
   protected List<String> monitorIds;
   protected String deployMode;
   protected String store;
   
   private static Log logger = LogFactory.getLog(UIDeployWebsite.class);
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * Default constructor
    */
   public UIDeployWebsite()
   {
      setRendererType(null);
   }
   
   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.DeployWebsite";
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public void decode(FacesContext context)
   {
      super.decode(context);

      List<String> selectedNodes = new LinkedList<String>();
      Map valuesMap = context.getExternalContext().getRequestParameterValuesMap();

      // Non grouped checkboxes have the name of the clientId
      addValues(selectedNodes, valuesMap, this.getClientId(context));

      // If we have been grouping the checkboxes then the name will have been generated as follows
      // name = this.getClientId(context) + ":group1:child";  
      Set<String> keys = valuesMap.keySet();
      for (String key : keys) 
      {
         // Check whether the key matches the pattern for a child checkbox
         if (key.matches(this.getClientId(context) + ParentChildCheckboxHelper.helperChildPattern)) 
         { 
            // Key does matches the pattern for a child
            addValues(selectedNodes, valuesMap, key);
         }
      }

      // Need to convert between between Object[] and String[] otherwise we get a class cast exception in the 
      // bowels of JSF.
      String[] retVal = new String[selectedNodes.size()];
      java.lang.System.arraycopy(selectedNodes.toArray(), 0 , retVal, 0, selectedNodes.size());

      // These are the selected nodeIds of the servers which have been selected
      setSubmittedValue(retVal);
   }
   
   /**
    * Add the values from a map into a list
    * @param selectedNodes list to add to
    * @param valuesMap map of values
    * @param key key into map
    */
   private void addValues(List<String> selectedNodes, Map<String, String[]> valuesMap, String key)
   {
	   String[] values = (String[])valuesMap.get(key);
	   if (values != null) 
	   {
	      for(String value : values) 
	      {
	         selectedNodes.add(value);
	      }
	   }
   }

   @SuppressWarnings("unchecked")
   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.webProjectRef = (NodeRef)values[1];
      this.monitorDeployment = (Boolean)values[2];
      this.monitorIds = (List<String>)values[3];
      this.snapshotVersion = (Integer)values[4];
      this.deployMode = (String)values[5];
      this.store = (String)values[6];
   }
   
   @Override
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[7];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.webProjectRef;
      values[2] = this.monitorDeployment;
      values[3] = this.monitorIds;
      values[4] = this.snapshotVersion;
      values[5] = this.deployMode;
      values[6] = this.store;
      return values;
   }
   
   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
         tx.begin();
         
         NodeRef webProject = this.getWebsite();
         if (webProject == null)
         {
            throw new IllegalArgumentException("The web project must be specified.");
         }
         
         NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
         
         out.write("<script type='text/javascript' src='");
         out.write(context.getExternalContext().getRequestContextPath());
         out.write("/scripts/select-all.js'></script>\n");
         
         // add some before the panels
         out.write("\n<div style='padding-top:4px;'></div>\n");
      
         if (this.getMonitor())
         {
            // get the ids of the deployment monitor objects
            List<String> deployMonitorIds = this.getMonitorIds();
            
            if (logger.isDebugEnabled())
            {
               logger.debug("Monitoring deployment of: " + deployMonitorIds);
            }
            
            // TODO: Add support for 'sniffing' the session for deployment monitors
            //       this could be useful when the monitor ids are not known
            
            // render the supporting script required for monitoring
            renderScript(context, out, deployMonitorIds);
            
            // render each server being deployed with an animated icon, the subsequent
            // AJAX callback will update the progress.
            for (String id : deployMonitorIds)
            {
               // try and find the monitor object in the session, if it's not there
               // it has probably completed and been removed
               DeploymentMonitor monitor = (DeploymentMonitor)context.getExternalContext().
                     getSessionMap().get(id);
               if (monitor != null)
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Found deployment monitor: " + monitor);

                  renderMonitoredServer(context, out, nodeService, monitor.getTargetServer(), id);
               }
            }
         }
         else
         {
            if (WCMAppModel.CONSTRAINT_TESTSERVER.equals(getDeployMode()))
            {
               // determine the state, the sandbox may already have a test server
               // allocated, in which case we need to allow the user to preview or
               // re-deploy. If this is the first deployment or the test server has
               // been removed then show a list of available test servers to choose
               // from.
               
               List<NodeRef> allocatedServers = DeploymentUtil.findAllocatedTestServers(getStore());
               if (!allocatedServers.isEmpty())
               {
                  // there is at least one allocated server
                  for(NodeRef allocatedServer : allocatedServers)
                  {
                     renderAllocatedTestServer(context, out, nodeService, allocatedServer);
                  }
               }
               else
               {
            	  // a test server(s) needs to be selected - display the list of test servers
                  List<NodeRef> refs = Repository.getServiceRegistry(context).getDeploymentService().findTestDeploymentServers(webProject, true);

                  // Resolve the unsorted list of NodeRef to a sorted list of DeploymentServerConfig.
                  List<DeploymentServerConfig> servers = toSortedDeploymentServerConfig(nodeService, refs);

                  if (servers.size() > 0)
                  {
                     ParentChildCheckboxHelper helper = new ParentChildCheckboxHelper(this.getClientId(context));
                     boolean selected = false;

                     for (DeploymentServerConfig server: servers)
                     {                    	 
                        // Get the display group
                        String displayGroup = (String)server.getProperties().get(DeploymentServerConfig.PROP_GROUP);

                        helper.setCurrentDisplayGroup(displayGroup);
                        if(helper.newGroup) 
                        {       		
                           out.write("<p class='mainSubTitle'>");
                           out.write("<input type='checkbox' id='");
                           out.write(helper.groupParentId);
                           out.write("' value='");
                           out.write(Utils.encode(displayGroup));
                           out.write("'");
                           out.write(" ");
                           out.write("onClick=\"select_all(\'");
                           out.write(helper.groupChildName);
                           out.write("\', this.checked);\" "); 
                           out.write(" /> ");
                           out.write(Utils.encode(displayGroup));
                           out.write("</p>");
                        }

                        if (helper.groupParentId.length() > 0) 
                        {
                           // render the test server with a child checkbox
                           renderCheckableServer(context, out, nodeService, server.getServerRef(), selected, helper.groupChildName, helper.groupParentId);
                        }
                        else
                        {
                           // render the test server without a parent checkbox
                           renderCheckableServer(context, out, nodeService, server.getServerRef(), selected, this.getClientId(context));
                        }
                     }
                  }
                  else
                  {
                     // show the none available message
                     out.write("<div class='deployServersInfo'><img src='");
                     out.write(context.getExternalContext().getRequestContextPath());
                     out.write("/images/icons/info_icon.gif' />&nbsp;");
                     out.write(Application.getMessage(context, "deploy_test_server_not_available"));
                     out.write("</div>\n");
                     out.write("<script type='text/javascript'>\n");
                     out.write("disableOKButton = function() { ");
                     out.write("document.getElementById('dialog:finish-button').disabled = true; }\n");
                     out.write("window.onload = disableOKButton;\n");
                     out.write("</script>\n");
                  }
               }
            }
            else
            {
               // Display live servers not test servers

               // TODO: get a list of the servers that have been successfully deployed to 

               List<NodeRef> refs = Repository.getServiceRegistry(context).getDeploymentService().findLiveDeploymentServers(webProject);

               // Resolve the unsorted list of NodeRef to a sorted list of DeploymentServerConfig.
               List<DeploymentServerConfig> servers = toSortedDeploymentServerConfig(nodeService, refs);

               ParentChildCheckboxHelper helper = new ParentChildCheckboxHelper(this.getClientId(context));

               // Now display the servers         
               for (DeploymentServerConfig server : servers)
               {
                  // TODO: determine if the server has already been successfully deployed to
                  boolean selected = true;

                  // Get the display group
                  String displayGroup = (String)server.getProperties().get(DeploymentServerConfig.PROP_GROUP);

                  helper.setCurrentDisplayGroup(displayGroup);
                  if (helper.newGroup) 
                  {       		
                     out.write("<p class='mainSubTitle'>");
                     out.write("<input type='checkbox' id='");
                     out.write(helper.groupParentId);
                     out.write("' value='");
                     out.write(Utils.encode(displayGroup));
                     out.write("'");
                     out.write(" checked='checked' ");

                     out.write("onClick=\"select_all(\'");
                     out.write(helper.groupChildName);
                     out.write("\', this.checked);\" "); 
                     out.write(" /> ");

                     out.write(Utils.encode(displayGroup));
                     out.write("</p>");
                  }

                  if (helper.groupParentId.length() > 0) 
                  {
                     // render the live server with a child checkbox
                     renderCheckableServer(context, out, nodeService, server.getServerRef(), selected, helper.groupChildName, helper.groupParentId);
                  }
                  else
                  {
                     // render the live server without a parent checkbox
                     renderCheckableServer(context, out, nodeService, server.getServerRef(), selected, this.getClientId(context));
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
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors
   
   /**
    * @return The NodeRef representation of the web project to show the deployment reports for 
    */
   public NodeRef getWebsite()
   {
      ValueBinding vb = getValueBinding("website");
      if (vb != null)
      {
         this.webProjectRef = (NodeRef)vb.getValue(getFacesContext());
      }
      
      return this.webProjectRef;
   }
   
   /**
    * @param value The NodeRef representation of the web project to show the deployment reports for 
    */
   public void setWebsite(NodeRef value)
   {
      this.webProjectRef = value;
   }
   
   /**
    * @return true if the component should monitor a deployment
    */
   public boolean getMonitor()
   {
      ValueBinding vb = getValueBinding("monitor");
      if (vb != null)
      {
         this.monitorDeployment = (Boolean)vb.getValue(getFacesContext());
      }
      
      if (this.monitorDeployment == null)
      {
         this.monitorDeployment = Boolean.FALSE;
      }
      
      return this.monitorDeployment.booleanValue();
   }
   
   /**
    * @param monitor Determines whether a deployment should be monitored
    */
   public void setMonitor(boolean monitor)
   {
      this.monitorDeployment = new Boolean(monitor);
   }
   
   /**
    * @return The list of deployment monitor IDs
    */
   @SuppressWarnings("unchecked")
   public List<String> getMonitorIds()
   {
      ValueBinding vb = getValueBinding("monitorIds");
      if (vb != null)
      {
         this.monitorIds = (List<String>)vb.getValue(getFacesContext());
      }
      
      return this.monitorIds;
   }
   
   /**
    * @param monitorIds List of monitor IDs to look for
    */
   public void setMonitorIds(List<String> monitorIds)
   {
      this.monitorIds = monitorIds;
   }
   
   /**
    * @return The version of the snapshot being deployed
    */
   public int getSnapshotVersion()
   {
      ValueBinding vb = getValueBinding("snapshotVersion");
      if (vb != null)
      {
         this.snapshotVersion = (Integer)vb.getValue(getFacesContext());
      }
      
      return this.snapshotVersion;
   }
   
   /**
    * @param snapshotVersion The version of the snapshot being deployed
    */
   public void setSnapshotVersion(int snapshotVersion)
   {
      this.snapshotVersion = snapshotVersion;
   }
   
   /**
    * @return The type of server being deployed to, 'live' or 'test'
    */
   public String getDeployMode()
   {
      ValueBinding vb = getValueBinding("deployMode");
      if (vb != null)
      {
         this.deployMode = (String)vb.getValue(getFacesContext());
      }
      
      if (this.deployMode == null || this.deployMode.length() == 0)
      {
         this.deployMode = WCMAppModel.CONSTRAINT_TESTSERVER;
      }
      
      return this.deployMode;
   }
   
   /**
    * @param deployMode The type of server to deploy to, 'live' or 'test'
    */
   public void setDeployMode(String deployMode)
   {
      this.deployMode = deployMode;
   }
   
   /**
    * @return The store being deployed to
    */
   public String getStore()
   {
      ValueBinding vb = getValueBinding("store");
      if (vb != null)
      {
         this.store = (String)vb.getValue(getFacesContext());
      }
      
      return this.store;
   }
   
   /**
    * @param store The store to deploy to
    */
   public void setStore(String store)
   {
      this.store = store;
   }
   
   // ------------------------------------------------------------------------------
   // Helpers
   
   private void renderScript(FacesContext context, ResponseWriter out, 
            List<String> monitorIds) throws IOException
   {
      // create comma separated list of deplyment ids
      StringBuilder ids = new StringBuilder();
      for (int x = 0; x < monitorIds.size(); x++)
      {
         if (x > 0)
         {
            ids.append(",");
         }
         
         String id = monitorIds.get(x);
         ids.append(id);
      }
      
      // determine the polling frequency value
      int pollFreq = AVMUtil.getRemoteDeploymentPollingFrequency() * 1000;

      // render the script to handle the progress monitoring
      out.write("<script type='text/javascript' src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/scripts/ajax/deployment.js'></script>\n");
      
      out.write("<script type='text/javascript'>\n");
      out.write("Alfresco.initDeploymentMonitor = function() {\n");
      out.write("   Alfresco.monitor = new Alfresco.DeploymentMonitor('");
      out.write(ids.toString());
      out.write("', ");
      out.write(Integer.toString(pollFreq));
      out.write(", '");
      out.write(Application.getMessage(context, "deploy_failed"));
      out.write("', '");
      out.write(Application.getMessage(context, "deploy_successful"));
      out.write("');\n");
      out.write("   Alfresco.monitor.retrieveDeploymentStatus();\n");
      out.write("}\n");
      out.write("Alfresco.monitor = null;\n");
      out.write("window.onload = Alfresco.initDeploymentMonitor;\n");
      out.write("</script>\n");
   }
   
   private void renderCheckableServer(FacesContext context, ResponseWriter out, NodeService nodeService,
           NodeRef server, boolean selected, String checkBoxName) throws IOException
   {
	   renderCheckableServer(context, out, nodeService, server, selected, checkBoxName, "");
   }

   private void renderCheckableServer(FacesContext context, ResponseWriter out, NodeService nodeService,
            NodeRef server, boolean selected, String checkBoxName, String parentId) throws IOException
   {
      String contextPath = context.getExternalContext().getRequestContextPath();

      renderPanelStart(out, contextPath);

      out.write("<div class='deployPanelControl'>");
      out.write("<input type='checkbox' ");
      if (checkBoxName != null && checkBoxName.length() > 0) 
      {
         out.write("name='");
         out.write(checkBoxName);
         out.write("' "); 

         // If there is a parent checkbox
         // generate java script of the form
         // onClick="select_one('xxx', 'area');"
         if (parentId != null && parentId.length() > 0 ) 
         {
            out.write("onClick=\"select_one(\'");
            out.write(parentId);
            out.write("\', \'");
            out.write(checkBoxName);
            out.write("');\" "); 
         }
      }      
      out.write("value='");
      out.write(server.toString());
      out.write("'");
      if (selected)
      {
         out.write(" checked='checked'");
      }
      out.write(" /></div>");

      renderPanelMiddle(out, contextPath, nodeService, server, true);

      renderPanelEnd(out, contextPath);
   }
   
   private void renderAllocatedTestServer(FacesContext context, ResponseWriter out, NodeService nodeService,
            NodeRef server) throws IOException
   {
      String contextPath = context.getExternalContext().getRequestContextPath();

      renderPanelStart(out, contextPath);
      
      renderPanelMiddle(out, contextPath, nodeService, server, false);

      String url = (String)nodeService.getProperty(server, WCMAppModel.PROP_DEPLOYSERVERURL);
      if (url != null && url.length() > 0)
      {
         out.write("<div class='deployServersUrl'><a target='new' href='");
         out.write(url);
         out.write("'>");
         out.write(Utils.encode(url));
         out.write("</a></div>");
      }
      
      renderPanelEnd(out, contextPath);
      
      // render a hidden field with the value of the allocated test server
      out.write("<input type='hidden' name='");
      out.write(this.getClientId(context));
      out.write("' value='");
      out.write(server.toString());
      out.write("' />");
   }
   
   private void renderMonitoredServer(FacesContext context, ResponseWriter out, NodeService nodeService,
            NodeRef server, String monitorId) throws IOException
   {
      String contextPath = context.getExternalContext().getRequestContextPath();
      
      renderPanelStart(out, contextPath);
      
      out.write("<div class='deployPanelStatusIcon'>");
      out.write("<img id='");
      out.write(monitorId);
      out.write("_icon' src='");
      out.write(contextPath);
      out.write("/images/icons/ajax_anim.gif' />");
      out.write("</div>");
      
      renderPanelMiddle(out, contextPath, nodeService, server, true);
      
      out.write("<div class='deployPanelServerStatus' id='");
      out.write(monitorId);
      out.write("_status'>");
      out.write(Application.getMessage(context, "deploying"));
      out.write("</div>");
      
      out.write("<div class='deployPanelServerMsg' id='");
      out.write(monitorId);
      out.write("_msg'>");
      out.write("</div>");
      
      renderPanelEnd(out, contextPath);
   }
   
   private void renderPanelStart(ResponseWriter out, String contextPath) throws IOException
   {
      // render start of panel
      out.write("<table cellspacing='0' cellpadding='0' border='0' width='100%'>");
      out.write("<tr><td width='10'><img src='");
      out.write(contextPath);
      out.write("/images/parts/deploy_panel_start.gif' /></td>");
      out.write("<td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/deploy_panel_bg.gif); background-repeat: repeat-x;'>");
   }
   
   private void renderPanelMiddle(ResponseWriter out, String contextPath, 
            NodeService nodeService, NodeRef server, boolean showSeparator) throws IOException
   {
      Map<QName, Serializable> props = nodeService.getProperties(server);
      String deployType = (String)props.get(WCMAppModel.PROP_DEPLOYTYPE);
      String serverName = (String)props.get(WCMAppModel.PROP_DEPLOYSERVERNAME);
      if (serverName == null || serverName.length() == 0)
      {
         serverName = AVMDeployWebsiteAction.calculateServerUri(props);
      }
      
      out.write("</td>");
      if (showSeparator)
      {
         out.write("<td width='2'><img src='");
         out.write(contextPath);
         out.write("/images/parts/deploy_panel_separator.gif' /></td>");
      }
      out.write("<td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/deploy_panel_bg.gif); background-repeat: repeat-x;'>");
      out.write("<div class='deployPanelIcon'>");
      out.write("<img src='");
      out.write(contextPath);
      out.write("/images/icons/deploy_server_");
      out.write(deployType);
      out.write(".gif' /></div></td>");
      out.write("<td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/deploy_panel_bg.gif); background-repeat: repeat-x; width: 100%;'>");
      out.write("<div class='deployPanelServerName'>");
      out.write(Utils.encode(serverName));
      out.write("</div>");
   }
   
   
   private void renderPanelEnd(ResponseWriter out, String contextPath) throws IOException
   {
      // render end of panel
      out.write("</td><td width='10'><img src='");
      out.write(contextPath);
      out.write("/images/parts/deploy_panel_end.gif' /></td></tr></table>");
      
      // add some padding under each panel
      out.write("\n<div style='padding-top:8px;'></div>\n");
   }
   
   /**
    * Utility method to read the details of the deployment nodes
    * @param nodeService the node service
    * @param refs a list of NodeRefs
    * 
    * @return a sorted list of DeploymentServerConfig objects.
    */
   @SuppressWarnings("unchecked")
   private List<DeploymentServerConfig> toSortedDeploymentServerConfig(NodeService nodeService, List<NodeRef> refs) { 
	   // Resolve the list of NodeRef to a list of DeploymentServerConfig.
	   List<DeploymentServerConfig> servers = new ArrayList<DeploymentServerConfig>();
	   for (NodeRef ref : refs) 
	   {
		   DeploymentServerConfig server = new DeploymentServerConfig(ref, nodeService.getProperties(ref));
		   servers.add(server);
	   }
   
	   // Sort the deployment servers by display group then display name      
	   CompoundComparator comp = new CompoundComparator();
	   comp.addComparator(new DeploymentServerConfigComparator(DeploymentServerConfig.PROP_GROUP));
	   comp.addComparator(new DeploymentServerConfigComparator(DeploymentServerConfig.PROP_NAME));
	   Collections.sort(servers, comp);
	   
	   return servers;
   }

   private class ParentChildCheckboxHelper 
   {
      private String clientId;
      String currentDisplayGroup = "";
      String groupChildName = "";
      String groupParentId = "";
      String groupName = "";
      int groupNumber = 1;
      boolean newGroup = false;

      public ParentChildCheckboxHelper(String clientId)
      {
         this.clientId = clientId;
      }

      public void setCurrentDisplayGroup(String currentDisplayGroup) 
      {
         this.newGroup = !this.currentDisplayGroup.equalsIgnoreCase(currentDisplayGroup);
         this.currentDisplayGroup = currentDisplayGroup;
         if (this.newGroup)
         {
            changeGroup(currentDisplayGroup);
         }
      }
      public String getCurrentDisplayGroup() 
      {
         return currentDisplayGroup;
      }

      private void changeGroup(String newGroupName) 
      {
         // Examples of HTML naming scheme 
         //  jsp17:group1:parent, jsp17:group1:child
         //  jsp17:group2:parent, jsp17:group2:child
         groupName = clientId + ":group" + Integer.toString(groupNumber++);  
         groupChildName = groupName + ":child";
         groupParentId = groupName + ":parent";
      }

      /**
       * Regex pattern for child checkbox names - matches implementation within changeGroup method of this class
       */
      static final String helperChildPattern = ":group[\\d]+:child";
   }
}
