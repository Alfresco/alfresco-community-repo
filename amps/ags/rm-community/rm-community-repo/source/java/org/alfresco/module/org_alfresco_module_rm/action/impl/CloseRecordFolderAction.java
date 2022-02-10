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

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Action to close the records folder
 *
 * @author Roy Wetherall
 */
public class CloseRecordFolderAction extends RMActionExecuterAbstractBase
{
    /** Parameter names */
    public static final String PARAM_CLOSE_PARENT = "closeParent";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, final NodeRef actionedUponNodeRef)
    {
        if (eligibleForAction(actionedUponNodeRef))
        {
            // do the work of creating the record as the system user
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    getRecordFolderService().closeRecordFolder(actionedUponNodeRef);

                    return null;
                }
            });
        }
    }

    /**
     * Helper method to check the actioned upon node reference to decide to execute the action
     * The preconditions are:
     *  - The node must exist
     *  - The node must not be frozen
     *
     * @param actionedUponNodeRef node reference
     * @return Return true if the node reference passes all the preconditions for executing the action, false otherwise
     */
    private boolean eligibleForAction(NodeRef actionedUponNodeRef)
    {
        boolean result = false;
        if (getNodeService().exists(actionedUponNodeRef) &&
                !getFreezeService().isFrozen(actionedUponNodeRef) &&
                !TYPE_UNFILED_RECORD_FOLDER.equals(getNodeService().getType(actionedUponNodeRef)))
        {
            result = true;
        }
        return result;
    }
}
