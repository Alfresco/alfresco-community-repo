/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
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
 */
public class Exists extends BaseFunction
{
    public final static String NAME = "Exists";

    public final static String ARG_PROPERTY = "Property";

    public final static String ARG_NOT = "Not";

    public static LinkedHashMap<String, ArgumentDefinition> args;

    static
    {
        args = new LinkedHashMap<String, ArgumentDefinition>();
        args.put(ARG_PROPERTY, new BaseArgumentDefinition(Multiplicity.SINGLE_VALUED, ARG_PROPERTY, DataTypeDefinition.ANY, true));
        args.put(ARG_NOT, new BaseArgumentDefinition(Multiplicity.SINGLE_VALUED, ARG_NOT, DataTypeDefinition.BOOLEAN, false));
    }

    public Exists()
    {
        super(NAME, DataTypeDefinition.BOOLEAN, args);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Function#getValue(java.util.Set) */
    public Serializable getValue(Map<String, Argument> args, FunctionEvaluationContext context)
    {
        throw new UnsupportedOperationException();
    }

}
