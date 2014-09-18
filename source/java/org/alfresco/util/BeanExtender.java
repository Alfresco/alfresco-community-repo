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
package org.alfresco.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Extends the definition of a bean with another.
 * <p>
 * Implements bean factory post processor.
 *
 * @author Roy Wetherall
 * @since 5.0
 */
public class BeanExtender implements BeanFactoryPostProcessor
{
    /** name of bean to extend */
    private String beanName;

    /** extending bean name */
    private String extendingBeanName;

    /**
     * @param beanName  bean name
     */
    public void setBeanName(String beanName)
    {
        this.beanName = beanName;
    }

    /**
     * @param extendingBeanName extending bean name
     */
    public void setExtendingBeanName(String extendingBeanName)
    {
        this.extendingBeanName = extendingBeanName;
    }

    /**
     * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
    {
        ParameterCheck.mandatory("beanName", beanName);
        ParameterCheck.mandatory("extendingBeanName", extendingBeanName);

        // check for bean name
        if (!beanFactory.containsBean(beanName))
        {
            throw new NoSuchBeanDefinitionException("Can't find bean '" + beanName + "' to be extended.");
        }

        // check for extending bean
        if (!beanFactory.containsBean(extendingBeanName))
        {
            throw new NoSuchBeanDefinitionException("Can't find bean '" + extendingBeanName + "' that is going to extend origional bean definition.");
        }

        // get the bean definitions
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        BeanDefinition extendingBeanDefinition = beanFactory.getBeanDefinition(extendingBeanName);

        // update class
        if (StringUtils.isNotBlank(extendingBeanDefinition.getBeanClassName()) &&
            !beanDefinition.getBeanClassName().equals(extendingBeanDefinition.getBeanClassName()))
        {
            beanDefinition.setBeanClassName(extendingBeanDefinition.getBeanClassName());
        }

        // update properties
        MutablePropertyValues properties = beanDefinition.getPropertyValues();
        MutablePropertyValues extendingProperties = extendingBeanDefinition.getPropertyValues();
        for (PropertyValue propertyValue : extendingProperties.getPropertyValueList())
        {
            properties.add(propertyValue.getName(), propertyValue.getValue());
        }
    }
}
