/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.search.impl.querymodel.impl;

import java.util.LinkedHashMap;

import org.alfresco.repo.search.impl.querymodel.ArgumentDefinition;
import org.alfresco.repo.search.impl.querymodel.Multiplicity;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 */
public abstract class BaseComparison extends BaseFunction
{
    public final static String ARG_LHS = "LHS";
    
    public final static String ARG_RHS = "RHS";
    
    public static LinkedHashMap<String, ArgumentDefinition> args;
    
    static 
    {
        args = new LinkedHashMap<String, ArgumentDefinition>();
        args.put(ARG_LHS, new BaseArgumentDefinition(Multiplicity.ANY, ARG_LHS, DataTypeDefinition.ANY, true));
        args.put(ARG_RHS, new BaseArgumentDefinition(Multiplicity.ANY, ARG_RHS, DataTypeDefinition.ANY, true));
    }

    /**
     * @param name
     * @param returnType
     * @param argumentDefinitions
     */
    public BaseComparison(String name, QName returnType, LinkedHashMap<String, ArgumentDefinition> argumentDefinitions)
    {
        super(name, returnType, argumentDefinitions);
    }

}
