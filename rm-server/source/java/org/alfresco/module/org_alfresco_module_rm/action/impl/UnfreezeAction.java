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

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Unfreeze Action
 * 
 * @author Roy Wetherall
 */
public class UnfreezeAction extends RMActionExecuterAbstractBase
{
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
      freezeService.unFreeze(actionedUponNodeRef);
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
      return this.nodeService.hasAspect(filePlanComponent, ASPECT_FROZEN);
   }

}