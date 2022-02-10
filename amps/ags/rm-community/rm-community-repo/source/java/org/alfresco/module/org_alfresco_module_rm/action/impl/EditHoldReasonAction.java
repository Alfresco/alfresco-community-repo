/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.action.impl;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Edit freeze reason Action
 *
 * @author Roy Wetherall
 */
public class EditHoldReasonAction extends RMActionExecuterAbstractBase
{
   private static final String MSG_HOLD_EDIT_REASON_NONE = "rm.action.hold-edit-reason-none";
   private static final String MSG_HOLD_EDIT_TYPE = "rm.action.hold-edit-type";

   /** Parameter names */
   public static final String PARAM_REASON = "reason";

   /**
    * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
    */
   @SuppressWarnings("deprecation")
   @Override
   protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
   {
      if (getFreezeService().isHold(actionedUponNodeRef))
      {
         // Get the property values
         String reason = (String) action.getParameterValue(PARAM_REASON);
         if (StringUtils.isBlank(reason))
         {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_HOLD_EDIT_REASON_NONE));
         }

         // Update hold reason
         getFreezeService().updateReason(actionedUponNodeRef, reason);
      }
      else
      {
         throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_HOLD_EDIT_TYPE, TYPE_HOLD.toString(), actionedUponNodeRef.toString()));
      }
   }
}
