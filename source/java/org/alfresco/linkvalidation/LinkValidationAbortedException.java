/*-----------------------------------------------------------------------------
*  Copyright 2007-2010 Alfresco Software Limited.
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
*  
*  
*  Author  Jon Cox  <jcox@alfresco.com>
*  File    LinkValidationException.java
*----------------------------------------------------------------------------*/

package org.alfresco.linkvalidation;
import java.io.Serializable;

/**
 * An exception class thrown when a link validation operation is aborted.
 *
 * @author Jon Cox
 */
public class LinkValidationAbortedException extends    LinkValidationException
                                            implements Serializable
{
    // serialVersionUID via:
    //
    //   CLASSPATH=$CLASSPATH:projects/repository/build/classes   \
    //     serialver  org.alfresco.linkvalidation.LinkValidationAbortedException
    //
    static final long serialVersionUID = 8307355006036359098L;


    public LinkValidationAbortedException()
    { 
        super(); 
    }

    public LinkValidationAbortedException(String message) 
    {
        super(message); 
    }

    public LinkValidationAbortedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public LinkValidationAbortedException(Throwable cause)
    { 
        super(cause); 
    }
}
