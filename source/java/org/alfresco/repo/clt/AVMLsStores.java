/**
 * 
 */
package org.alfresco.repo.clt;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.avm.AVMStoreDescriptor;

/**
 * List all avm stores in the repository.
 * @author britt
 */
public class AVMLsStores extends CltBase 
{
    private static Object[] flagDefs = { };
    
    private static String USAGE = "usage: AVMLsStores";
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.clt.AVMCltBase#run(java.util.Map, java.util.List)
     */
    @Override
    protected void run(Map<String, List<String>> flags, List<String> args) 
    {
        List<AVMStoreDescriptor> stores = fAVMRemote.getStores();
        for (AVMStoreDescriptor store : stores)
        {
            System.out.println(store);
        }
    }

    public static void main(String[] args) 
    {
        AVMLsStores me = new AVMLsStores();
        me.exec(args, flagDefs, 0, USAGE);
    }
}
