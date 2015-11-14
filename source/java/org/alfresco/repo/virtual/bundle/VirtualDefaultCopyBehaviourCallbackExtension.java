package org.alfresco.repo.virtual.bundle;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.traitextender.DefaultCopyBehaviourCallbackExtension;
import org.alfresco.repo.copy.traitextender.DefaultCopyBehaviourCallbackTrait;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.service.namespace.QName;
import org.alfresco.traitextender.SpringBeanExtension;

public class VirtualDefaultCopyBehaviourCallbackExtension extends
SpringBeanExtension<DefaultCopyBehaviourCallbackExtension, DefaultCopyBehaviourCallbackTrait> implements
DefaultCopyBehaviourCallbackExtension
{
    private ActualEnvironment environment;

    public VirtualDefaultCopyBehaviourCallbackExtension()
    {
        super(DefaultCopyBehaviourCallbackTrait.class);
    }

    public void setEnvironment(ActualEnvironment environment)
    {
        this.environment = environment;
    }

    @Override
    public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
    {
        if(environment.isSubClass(classQName, ContentModel.TYPE_FOLDER)){
            if(copyDetails.getSourceNodeAspectQNames().contains(VirtualContentModel.ASPECT_VIRTUAL)){
                return false;
            }
        }
        return getTrait().getMustCopy(classQName,
                                      copyDetails);
    }

}
