/**
 * 
 */
package org.alfresco.repo.avm.util;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Takes a String representation of a list of Version/Paths (created
 * by VersionPathStuffer) and gives you back a list of Version/Paths
 * @author britt
 */
public final class VersionPathUnstuffer 
{
    /**
     * The unpacked version paths.
     */
    private List<Pair<Integer, String>> fVersionPaths;
    
    /**
     * Construct one and in the process unstuff the String.
     */
    public VersionPathUnstuffer(String stuffed)
    {
        fVersionPaths = new ArrayList<Pair<Integer, String>>();
        String[] versionPaths = stuffed.split(";");
        for (String path : versionPaths)
        {
            String [] pathVersion = path.split("@");
            Pair<Integer, String> item = 
                new Pair<Integer, String>(new Integer(pathVersion[1]),
                                          pathVersion[0]);
            fVersionPaths.add(item);
        }
    }
    
    /**
     * Get the raw list of Version/Paths.
     */
    public List<Pair<Integer, String>> getVersionPaths()
    {
        return fVersionPaths;
    }
    
    /**
     * Get the Version/Paths as NodeRefs.
     */
    public List<NodeRef> getNodeRefs()
    {
        List<NodeRef> result = new ArrayList<NodeRef>();
        for (Pair<Integer, String> item : fVersionPaths)
        {
            result.add(AVMNodeConverter.ToNodeRef(item.getFirst(), item.getSecond()));   
        }
        return result;
    }
}
