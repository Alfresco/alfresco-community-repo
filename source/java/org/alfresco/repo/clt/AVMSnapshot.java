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

/**
 * Snapshot a store.
 * @author britt
 */
public class AVMSnapshot extends CltBase 
{
    private static Object [] flagDefs = { };
    
    private static String USAGE = "usage: AVMSnapshot storename label comment";
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.clt.AVMCltBase#run(java.util.Map, java.util.List)
     */
    @Override
    protected void run(Map<String, List<String>> flags, List<String> args) 
    {
        fAVMRemote.createSnapshot(args.get(0), args.get(1), args.get(2));
    }

    /**
     * @param args
     */
    public static void main(String[] args) 
    {
        AVMSnapshot me = new AVMSnapshot();
        me.exec(args, flagDefs, 3, USAGE);
    }
}
