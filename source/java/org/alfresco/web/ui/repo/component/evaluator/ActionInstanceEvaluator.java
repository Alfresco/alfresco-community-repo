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
package org.alfresco.web.ui.repo.component.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.web.action.ActionEvaluator;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.component.evaluator.BaseEvaluator;

/**
 * Evaluator for executing an ActionEvaluator instance.
 * 
 * @author Kevin Roast
 */
public class ActionInstanceEvaluator extends BaseEvaluator
{
   /**
    * Evaluate by executing the specified action instance evaluator.
    * 
    * @return true to allow rendering of child components, false otherwise
    */
   public boolean evaluate()
   {
      boolean result = false;
      
      try
      {
         Object obj = getValue();
         if (obj instanceof Node)
         {
            result = getEvaluator().evaluate((Node)obj);
         }
      }
      catch (Exception err)
      {
         // return default value on error
         s_logger.debug("Error during ActionInstanceEvaluator evaluation: " + err.getMessage());
      }
      
      return result;
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.evaluator = (ActionEvaluator)values[1];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[2];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.evaluator;
      return (values);
   }
   
   /** 
    * @return the ActionEvaluator to execute
    */
   public ActionEvaluator getEvaluator()
   {
      return this.evaluator;
   }
   
   /**
    * @param evaluator     The ActionEvaluator to execute
    */
   public void setEvaluator(ActionEvaluator evaluator)
   {
      this.evaluator = evaluator;
   }
   
   
   private ActionEvaluator evaluator;
}
