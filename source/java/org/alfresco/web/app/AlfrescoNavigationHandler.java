/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.app;

import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigService;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.config.NavigationConfigElement;
import org.alfresco.web.config.NavigationElementReader;
import org.alfresco.web.config.NavigationResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * @author gavinc
 */
public class AlfrescoNavigationHandler extends NavigationHandler
{
   private final static Log logger = LogFactory.getLog(AlfrescoNavigationHandler.class);
   
   // The original navigation handler
   private NavigationHandler origHandler;
   
   /**
    * Default constructor
    * 
    * @param origHandler The original navigation handler
    */
   public AlfrescoNavigationHandler(NavigationHandler origHandler)
   {
      super();
      this.origHandler = origHandler;
   }

   /**
    * @see javax.faces.application.NavigationHandler#handleNavigation(javax.faces.context.FacesContext, java.lang.String, java.lang.String)
    */
   @Override
   public void handleNavigation(FacesContext context, String fromAction, String outcome)
   {
      if (logger.isDebugEnabled())
         logger.debug("handleNavigation (fromAction=" + fromAction + ", outcome=" + outcome + ")");
      
      boolean useOriginalNavHandler = true;
      String finalOutcome = outcome;
      String viewId = context.getViewRoot().getViewId();
      
      if (logger.isDebugEnabled())
         logger.debug("Current view id: " + viewId);
      
      NavigationBean navBean = (NavigationBean)context.getExternalContext().
         getSessionMap().get("NavigationBean");
      
      // only continue if we have some dispatching context
      if (navBean != null && navBean.getDispatchContextNode() != null)
      {
         Node node = navBean.getDispatchContextNode();
               
         if (logger.isDebugEnabled())
            logger.debug("Found node in dispatch context: " + node);

         // see if there is any navigation config for the node type
         ConfigService configSvc = (ConfigService)FacesContextUtils.getRequiredWebApplicationContext(
               context).getBean(Application.BEAN_CONFIG_SERVICE);
         
         Config nodeConfig = configSvc.getConfig(node);
         NavigationConfigElement navigationCfg = (NavigationConfigElement)nodeConfig.
               getConfigElement(NavigationElementReader.ELEMENT_NAVIGATION);
         
         if (navigationCfg != null)
         {
            // see if there is config for the current view state
            NavigationResult navResult = navigationCfg.getOverride(viewId, outcome);
            
            if (navResult != null)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Found navigation config: " + navResult);
               
               if (navResult.isOutcome())
               {
                  finalOutcome = navResult.getResult();
               }
               else
               {
                  String newViewId = navResult.getResult();
                  
                  if (newViewId.equals(viewId) == false)
                  {
                     useOriginalNavHandler = false;
                     
                     if (logger.isDebugEnabled())
                        logger.debug("Dispatching to new view id: " + newViewId);
                  
                     ViewHandler viewHandler = context.getApplication().getViewHandler();
                     UIViewRoot viewRoot = viewHandler.createView(context, newViewId);
                     viewRoot.setViewId(newViewId);
                     context.setViewRoot(viewRoot);
                     context.renderResponse();
                  }
                  else
                  {
                     if (logger.isDebugEnabled())
                        logger.debug("New view id is the same as the current one so setting outcome to null");
                     
                     finalOutcome = null;
                  }
               }
            }
            else if (logger.isDebugEnabled())
            {
               logger.debug("No override configuration found for current view or outcome");
            }
         }
         else if (logger.isDebugEnabled())
         {
            logger.debug("No navigation configuration found for node");
         }
         
         // reset the dispatch context
         navBean.resetDispatchContext();
      }
      else if (logger.isDebugEnabled())
      {
         logger.debug("No dispatch context found");
      }
      
      // do the appropriate navigation handling
      if (useOriginalNavHandler)
      {
         if (logger.isDebugEnabled())
            logger.debug("Passing outcome '" + finalOutcome + "' to original navigation handler");
         
         this.origHandler.handleNavigation(context, fromAction, finalOutcome);
      }
   }
}
