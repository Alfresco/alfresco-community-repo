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
package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Unlock a locked document.
 * 
 * @author Kevin Roast
 */
public class UnlockDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -7056759932698306087L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      if (node.isLocked())
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         CheckOutCheckInService checkOutCheckInService =  (CheckOutCheckInService) FacesHelper.getManagedBean(fc, "CheckoutCheckinService");
         if (checkOutCheckInService.getWorkingCopy(node.getNodeRef()) == null)
         {
            return true;
         }
      }
       
      return false;
   }
}
