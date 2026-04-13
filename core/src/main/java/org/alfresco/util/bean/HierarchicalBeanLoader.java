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

import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Factory bean to find beans using a class hierarchy to drive the lookup. The well-known
 * placeholder {@link #DEFAULT_DIALECT_PLACEHOLDER} is replaced with successive class
 * names starting from the {@link #setDialectClass(String) dialect class} and
 * progressing up the hierarchy until the {@link #setDialectBaseClass(String) base class}
 * is reached.  The bean is looked up in the context at each point until the
 * bean is found or the base of the class hierarchy is reached.
 * <p/>
 * For example assume bean names:<br/>
 * <pre>
 *    BEAN 1: contentDAO.org.hibernate.dialect.Dialect
 *    BEAN 2: contentDAO.org.hibernate.dialect.MySQLInnoDBDialect
 *    BEAN 3: propertyValueDAO.org.hibernate.dialect.Dialect
 *    BEAN 4: propertyValueDAO.org.hibernate.dialect.MySQLDialect
 * </pre>
 * and<br/>
 * <pre>
 *    dialectBaseClass = org.hibernate.dialect.Dialect
 * </pre>
 * For dialect <b>org.hibernate.dialect.MySQLInnoDBDialect</b> the following will be returned:<br>
 * <pre>
 *    contentDAO.bean.dialect == BEAN 2
 *    propertyValueDAO.bean.dialect == BEAN 4
 * </pre>
 * For dialect<b>org.hibernate.dialect.MySQLDBDialect</b> the following will be returned:<br>
 * <pre>
 *    contentDAO.bean.dialect == BEAN 1
 *    propertyValueDAO.bean.dialect == BEAN 4
 * </pre>
 * For dialect<b>org.hibernate.dialect.Dialect</b> the following will be returned:<br>
 * <pre>
 *    contentDAO.bean.dialect == BEAN 1
 *    propertyValueDAO.bean.dialect == BEAN 3
 * </pre>
 * 
 * @author Derek Hulley
 * @since 3.2SP1
 */
public class HierarchicalBeanLoader
    implements InitializingBean, FactoryBean, ApplicationContextAware
{
    public static final String DEFAULT_DIALECT_PLACEHOLDER = "#bean.dialect#";
    public static final String DEFAULT_DIALECT_REGEX = "\\#bean\\.dialect\\#";
    
    private ApplicationContext ctx;
    private String targetBeanName;
    private Class<?> targetClass;
    private String dialectBaseClass;
    private String dialectClass;
    
    /**
     * Create a new HierarchicalResourceLoader.
     */
    public HierarchicalBeanLoader()
    {
        super();
    }

    /**
     * The application context that this bean factory serves.
     */
    public void setApplicationContext(ApplicationContext ctx)
    {
        this.ctx = ctx;
    }

    /**
     * @param targetBeanName        the name of the target bean to return,
     *                              including the {@link #DEFAULT_DIALECT_PLACEHOLDER}
     *                              where the specific dialect must be replaced.
     */
    public void setTargetBeanName(String targetBeanName)
    {
        this.targetBeanName = targetBeanName;
    }

    /**
     * Set the target class that will be returned by {@link #getObjectType()}
     * 
     * @param targetClass           the type that this factory returns
     */
    public void setTargetClass(Class<?> targetClass)
    {
        this.targetClass = targetClass;
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
        PropertyCheck.mandatory(this, "targetBeanName", targetBeanName);
        PropertyCheck.mandatory(this, "targetClass", targetClass);
        PropertyCheck.mandatory(this, "dialectBaseClass", dialectBaseClass);
        PropertyCheck.mandatory(this, "dialectClass", dialectClass);
    }
    
    /**
     * @return          Returns {@link #setTargetClass(Class) target class}
     */
    public Class<?> getObjectType()
    {
        return targetClass;
    }

    /**
     * @return          Returns <tt>true</tt> always
     */
    public boolean isSingleton()
    {
        return true;
    }

    /**
     * Replaces the 
     */
    public Object getObject() throws Exception
    {
        if (dialectClass == null || dialectBaseClass == null)
        {
            ctx.getBean(targetBeanName);
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
                    "Non-existent HierarchicalBeanLoader hierarchy: " +
                    dialectBaseClazz.getName() + " is not a superclass of " + dialectClazz);
        }
        
        Class<? extends Object> clazz = dialectClazz;
        Object bean = null;
        while (bean == null)
        {
            // Do replacement
            String newBeanName = targetBeanName.replaceAll(DEFAULT_DIALECT_REGEX, clazz.getName());
            try
            {
                bean = ctx.getBean(newBeanName);
                // Found it
                break;
            }
            catch (NoSuchBeanDefinitionException e)
            {
            }
            // Not found
            bean = null;
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
                        "Non-existent HierarchicalBeanLoaderBean hierarchy: " +
                        dialectBaseClazz.getName() + " is not a superclass of " + dialectClazz);
            }
        }
        return bean;
    }
}
