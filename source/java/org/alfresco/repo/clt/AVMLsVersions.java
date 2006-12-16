/**
 * 
 */
package org.alfresco.repo.clt;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.avm.VersionDescriptor;

/**
 * List all versions of a given store.
 * @author britt
 */
public class AVMLsVersions extends CltBase 
{
    private static Object [] flagDefs = { };
    
    private static String USAGE = "usage: AVMLsVersion storename";
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.clt.AVMCltBase#run(java.util.Map, java.util.List)
     */
    @Override
    protected void run(Map<String, List<String>> flags, List<String> args) 
    {
        List<VersionDescriptor> versions = fAVMRemote.getStoreVersions(args.get(0));
        for (VersionDescriptor version : versions)
        {
            System.out.println(version);
        }
    }

    public static void main(String[] args) 
    {
        AVMLsVersions me = new AVMLsVersions();
        me.exec(args, flagDefs, 1, USAGE);
    }
}
