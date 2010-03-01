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

/**
 * @author andyh
 *
 */
public class In extends BaseFunction
{
    public final static String NAME = "In";
    
    public final static String ARG_PROPERTY = "Property";
    
    public final static String ARG_LIST = "List";
    
    public final static String ARG_NOT = "Not";
    
    public final static String ARG_MODE = "Mode";
    
    public static LinkedHashMap<String, ArgumentDefinition> args;
    
    static 
    {
        args = new LinkedHashMap<String, ArgumentDefinition>();
        args.put(ARG_MODE, new BaseArgumentDefinition(Multiplicity.ANY, ARG_MODE, DataTypeDefinition.ANY, true));
        args.put(ARG_PROPERTY, new BaseArgumentDefinition(Multiplicity.ANY, ARG_PROPERTY, DataTypeDefinition.ANY, true));
        args.put(ARG_LIST, new BaseArgumentDefinition(Multiplicity.ANY, ARG_LIST, DataTypeDefinition.ANY, true));
        args.put(ARG_NOT, new BaseArgumentDefinition(Multiplicity.ANY, ARG_NOT, DataTypeDefinition.ANY, false));
    }
    
    /**
     * @param name
     * @param returnType
     * @param argumentDefinitions
     */
    public In()
    {
        super(NAME, DataTypeDefinition.BOOLEAN, args);   
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.Function#getValue(java.util.Set)
     */
    public Serializable getValue(Map<String, Argument> args, FunctionEvaluationContext context)
    {
        throw new UnsupportedOperationException();
    }

}
