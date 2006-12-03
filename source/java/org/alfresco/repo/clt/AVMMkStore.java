/**
 * 
 */
package org.alfresco.repo.clt;

import java.util.List;
import java.util.Map;

/**
 * Create an AVM store.
 * @author britt
 */
public class AVMMkStore extends CltBase 
{
    private static Object [] flagDefs = { };
    
    private static String USAGE = "usage: AVMMkStore storename";
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.clt.AVMCltBase#run(java.util.Map, java.util.List)
     */
    @Override
    protected void run(Map<String, List<String>> flags, List<String> args) 
    {
        fAVMRemote.createAVMStore(args.get(0));
    }

    /**
     * @param args
     */
    public static void main(String[] args) 
    {
        AVMMkStore me = new AVMMkStore();
        me.exec(args, flagDefs, 1, USAGE);
    }
}
