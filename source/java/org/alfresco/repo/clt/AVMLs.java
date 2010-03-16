/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.clt;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.util.Pair;

/**
 * Get a listing of a node.
 * @author britt
 */
public class AVMLs extends CltBase
{
    private static Object [] flagDefs = { "-R", 0 };
    
    private static String USAGE = "usage: AVMLs [-R] nodepath";
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.clt.AVMCltBase#run(java.util.Map, java.util.List)
     */
    @Override
    protected void run(Map<String, List<String>> flags, List<String> args) 
    {
        Pair<String, Integer> pathVersion = splitPathVersion(args.get(0));
        AVMNodeDescriptor desc = fAVMRemote.lookup(pathVersion.getSecond(),
                                                   pathVersion.getFirst());
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
