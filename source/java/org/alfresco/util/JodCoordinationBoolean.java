/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.util;

import org.alfresco.util.bean.BooleanBean;

/**
 * Wraps a {@link #JodCoordination} object to return one of its boolean methods,
 * so that it may be used as the input to another bean.
 *  
 * @author Alan Davis
 */
public class JodCoordinationBoolean implements BooleanBean
{
    private JodCoordination jodCoordination;
    private String returnValue;

    public void setJodCoordination(JodCoordination jodCoordination)
    {
        this.jodCoordination = jodCoordination;
    }
    
    public void setReturnValue(String returnValue)
    {
        this.returnValue = returnValue;
    }
    
    @Override
    public boolean isTrue()
    {
        if ("startOpenOffice".equals(returnValue))
        {
            return jodCoordination.startOpenOffice();
        }
        else if ("startListener".equals(returnValue))
        {
            return jodCoordination.startListener();
        }
        else
        {
            throw new IllegalArgumentException("Expected \"startOpenOffice\" or \"startListener\" " +
                "as the returnValue property, but it was \""+returnValue+"\"");
        }
    }
}
