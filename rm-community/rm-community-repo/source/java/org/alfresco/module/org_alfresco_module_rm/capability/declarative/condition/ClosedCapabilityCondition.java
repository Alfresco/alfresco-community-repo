 
package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * @author Roy Wetherall
 */
public class ClosedCapabilityCondition extends AbstractCapabilityCondition
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;
        if (recordFolderService.isRecordFolder(nodeRef))
        {
            result = recordFolderService.isRecordFolderClosed(nodeRef);
        }
        else if (recordService.isRecord(nodeRef))
        {
            final List<ChildAssociationRef> assocs = nodeService.getParentAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            
            result = AuthenticationUtil.runAsSystem(new RunAsWork<Boolean>()
            {
                @Override
                public Boolean doWork()
                {
                    for (ChildAssociationRef assoc : assocs)
                    {
                        NodeRef parent = assoc.getParentRef();
                        if (recordFolderService.isRecordFolder(parent) && recordFolderService.isRecordFolderClosed(parent))
                        {
                            return true;
                        }
                    }
                    return false;
                }
            });
          
        }
        return result;
    }

}
