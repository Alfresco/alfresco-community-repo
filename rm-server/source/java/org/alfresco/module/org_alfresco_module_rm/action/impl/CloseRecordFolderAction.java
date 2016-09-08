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

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Action to close the records folder
 * 
 * @author Roy Wetherall
 */
public class CloseRecordFolderAction extends RMActionExecuterAbstractBase
{
    /** Logger */
    private static Log logger = LogFactory.getLog(CloseRecordFolderAction.class);

    /** I18N */
    private static final String MSG_CLOSE_RECORD_FOLDER_NOT_FOLDER = "rm.action.close-record-folder-not-folder";

    /** Parameter names */
    public static final String PARAM_CLOSE_PARENT = "closeParent";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (nodeService.exists(actionedUponNodeRef) == true && 
            freezeService.isFrozen(actionedUponNodeRef) == false)
        {
            if (recordService.isRecord(actionedUponNodeRef))
            {
                ChildAssociationRef assocRef = nodeService.getPrimaryParent(actionedUponNodeRef);
                if (assocRef != null)
                {
                    actionedUponNodeRef = assocRef.getParentRef();
                }
            }

            if (this.recordsManagementService.isRecordFolder(actionedUponNodeRef) == true)
            {
                Boolean isClosed = (Boolean) this.nodeService.getProperty(actionedUponNodeRef, PROP_IS_CLOSED);
                if (Boolean.FALSE.equals(isClosed) == true)
                {
                    this.nodeService.setProperty(actionedUponNodeRef, PROP_IS_CLOSED, true);
                }
            }
            else
            {
                if (logger.isWarnEnabled())
                    logger.warn(I18NUtil.getMessage(MSG_CLOSE_RECORD_FOLDER_NOT_FOLDER, actionedUponNodeRef.toString()));
            }
        }
    }
}
