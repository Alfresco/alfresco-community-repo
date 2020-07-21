/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.virtual.bundle;

import java.util.List;

import org.alfresco.repo.virtual.store.VirtualStore;
import org.alfresco.repo.workflow.traitextender.WorkflowPackageExtension;
import org.alfresco.repo.workflow.traitextender.WorkflowPackageTrait;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.traitextender.SpringBeanExtension;

public class VirtualWorkflowPackageExtension extends
            SpringBeanExtension<WorkflowPackageExtension, WorkflowPackageTrait> implements WorkflowPackageExtension
{
    private VirtualStore smartStore;

    public VirtualWorkflowPackageExtension()
    {
        super(WorkflowPackageTrait.class);
    }

    public void setSmartStore(VirtualStore smartStore)
    {
        this.smartStore = smartStore;
    }

    @Override
    public NodeRef createPackage(NodeRef container)
    {
        return getTrait().createPackage(smartStore.materializeIfPossible(container));
    }

    @Override
    public void deletePackage(NodeRef container)
    {
        getTrait().deletePackage(smartStore.materializeIfPossible(container));
    }

    @Override
    public List<String> getWorkflowIdsForContent(NodeRef packageItem)
    {
        return getTrait().getWorkflowIdsForContent(smartStore.materializeIfPossible(packageItem));
    }

    @Override
    public boolean setWorkflowForPackage(WorkflowInstance instance)
    {
        return getTrait().setWorkflowForPackage(instance);
    }

}
