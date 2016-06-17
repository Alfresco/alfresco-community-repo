/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.util.Date;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Edit review as of date action.
 *
 * @author Roy Wetherall
 */
public class EditDispositionActionAsOfDateAction extends RMActionExecuterAbstractBase
{
    /** I18N */
    private static final String MSG_VALID_DATE_DISP_ASOF = "rm.action.valid-date-disp-asof";
    private static final String MSG_DISP_ASOF_LIFECYCLE_APPLIED = "rm.action.disp-asof-lifecycle-applied";

    /** Action name */
    public static final String NAME = "editDispositionActionAsOfDate";
    
    /** Action parameters */
    public static final String PARAM_AS_OF_DATE = "asOfDate";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (this.getNodeService().hasAspect(actionedUponNodeRef, ASPECT_DISPOSITION_LIFECYCLE))
        {
            // Get the action parameter
            Date asOfDate = (Date)action.getParameterValue(PARAM_AS_OF_DATE);
            if (asOfDate == null)
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_VALID_DATE_DISP_ASOF));
            }

            // Set the dispostion action as of date
            DispositionAction da = getDispositionService().getNextDispositionAction(actionedUponNodeRef);
            if (da != null)
            {
                getNodeService().setProperty(da.getNodeRef(), PROP_DISPOSITION_AS_OF, asOfDate);
            }
        }
        else
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_DISP_ASOF_LIFECYCLE_APPLIED));
        }
    }
}
