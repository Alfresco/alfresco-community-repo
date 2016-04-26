package org.alfresco.repo.transfer;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * Details back from reading the manifest to say what is required to fulfill the manifest.
 *
 * @author Mark Rogers
 */
public class DeltaList
{
    /**
     * The set of requiredParts
     */
    
    private TreeSet<String> requiredParts = new TreeSet<String>();
    
    /**
     * get the list of URLs reqired by the manifest.
     * @return the list of required URLs
     */
    public Set<String> getRequiredParts()
    {
        return requiredParts;
    }
     
}
