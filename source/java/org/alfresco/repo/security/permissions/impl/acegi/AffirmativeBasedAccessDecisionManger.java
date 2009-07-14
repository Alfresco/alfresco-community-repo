package org.alfresco.repo.security.permissions.impl.acegi;

import java.util.Iterator;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AccessStatus;

import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.vote.AccessDecisionVoter;
import net.sf.acegisecurity.vote.AffirmativeBased;

public class AffirmativeBasedAccessDecisionManger extends AffirmativeBased
{
    public AccessStatus pre(Object object, ConfigAttributeDefinition attr)
    {
        Iterator iter = this.getDecisionVoters().iterator();
        int deny = 0;

        while (iter.hasNext())
        {
            AccessDecisionVoter voter = (AccessDecisionVoter) iter.next();
            int result = voter.vote(AuthenticationUtil.getFullAuthentication(), object, attr);

            switch (result)
            {
            case AccessDecisionVoter.ACCESS_GRANTED:
                return AccessStatus.ALLOWED;

            case AccessDecisionVoter.ACCESS_DENIED:
                deny++;

                break;

            default:
                break;
            }
        }

        if (deny > 0)
        {
            return AccessStatus.DENIED;
        }

        // To get this far, every AccessDecisionVoter abstained
        if (this.isAllowIfAllAbstainDecisions())
        {
            return AccessStatus.ALLOWED;
        }
        else
        {
            return AccessStatus.DENIED;
        }

    }
}
