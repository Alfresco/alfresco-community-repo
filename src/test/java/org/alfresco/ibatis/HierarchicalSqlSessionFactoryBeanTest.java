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
package org.alfresco.ibatis;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.alfresco.util.resource.HierarchicalResourceLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * @see HierarchicalSqlSessionFactoryBean
 * @see HierarchicalXMLConfigBuilder
 * @see HierarchicalResourceLoader
 * 
 * @author Derek Hulley, janv
 * @since 4.0
 */
public class HierarchicalSqlSessionFactoryBeanTest extends TestCase
{
    private static final String QUERY_OBJECT = Object.class.getName();
    private static final String QUERY_ABSTRACTCOLLECTION = "org.alfresco.ibatis.abstractcollection."+AbstractCollection.class.getName().replace(".", "_");
    private static final String QUERY_ABSTRACTLIST = "org.alfresco.ibatis.abstractlist."+AbstractList.class.getName().replace(".", "_");
    private static final String QUERY_TREESET = "org.alfresco.ibatis.treeset."+TreeSet.class.getName().replace(".", "_");
    
    private static Log logger = LogFactory.getLog(HierarchicalSqlSessionFactoryBeanTest.class);
    
    private ClassPathXmlApplicationContext ctx;
    private TestDAO testDao;
    
    @Override
    public void setUp() throws Exception
    {
        testDao = new TestDAO();
        testDao.setId(5L);
        testDao.setPropOne("prop-one");
        testDao.setPropTwo("prop-two");
    }
    
    @Override
    public void tearDown() throws Exception
    {
        try
        {
            if (ctx != null)
            {
                ctx.close();
            }
        }
        catch (Throwable e)
        {
            logger.error("Failed to neatly close application context", e);
        }
    }
    
    /**
     * Pushes the dialect class into the system properties, closes an current context and
     * recreates it; the MyBatis Configuration is then returned.
     */
    @SuppressWarnings("unchecked")
    private Configuration getConfiguration(Class dialectClass) throws Exception
    {
        System.setProperty("hierarchy-test.dialect", dialectClass.getName());
        if (ctx != null)
        {
            try
            {
                ctx.close();
                ctx = null;
            }
            catch (Throwable e)
            {
                logger.error("Failed to neatly close application context", e);
            }
        }
        ctx = new ClassPathXmlApplicationContext("ibatis/hierarchy-test/hierarchy-test-context.xml");
        return ((SqlSessionFactory)ctx.getBean("mybatisConfig")).getConfiguration();
    }
    
    /**
     * Check context startup and shutdown
     */
    public void testContextStartup() throws Exception
    {
        getConfiguration(TreeSet.class);
        getConfiguration(HashSet.class);
        getConfiguration(ArrayList.class);
        getConfiguration(AbstractCollection.class);
        try
        {
            getConfiguration(Collection.class);
            fail("Failed to detect incompatible class hierarchy");
        }
        catch (Throwable e)
        {
            // Expected
        }
    }
    
    public void testHierarchyTreeSet() throws Exception
    {
        Configuration mybatisConfig = getConfiguration(TreeSet.class);
        MappedStatement stmt = mybatisConfig.getMappedStatement(QUERY_TREESET);
        assertNotNull("Query missing for " + QUERY_TREESET + " using " + TreeSet.class, stmt);
        try
        {
            mybatisConfig.getMappedStatement(QUERY_ABSTRACTCOLLECTION);
            fail("Query not missing for " + QUERY_ABSTRACTCOLLECTION + " using " + TreeSet.class);
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    public void testHierarchyHashSet() throws Exception
    {
        Configuration mybatisConfig = getConfiguration(HashSet.class);
        MappedStatement stmt = mybatisConfig.getMappedStatement(QUERY_ABSTRACTCOLLECTION);
        assertNotNull("Query missing for " + QUERY_ABSTRACTCOLLECTION + " using " + HashSet.class, stmt);
        try
        {
            mybatisConfig.getMappedStatement(QUERY_OBJECT);
            fail("Query not missing for " + QUERY_OBJECT + " using " + HashSet.class);
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    public void testHierarchyArrayList() throws Exception
    {
        Configuration mybatisConfig = getConfiguration(ArrayList.class);
        MappedStatement stmt = mybatisConfig.getMappedStatement(QUERY_ABSTRACTLIST);
        assertNotNull("Query missing for " + QUERY_ABSTRACTLIST + " using " + ArrayList.class, stmt);
        try
        {
            mybatisConfig.getMappedStatement(QUERY_ABSTRACTCOLLECTION);
            fail("Query not missing for " + QUERY_ABSTRACTCOLLECTION + " using " + ArrayList.class);
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    public void testHierarchyAbstractCollection() throws Exception
    {
        Configuration mybatisConfig = getConfiguration(AbstractCollection.class);
        MappedStatement stmt = mybatisConfig.getMappedStatement(QUERY_ABSTRACTCOLLECTION);
        assertNotNull("Query missing for " + QUERY_ABSTRACTCOLLECTION + " using " + AbstractCollection.class, stmt);
        try
        {
            mybatisConfig.getMappedStatement(QUERY_OBJECT);
            fail("Query not missing for " + QUERY_OBJECT + " using " + AbstractCollection.class);
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    /**
     * Helper class that iBatis will use in the test mappings
     * @author Derek Hulley
     */
    public static class TestDAO
    {
        private Long id;
        private String propOne;
        private String propTwo;
        
        public Long getId()
        {
            return id;
        }
        public void setId(Long id)
        {
            this.id = id;
        }
        public String getPropOne()
        {
            return propOne;
        }
        public void setPropOne(String propOne)
        {
            this.propOne = propOne;
        }
        public String getPropTwo()
        {
            return propTwo;
        }
        public void setPropTwo(String propTwo)
        {
            this.propTwo = propTwo;
        }
    }
}
