/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
