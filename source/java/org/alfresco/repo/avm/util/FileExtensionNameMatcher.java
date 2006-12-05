/**
 * 
 */
package org.alfresco.repo.avm.util;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.util.NameMatcher;

/**
 * NameMatcher that matches a list of extensions (case insensitively).
 * @author britt
 */
public class FileExtensionNameMatcher implements NameMatcher 
{
    /**
     * The extensions to match.
     */
    private List<String> fExtensions;
    
    /**
     * Default constructor.
     */
    public FileExtensionNameMatcher()
    {
        fExtensions = new ArrayList<String>();
    }
    
    /**
     * Set the extensions case insensitively.
     * @param extensions
     */
    public void setExtensions(List<String> extensions)
    {
        for (String extension : extensions)
        { 
            fExtensions.add(extension.toLowerCase());
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.util.NameMatcher#matches(java.lang.String)
     */
    public boolean matches(String name) 
    {
        String lcName = name.toLowerCase();
        for (String ext : fExtensions)
        {
            if (lcName.endsWith(ext))
            {
                return true;
            }
        }
        return false;
    }
}
