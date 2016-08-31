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
package org.alfresco.util.resource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.core.io.Resource;

/**
 * @see HierarchicalResourceLoader
 * 
 * @author Derek Hulley
 * @since 3.2 (Mobile)
 */
public class HierarchicalResourceLoaderTest extends TestCase
{
    private HierarchicalResourceLoader getLoader(
            Class<? extends Object> baseClazz,
            Class<? extends Object> clazz) throws Throwable
    {
        HierarchicalResourceLoader loader = new HierarchicalResourceLoader();
        loader.setDialectBaseClass(baseClazz.getName());
        loader.setDialectClass(clazz.getName());
        loader.afterPropertiesSet();
        return loader;
    }
    
    /**
     * Check that unmatched hierarchies are detected
     */
    public void testMismatchDetection() throws Throwable
    {
        // First, do a successful few
        getLoader(AbstractCollection.class, TreeSet.class);
        getLoader(AbstractCollection.class, HashSet.class);
        getLoader(AbstractCollection.class, ArrayList.class);
        getLoader(AbstractCollection.class, AbstractCollection.class);
        // Now blow up a bit
        try
        {
            getLoader(Collection.class, Object.class).getResource("abc");
            fail("Failed to detect incompatible class hierarchy");
        }
        catch (RuntimeException e)
        {
            // Expected
        }
        try
        {
            getLoader(ArrayList.class, AbstractCollection.class).getResource("abc");
            fail("Failed to detect incompatible class hierarchy");
        }
        catch (RuntimeException e)
        {
            // Expected
        }
    }
    
    private void checkResource(Resource resource, String check) throws Throwable
    {
        assertNotNull("Resource not found", resource);
        assertTrue("Resource doesn't exist", resource.exists());
        InputStream is = resource.getInputStream();
        StringBuilder builder = new StringBuilder(128);
        byte[] bytes = new byte[128];
        InputStream tempIs = null;
        try
        {
            tempIs = new BufferedInputStream(is, 128);
            int count = -2;
            while (count != -1)
            {
                // do we have something previously read?
                if (count > 0)
                {
                    String toWrite = new String(bytes, 0, count, "UTF-8");
                    builder.append(toWrite);
                }
                // read the next set of bytes
                count = tempIs.read(bytes);
            }
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Unable to read stream", e);
        }
        finally
        {
            // close the input stream
            try
            {
                is.close();
            }
            catch (Exception e)
            {
            }
        }
        // The string
        String fileValue = builder.toString();
        assertEquals("Incorrect file retrieved: ", check, fileValue);
    }
    
    private static final String RESOURCE = "classpath:resource-loader/#resource.dialect#/file.txt";
    /**
     * Check that resource loading works.
     * 
     * The data available is:
     * <pre>
     * classpatch:resource-loader/
     *    java.util.AbstractCollection
     *    java.util.AbstractList
     *    java.util.TreeSet 
     * </pre>
     * With each folder containing a text file with the name of the folder.
     */
    public void testResourceLoading() throws Throwable
    {
        // First, do a successful few
        HierarchicalResourceLoader bean;
        Resource resource;

        bean = getLoader(AbstractCollection.class, TreeSet.class);
        resource = bean.getResource(RESOURCE);
        checkResource(resource, "java.util.TreeSet");

        bean = getLoader(AbstractCollection.class, AbstractSet.class);
        resource = bean.getResource(RESOURCE);
        checkResource(resource, "java.util.AbstractCollection");

        bean = getLoader(AbstractCollection.class, AbstractCollection.class);
        resource = bean.getResource(RESOURCE);
        checkResource(resource, "java.util.AbstractCollection");

        bean = getLoader(AbstractCollection.class, ArrayList.class);
        resource = bean.getResource(RESOURCE);
        checkResource(resource, "java.util.AbstractList");

        bean = getLoader(AbstractCollection.class, AbstractList.class);
        resource = bean.getResource(RESOURCE);
        checkResource(resource, "java.util.AbstractList");
    }
}
