 
package org.alfresco.module.org_alfresco_module_rm.security;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;

/**
 * File plan authentication service.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public interface FilePlanAuthenticationService
{
    /**
     * @return  rm admin user name
     *
     * @deprecated as of 2.2, use {@link AuthenticationUtil#getAdminUserName()}
     */
    String getRmAdminUserName();

    /**
     * Run provided work as the global rm admin user.
     *
     * @param <R>       return type
     * @param runAsWork work to execute as the rm admin user
     * @return R        result of work execution
     *
     * @deprecated as of 2.2, use {@link AuthenticationUtil#runAs(RunAsWork, AuthenticationUtil#getAdminUserName())}
     */
    <R> R runAsRmAdmin(RunAsWork<R> runAsWork);
}
