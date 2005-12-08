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
package org.alfresco.web.ui.common.component.evaluator;

/**
 * @author kevinr
 * 
 * Evaluates to true if the value suppied is a boolean string of "true".
 */
public class BooleanEvaluator extends BaseEvaluator
{
   /**
    * Evaluate against the component attributes. Return true to allow the inner
    * components to render, false to hide them during rendering.
    * 
    * @return true to allow rendering of child components, false otherwise
    */
   public boolean evaluate()
   {
      boolean result = false;
      
      try
      {
         if (getValue() instanceof Boolean)
         {
            result = ((Boolean)getValue()).booleanValue();
         }
         else
         {
            result = Boolean.valueOf((String)getValue()).booleanValue();
         }
      }
      catch (Exception err)
      {
         // return default value on error
         s_logger.debug("Unable to evaluate value to boolean: " + getValue());
      }
      
      return result;
   }
}
