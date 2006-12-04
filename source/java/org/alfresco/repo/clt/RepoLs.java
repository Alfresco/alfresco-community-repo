/**
 * 
 */
package org.alfresco.repo.clt;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * List the contents of a directory in a repo.
 * @author britt
 */
public class RepoLs extends CltBase 
{
    private static Object [] flagDefs = { "-R", 0 };
    
    private static String USAGE = "usage: RepoLs path";
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.clt.CltBase#run(java.util.Map, java.util.List)
     */
    @Override
    protected void run(Map<String, List<String>> flags, List<String> args) 
    {
        NodeRef root = fRepoRemote.getRoot();
        NodeRef dir = null;
        String path = args.get(0);
        if (path.equals("/"))
        {
            dir = root;
        }
        else
        {
            while (path.startsWith("/"))
            {
                path = path.substring(1);
            }
            dir = fRepoRemote.lookup(root, path);
            if (dir == null)
            {
                System.err.println(path + " does not exist");
                fContext.close();
                System.exit(1);
            }
        }
        Map<String, Pair<NodeRef, QName>> listing = fRepoRemote.getListing(dir);
        for (String name : listing.keySet())
        {
            System.out.println(name + "\t" + listing.get(name));
        }
    }

    public static void main(String[] args)
    {
        RepoLs me = new RepoLs();
        me.exec(args, flagDefs, 1, USAGE);
    }
}
