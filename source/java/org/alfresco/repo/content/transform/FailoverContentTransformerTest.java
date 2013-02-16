/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Tests the FailoverContentTransformer.
 * 
 * @see org.alfresco.repo.content.transform.FailoverContentTransformer
 * 
 * @author Neil McErlean
 */
public class FailoverContentTransformerTest extends AbstractContentTransformerTest
{
    private static final String sourceMimeType = MimetypeMap.MIMETYPE_PDF;
    private static final String targetMimeType = MimetypeMap.MIMETYPE_IMAGE_PNG;

    private static ApplicationContext failoverAppContext =
        new ClassPathXmlApplicationContext(new String[] {"classpath:org/alfresco/repo/content/transform/FailoverContentTransformerTest-context.xml"},
            ApplicationContextHelper.getApplicationContext());
    
    private FailoverContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        ApplicationContextHelper.getApplicationContext();
        
        transformer = (FailoverContentTransformer) failoverAppContext.getBean("transformer.failover.Test-FailThenSucceed");
        transformer.setMimetypeService(mimetypeService);
        transformer.setTransformerDebug(transformerDebug);
        transformer.setTransformerConfig(transformerConfig);
    }
    
    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }
    
    public void testReliability() throws Exception
    {
        // The MIME types here are rather arbitrary

        boolean reliability = transformer.isTransformable(sourceMimeType, -1, targetMimeType, new TransformationOptions());
        assertEquals("Mimetype should be supported", true, reliability);
    }
}

/**
 * This dummy class is used only for test purposes within this source file.
 * 
 * @author Neil McErlean
 */
class DummyTestContentTransformer extends AbstractContentTransformer2 implements BeanNameAware
{
    private static Log logger = LogFactory.getLog(DummyTestContentTransformer.class);

    /** Bean name for logging */
    private String springBeanName;
    private boolean alwaysFail;
    
    public void setAlwaysFail(boolean value)
    {
        this.alwaysFail = value;
    }

    @Override
    protected void transformInternal(ContentReader reader,
            ContentWriter writer, TransformationOptions options)
            throws Exception
    {
        // Do not actually perform any transformation. The test above is only interested in whether
        // an exception is thrown and handled.
        if (logger.isInfoEnabled())
        {
            logger.info(springBeanName + " is attempting a transformation");
        }

        reader.getContentString();
        
        if (alwaysFail)
        {
            throw new AlfrescoRuntimeException("Test code intentionally failed method call.");
        }
        else
        {
            return;
        }
    }

    @Override
    public boolean isTransformableMimetype(String sourceMimetype,
            String targetMimetype, TransformationOptions options)
    {
        // We'll arbitrarily claim to be able to transform PDF to PNG
        return (MimetypeMap.MIMETYPE_PDF.equals(sourceMimetype) &&
                MimetypeMap.MIMETYPE_IMAGE_PNG.equals(targetMimetype));
    }

    public void setBeanName(String name)
    {
        this.springBeanName = name;
    }
}
