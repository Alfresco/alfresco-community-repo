/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
