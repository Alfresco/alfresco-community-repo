/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Helper class to provide static and common access to the Spring
 * {@link org.springframework.context.ApplicationContext application context}.
 * 
 * @author Derek Hulley
 */
public class ApplicationContextHelper
{
    /** location of required configuration files */
    public static final String[] CONFIG_LOCATIONS = new String[] { "classpath:alfresco/application-context.xml" };
    
    /**
     * Instantiates a new application context.
     * 
     * @return Returns a new application context
     */
    public static ApplicationContext getApplicationContext()
    {
        return new ClassPathXmlApplicationContext(CONFIG_LOCATIONS);
    }
}
