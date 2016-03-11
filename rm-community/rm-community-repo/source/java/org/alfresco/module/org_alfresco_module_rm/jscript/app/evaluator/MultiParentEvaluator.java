package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Determines whether a node has multiple parents within a file plan
 *
 * @author Roy Wetherall
 * @since 2.0
 */
public class MultiParentEvaluator extends BaseEvaluator
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator#evaluateImpl(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected boolean evaluateImpl(final NodeRef nodeRef)
    {
        return AuthenticationUtil.runAsSystem(new RunAsWork<Boolean>()
        {
            public Boolean doWork()
            {
               
                // get parent associations
                List<ChildAssociationRef> parents = nodeService.getParentAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
                int count = 0;
                for (ChildAssociationRef parent : parents)
                {
                    // count file plan component parents
                    if (nodeService.hasAspect(parent.getParentRef(), ASPECT_FILE_PLAN_COMPONENT))
                    {
                        count++;
                    }
                }

                // return true if more than one
                return (count > 1);
            }
        });
    }
}
