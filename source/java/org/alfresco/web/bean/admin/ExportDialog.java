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
package org.alfresco.web.bean.admin;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.repo.action.executer.ExporterActionExecuter;
import org.alfresco.repo.action.executer.RepositoryExporterActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing bean implementation for the Export dialog.
 * 
 * @author gavinc
 */
public class ExportDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -2592252768301728700L;

   private static final Log logger = LogFactory.getLog(ExportDialog.class);
   
   private static final String ALL_SPACES = "all";
   private static final String CURRENT_SPACE = "current";
   private static final String DEFAULT_OUTCOME = "dialog:close";
   private static final String MSG_EXPORT_TITLE = "export_title";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";
   
   protected BrowseBean browseBean;
   transient private ActionService actionService;
   
   private String packageName;
   private String encoding = "UTF-8";
   private String mode = CURRENT_SPACE;
   private NodeRef destination;
   private boolean includeChildren = true;
   private boolean runInBackground = true;
   private boolean includeSelf;
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      if (logger.isDebugEnabled())
         logger.debug("Called export for " + this.mode + " with package name: " + this.packageName);
      
      // construct appropriate action to execute
      Action action = null;
      NodeRef startNode = this.browseBean.getActionSpace().getNodeRef();

      // get the appropriate node
      if (this.mode.equals(ALL_SPACES))
      {
         Map<String, Serializable> params = new HashMap<String, Serializable>(5);
         params.put(ExporterActionExecuter.PARAM_PACKAGE_NAME, this.packageName);
         params.put(ExporterActionExecuter.PARAM_DESTINATION_FOLDER, this.destination);
         action = this.getActionService().createAction(RepositoryExporterActionExecuter.NAME, params);
      }
      else
      {
         Map<String, Serializable> params = new HashMap<String, Serializable>(5);
         params.put(ExporterActionExecuter.PARAM_STORE, Repository.getStoreRef().toString());
         params.put(ExporterActionExecuter.PARAM_PACKAGE_NAME, this.packageName);
         params.put(ExporterActionExecuter.PARAM_ENCODING, this.encoding);
         params.put(ExporterActionExecuter.PARAM_DESTINATION_FOLDER, this.destination);
         params.put(ExporterActionExecuter.PARAM_INCLUDE_CHILDREN, Boolean.valueOf(includeChildren));
         params.put(ExporterActionExecuter.PARAM_INCLUDE_SELF, new Boolean(includeSelf));
         action = this.getActionService().createAction(ExporterActionExecuter.NAME, params);
      }

      // execute action
      action.setExecuteAsynchronously(this.runInBackground);
      this.getActionService().executeAction(action, startNode);

      if (logger.isDebugEnabled())
      {
         logger.debug("Executed space export action with action params of " + action.getParameterValues());
      }
      
      // reset the bean
      reset();
      
      return outcome;
   }
   
   @Override
   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       String name = Application.getMessage(fc, MSG_LEFT_QUOTE)
       + browseBean.getActionSpace().getName()
       + Application.getMessage(fc, MSG_RIGHT_QUOTE);
       return MessageFormat.format(Application.getMessage(fc, MSG_EXPORT_TITLE), name);
   }
   
   /**
    * Action called when the dialog is cancelled, just resets the bean's state
    * 
    * @return The outcome
    */
   public String cancel()
   {
      reset();
      
      return DEFAULT_OUTCOME;
   }
   
   /**
    * Resets the dialog state back to the default
    */
   public void reset()
   {
      this.packageName = null;
      this.mode = CURRENT_SPACE;
      this.destination = null;
      this.includeChildren = true;
      this.includeSelf = false;
      this.runInBackground = true;
   }
   
   /**
    * Returns the package name for the export
    * 
    * @return The export package name
    */
   public String getPackageName()
   {
      return this.packageName;
   }
   
   /**
    * Sets the package name for the export
    * 
    * @param packageName The export package name 
    */
   public void setPackageName(String packageName)
   {
      this.packageName = packageName;
   }
   
   /**
    * The destination for the export as a NodeRef
    * 
    * @return The destination
    */
   public NodeRef getDestination()
   {
      return this.destination;
   }
   
   /**
    * Sets the destination for the export
    * 
    * @param destination The destination for the export
    */
   public void setDestination(NodeRef destination)
   {
      this.destination = destination;
   }
   
   /**
    * Determines whether the export will include child spaces 
    * 
    * @return true includes children
    */
   public boolean getIncludeChildren()
   {
      return this.includeChildren;
   }
   
   /**
    * Sets whether child spaces are included in the export 
    * 
    * @param includeChildren true to include the child spaces
    */
   public void setIncludeChildren(boolean includeChildren)
   {
      this.includeChildren = includeChildren;
   }
   
   /**
    * Determines whether the export will include the space itself 
    * 
    * @return true includes the space being exported from
    */
   public boolean getIncludeSelf()
   {
      return this.includeSelf;
   }
   
   /**
    * Sets whether the space itself is included in the export 
    * 
    * @param includeSelf true to include the space itself
    */
   public void setIncludeSelf(boolean includeSelf)
   {
      this.includeSelf = includeSelf;
   }
   
   /**
    * Determines whether to export only the current space or all spaces
    * 
    * @return "all" to export all space and "current" to export the current space
    */
   public String getMode()
   {
      return this.mode;
   }
   
   /**
    * Sets whether to export the current space or all spaces
    * 
    * @param mode "all" to export all space and "current" to export the current space
    */
   public void setMode(String mode)
   {
      this.mode = mode;
   }
   
   /**
    * Returns the encoding to use for the export
    *  
    * @return The encoding
    */
   public String getEncoding()
   {
      return this.encoding;
   }

   /**
    * Sets the encoding to use for the export package
    * 
    * @param encoding The encoding
    */
   public void setEncoding(String encoding)
   {
      this.encoding = encoding;
   }

   /**
    * Determines whether the import should run in the background
    * 
    * @return true means the import will run in the background 
    */
   public boolean getRunInBackground()
   {
      return this.runInBackground;
   }

   /**
    * Determines whether the import will run in the background
    * 
    * @param runInBackground true to run the import in the background
    */
   public void setRunInBackground(boolean runInBackground)
   {
      this.runInBackground = runInBackground;
   }
   
   /**
    * Sets the BrowseBean instance to use to retrieve the current document
    * 
    * @param browseBean BrowseBean instance
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
   }
   
   /**
    * Sets the action service
    * 
    * @param actionService  the action service
    */
   public void setActionService(ActionService actionService)
   {
      this.actionService = actionService;
   }
   
   protected ActionService getActionService()
   {
      if (actionService == null)
      {
         actionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getActionService();
      }
      return actionService;
   }
}
