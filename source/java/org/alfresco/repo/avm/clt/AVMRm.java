/**
 * 
 */
package org.alfresco.repo.avm.clt;

import java.util.List;
import java.util.Map;

/**
 * Remove an AVM Node.
 * @author britt
 */
public class AVMRm extends AVMCltBase 
{
    private static Object [] flagDefs = { };
    
    private static String USAGE = "usage: AVMRm nodepath";
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.clt.AVMCltBase#run(java.util.Map, java.util.List)
     */
    @Override
    protected void run(Map<String, List<String>> flags, List<String> args) 
    {
        String [] pathBase = splitPath(args.get(0));
        if (pathBase.length == 1)
        {
            System.err.println("One cannot remove a root node.");
            fContext.close();
            System.exit(1);
        }
        fAVMRemote.removeNode(pathBase[0], pathBase[1]);
    }

    public static void main(String[] args) 
    {
        AVMRm me = new AVMRm();
        me.exec(args, flagDefs, 1, USAGE);
    }
}
