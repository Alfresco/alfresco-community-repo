/**
 * 
 */
package org.alfresco.repo.avm.clt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;

/**
 * Like cp from a local filesystem to AVM.
 * @author britt
 */
public class AVMCopyIn extends AVMCltBase 
{
    private static Object [] flagDefs = { "-r", 0, "-v", 0 };
    
    private static String USAGE = "usage: [-r] sourcepath nodepath";
    
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
        if (flags.containsKey("-r"))
        {
            recursiveCopy(args.get(0), args.get(1));
            return;
        }
        File file = new File(args.get(0));
        if (!file.isFile())
        {
            System.err.println(args.get(0) + " not found, or not a file.");
            fContext.close();
            System.exit(1);
        }
        AVMNodeDescriptor desc = fAVMRemote.lookup(-1, args.get(1));
        if (desc == null)
        {
            try
            {
                String [] pathBase = splitPath(args.get(1));
                if (pathBase.length == 1)
                {
                    System.err.println(args.get(1) + " is a root path.");
                    fContext.close();
                    System.exit(1);
                }
                if (fVerbose)
                {
                    System.out.println(file.getName() + " -> " + pathBase[0]);
                }
                InputStream in =
                    new FileInputStream(file);
                OutputStream out = fAVMRemote.createFile(pathBase[0], pathBase[1]);
                                   
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
            if (!desc.isDirectory())
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

    private void copyStream(InputStream in, OutputStream out)
    {
        try
        {
            byte [] buff = new byte[8192];
            int read = 0;
            while ((read = in.read(buff)) != -1)
            {
                out.write(buff, 0, read);
            }
            in.close();
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fContext.close();
            System.exit(1);
        }
    }
    
    private void recursiveCopy(String sourcePath, String dest)
    {
        File file = new File(sourcePath);
        if (fVerbose)
        {
            System.out.println(sourcePath + " -> " + dest);
        }
        if (file.isDirectory())
        {
            fAVMRemote.createDirectory(dest, file.getName());
            String newDest = dest + '/' + file.getName();
            String [] names = file.list();
            for (String name : names)
            {
                recursiveCopy(sourcePath + File.separatorChar + name,
                              newDest);                
            }
        }
        else
        {
            try
            {
                InputStream in =
                    new FileInputStream(file);
                OutputStream out = fAVMRemote.createFile(dest, file.getName());
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
    
    /**
     * @param args
     */
    public static void main(String[] args) 
    {
        AVMCopyIn me = new AVMCopyIn();
        me.exec(args, flagDefs, 2, USAGE);
    }
}
