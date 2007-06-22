/*-----------------------------------------------------------------------------
*  Copyright 2007 Alfresco Inc.
*  
*  This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*  
*  This program is distributed in the hope that it will be useful, but
*  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
*  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
*  for more details.
*  
*  You should have received a copy of the GNU General Public License along
*  with this program; if not, write to the Free Software Foundation, Inc.,
*  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.  As a special
*  exception to the terms and conditions of version 2.0 of the GPL, you may
*  redistribute this Program in connection with Free/Libre and Open Source
*  Software ("FLOSS") applications as described in Alfresco's FLOSS exception.
*  You should have received a copy of the text describing the FLOSS exception,
*  and it is also available here:   http://www.alfresco.com/legal/licensing
*  
*  
*  Author  Jon Cox  <jcox@alfresco.com>
*  File    LinkValidationException.java
*----------------------------------------------------------------------------*/

package org.alfresco.linkvalidation;
import java.io.Serializable;

/**
 * Class for generic LinkValidation Exceptions.
 *
 * @author Jon Cox
 */
public class LinkValidationException extends    Exception
                                     implements Serializable
{
    // serialVersionUID via:
    //
    //   CLASSPATH=$CLASSPATH:projects/repository/build/classes   \
    //     serialver  org.alfresco.linkvalidation.LinkValidationException
    //
    static final long serialVersionUID = 571631235536445801L;

    public LinkValidationException()
    { 
        super(); 
    }

    public LinkValidationException(String message) 
    {
        super(message); 
    }

    public LinkValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public LinkValidationException(Throwable cause)
    { 
        super(cause); 
    }
}
