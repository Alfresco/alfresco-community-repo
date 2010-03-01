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
