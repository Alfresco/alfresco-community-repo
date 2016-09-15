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
package org.alfresco.repo.search.impl.querymodel.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.ArgumentDefinition;
import org.alfresco.repo.search.impl.querymodel.FunctionArgument;
import org.alfresco.repo.search.impl.querymodel.Multiplicity;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.StaticArgument;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Lower;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Upper;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 */
public abstract class BaseComparison extends BaseFunction
{
    /**
     * Left hand side
     */
    public final static String ARG_LHS = "LHS";

    /**
     * Right hand side
     */
    public final static String ARG_RHS = "RHS";
    
    /**
     * Mode: SVP or mode for MVP comparisons
     */
    public final static String ARG_MODE = "Mode";

    /**
     * Args
     */
    public static LinkedHashMap<String, ArgumentDefinition> ARGS;

    private PropertyArgument propertyArgument;

    private StaticArgument staticArgument;

    private FunctionArgument functionArgument;
    
    private String staticPosition;

    static
    {
        ARGS = new LinkedHashMap<String, ArgumentDefinition>();
        ARGS.put(ARG_LHS, new BaseArgumentDefinition(Multiplicity.ANY, ARG_LHS, DataTypeDefinition.ANY, true));
        ARGS.put(ARG_RHS, new BaseArgumentDefinition(Multiplicity.ANY, ARG_RHS, DataTypeDefinition.ANY, true));
        ARGS.put(ARG_MODE, new BaseArgumentDefinition(Multiplicity.ANY, ARG_MODE, DataTypeDefinition.ANY, true));
        
    }

    /**
     * @param name String
     * @param returnType QName
     */
    public BaseComparison(String name, QName returnType, LinkedHashMap<String, ArgumentDefinition> argumentDefinitions)
    {
        super(name, returnType, argumentDefinitions);
    }

    public void setPropertyAndStaticArguments(Map<String, Argument> functionArgs)
    {
        Argument lhs = functionArgs.get(ARG_LHS);
        Argument rhs = functionArgs.get(ARG_RHS);

        if (lhs instanceof PropertyArgument)
        {
            if ((rhs instanceof PropertyArgument) || (rhs instanceof FunctionArgument))
            {
                throw new QueryModelException("Implicit join is not supported");
            }
            else if (rhs instanceof StaticArgument)
            {
                propertyArgument = (PropertyArgument) lhs;
                staticArgument = (StaticArgument) rhs;
                staticPosition = ARG_RHS;
            }
            else
            {
                throw new QueryModelException("Argument of type " + rhs.getClass().getName() + " is not supported");
            }
        }
        else if (lhs instanceof FunctionArgument)
        {
            if ((rhs instanceof PropertyArgument) || (rhs instanceof FunctionArgument))
            {
                throw new QueryModelException("Implicit join is not supported");
            }
            else if (rhs instanceof StaticArgument)
            {
                functionArgument = (FunctionArgument) lhs;
                staticArgument = (StaticArgument) rhs;
                staticPosition = ARG_RHS;
            }
            else
            {
                throw new QueryModelException("Argument of type " + rhs.getClass().getName() + " is not supported");
            }
        }
        else if (rhs instanceof PropertyArgument)
        {
            if ((lhs instanceof PropertyArgument) || (lhs instanceof FunctionArgument))
            {
                throw new QueryModelException("Implicit join is not supported");
            }
            else if (lhs instanceof StaticArgument)
            {
                propertyArgument = (PropertyArgument) rhs;
                staticArgument = (StaticArgument) lhs;
                staticPosition = ARG_LHS;
            }
            else
            {
                throw new QueryModelException("Argument of type " + lhs.getClass().getName() + " is not supported");
            }
        }
        else if (rhs instanceof FunctionArgument)
        {
            if ((lhs instanceof PropertyArgument) || (lhs instanceof FunctionArgument))
            {
                throw new QueryModelException("Implicit join is not supported");
            }
            else if (lhs instanceof StaticArgument)
            {
                functionArgument = (FunctionArgument) rhs;
                staticArgument = (StaticArgument) lhs;
                staticPosition = ARG_LHS;
            }
            else
            {
                throw new QueryModelException("Argument of type " + lhs.getClass().getName() + " is not supported");
            }
        }
        else
        {
            throw new QueryModelException("Equals must have one property argument");
        }
    }

    /**
     * @return the propertyArgument - there must be a property argument of a function argument
     */
    protected PropertyArgument getPropertyArgument()
    {
        return propertyArgument;
    }

    /**
     * @return the staticArgument - must be set
     */
    protected StaticArgument getStaticArgument()
    {
        return staticArgument;
    }

    /**
     * @return the staticPosition
     */
    public String getStaticPosition()
    {
        return staticPosition;
    }

    /**
     * @return the functionArgument
     */
    protected FunctionArgument getFunctionArgument()
    {
        return functionArgument;
    }

    public String getPropertyName()
    {
        if (propertyArgument != null)
        {
            return propertyArgument.getPropertyName();
        }
        else if (functionArgument != null)
        {
            String functionName = functionArgument.getFunction().getName();
            if (functionName.equals(Upper.NAME))
            {
                Argument arg = functionArgument.getFunctionArguments().get(Upper.ARG_ARG);
                if (arg instanceof PropertyArgument)
                {
                    return ((PropertyArgument) arg).getPropertyName();
                }
                else
                {
                    throw new QueryModelException("Upper must have a column argument " + arg);
                }
            }
            else if (functionName.equals(Lower.NAME))
            {
                Argument arg = functionArgument.getFunctionArguments().get(Lower.ARG_ARG);
                if (arg instanceof PropertyArgument)
                {
                    return ((PropertyArgument) arg).getPropertyName();
                }
                else
                {
                    throw new QueryModelException("Lower must have a column argument " + arg);
                }
            }
            else
            {
                throw new QueryModelException("Unsupported function: " + functionName);
            }
        }
        else
        {
            throw new QueryModelException("A property of function argument must be provided");
        }
    }

}
