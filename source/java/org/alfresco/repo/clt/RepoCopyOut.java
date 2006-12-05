/**
 * 
 */
package org.alfresco.repo.clt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Like cp from Repo to a local filesystem.
 * @author britt
 */
public class RepoCopyOut extends CltBase 
{
    private static Object [] flagDefs = { "-r", 0, "-v", 0 };
    
    private static String USAGE = "usage: RepoCopyOut [-r] [-v] repopath fspath";
    
    private boolean fVerbose;
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.clt.CltBase#run(java.util.Map, java.util.List)
     */
    @Override
    protected void run(Map<String, List<String>> flags, List<String> args) 
    {
        if (flags.containsKey("-v"))
        {
            fVerbose = true;
        }
        else
        {
            fVerbose = false;
        }
        String [] pathBase = splitPath(args.get(0));
        if (pathBase.length != 2)
        {
            System.err.println("Cannot copy out root.");
            fContext.close();
            System.exit(1);
        }
        String srcPath = args.get(0);
        while (srcPath.startsWith("/"))
        {
            srcPath = srcPath.substring(1);
        }
        NodeRef root = fRepoRemote.getRoot();
        Pair<NodeRef, Boolean> src = fRepoRemote.lookup(root, srcPath);
        if (src == null)
        {
            System.err.println(srcPath + "Not Found.");
            fContext.close();
            System.exit(1);
        }
        if (flags.containsKey("-r"))
        {
            recursiveCopy(src, pathBase[1], args.get(1));
            return;
        }
        if (src.getSecond())
        {
            System.err.println(srcPath + " is not a file.");
            fContext.close();
            System.exit(1);
        }
        File dest = new File(args.get(1));
        if (dest.exists())
        {
            if (!dest.isDirectory())
            {
                System.err.println("Destination must be a directory.");
                fContext.close();
                System.exit(1);
            }
            try
            {
                InputStream in = fRepoRemote.readFile(src.getFirst());
                OutputStream out = new FileOutputStream(args.get(1) + File.separator + pathBase[1]);
                copyStream(in, out);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                fContext.close();
                System.exit(1);
            }
        }
        else
        {
            try
            {
                InputStream in = fRepoRemote.readFile(src.getFirst());
                OutputStream out = new FileOutputStream(args.get(1));
                copyStream(in, out);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                fContext.close();
                System.exit(1);
            }
        }
    }

    private void recursiveCopy(Pair<NodeRef, Boolean> src, String name, String dst)
    {
        String newDst = dst + File.separator + name;
        if (fVerbose)
        {
            System.out.println(name + " -> " + dst);
        }
        if (src.getSecond())
        {
            File destFile = new File(newDst);
            destFile.mkdir();
            Map<String, Pair<NodeRef, Boolean>> listing = fRepoRemote.getListing(src.getFirst());
            for (Map.Entry<String, Pair<NodeRef, Boolean>> entry : listing.entrySet())
            {
                recursiveCopy(entry.getValue(), entry.getKey(), newDst);
            }
            return;
        }
        try
        {
            InputStream in = fRepoRemote.readFile(src.getFirst());
            OutputStream out = new FileOutputStream(newDst);
            copyStream(in, out);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fContext.close();
            System.exit(1);
        }
    }

    public static void main(String[] args) 
    {
        RepoCopyOut me = new RepoCopyOut();
        me.exec(args, flagDefs, 2, USAGE);
    }
}
