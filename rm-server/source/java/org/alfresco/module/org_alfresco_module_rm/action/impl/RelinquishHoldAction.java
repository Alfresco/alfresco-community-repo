/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Relinquish Hold Action
 * 
 * @author Roy Wetherall
 */
public class RelinquishHoldAction extends RMActionExecuterAbstractBase
{
   /** I18N */
   private static final String MSG_NOT_HOLD_TYPE = "rm.action.not-hold-type";

   /** Freeze Service */
   private FreezeService freezeService;

   /**
    * Set freeze service
    * 
    * @param freezeService freeze service
    */
   public void setFreezeService(FreezeService freezeService)
   {
      this.freezeService = freezeService;
   }

   /**
    * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
    */
   @Override
   protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
   {
      if (freezeService.isHold(actionedUponNodeRef))
      {
         freezeService.relinquish(actionedUponNodeRef);
      }
      else
      {
         throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NOT_HOLD_TYPE, TYPE_HOLD.toString(), actionedUponNodeRef.toString()));
      }
   }

   /**
    * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#getProtectedAspects()
    */
   @Override
   public Set<QName> getProtectedAspects()
   {
      HashSet<QName> qnames = new HashSet<QName>();
      qnames.add(ASPECT_FROZEN);
      return qnames;
   }

   /**
    * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#isExecutableImpl(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, boolean)
    */
   @Override
   protected boolean isExecutableImpl(NodeRef filePlanComponent, Map<String, Serializable> parameters, boolean throwException)
   {
      QName nodeType = this.nodeService.getType(filePlanComponent);
      if (this.dictionaryService.isSubClass(nodeType, TYPE_HOLD) == true)
      {
         return true;
      }
      else
      {
         if(throwException)
         {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NOT_HOLD_TYPE, TYPE_HOLD.toString(), filePlanComponent.toString()));
         }
         else
         {
            return false;
         }
      }
   }

}