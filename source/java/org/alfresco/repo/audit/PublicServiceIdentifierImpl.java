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
package org.alfresco.repo.audit;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.service.PublicService;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Identify public services by method invocation. Look ups are cached on a thread local as they are quite expensive. All public service names end with "Service" and start with
 * capital letter. This pattern is used to filter bean names. TODO: Look at pulling out all the mappings at start up.
 * 
 * @author Andy Hind
 */
public class PublicServiceIdentifierImpl implements PublicServiceIdentifier, BeanFactoryPostProcessor
{
    private static Log s_logger = LogFactory.getLog(PublicServiceIdentifierImpl.class);

    private static ThreadLocal<HashMap<Method, String>> methodToServiceMap = new ThreadLocal<HashMap<Method, String>>();

    private ConfigurableListableBeanFactory beanFactory;

    public PublicServiceIdentifierImpl()
    {
        super();
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }

    public String getPublicServiceName(MethodInvocation mi)
    {
        return getServiceName(mi);
    }

    /**
     * Cache service name look up.
     * 
     * @param mi
     * @return
     * @throws BeansException
     */
    private String getServiceName(MethodInvocation mi) throws BeansException
    {
        if (methodToServiceMap.get() == null)
        {
            methodToServiceMap.set(new HashMap<Method, String>());
        }
        Method method = mi.getMethod();
        String serviceName = methodToServiceMap.get().get(method);
        if (serviceName == null)
        {
            serviceName = getServiceNameImpl(mi);
            methodToServiceMap.get().put(method, serviceName);
        }
        else
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Cached look up for " + serviceName + "." + method.getName());
            }
        }
        return serviceName;
    }

    /**
     * Do the look up by interface type.
     * 
     * @param mi
     * @return
     * @throws BeansException
     */

    private String getServiceNameImpl(MethodInvocation mi) throws BeansException
    {
        Class clazz = mi.getThis().getClass();
        while (clazz != null)
        {
            Class[] interfaces = clazz.getInterfaces();
            for (Class iFace : interfaces)
            {
                Class publicServiceInterface = findPublicService(iFace);
                if (publicServiceInterface != null)
                {
                    Map beans = beanFactory.getBeansOfType(publicServiceInterface);
                    Iterator iter = beans.entrySet().iterator();
                    while (iter.hasNext())
                    {
                        Map.Entry entry = (Map.Entry) iter.next();
                        String serviceName = (String) entry.getKey();
                        if ((serviceName.endsWith("Service"))
                                && (Character.isUpperCase(serviceName.charAt(0)))
                                && !serviceName.equals("DescriptorService"))
                        {
                            return serviceName;
                        }
                    }
                }

            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * We use a marker annotation to identify public interfaces.
     * The interfaces have to be walked to determine if a public interface is implemented.
     * 
     * Only one public service interface is expected.
     * 
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    private Class findPublicService(Class clazz)
    {
        if (!clazz.isInterface())
        {
            return null;
        }
        
        if (clazz.isAnnotationPresent(PublicService.class))
        {
            return clazz;
        }

        Class[] classes = clazz.getInterfaces();
        for(Class implemented: classes)
        {
            Class answer = findPublicService(implemented);
            if(answer != null)
            {
                return answer;
            }
        }
        return null;

    }
}
