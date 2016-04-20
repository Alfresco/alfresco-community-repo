package org.alfresco.repo.virtual.bundle;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.traitextender.SpringBeanExtension;
import org.alfresco.traitextender.Trait;

public abstract class VirtualSpringBeanExtension<E, T extends Trait> extends SpringBeanExtension<E, T>
{
    public VirtualSpringBeanExtension(Class<T> traitClass)
    {
        super(traitClass);
    }

    public boolean isVirtualContextFolder(NodeRef nodeRef, ActualEnvironment environment)
    {
        boolean isReference=Reference.isReference(nodeRef);
        boolean isFolder=environment.isSubClass(environment.getType(nodeRef),
                                                ContentModel.TYPE_FOLDER);
        boolean virtualContext=environment.hasAspect(nodeRef,VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT);
        return  isReference && isFolder && virtualContext;
    }
}
