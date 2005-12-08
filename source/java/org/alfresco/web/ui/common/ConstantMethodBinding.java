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
package org.alfresco.web.ui.common;

import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;

public class ConstantMethodBinding extends MethodBinding implements StateHolder
{
   private String outcome = null;
   private boolean transientFlag = false;

   public ConstantMethodBinding()
   {
   }

   public ConstantMethodBinding(String yourOutcome)
   {
      outcome = yourOutcome;
   }

   public Object invoke(FacesContext context, Object params[])
   {
      return outcome;
   }

   public Class getType(FacesContext context)
   {
      return String.class;
   }

   public Object saveState(FacesContext context)
   {
      return outcome;
   }

   public void restoreState(FacesContext context, Object state)
   {
      outcome = (String) state;
   }

   public boolean isTransient()
   {
      return (this.transientFlag);
   }

   public void setTransient(boolean transientFlag)
   {
      this.transientFlag = transientFlag;
   }
}
