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
package org.alfresco.repo.admin.patch.impl;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.workflow.WorkflowPackageImpl;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Ensures the system folder for Workflows is created.
 * 
 * @author davidc
 */
public class SystemWorkflowFolderPatch extends AbstractPatch
{
    private static final String MSG_CREATED = "patch.systemWorkflowFolder.result.created";
    
    private WorkflowPackageImpl workflowPackageImpl;

    public void setWorkflowPackageImpl(WorkflowPackageImpl workflowPackageImpl)
    {
        this.workflowPackageImpl = workflowPackageImpl;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {
        NodeRef systemContainer = workflowPackageImpl.createSystemWorkflowContainer();
        return I18NUtil.getMessage(MSG_CREATED, systemContainer);
    }

}
