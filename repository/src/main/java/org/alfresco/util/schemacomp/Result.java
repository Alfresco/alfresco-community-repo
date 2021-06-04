/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.util.schemacomp;

import java.util.Locale;

/**
 * Base class for the result of a differencing or validation operation.
 *  
 * @author Matt Ward
 */
public abstract class Result
{    
    /**
     * A loggable message to describe the comparison result. Default implementation
     * delegates to toString() but this should generally be overridden as toString()
     * is used in a multitude of contexts.
     * 
     * @return String
     */
    public String describe()
    {
        return toString();
    }

    /**
     * An overload of the {@link #describe()} that allows you to specify what locale
     * to use for the loggable message that describes the comparison result.
     *
     * @param locale The locale to use for comparison description.
     * @return String
     */
    public abstract String describe(Locale locale);
}
