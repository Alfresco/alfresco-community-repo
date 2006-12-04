/**
 * 
 */
package org.alfresco.repo.clt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Simplified cp from a local filesystem to the repo.
 * @author britt
 */
public class RepoCopyIn extends CltBase 
{
    private static Object [] flagDefs = { "-r", 0, "-v", 0 };
    
    private static String USAGE = "usage: RepoCopyIn fspath repopath";
    
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
        NodeRef root = fRepoRemote.getRoot();
        String path = args.get(1);
        while (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        Pair<NodeRef, Boolean> dst = fRepoRemote.lookup(root, path);
        if (flags.containsKey("-r"))
        {
            if (dst == null)
            {
                System.err.println(args.get(1) + " does not exist.");
                fContext.close();
                System.exit(1);
            }
            recursiveCopy(args.get(0), dst.getFirst());
            return;
        }
        File file = new File(args.get(0));
        if (!file.isFile())
        {
            System.err.println(args.get(0) + " not found, or not a file.");
            fContext.close();
            System.exit(1);
        }
        if (dst == null)
        {
            try
            {
                if (fVerbose)
                {
                    System.out.println(file.getName() + " -> " + args.get(1));
                }
                InputStream in =
                    new FileInputStream(file);
                OutputStream out = fRepoRemote.createFile(root, path);
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
            if (!dst.getSecond())
            {
                System.err.println("Target must be a directory.");
                fContext.close();
                System.exit(1);
            }
            try
            {
                if (fVerbose)
                {
                    System.out.println(file.getName() + " -> " + args.get(1));
                }
                InputStream in = 
                    new FileInputStream(file);
                OutputStream out =
                    fAVMRemote.createFile(args.get(1), file.getName());
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

    private void recursiveCopy(String sourcePath, NodeRef dest)
    {
        File file = new File(sourcePath);
        if (fVerbose)
        {
            System.out.println(sourcePath + " -> " + dest);
        }
        if (file.isDirectory())
        {
            NodeRef dir = fRepoRemote.createDirectory(dest, file.getName());
            String [] names = file.list();
            for (String name : names)
            {
                recursiveCopy(sourcePath + File.separatorChar + name,
                              dir);                
            }
        }
        else
        {
            try
            {
                InputStream in =
                    new FileInputStream(file);
                OutputStream out = fRepoRemote.createFile(dest, file.getName());
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

    public static void main(String[] args) 
    {
        RepoCopyIn me = new RepoCopyIn();
        me.exec(args, flagDefs, 2, USAGE);
    }
}
