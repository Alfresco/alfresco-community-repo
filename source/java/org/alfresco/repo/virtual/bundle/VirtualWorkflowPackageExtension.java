
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
