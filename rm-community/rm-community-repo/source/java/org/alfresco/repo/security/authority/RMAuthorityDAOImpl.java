 
package org.alfresco.repo.security.authority;

import java.util.Set;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.security.AuthorityType;

/**
 * This class extends {@link AuthorityDAOImpl}</br>
 * and overrides two methods from the original class</br>
 * </br>
 * addAuthorityNameIfMatches(Set<String> authorities, String authorityName, AuthorityType type)</br>
 * </br>
 * and</br>
 * </br>
 * addAuthorityNameIfMatches(Set<String> authorities, String authorityName, AuthorityType type, Pattern pattern)</br>
 */
public class RMAuthorityDAOImpl extends AuthorityDAOImpl
{
    protected void addAuthorityNameIfMatches(Set<String> authorities, String authorityName, AuthorityType type)
    {
        if (isAuthorityNameMatching(authorityName, type))
        {
            authorities.add(authorityName);
        }
    }

    protected void addAuthorityNameIfMatches(Set<String> authorities, String authorityName, AuthorityType type, Pattern pattern)
    {
        if (isAuthorityNameMatching(authorityName, type))
        {
            if (pattern == null)
            {
                authorities.add(authorityName);
            }
            else
            {
                if (pattern.matcher(getShortName(authorityName)).matches())
                {
                    authorities.add(authorityName);
                }
                else
                {
                    String displayName = getAuthorityDisplayName(authorityName);
                    if (displayName != null && pattern.matcher(displayName).matches())
                    {
                        authorities.add(authorityName);
                    }
                }
            }
        }
    }

    private boolean isAuthorityNameMatching(String authorityName, AuthorityType type)
    {
        boolean isMatching = false;
        if (type == null || AuthorityType.getAuthorityType(authorityName).equals(type) && !getAuthorityZones(authorityName).contains("APP.RM"))
        {
            isMatching = true;
        }
        return isMatching;
    }
}
