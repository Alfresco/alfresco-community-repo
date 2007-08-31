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
package org.alfresco.web.app;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.DelegatingVariableResolver;

/**
 * JSF VariableResolver that first delegates to the Spring JSF variable 
 * resolver. The sole purpose of this variable resolver is to look out
 * for the <code>Container</code> variable. If this variable is encountered
 * the current viewId is examined. If the current viewId matches the 
 * configured dialog or wizard container the appropriate manager object is
 * returned i.e. DialogManager or WizardManager.
 * 
 * <p>Configure this resolver in your <code>faces-config.xml</code> file as follows:
 *
 * <pre>
 * &lt;application&gt;
 *   ...
 *   &lt;variable-resolver&gt;org.alfresco.web.app.AlfrescoVariableResolver&lt;/variable-resolver&gt;
 * &lt;/application&gt;</pre>
 * 
 * @see org.alfresco.web.bean.dialog.DialogManager
 * @see org.alfresco.web.bean.wizard.WizardManager
 * @author gavinc
 */
public class AlfrescoVariableResolver extends DelegatingVariableResolver
{
   protected String dialogContainer = null;
   protected String wizardContainer = null;
   
   private static final String CONTAINER = "Container";
   
   private static final Log logger = LogFactory.getLog(AlfrescoVariableResolver.class);
   
   /**
    * Creates a new VariableResolver.
    * 
    * @param originalVariableResolver The original variable resolver
    */
   public AlfrescoVariableResolver(VariableResolver originalVariableResolver)
   {
      super(originalVariableResolver);
   }
   
   /**
    * Resolves the variable with the given name.
    * <p>
    * This implementation will first delegate to the Spring variable resolver.
    * If the variable is not found by the Spring resolver and the variable name
    * is <code>Container</code> the current viewId is examined.
    * If the current viewId matches the configured dialog or wizard container 
    * the appropriate manager object is returned i.e. DialogManager or WizardManager.
    * 
    * @param context FacesContext
    * @param name The name of the variable to resolve
    */
   public Object resolveVariable(FacesContext context, String name) 
      throws EvaluationException 
   {
      Object variable = super.resolveVariable(context, name);
      
      if (variable == null)
      {
         // if the variable was not resolved see if the name is "Container"
         if (name.equals(CONTAINER))
         {
            // get the current view id and the configured dialog and wizard 
            // container pages
            String viewId = context.getViewRoot().getViewId();
            String dialogContainer = getDialogContainer(context);
            String wizardContainer = getWizardContainer(context);
            
            // see if we are currently in a wizard or a dialog
            if (viewId.equals(dialogContainer))
            {
               variable = Application.getDialogManager();
            }
            else if (viewId.equals(wizardContainer))
            {
               variable = Application.getWizardManager();   
            }
            
            if (variable != null && logger.isDebugEnabled())
            {
               logger.debug("Resolved 'Container' variable to: " + variable);
            }
         }
      }
      
      return variable;
   }
   
   /**
    * Retrieves the configured dialog container page
    * 
    * @param context FacesContext
    * @return The container page
    */
   protected String getDialogContainer(FacesContext context)
   {
	  if ((this.dialogContainer == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         ConfigService configSvc = Application.getConfigService(context);
         Config globalConfig = configSvc.getGlobalConfig();
         
         if (globalConfig != null)
         {
            this.dialogContainer = globalConfig.getConfigElement("dialog-container").getValue();
         }
      }
      
      return this.dialogContainer;
   }
   
   /**
    * Retrieves the configured wizard container page
    * 
    * @param context FacesContext
    * @return The container page
    */
   protected String getWizardContainer(FacesContext context)
   {
	  if ((this.wizardContainer == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         ConfigService configSvc = Application.getConfigService(context);
         Config globalConfig = configSvc.getGlobalConfig();
         
         if (globalConfig != null)
         {
            this.wizardContainer = globalConfig.getConfigElement("wizard-container").getValue();
         }
      }
      
      return this.wizardContainer;
   }
}
