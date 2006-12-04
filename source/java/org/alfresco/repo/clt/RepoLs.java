/**
 * 
 */
package org.alfresco.repo.clt;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
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
            Pair<NodeRef, Boolean> info = fRepoRemote.lookup(root, path);
            if (info == null)
            {
                System.err.println(path + " does not exist");
                fContext.close();
                System.exit(1);
            }
            dir = info.getFirst();
        }
        if (flags.containsKey("-R"))
        {
            recursiveList(dir, 0);
            return;
        }
        Map<String, Pair<NodeRef, Boolean>> listing = fRepoRemote.getListing(dir);
        for (String name : listing.keySet())
        {
            System.out.println(name + "\t" + listing.get(name));
        }
    }

    private void recursiveList(NodeRef dir, int indent)
    {
        Map<String, Pair<NodeRef, Boolean>> listing = fRepoRemote.getListing(dir);
        for (Map.Entry<String, Pair<NodeRef, Boolean>> entry : listing.entrySet())
        {
            for (int i = 0; i < indent; i++)
            {
                System.out.print(' ');
            }
            System.out.println(entry.getKey() + '\t' + entry.getValue());
            if (entry.getValue().getSecond())
            {
                recursiveList(entry.getValue().getFirst(), indent + 2);
            }
        }
    }
    
    public static void main(String[] args)
    {
        RepoLs me = new RepoLs();
        me.exec(args, flagDefs, 1, USAGE);
    }
}
