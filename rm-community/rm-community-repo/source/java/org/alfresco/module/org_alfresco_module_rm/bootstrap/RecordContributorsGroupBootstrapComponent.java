 
package org.alfresco.module.org_alfresco_module_rm.bootstrap;

import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * Record contributors group bootstrap component
 * 
 * @author Roy Wetherall
 * @since  2.3
 */
public class RecordContributorsGroupBootstrapComponent
{
    // default record contributors group
    public static final String RECORD_CONTRIBUTORS = "RECORD_CONTRIBUTORS";
    public static final String GROUP_RECORD_CONTRIBUTORS = "GROUP_" + RECORD_CONTRIBUTORS;
    
    /** authority service */
    private AuthorityService authorityService;
    
    /** authentication utils */
    private AuthenticationUtil authenticationUtil;
    
    /**
     * @param authorityService  authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * @param authenticationUtil    authentication util
     */
    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil)
    {
        this.authenticationUtil = authenticationUtil;
    }
        
    /**
     * Create record contributor group
     */
    public void createRecordContributorsGroup()
    {
        if (!authorityService.authorityExists(GROUP_RECORD_CONTRIBUTORS))
        {
            // create record contributors group
            authorityService.createAuthority(AuthorityType.GROUP, RECORD_CONTRIBUTORS);       
            
            // add the admin user 
            authorityService.addAuthority(GROUP_RECORD_CONTRIBUTORS, authenticationUtil.getAdminUserName());
        }        
    }
}
