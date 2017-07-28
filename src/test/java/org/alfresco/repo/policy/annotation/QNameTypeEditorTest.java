/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.policy.annotation;

import static org.junit.Assert.assertEquals;

import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * Application context tests for the automatic QName converter.
 *
 * @author Tom Page
 */
@Category(OwnJVMTestsCategory.class)
public class QNameTypeEditorTest
{
    /** The location of the Spring context file used by this test. */
    private static final String TEST_CONFIG_LOCATION = "classpath:org/alfresco/repo/policy/annotation/test-qname-type-editor-context.xml";
    /** A list containing the standard Spring context files and the context file for this test. */
    private static final String[] CONFIG_LOCATIONS = ArrayUtils.add(ApplicationContextHelper.CONFIG_LOCATIONS, TEST_CONFIG_LOCATION);
    /** The Spring application context. */
    private static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext(CONFIG_LOCATIONS);

    /** Check that loading a bean with a QName containing a reference to a namespace works. */
    @Test
    public void testPopulateBeanWithNamespace() throws Exception
    {
        QNameContainer qNameContainer = (QNameContainer) applicationContext.getBean("qNameContainerWithNamespace");

        QName expectedQName = QName.createQName("http://www.alfresco.org/model/content/1.0", "namespacedName");
        assertEquals("Loading String as QName failed.", expectedQName, qNameContainer.getQName());
    }

    /** Check loading a bean with a non-namespaced QName. */
    @Test
    public void testPopulateBeanWithoutNamespace() throws Exception
    {
        QNameContainer qNameContainer = (QNameContainer) applicationContext.getBean("qNameContainerNoNamespace");

        QName expectedQName = QName.createQName("noNamespaceName");
        assertEquals("Loading String as QName failed.", expectedQName, qNameContainer.getQName());
    }

    /** A POJO used to check the QName property behaviour. */
    public static class QNameContainer
    {
        private QName qName;

        /** Store a QName. */
        public void setQName(QName qName)
        {
            this.qName = qName;
        }

        /** Retrieve the stored QName. */
        public QName getQName()
        {
            return qName;
        }
    }

    /**
     * Clear up after all the tests have finished and dereference the application context to allow it to be garbage
     * collected.
     */
    @AfterClass
    public static void tidyAfterTestClass()
    {
        ApplicationContextHelper.closeApplicationContext();
        applicationContext = null;
    }
}
