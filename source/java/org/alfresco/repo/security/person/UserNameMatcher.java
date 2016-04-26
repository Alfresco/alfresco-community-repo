package org.alfresco.repo.security.person;

/**
 * Check if userNames match
 * @author andyh
 * @since 3.1
 */
public interface UserNameMatcher
{
    /**
     * Do the two user names match?
     */
    public boolean matches(String userName1, String userName2);
    
    public boolean getUserNamesAreCaseSensitive();
  
    public boolean getDomainNamesAreCaseSensitive();
    
    public String getDomainSeparator();
}
