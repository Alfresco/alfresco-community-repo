/*-----------------------------------------------------------------------------
*  Copyright 2006 Alfresco Inc.
*  
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.*  
*  
*  Author  Jon Cox  <jcox@alfresco.com>
*  File    RuntimeSystemPropertiesSetter.java
*----------------------------------------------------------------------------*/

package org.alfresco.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor; 
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;



/**
*   Sets runtime JVM system properties for Spring Framework. 
*
*   This class is used by the Spring framework to inject system properties into
*   the runtime environment (e.g.:  alfresco.jmx.dir).   The motivation for this 
*   is that certain values must be set within spring must be computed in advance
*   for org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
*   to work properly.
*
*/
public class      RuntimeSystemPropertiesSetter 
       implements BeanFactoryPostProcessor, Ordered
{
    // default: just before PropertyPlaceholderConfigurer
    private int order = Integer.MAX_VALUE - 1;  

    public void RuntimeSystemPropertiesSetter() { }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) 
                throws BeansException 
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String path=null;
        try 
        {
            path = loader.getResource("alfresco/alfresco-jmxrmi.password").toURI().getPath();
        }
        catch (java.net.URISyntaxException e ) { e.printStackTrace(); }

        String alfresco_jmx_dir =  
               path.substring(0,path.lastIndexOf("/alfresco-jmxrmi.password"));

        System.setProperty("alfresco.jmx.dir", alfresco_jmx_dir);
    }
    public void setOrder(int order) { this.order = order; }
    public int getOrder()           { return order; }               
}
