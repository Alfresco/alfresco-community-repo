package org.alfresco.module.org_alfresco_module_rm.audit.event;

import static org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Audits recordable version policy property updates
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
@BehaviourBean
public class RecordableVersionPolicyAuditEvent extends AuditEvent implements OnUpdatePropertiesPolicy
{
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       type = "cm:cmobject"
    )
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if (before.get(PROP_RECORDABLE_VERSION_POLICY) != after.get(PROP_RECORDABLE_VERSION_POLICY))
        {
            recordsManagementAuditService.auditEvent(nodeRef, getName(), before, after, true, true);
        }
    }
}
