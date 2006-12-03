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

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;

/**
 * Copy out a file or a directory recursively from the repository
 * to a local filesystem.
 * @author britt
 */
public class AVMCopyOut extends CltBase 
{
    private static Object [] flagDefs = { "-r", 0, "-v", 0 };
    
    private static String USAGE = "usage: AVMCopyOut [-r] [-v] nodepath@version fspath";
    
    private boolean fVerbose;
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.clt.AVMCltBase#run(java.util.Map, java.util.List)
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
        String [] versionPath = args.get(0).split("@");
        if (versionPath.length != 2)
        {
            usage(USAGE);
        }
        String path = versionPath[0];
        int version = Integer.parseInt(versionPath[1]);
        AVMNodeDescriptor desc = fAVMRemote.lookup(version, path);
        if (flags.containsKey("-r"))
        {
            recursiveCopy(desc, args.get(1));
            return;
        }
        if (desc == null)
        {
            System.err.println(versionPath[0] + " does not exist.");
            fContext.close();
            System.exit(1);
        }
        if (!desc.isFile())
        {
            System.err.println(versionPath[0] + " is not a file.");
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
                InputStream in = fAVMRemote.getFileInputStream(version, path);
                String [] parentBase = splitPath(path);
                OutputStream out = new FileOutputStream(args.get(1) + File.separator + parentBase[1]);
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
                InputStream in = fAVMRemote.getFileInputStream(version, path);
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

    private void recursiveCopy(AVMNodeDescriptor src, String dst)
    {
        String newDst = dst + File.separator + src.getName();
        if (fVerbose)
        {
            System.out.println(src.getPath() + " -> " + dst);
        }
        if (src.isDirectory())
        {
            File destFile = new File(newDst);
            destFile.mkdir();
            Map<String, AVMNodeDescriptor> listing = fAVMRemote.getDirectoryListing(src);
            for (AVMNodeDescriptor child : listing.values())
            {
                recursiveCopy(child, newDst);
            }
            return;
        }
        try
        {
            InputStream in = fAVMRemote.getFileInputStream(src);
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
        AVMCopyOut me = new AVMCopyOut();
        me.exec(args, flagDefs, 2, USAGE);
    }
}
