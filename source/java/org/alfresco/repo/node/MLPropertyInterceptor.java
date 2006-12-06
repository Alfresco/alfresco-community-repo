/*
 * Copyright (C) 2006 Alfresco, Inc.
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
package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interceptor to filter out multilingual text properties from getter methods and
 * transform to multilingual text for setter methods.
 * <p>
 * This interceptor ensures that all multilingual (ML) text is transformed to the
 * locale chosen {@link org.alfresco.service.cmr.repository.MLText#getContextLocale() for the request}
 * for getters and transformed to the default locale type for setters.
 * <p>
 * Where {@link org.alfresco.service.cmr.repository.MLText ML text} has been passed in, this
 * will be allowed to pass.
 * 
 * @see org.alfresco.service.cmr.repository.MLText#getContextLocale()
 * @see org.alfresco.service.cmr.repository.NodeService#getProperty(NodeRef, QName)
 * @see org.alfresco.service.cmr.repository.NodeService#getProperties(NodeRef)
 * @see org.alfresco.service.cmr.repository.NodeService#setProperty(NodeRef, QName, Serializable)
 * @see org.alfresco.service.cmr.repository.NodeService#setProperties(NodeRef, Map)
 * 
 * @author Derek Hulley
 */
public class MLPropertyInterceptor implements MethodInterceptor
{
    private static Log logger = LogFactory.getLog(MLPropertyInterceptor.class);
    
    private DictionaryService dictionaryService;
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    @SuppressWarnings("unchecked")
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        Object ret = null;
        String methodName = invocation.getMethod().getName();
        if (methodName.equals("getProperty"))
        {
            ret = invocation.proceed();
            // The return value might need to be converted to a String
            if (ret != null && ret instanceof MLText)
            {
                MLText mlText = (MLText) ret;
                ret = mlText.getDefaultValue();
                // done
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Converted ML text: \n" +
                            "   initial: " + mlText + "\n" +
                            "   converted: " + ret);
                }
            }
        }
        else if (methodName.equals("getProperties"))
        {
            Map<QName, Serializable> properties = (Map<QName, Serializable>) invocation.proceed();
            Map<QName, Serializable> convertedProperties = new HashMap<QName, Serializable>(properties.size() * 2);
            // Check each return value type
            for (Map.Entry<QName, Serializable> entry : properties.entrySet())
            {
                QName key = entry.getKey();
                Serializable value = entry.getValue();
                if (value != null && value instanceof MLText)
                {
                    MLText mlText = (MLText) value;
                    value = mlText.getDefaultValue();
                    // Store the converted value
                    convertedProperties.put(key, value);
                }
                else
                {
                    // The value goes straight back in
                    convertedProperties.put(key, value);
                }
            }
            ret = convertedProperties;
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Converted getProperties return value: \n" +
                        "   initial:   " + properties + "\n" +
                        "   converted: " + convertedProperties);
            }
        }
        else
        {
            ret = invocation.proceed();
        }
        // done
        return ret;
    }
}
