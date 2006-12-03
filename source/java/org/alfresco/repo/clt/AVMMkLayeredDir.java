/**
 * 
 */
package org.alfresco.repo.clt;

import java.util.List;
import java.util.Map;

/**
 * @author britt
 *
 */
public class AVMMkLayeredDir extends CltBase 
{
    private static Object [] flagDefs = { };
    
    private static String USAGE = "usage: AVMMkLayeredDir nodepath targetnodepath";
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.clt.AVMCltBase#run(java.util.Map, java.util.List)
     */
    @Override
    protected void run(Map<String, List<String>> flags, List<String> args) 
    {
        String [] pathBase = splitPath(args.get(0));
        if (pathBase.length == 1)
        {
            System.err.println("Cannot make a layered root directory.");
            fContext.close();
            System.exit(1);
        }
        fAVMRemote.createLayeredDirectory(args.get(1), pathBase[0], pathBase[1]);
    }
    
    public static void main(String [] args)
    {
        AVMMkLayeredDir me = new AVMMkLayeredDir();
        me.exec(args, flagDefs, 2, USAGE);
    }
}
