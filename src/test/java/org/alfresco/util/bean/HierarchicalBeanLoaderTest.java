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
package org.alfresco.util.bean;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.Collection;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @see HierarchicalBeanLoader
 * 
 * @author Derek Hulley
 * @since 3.2SP1
 */
public class HierarchicalBeanLoaderTest extends TestCase
{
    private ClassPathXmlApplicationContext ctx;
    
    private String getBean(Class<?> clazz, boolean setBeforeInit) throws Exception
    {
        if (setBeforeInit)
        {
            System.setProperty("hierarchy-test.dialect", clazz.getName());
        }
        ctx = new ClassPathXmlApplicationContext("bean-loader/hierarchical-bean-loader-test-context.xml");
        if (!setBeforeInit)
        {
            System.setProperty("hierarchy-test.dialect", clazz.getName());
        }
        return (String) ctx.getBean("test.someString");
    }
    
    public void tearDown()
    {
        try
        {
            ctx.close();
        }
        catch (Throwable e)
        {
        }
    }
    
    public void testSuccess1() throws Throwable
    {
        String str = getBean(TreeSet.class, true);
        assertEquals("Bean value incorrect", "TreeSet", str);
    }
    
    public void testSuccess2() throws Throwable
    {
        String str = getBean(AbstractList.class, true);
        assertEquals("Bean value incorrect", "AbstractList", str);
    }
    
    public void testSuccess3() throws Throwable
    {
        String str = getBean(AbstractCollection.class, true);
        assertEquals("Bean value incorrect", "AbstractCollection", str);
    }
    
    public void testFailure1() throws Throwable
    {
        try
        {
            getBean(Collection.class, true);
            fail("Should not be able to retrieve bean using class " + Collection.class);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
}
