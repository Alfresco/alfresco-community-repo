package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.namespace.QName;

/**
 * rma:extendedSecurity behaviour bean
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:extendedSecurity"
)
public class ExtendedSecurityAspect extends BaseBehaviourBean
{
    /** extended security service */
    protected ExtendedSecurityService extendedSecurityService;

    /**
     * @param extendedSecurityService   extended security service
     */
    public void setExtendedSecurityService(ExtendedSecurityService extendedSecurityService)
    {
        this.extendedSecurityService = extendedSecurityService;
    }

    /**
     * Copy callback.
     *
     * Aspect should not be copied.
     */
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            policy = "alf:getCopyCallback"
    )
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return new DoNothingCopyBehaviourCallback();
    }
}
