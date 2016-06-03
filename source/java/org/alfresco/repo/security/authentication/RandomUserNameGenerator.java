package org.alfresco.repo.security.authentication;

import org.alfresco.repo.tenant.TenantService;
import org.apache.commons.lang.RandomStringUtils;

/**
 * Generates a user name based upon a random numeric 
 *
 */
public class RandomUserNameGenerator implements UserNameGenerator
{
    // user name length property
    private int userNameLength;
    
    /**
     * Returns a generated user name
     * 
     * @return the generated user name
     */
    public String generateUserName(String firstName, String lastName, String emailAddress, int seed)
    {
        String userName = RandomStringUtils.randomNumeric(getUserNameLength());
        return userName;
    }

	public void setUserNameLength(int userNameLength) {
		this.userNameLength = userNameLength;
	}

	public int getUserNameLength() {
		return userNameLength;
	}
}
