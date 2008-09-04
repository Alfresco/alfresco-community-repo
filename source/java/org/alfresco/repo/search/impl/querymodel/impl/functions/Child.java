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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;

/**
 * @author andyh
 */
public class Child extends BaseFunction
{
    public final static String NAME = "Child";

    public final static String ARG_PARENT = "Parent";

    public final static String ARG_SELECTOR = "Selector";

    public static LinkedHashMap<String, ArgumentDefinition> args;

    static
    {
        args = new LinkedHashMap<String, ArgumentDefinition>();
        args.put(ARG_PARENT, new BaseArgumentDefinition(Multiplicity.SINGLE_VALUED, ARG_PARENT, DataTypeDefinition.TEXT, true));
        args.put(ARG_SELECTOR, new BaseArgumentDefinition(Multiplicity.SINGLE_VALUED, ARG_SELECTOR, DataTypeDefinition.TEXT, false));
    }

    /**
     * @param name
     * @param returnType
     * @param argumentDefinitions
     */
    public Child()
    {
        super(NAME, DataTypeDefinition.BOOLEAN, args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Function#getValue(java.util.Set)
     */
    public Serializable getValue(Map<String, Argument> args, FunctionEvaluationContext context)
    {
        Argument selectorArgument = args.get(ARG_SELECTOR);
        String selectorName = DefaultTypeConverter.INSTANCE.convert(String.class, selectorArgument.getValue(context));
        Argument parentArgument = args.get(ARG_PARENT);
        NodeRef parent = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, parentArgument.getValue(context));

        NodeRef child = context.getNodeRefs().get(selectorName);

        for (ChildAssociationRef car : context.getNodeService().getParentAssocs(child))
        {
            if (car.getParentRef().equals(parent))
            {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

}
