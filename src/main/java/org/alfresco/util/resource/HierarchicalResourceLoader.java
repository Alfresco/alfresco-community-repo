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

import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/**
 * Locate resources by using a class hierarchy to drive the search.  The well-known
 * placeholder {@link #DEFAULT_DIALECT_PLACEHOLDER} is replaced with successive class
 * names starting from the {@link #setDialectClass(String) dialect class} and
 * progressing up the hierarchy until the {@link #setDialectBaseClass(String) base class}
 * is reached.  A full resource search using Spring's {@link DefaultResourceLoader} is
 * done at each point until the resource is found or the base of the class hierarchy is
 * reached.
 * <p/>
 * For example assume classpath resources:<br/>
 * <pre>
 *    RESOURCE 1: config/ibatis/org.hibernate.dialect.Dialect/SqlMap-DOG.xml
 *    RESOURCE 2: config/ibatis/org.hibernate.dialect.MySQLInnoDBDialect/SqlMap-DOG.xml
 *    RESOURCE 3: config/ibatis/org.hibernate.dialect.Dialect/SqlMap-CAT.xml
 *    RESOURCE 4: config/ibatis/org.hibernate.dialect.MySQLDialect/SqlMap-CAT.xml
 * </pre>
 * and<br/>
 * <pre>
 *    dialectBaseClass = org.hibernate.dialect.Dialect
 * </pre>
 * For dialect <b>org.hibernate.dialect.MySQLInnoDBDialect</b> the following will be returned:<br>
 * <pre>
 *    config/ibatis/#resource.dialect#/SqlMap-DOG.xml == RESOURCE 2
 *    config/ibatis/#resource.dialect#/SqlMap-CAT.xml == RESOURCE 4
 * </pre>
 * For dialect<b>org.hibernate.dialect.MySQLDBDialect</b> the following will be returned:<br>
 * <pre>
 *    config/ibatis/#resource.dialect#/SqlMap-DOG.xml == RESOURCE 1
 *    config/ibatis/#resource.dialect#/SqlMap-CAT.xml == RESOURCE 4
 * </pre>
 * For dialect<b>org.hibernate.dialect.Dialect</b> the following will be returned:<br>
 * <pre>
 *    config/ibatis/#resource.dialect#/SqlMap-DOG.xml == RESOURCE 1
 *    config/ibatis/#resource.dialect#/SqlMap-CAT.xml == RESOURCE 3
 * </pre>
 * 
 * @author Derek Hulley
 * @since 3.2 (Mobile)
 */
public class HierarchicalResourceLoader extends DefaultResourceLoader implements InitializingBean
{
    public static final String DEFAULT_DIALECT_PLACEHOLDER = "#resource.dialect#";
    public static final String DEFAULT_DIALECT_REGEX = "\\#resource\\.dialect\\#";
    
    private String dialectBaseClass;
    private String dialectClass;
    
    /**
     * Create a new HierarchicalResourceLoader.
     */
    public HierarchicalResourceLoader()
    {
        super();
    }

    /**
     * Set the class to be used during hierarchical dialect replacement.  Searches for the
     * configuration location will not go further up the hierarchy than this class.
     * 
     * @param className     the name of the class or interface
     */
    public void setDialectBaseClass(String className)
    {
        this.dialectBaseClass = className;
    }
    
    public void setDialectClass(String className)
    {
        this.dialectClass = className;
    }

    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "dialectBaseClass", dialectBaseClass);
        PropertyCheck.mandatory(this, "dialectClass", dialectClass);
    }
    
    /**
     * Get a resource using the defined class hierarchy as a search path.
     * 
     * @param location          the location including a {@link #DEFAULT_DIALECT_PLACEHOLDER placeholder}
     * @return                  a resource found by successive searches using class name replacement, or
     *                          <tt>null</tt> if not found.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Resource getResource(String location)
    {
        if (dialectClass == null || dialectBaseClass == null)
        {
            return super.getResource(location);
        }
        
        // If a property value has not been substituted, extract the property name and load from system
        String dialectBaseClassStr = dialectBaseClass;
        if (!PropertyCheck.isValidPropertyString(dialectBaseClass))
        {
            String prop = PropertyCheck.getPropertyName(dialectBaseClass);
            dialectBaseClassStr = System.getProperty(prop, dialectBaseClass);
        }
        String dialectClassStr = dialectClass;
        if (!PropertyCheck.isValidPropertyString(dialectClass))
        {
            String prop = PropertyCheck.getPropertyName(dialectClass);
            dialectClassStr = System.getProperty(prop, dialectClass);
        }

        Class<?> dialectBaseClazz;
        try
        {
            dialectBaseClazz = Class.forName(dialectBaseClassStr);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Dialect base class not found: " + dialectBaseClassStr);
        }
        Class<?> dialectClazz;
        try
        {
            dialectClazz = Class.forName(dialectClassStr);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Dialect class not found: " + dialectClassStr);
        }
        // Ensure that we are dealing with classes and not interfaces
        if (!Object.class.isAssignableFrom(dialectBaseClazz))
        {
            throw new RuntimeException(
                    "Dialect base class must be derived from java.lang.Object: " +
                    dialectBaseClazz.getName());
        }
        if (!Object.class.isAssignableFrom(dialectClazz))
        {
            throw new RuntimeException(
                    "Dialect class must be derived from java.lang.Object: " +
                    dialectClazz.getName());
        }
        // We expect these to be in the same hierarchy
        if (!dialectBaseClazz.isAssignableFrom(dialectClazz))
        {
            throw new RuntimeException(
                    "Non-existent HierarchicalResourceLoader hierarchy: " +
                    dialectBaseClazz.getName() + " is not a superclass of " + dialectClazz);
        }
        
        Class<? extends Object> clazz = dialectClazz;
        Resource resource = null;
        while (resource == null)
        {
            // Do replacement
            String newLocation = location.replaceAll(DEFAULT_DIALECT_REGEX, clazz.getName());
            resource = super.getResource(newLocation);
            if (resource != null && resource.exists())
            {
                // Found
                break;
            }
            // Not found
            resource = null;
            // Are we at the base class?
            if (clazz.equals(dialectBaseClazz))
            {
                // We don't go any further
                break;
            }
            // Move up the hierarchy
            clazz = clazz.getSuperclass();
            if (clazz == null)
            {
                throw new RuntimeException(
                        "Non-existent HierarchicalResourceLoaderBean hierarchy: " +
                        dialectBaseClazz.getName() + " is not a superclass of " + dialectClazz);
            }
        }
        return resource;
    }
}
