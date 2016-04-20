
package org.alfresco.repo.virtual.bundle;

import org.alfresco.repo.lock.mem.LockState;
import org.alfresco.repo.lock.traitextender.LockableAspectInterceptorExtension;
import org.alfresco.repo.lock.traitextender.LockableAspectInterceptorTrait;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ref.GetActualNodeRefMethod;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.traitextender.SpringBeanExtension;

public class VirtualLockableAspectInterceptorExtension extends
            SpringBeanExtension<LockableAspectInterceptorExtension, LockableAspectInterceptorTrait> implements
            LockableAspectInterceptorExtension
{

    private ActualEnvironment environment;

    public VirtualLockableAspectInterceptorExtension()
    {
        super(LockableAspectInterceptorTrait.class);
    }

    public void setEnvironment(ActualEnvironment environment)
    {
        this.environment = environment;
    }

    @Override
    public LockState getLockState(NodeRef nodeRef)
    {
        if(Reference.isReference(nodeRef)){
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef actualNodeRef = reference.execute(new GetActualNodeRefMethod(environment));
            return getTrait().traitImplOf_getLockState(actualNodeRef);
        }
        return getTrait().traitImplOf_getLockState(nodeRef);
    }

}
