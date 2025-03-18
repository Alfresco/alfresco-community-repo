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

    public FTSRange()
    {
        super(NAME, DataTypeDefinition.BOOLEAN, args);
    }

    public Serializable getValue(Map<String, Argument> args, FunctionEvaluationContext context)
    {
        throw new UnsupportedOperationException();
    }

}
