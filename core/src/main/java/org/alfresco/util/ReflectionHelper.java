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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Static Helper methods for instantiating objects from reflection.
 * 
 * @author muzquiano
 */
public class ReflectionHelper
{
    private static Log logger = LogFactory.getLog(ReflectionHelper.class);
    
    private ReflectionHelper()
    {
    }

    /**
     * Constructs a new object for the given class name.
     * The construction takes no arguments.
     * 
     * If an exception occurs during construction, null is returned.
     * 
     * All exceptions are written to the Log instance for this class.
     * 
     * @param className String
     * @return Object
     */
    public static Object newObject(String className)
    {
        Object o = null;

        try
        {
            Class clazz = Class.forName(className);
            o = clazz.newInstance();
        }
        catch (ClassNotFoundException cnfe)
        {
            logger.debug(cnfe);
        }
        catch (InstantiationException ie)
        {
            logger.debug(ie);
        }
        catch (IllegalAccessException iae)
        {
            logger.debug(iae);
        }
        return o;
    }

    /**
     * Constructs a new object for the given class name and with the given
     * arguments.  The arguments must be specified in terms of their Class[]
     * types and their Object[] values.
     * 
     * Example:
     * 
     *   String s = newObject("java.lang.String", new Class[] { String.class},
     *              new String[] { "test"});
     *              
     * is equivalent to:
     * 
     *   String s = new String("test");
     * 
     * If an exception occurs during construction, null is returned.
     * 
     * All exceptions are written to the Log instance for this class.

     * @param className String
     * @param argTypes Class[]
     * @param args Object[]
     * @return Object
     */
    public static Object newObject(String className, Class[] argTypes, Object[] args)
    {
        /**
         * We have some mercy here - if they called and did not pass in any
         * arguments, then we will call through to the pure newObject() method.
         */
        if (args == null || args.length == 0)
        {
            return newObject(className);
        }

        /**
         * Try to build the object
         * 
         * If an exception occurs, we log it and return null.
         */
        Object o = null;
        try
        {
            // base class
            Class clazz = Class.forName(className);

            Constructor c = clazz.getDeclaredConstructor(argTypes);
            o = c.newInstance(args);
        }
        catch (ClassNotFoundException cnfe)
        {
            logger.debug(cnfe);
        }
        catch (InstantiationException ie)
        {
            logger.debug(ie);
        }
        catch (IllegalAccessException iae)
        {
            logger.debug(iae);
        }
        catch (NoSuchMethodException nsme)
        {
            logger.debug(nsme);
        }
        catch (InvocationTargetException ite)
        {
            logger.debug(ite);
        }
        return o;
    }

    /**
     * Invokes a method on the given object by passing the given arguments
     * into the method.
     * 
     * @param obj Object
     * @param method String
     * @param argTypes Class[]
     * @param args Object[]
     * @return Object
     */
    public static Object invoke(Object obj, String method, Class[] argTypes, Object[] args)
    {
        if (obj == null || method == null)
        {
            throw new IllegalArgumentException("Object and Method must be supplied.");
        }
        
        /**
         * Try to invoke the method.
         * 
         * If the method is unable to be invoked, we log and return null.
         */
        try
        {
            Method m = obj.getClass().getMethod(method, argTypes);
            if(m != null)
            {
                return m.invoke(obj, args);
            }
        }
        catch(NoSuchMethodException nsme)
        {
            logger.debug(nsme);
        }
        catch(IllegalAccessException iae)
        {
            logger.debug(iae);
        }
        catch(InvocationTargetException ite)
        {
            logger.debug(ite);
        }
        
        return null;
    }
}
