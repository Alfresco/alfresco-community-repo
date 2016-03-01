 
package org.alfresco.module.org_alfresco_module_rm.security;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;

/**
 * @author Roy Wetherall
 * @since 2.1
 */
public class FilePlanAuthenticationServiceImpl implements FilePlanAuthenticationService
{
    /** Default rm admin user values */
    @Deprecated
    public static final String DEFAULT_RM_ADMIN_USER = "rmadmin";

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.FilePlanAuthenticationService#getRMAdminUserName()
     */
    @Override
    @Deprecated
    public String getRmAdminUserName()
    {
        return AuthenticationUtil.getAdminUserName();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.FilePlanAuthenticationService#runAsRMAdmin(org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork)
     */
    @Override
    @Deprecated
    public <R> R runAsRmAdmin(RunAsWork<R> runAsWork)
    {
        return AuthenticationUtil.runAs(runAsWork, AuthenticationUtil.getAdminUserName());
    }
}
