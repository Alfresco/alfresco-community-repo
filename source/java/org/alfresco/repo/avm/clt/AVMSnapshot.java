/**
 * 
 */
package org.alfresco.repo.avm.clt;

import java.util.List;
import java.util.Map;

/**
 * Snapshot a store.
 * @author britt
 */
public class AVMSnapshot extends AVMCltBase 
{
    private static Object [] flagDefs = { };
    
    private static String USAGE = "usage: AVMSnapshot storename";
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.clt.AVMCltBase#run(java.util.Map, java.util.List)
     */
    @Override
    protected void run(Map<String, List<String>> flags, List<String> args) 
    {
        fAVMRemote.createSnapshot(args.get(0));
    }

    /**
     * @param args
     */
    public static void main(String[] args) 
    {
        AVMSnapshot me = new AVMSnapshot();
        me.exec(args, flagDefs, 1, USAGE);
    }
}
