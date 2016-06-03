package org.alfresco.repo.security.authentication;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.lang.RandomStringUtils;

/**
 * Tenant Aware user name generator generates user names for each specific tenant.
 * 
 * It does this by delegating to other user name generators.

 */
public class TenantAwareUserNameGenerator implements UserNameGenerator
{    
    private TenantService tenantService;
    
    private UserNameGenerator generator;
    
    public void init()
    {
        PropertyCheck.mandatory(this, "tenantService", tenantService);
        PropertyCheck.mandatory(this, "generator", generator);
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
        
    /**
     * Returns a generated user name
     * 
     * @return the generated user name
     */
    public String generateUserName(String firstName, String lastName, String emailAddress, int seed)
    {
        String userName = generator.generateUserName(firstName, lastName, emailAddress, seed);
        if (tenantService.isEnabled())
        {
            userName = tenantService.getDomainUser(userName, tenantService.getCurrentUserDomain());
        }
        return userName;
    }

	public void setGenerator(UserNameGenerator generator) {
		this.generator = generator;
	}

	public UserNameGenerator getGenerator() {
		return generator;
	}
}
