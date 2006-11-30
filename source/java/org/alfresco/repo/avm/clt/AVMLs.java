/**
 * 
 */
package org.alfresco.repo.avm.clt;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;

/**
 * Get a listing of a node.
 * @author britt
 */
public class AVMLs extends AVMCltBase
{
    private static Object [] flagDefs = { "-R", 0 };
    
    private static String USAGE = "usage: AVMLs [-R] nodepath";
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.clt.AVMCltBase#run(java.util.Map, java.util.List)
     */
    @Override
    protected void run(Map<String, List<String>> flags, List<String> args) 
    {
        String[] pathVersion = args.get(0).split("@");
        AVMNodeDescriptor desc = fAVMRemote.lookup(Integer.parseInt(pathVersion[1]), 
                                                   pathVersion[0]);
        if (flags.containsKey("-R"))
        {
            recursiveList(desc, 0);
        }
        else
        {
            list(desc);
        }
    }

    private void list(AVMNodeDescriptor desc)
    {
        if (desc.isFile())
        {
            System.out.println(desc.getName() + '\t' + desc);
            return;
        }
        Map<String, AVMNodeDescriptor> listing = fAVMRemote.getDirectoryListing(desc);
        for (Map.Entry<String, AVMNodeDescriptor> entry : listing.entrySet())
        {
            System.out.println(entry.getKey() + '\t' + entry.getValue());
        }
    }
    
    private void recursiveList(AVMNodeDescriptor desc, int indent)
    {
        for (int i = 0; i < indent; i++)
        {
            System.out.print(' ');
        }
        System.out.println(desc.getName() + '\t' + desc);
        if (desc.isDirectory())
        {
            indent += 2;
            Map<String, AVMNodeDescriptor> listing = fAVMRemote.getDirectoryListing(desc);
            for (Map.Entry<String, AVMNodeDescriptor> entry : listing.entrySet())
            {
                recursiveList(entry.getValue(), indent);
            }
        }
    }
    
    public static void main(String[] args) 
    {
        AVMLs me = new AVMLs();
        me.exec(args, flagDefs, 1, USAGE);
    }
}
