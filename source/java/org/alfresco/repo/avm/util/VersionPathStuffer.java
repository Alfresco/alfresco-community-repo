/**
 * 
 */
package org.alfresco.repo.avm.util;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * A utility to build a (possibly long) String representation of
 * a collection of AVM path,versions. The path,versions can be recovered
 * by VersionPathUnStuffer.
 * @author britt
 */
public class VersionPathStuffer 
{
    /**
     * The internal buffer.
     */
    private StringBuilder fBuilder;
    
    /**
     * Whether any paths have been added yet.
     */
    private boolean fAnyAdded;
    
    /**
     * Make up one.
     */
    public VersionPathStuffer()
    {
        fBuilder = new StringBuilder();
        fAnyAdded = false;
    }
    
    /**
     * Add a version path expressed by the version and path.
     */
    public void add(int version, String path)
    {
        if (fAnyAdded)
        {
            fBuilder.append(';');
        }
        fBuilder.append(path);
        fBuilder.append('@');
        fBuilder.append(version);
        fAnyAdded = true;
    }
    
    /**
     * Add a version path expressed as a NodeRef.
     */
    public void add(NodeRef nodeRef)
    {
        Pair<Integer, String> versionPath =
            AVMNodeConverter.ToAVMVersionPath(nodeRef);
        add(versionPath.getFirst(), versionPath.getSecond());
    }
    
    /**
     * Get the stuffed String version of the Version/Paths contained in this.
     */
    public String toString()
    {
        return fBuilder.toString();
    }
}
