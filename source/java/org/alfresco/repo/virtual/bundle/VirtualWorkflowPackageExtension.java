
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
    private VirtualStore virtualStore;

    public VirtualWorkflowPackageExtension()
    {
        super(WorkflowPackageTrait.class);
    }

    public void setVirtualStore(VirtualStore virtualStore)
    {
        this.virtualStore = virtualStore;
    }

    @Override
    public NodeRef createPackage(NodeRef container)
    {
        return getTrait().createPackage(virtualStore.materializeIfPossible(container));
    }

    @Override
    public void deletePackage(NodeRef container)
    {
        getTrait().deletePackage(virtualStore.materializeIfPossible(container));
    }

    @Override
    public List<String> getWorkflowIdsForContent(NodeRef packageItem)
    {
        return getTrait().getWorkflowIdsForContent(virtualStore.materializeIfPossible(packageItem));
    }

    @Override
    public boolean setWorkflowForPackage(WorkflowInstance instance)
    {
        return getTrait().setWorkflowForPackage(instance);
    }

}
