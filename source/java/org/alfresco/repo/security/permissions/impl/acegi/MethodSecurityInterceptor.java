package org.alfresco.repo.security.permissions.impl.acegi;

import org.alfresco.service.cmr.security.AccessStatus;

import net.sf.acegisecurity.ConfigAttributeDefinition;

public class MethodSecurityInterceptor extends net.sf.acegisecurity.intercept.method.aopalliance.MethodSecurityInterceptor
{

    public MethodSecurityInterceptor()
    {
        super();
    }

    public AccessStatus pre(Object object)
    {
        ConfigAttributeDefinition attr = this.obtainObjectDefinitionSource().getAttributes(object);
        if (this.getAccessDecisionManager() instanceof AffirmativeBasedAccessDecisionManger)
        {
            return ((AffirmativeBasedAccessDecisionManger)getAccessDecisionManager()).pre(object, attr); 
        }
        else
        {
            return AccessStatus.ALLOWED;
        }
    }

}
