package org.alfresco.repo.security.person;

import org.alfresco.util.Pair;

public class UserNameMatcherImpl implements UserNameMatcher
{
    private boolean userNamesAreCaseSensitive = false;

    private boolean domainNamesAreCaseSensitive = false;

    private String domainSeparator = "";
    
    public boolean getUserNamesAreCaseSensitive()
    {
        return userNamesAreCaseSensitive;
    }

    public void setUserNamesAreCaseSensitive(boolean userNamesAreCaseSensitive)
    {
        this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
    }

    public boolean getDomainNamesAreCaseSensitive()
    {
        return domainNamesAreCaseSensitive;
    }

    public void setDomainNamesAreCaseSensitive(boolean domainNamesAreCaseSensitive)
    {
        this.domainNamesAreCaseSensitive = domainNamesAreCaseSensitive;
    }

    public String getDomainSeparator()
    {
        return domainSeparator;
    }

    public void setDomainSeparator(String domainSeparator)
    {
        this.domainSeparator = domainSeparator;
    }

    public boolean matches(String realUserName, String searchUserName)
    {
        // note: domain string may be empty
        Pair<String, String> real = splitByDomain(realUserName, domainSeparator);
        Pair<String, String> search = splitByDomain(searchUserName, domainSeparator);

        return (((userNamesAreCaseSensitive && (real.getFirst().equals(search.getFirst()))) || (!userNamesAreCaseSensitive && (real.getFirst().equalsIgnoreCase(search
                .getFirst())))) &&

        ((domainNamesAreCaseSensitive && (real.getSecond().equals(search.getSecond()))) || (!domainNamesAreCaseSensitive && (real.getSecond().equalsIgnoreCase(search
                .getSecond())))));
    }

    // Trailing domain only
    private Pair<String, String> splitByDomain(String name, String domainSeparator)
    {
        int idx = name.lastIndexOf(domainSeparator);
        if (idx != -1)
        {
            if ((idx + 1) > name.length())
            {
                return new Pair<String, String>(name.substring(0, idx), "");
            }
            else
            {
                return new Pair<String, String>(name.substring(0, idx), name.substring(idx + 1));
            }
        }

        return new Pair<String, String>(name, "");
    }
}