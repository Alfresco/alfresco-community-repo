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
package org.alfresco.repo.search.impl.querymodel.impl.functions;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.ArgumentDefinition;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.Multiplicity;
import org.alfresco.repo.search.impl.querymodel.impl.BaseArgumentDefinition;
import org.alfresco.repo.search.impl.querymodel.impl.BaseFunction;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;

public class FTSRange extends BaseFunction
{
    public final static String NAME = "FTSRange";

    public final static String ARG_FROM_INC = "FromInc";
    
    public final static String ARG_FROM = "From";
    
    public final static String ARG_TO = "To";
    
    public final static String ARG_TO_INC = "ToInc";
    
    public final static String ARG_PROPERTY = "Property";

    public static LinkedHashMap<String, ArgumentDefinition> args;

    static
    {
        args = new LinkedHashMap<String, ArgumentDefinition>();
        args.put(ARG_FROM_INC, new BaseArgumentDefinition(Multiplicity.SINGLE_VALUED, ARG_FROM_INC, DataTypeDefinition.BOOLEAN, true));
        args.put(ARG_FROM, new BaseArgumentDefinition(Multiplicity.SINGLE_VALUED, ARG_FROM, DataTypeDefinition.TEXT, true));
        args.put(ARG_TO, new BaseArgumentDefinition(Multiplicity.SINGLE_VALUED, ARG_TO, DataTypeDefinition.TEXT, true));
        args.put(ARG_TO_INC, new BaseArgumentDefinition(Multiplicity.SINGLE_VALUED, ARG_TO_INC, DataTypeDefinition.BOOLEAN, true));
        args.put(ARG_PROPERTY, new BaseArgumentDefinition(Multiplicity.SINGLE_VALUED, ARG_PROPERTY, DataTypeDefinition.ANY, false));
    }

    /**
     * @param name
     * @param returnType
     * @param argumentDefinitions
     */
    public FTSRange()
    {
        super(NAME, DataTypeDefinition.BOOLEAN, args);
    }
    
    
    public Serializable getValue(Map<String, Argument> args, FunctionEvaluationContext context)
    {
        throw new UnsupportedOperationException();
    }

}
