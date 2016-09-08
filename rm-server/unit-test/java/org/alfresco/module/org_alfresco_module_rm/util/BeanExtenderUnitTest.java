/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.util;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.util.GUID;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;


/**
 * Bean extender unit test.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public class BeanExtenderUnitTest extends BaseUnitTest
{
    private static final String BEAN_NAME = GUID.generate();
    private static final String EXTENDING_BEAN_NAME = GUID.generate();
         
    @Mock private ConfigurableListableBeanFactory mockedBeanFactory;
    @Mock private BeanDefinition mockedBeanDefinition;
    @Mock private BeanDefinition mockedExtendingBeanDefinition;
    @Mock private MutablePropertyValues mockedPropertyValuesBean;
    @Mock private MutablePropertyValues mockedPropertyValuesExtendingBean;
    
    @InjectMocks private BeanExtender beanExtender;
    
    @Override
    public void before()
    {
        super.before();
        
        // setup common interactions
        doReturn(mockedPropertyValuesBean).when(mockedBeanDefinition).getPropertyValues();
        doReturn(mockedPropertyValuesExtendingBean).when(mockedExtendingBeanDefinition).getPropertyValues();
    }
    
    /**
     * given that the bean name is not set, ensure that an Illegal Argument 
     * exception is thrown.
     */
    @Test
    public void beanNameNotSet()
    {
        // === given ===
        
        // set the extending bean name
        beanExtender.setExtendingBeanName(EXTENDING_BEAN_NAME);
        
        // expecting exception
        exception.expect(IllegalArgumentException.class);
        
        // === when ===
        beanExtender.postProcessBeanFactory(mockedBeanFactory);
    }
    
    /**
     * given that the extending bean name is not set, ensure that an illegal 
     * argument exception is thrown.
     */
    @Test
    public void extendingBeanNameNotSet()
    {
        // === given ===
        
        // set the extending bean name
        beanExtender.setBeanName(BEAN_NAME);
        
        // expecting exception
        exception.expect(IllegalArgumentException.class);
        
        // === when ===
        beanExtender.postProcessBeanFactory(mockedBeanFactory);
    }
    
    /**
     * given that the bean does not exist ensure that an exception is thrown
     */
    @Test
    public void beanDoesNotExist()
    {
        // === given ===
        
        // set the bean names
        beanExtender.setBeanName(BEAN_NAME);
        beanExtender.setExtendingBeanName(EXTENDING_BEAN_NAME);
        doReturn(false).when(mockedBeanFactory).containsBean(BEAN_NAME);
        doReturn(true).when(mockedBeanFactory).containsBean(EXTENDING_BEAN_NAME);
        
        // expecting exception
        exception.expect(NoSuchBeanDefinitionException.class);
        
        // === when ===
        beanExtender.postProcessBeanFactory(mockedBeanFactory);        
    }
    
    /**
     * given that the extending bean does not exist ensure that an exception is thrown
     */
    @Test
    public void extendingBeanDoesNotExist()
    {
        // === given ===
        
        // set the bean names
        beanExtender.setBeanName(BEAN_NAME);
        beanExtender.setExtendingBeanName(EXTENDING_BEAN_NAME);
        doReturn(true).when(mockedBeanFactory).containsBean(BEAN_NAME);
        doReturn(false).when(mockedBeanFactory).containsBean(EXTENDING_BEAN_NAME);
        
        // expecting exception
        exception.expect(NoSuchBeanDefinitionException.class);
        
        // === when ===
        beanExtender.postProcessBeanFactory(mockedBeanFactory); 
    }
    
    /**
     * given that a different class name has been set on the extending bean ensure it is 
     * set correctly on the origional bean
     */
    @Test
    public void beanClassNameSet()
    {
        // === given ===
        
        // set the bean names
        beanExtender.setBeanName(BEAN_NAME);
        beanExtender.setExtendingBeanName(EXTENDING_BEAN_NAME);
        
        // both beans are available in the bean factory
        doReturn(true).when(mockedBeanFactory).containsBean(BEAN_NAME);
        doReturn(true).when(mockedBeanFactory).containsBean(EXTENDING_BEAN_NAME);
        
        // return the mocked bean definitions
        doReturn(mockedBeanDefinition).when(mockedBeanFactory).getBeanDefinition(BEAN_NAME);
        doReturn(mockedExtendingBeanDefinition).when(mockedBeanFactory).getBeanDefinition(EXTENDING_BEAN_NAME);
        
        // bean class names
        doReturn("a").when(mockedBeanDefinition).getBeanClassName();
        doReturn("b").when(mockedExtendingBeanDefinition).getBeanClassName();
        
        // no properties have been defined
        doReturn(Collections.EMPTY_LIST).when(mockedPropertyValuesExtendingBean).getPropertyValueList();
        
        // === when ===
        beanExtender.postProcessBeanFactory(mockedBeanFactory); 
        
        // === then ===
        
        // expect the class name to be set on the bean
        verify(mockedBeanDefinition, times(1)).setBeanClassName("b");
        verify(mockedPropertyValuesBean, never()).add(anyString(), anyString());
        
    }
    
    /**
     * given that new property values have been set on the extending bean ensure that they
     * are correctly set on the original bean.
     */
    @Test
    public void beanPropertyValuesSet()
    {
        // === given ===
        
        // set the bean names
        beanExtender.setBeanName(BEAN_NAME);
        beanExtender.setExtendingBeanName(EXTENDING_BEAN_NAME);
        
        // both beans are available in the bean factory
        doReturn(true).when(mockedBeanFactory).containsBean(BEAN_NAME);
        doReturn(true).when(mockedBeanFactory).containsBean(EXTENDING_BEAN_NAME);
        
        // return the mocked bean definitions
        doReturn(mockedBeanDefinition).when(mockedBeanFactory).getBeanDefinition(BEAN_NAME);
        doReturn(mockedExtendingBeanDefinition).when(mockedBeanFactory).getBeanDefinition(EXTENDING_BEAN_NAME);
        
        // bean class names
        doReturn("a").when(mockedBeanDefinition).getBeanClassName();
        doReturn(null).when(mockedExtendingBeanDefinition).getBeanClassName();

        PropertyValue mockedPropertyValueOne = generateMockedPropertyValue("one", "1");
        PropertyValue mockedPropertyValueTwo = generateMockedPropertyValue("two", "2");
        List<PropertyValue> list = new ArrayList<PropertyValue>(2);
        list.add(mockedPropertyValueOne);
        list.add(mockedPropertyValueTwo);
        doReturn(list).when(mockedPropertyValuesExtendingBean).getPropertyValueList();
        
        // === when ===
        beanExtender.postProcessBeanFactory(mockedBeanFactory); 
        
        // === then ===
        
        // expect the class name to be set on the bean
        verify(mockedBeanDefinition, never()).setBeanClassName(anyString());
        verify(mockedPropertyValuesBean, times(1)).add("one", "1");
        verify(mockedPropertyValuesBean, times(1)).add("two", "2");
    }
    
    /**
     * Helper method to generate a mocked property value
     */
    private PropertyValue generateMockedPropertyValue(String name, String value)
    {
        PropertyValue result = mock(PropertyValue.class);
        doReturn(name).when(result).getName();
        doReturn(value).when(result).getValue();
        return result;
    }
}
