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
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.web.forms.xforms;


/**
 * This exception is thrown when implementations of <code>SchemaFormBuilder</code> 
 * encounters an error building a form.
 *
 * @author Brian Dueck
 */
public class FormBuilderException 
    extends Exception 
{

    /**
     * Creates a new instance of <code>FormBuilderException</code> without detail message.
     */
    public FormBuilderException() { }

    /**
     * Constructs an instance of <code>FormBuilderException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public FormBuilderException(String msg) 
    {
        super(msg);
    }

    /**
     * Constructs an instance of <code>FormBuilderException</code> with the specified root exception.
     *
     * @param x The root exception.
     */
    public FormBuilderException(Exception x) 
    {
	super(x);
    }

    /**
     * Constructs an instance of <code>FormBuilderException</code> with the specified root exception.
     *
     * @param msg the detail message.
     * @param x The root exception.
     */
   public FormBuilderException(String msg, Exception x) 
    {
       super(msg, x);
    }
}
