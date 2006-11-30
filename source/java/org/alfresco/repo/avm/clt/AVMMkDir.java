/**
 * 
 */
package org.alfresco.repo.avm.clt;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;

/**
 * Make a directory.
 * @author britt
 */
public class AVMMkDir extends AVMCltBase 
{
    private static Object [] flagDefs = { "-p", 0 };
    
    private static String USAGE = "usage: AVMMkDir [-p] nodepath";
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.clt.AVMCltBase#run(java.util.Map, java.util.List)
     */
    @Override
    protected void run(Map<String, List<String>> flags, List<String> args) 
    {
        if (flags.containsKey("-p"))
        {
            mkdirp(args.get(0));
            return;
        }
        String [] parentBase = splitPath(args.get(0));
        if (parentBase.length == 1)
        {
            System.err.println(args.get(0) + " is a root path.");
            fContext.close();
            System.exit(1);
        }
        fAVMRemote.createDirectory(parentBase[0], parentBase[1]);
    }
    
    private void mkdirp(String path)
    {
        AVMNodeDescriptor desc = fAVMRemote.lookup(-1, path);
        if (desc != null)
        {
            return;
        }
        String [] parentBase = splitPath(path);
        if (parentBase.length == 1)
        {
            System.err.println(path + " does not exist.");
            fContext.close();
            System.exit(1);
        }
        mkdirp(parentBase[0]);
        fAVMRemote.createDirectory(parentBase[0], parentBase[1]);
    }

    /**
     * @param args
     */
    public static void main(String[] args) 
    {
        AVMMkDir me = new AVMMkDir();
        me.exec(args, flagDefs, 1, USAGE);
    }
}
