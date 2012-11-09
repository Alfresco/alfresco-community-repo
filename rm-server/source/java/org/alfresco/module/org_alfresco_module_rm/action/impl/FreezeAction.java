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
 * Freeze Action
 * 
 * @author Roy Wetherall
 */
public class FreezeAction extends RMActionExecuterAbstractBase
{
   private static final String MSG_FREEZE_NO_REASON = "rm.action.freeze-no-reason";
   private static final String MSG_FREEZE_ONLY_RECORDS_FOLDERS = "rm.action.freeze-only-records-folders";

   /** Parameter names */
   public static final String PARAM_REASON = "reason";

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
      freezeService.freeze((String) action.getParameterValue(PARAM_REASON), actionedUponNodeRef);
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
    * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#getProtectedProperties()
    */
   @Override
   public Set<QName> getProtectedProperties()
   {
      HashSet<QName> qnames = new HashSet<QName>();
      qnames.add(PROP_HOLD_REASON);
      //TODO Add prop frozen at/by?
      return qnames;
   }

   /**
    * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#isExecutableImpl(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, boolean)
    */
   @Override
   protected boolean isExecutableImpl(NodeRef filePlanComponent, Map<String, Serializable> parameters, boolean throwException)
   {
      if (this.recordsManagementService.isRecord(filePlanComponent) == true ||
               this.recordsManagementService.isRecordFolder(filePlanComponent) == true)
      {
         // Get the property values
         if(parameters != null)
         {
            String reason = (String)parameters.get(PARAM_REASON);
            if (reason == null || reason.length() == 0)
            {
               if(throwException)
               {
                  throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_FREEZE_NO_REASON));
               }
               else
               {
                  return false;
               }
            }
         }
         return true;
      }
      else
      {
         if(throwException)
         {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_FREEZE_ONLY_RECORDS_FOLDERS));
         }
         else
         {
            return false;
         }
      }
   }

}