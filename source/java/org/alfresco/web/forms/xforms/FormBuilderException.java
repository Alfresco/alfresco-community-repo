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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */
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
