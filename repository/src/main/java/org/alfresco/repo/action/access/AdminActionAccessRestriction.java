package org.alfresco.repo.action.access;

import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;

import java.util.List;

public class AdminActionAccessRestriction implements ActionAccessRestriction {

    private AuthorityService authorityService;
    private NodeService nodeService;

    private static final List<String> CONTROLLED_ACTION_ACCESS_CONTEXT =
            List.of(ActionAccessRestriction.RULE_ACTION_CONTEXT, ActionAccessRestriction.V1_ACTION_CONTEXT);

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public void checkAccess(Action action) {
        if (isActionFromControlledContext(action)) {
            boolean isAdminOrSystemUser = authorityService.hasAdminAuthority() || AuthenticationUtil.isRunAsUserTheSystemUser();
            if (!isAdminOrSystemUser) {
                throw new ActionAccessException("Only admin or system user is allowed to execute this action");
            }
        }
    }

    @Override
    public void checkRunningActionAccess(Action action) {
        if (!isActionFromControlledContext(action) || isActionCausedByRule(action)) {
            //Already existing rules are deemed secure. Newly created/updated rules are verified
            return;
        }

        checkAccess(action);
    }

    private boolean isActionFromControlledContext(Action action) {
        return CONTROLLED_ACTION_ACCESS_CONTEXT.contains(ActionAccessRestriction.getActionContext(action));
    }

    private boolean isActionCausedByRule(Action action) {
        return nodeService.getParentAssocs(action.getNodeRef())
                .stream()
                .map(pa -> nodeService.getType(pa.getParentRef()))
                .filter(t -> RuleModel.RULE_MODEL_URI.equals(t.getNamespaceURI()))
                .filter(t -> RuleModel.RULE_MODEL_PREFIX.equals(t.getPrefixString()))
                .findFirst().isPresent();
    }
}
