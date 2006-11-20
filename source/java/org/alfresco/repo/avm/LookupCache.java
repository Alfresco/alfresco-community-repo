/**
 * 
 */
package org.alfresco.repo.avm;

import org.alfresco.repo.avm.util.SimplePath;
import org.apache.log4j.Logger;

/**
 * All lookup traffic goes through here.
 * @author britt
 */
public class LookupCache 
{
    private static Logger fgLogger = Logger.getLogger(LookupCache.class);

    private AVMNodeDAO fAVMNodeDAO;
    
    /**
     * Make one up.
     */
    public LookupCache()
    {
    }
    
    /**
     * Set up the node dao.
     * @param dao The dao to set.
     */
    public void setAvmNodeDAO(AVMNodeDAO dao)
    {
        fAVMNodeDAO = dao;
    }
    
    public Lookup lookup(AVMStore store, int version, SimplePath path, 
                         boolean write, boolean includeDeleted)
    {
        // Make up a Lookup to hold the results.
        if (path.size() == 0)
        {
            return null;
        }        
        Lookup result = new Lookup(store, store.getName());
        // Grab the root node to start the lookup.
        DirectoryNode dir = null;
        // Versions less than 0 mean get current.
        if (version < 0)
        {
            dir = store.getRoot();
        }
        else
        {
            dir = fAVMNodeDAO.getAVMStoreRoot(store, version);
        }
        if (dir == null)
        {
            return null;
        }
        // Add an entry for the root.
        result.add(dir, "", write);
        dir = (DirectoryNode)result.getCurrentNode();
        if (path.size() == 1 && path.get(0).equals(""))
        {
            return result;
        }
        // Now look up each path element in sequence up to one
        // before the end.
        for (int i = 0; i < path.size() - 1; i++)
        {
            AVMNode child = dir.lookupChild(result, path.get(i), includeDeleted);
            if (child == null)
            {
                return null;
            }
            // Every element that is not the last needs to be a directory.
            if (child.getType() != AVMNodeType.PLAIN_DIRECTORY &&
                child.getType() != AVMNodeType.LAYERED_DIRECTORY)
            {
                return null;
            }
            result.add(child, path.get(i), write);
            dir = (DirectoryNode)result.getCurrentNode();
        }
        // Now look up the last element.
        AVMNode child = dir.lookupChild(result, path.get(path.size() - 1),
                                        includeDeleted);
        if (child == null)
        {
            return null;
        }
        result.add(child, path.get(path.size() - 1), write);
        return result;        
    }
}
