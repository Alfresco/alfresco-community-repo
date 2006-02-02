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

import java.util.Stack;

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
   public final static String DIALOG_SEPARATOR = ":";
   public final static String DIALOG_PREXIX = "dialog" + DIALOG_SEPARATOR;
   public final static String CLOSE_DIALOG_OUTCOME = DIALOG_PREXIX + "close";
   
   private final static Log logger = LogFactory.getLog(AlfrescoNavigationHandler.class);
   private final static String VIEW_STACK = "_alfViewStack";
   
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
   @SuppressWarnings("unchecked")
   public void handleNavigation(FacesContext context, String fromAction, String outcome)
   {
      if (logger.isDebugEnabled())
         logger.debug("handleNavigation (fromAction=" + fromAction + ", outcome=" + outcome + ")");
      
      boolean useOriginalNavHandler = true;
      boolean closingDialog = false;
      String viewId = context.getViewRoot().getViewId();
      
      if (logger.isDebugEnabled())
         logger.debug("Current view id: " + viewId);
      
      // determine if we are dealing with a dialog
      if (outcome != null && outcome.startsWith(DIALOG_PREXIX))
      {
         // determine whether it's being closed or opened
         closingDialog = outcome.startsWith(CLOSE_DIALOG_OUTCOME);
         
         // remove the dialog prefix
         outcome = outcome.substring(DIALOG_PREXIX.length());

         if (closingDialog)
         {
            // if we are closing the dialog take the view off the 
            // top of the stack then decide whether to use the view
            // or any overridden outcome that may be present
            if (getViewStack(context).empty() == false)
            {
               String newViewId = (String)getViewStack(context).pop();
            
               // is there an overiddent outcome?
               int idx = outcome.indexOf(DIALOG_SEPARATOR);
               if (idx == -1)
               {
                  // there isn't an overidden outcome so go back to the previous view
                  if (logger.isDebugEnabled())
                     logger.debug("Closing dialog, going back to view id: " + newViewId);
               
                  goToView(context, newViewId);
               }
               else
               {
                  // there is an overidden outcome so extract it
                  outcome = outcome.substring(idx+1, outcome.length());
                  
                  // we also need to empty the dialog stack if we have been given
                  // an overidden outcome as we could be going anywhere in the app
                  getViewStack(context).clear();
                  
                  if (logger.isDebugEnabled())
                     logger.debug("Closing dialog with an overridden outcome of '" + outcome + "'");
                  
                  this.origHandler.handleNavigation(context, fromAction, outcome);
               }
            }
            else
            {
               // we are trying to close a dialog when one hasn't been opened!
               // log a warning and return a null outcome to stay on the same page
               if (logger.isWarnEnabled())
               {
                  logger.warn("Attempting to close a dialog with an empty view stack, returning null outcome");
               }
               
               this.origHandler.handleNavigation(context, fromAction, null);
            }
         }
         else
         {
            // if we are opening a dialog push the current view id 
            // on to the stack, but only if it is different than the 
            // current view at the top (you can't launch a dialog from
            // the same page 2 times in a row!)
            
            // TODO: This wouldn't happen if we could be sure a dialog is 
            //       ALWAYS exited properly, look into a way of ensuring
            //       dialogs get closed if a user navigates away from the page,
            //       would a PhaseListener help in any way??
            
            if (getViewStack(context).empty() || 
                viewId.equals(getViewStack(context).peek()) == false)
            {
               getViewStack(context).push(viewId);
            
               if (logger.isDebugEnabled())
                  logger.debug("Pushed current view to stack: " + viewId);
            }
            else
            {
               if (getViewStack(context).empty() == false && logger.isDebugEnabled())
               {
                  logger.debug("current view is already top the view stack!");
               }
            }
         }
         
         if (logger.isDebugEnabled())
            logger.debug("view stack: " + getViewStack(context));
      }
      
      if (closingDialog == false)
      {
         NavigationBean navBean = (NavigationBean)context.getExternalContext().
            getSessionMap().get("NavigationBean");
         
         // only continue if we have some dispatching context
         if (navBean != null && navBean.getDispatchContextNode() != null)
         {
            Node node = navBean.getDispatchContextNode();
                  
            if (logger.isDebugEnabled())
               logger.debug("Found node with type '" + node.getType().toString() + 
                            "' in dispatch context");
   
            // see if there is any navigation config for the node type
            ConfigService configSvc = Application.getConfigService(context);
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
                     outcome = navResult.getResult();
                  }
                  else
                  {
                     String newViewId = navResult.getResult();
                     
                     if (newViewId.equals(viewId) == false)
                     {
                        useOriginalNavHandler = false;
                        
                        if (logger.isDebugEnabled())
                           logger.debug("Dispatching to new view id: " + newViewId);
                     
                        goToView(context, newViewId);
                     }
                     else
                     {
                        if (logger.isDebugEnabled())
                           logger.debug("New view id is the same as the current one so setting outcome to null");
                        
                        outcome = null;
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
               logger.debug("Passing outcome '" + outcome + "' to original navigation handler");
            
            this.origHandler.handleNavigation(context, fromAction, outcome);
         }
      }
   }
   
   /**
    * Dispatches to the given view id
    * 
    * @param context Faces context
    * @param viewId The view id to go to
    */
   private void goToView(FacesContext context, String viewId)
   {
      ViewHandler viewHandler = context.getApplication().getViewHandler();
      UIViewRoot viewRoot = viewHandler.createView(context, viewId);
      viewRoot.setViewId(viewId);
      context.setViewRoot(viewRoot);
      context.renderResponse();
   }
   
   /**
    * Returns the view stack for the current user.
    *  
    * @param context FacesContext
    * @return A Stack representing the views that have launched dialogs in
    *         the users session, will never be null
    */
   @SuppressWarnings("unchecked")
   private Stack getViewStack(FacesContext context)
   {
      Stack viewStack = (Stack)context.getExternalContext().getSessionMap().get(VIEW_STACK);
      
      if (viewStack == null)
      {
         viewStack = new Stack();
         context.getExternalContext().getSessionMap().put(VIEW_STACK, viewStack);
      }
      
      return viewStack;
   }
}
