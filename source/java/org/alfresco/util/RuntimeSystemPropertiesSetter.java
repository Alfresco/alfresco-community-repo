/*-----------------------------------------------------------------------------
*  Copyright 2006 Alfresco Inc.
*  
*  Licensed under the Mozilla Public License version 1.1
*  with a permitted attribution clause. You may obtain a
*  copy of the License at:
*  
*      http://www.alfresco.org/legal/license.txt
*  
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
*  either express or implied. See the License for the specific
*  language governing permissions and limitations under the
*  License.
*  
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
