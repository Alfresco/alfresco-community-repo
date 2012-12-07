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

import java.util.Collections;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentAccessor;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * A class providing basic functionality shared by both {@link ContentTransformer}s and {@link ContentTransformerWorker}
 * s.
 * 
 * @author dward
 */
public class ContentTransformerHelper
{

    private MimetypeService mimetypeService;
    private List<ExplictTransformationDetails> explicitTransformations;
    private List<SupportedTransformation> supportedTransformations;
    private List<SupportedTransformation> unsupportedTransformations;

    /**
     * 
     */
    public ContentTransformerHelper()
    {
        setExplicitTransformations(Collections.<ExplictTransformationDetails> emptyList());
        setSupportedTransformations(null);
        setUnsupportedTransformations(null);
    }

    /**
     * Helper setter of the mimetype service. This is not always required.
     * 
     * @param mimetypeService
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    /**
     * @return Returns the mimetype helper
     */
    protected MimetypeService getMimetypeService()
    {
        return mimetypeService;
    }

    /**
     * Specifies transformations that are considered to be 'exceptional' so 
     * should be used in preference to other transformers that can perform
     * the same transformation.
     */
    public void setExplicitTransformations(List<ExplictTransformationDetails> explicitTransformations)
    {
        this.explicitTransformations = explicitTransformations;
    }

    /**
     * Restricts the transformations that may be performed even though the transformer
     * may perform other transformations. An null value applies no additional restrictions.
     * Even if a list is specified, the
     * {@link ContentTransformer#isTransformableMimetype(String, String, TransformationOptions)}
     * method will still be called.
     */
    public void setSupportedTransformations(List<SupportedTransformation> supportedTransformations)
    {
        this.supportedTransformations = supportedTransformations;
    }

    /**
     * Restricts the transformations that may be performed even though the transformer
     * may claim to perform the transformations. An null value applies no additional restrictions.
     * Even if a list is specified, the
     * {@link ContentTransformer#isTransformableMimetype(String, String, TransformationOptions)}
     * method will still be called.
     */
    public void setUnsupportedTransformations(List<SupportedTransformation> unsupportedTransformations)
    {
        this.unsupportedTransformations = unsupportedTransformations;
    }

    /**
     * Convenience to fetch and check the mimetype for the given content
     * 
     * @param content
     *            the reader/writer for the content
     * @return Returns the mimetype for the content
     * @throws AlfrescoRuntimeException
     *             if the content doesn't have a mimetype
     */
    protected String getMimetype(ContentAccessor content)
    {
        String mimetype = content.getMimetype();
        if (mimetype == null)
        {
            throw new AlfrescoRuntimeException("Mimetype is mandatory for transformation: " + content);
        }
        // done
        return mimetype;
    }

    /**
     * Default implementation, override if need to extend logic
     * 
     * @see org.alfresco.repo.content.transform.ContentTransformer#isExplicitTransformation(java.lang.String,
     *      java.lang.String, org.alfresco.service.cmr.repository.TransformationOptions)
     */
    public boolean isExplicitTransformation(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        boolean result = false;
        for (ExplictTransformationDetails explicitTransformation : this.explicitTransformations)
        {
            if (sourceMimetype.equals(explicitTransformation.getSourceMimetype()) == true
                    && targetMimetype.equals(explicitTransformation.getTargetMimetype()) == true)
            {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean isSupportedTransformation(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        boolean supported = true;
        if (supportedTransformations != null)
        {
            supported = false;
            for (SupportedTransformation suportedTransformation : supportedTransformations)
            {
                String supportedSourceMimetype = suportedTransformation.getSourceMimetype();
                String supportedTargetMimetype = suportedTransformation.getTargetMimetype();
                if ((supportedSourceMimetype == null || sourceMimetype.equals(supportedSourceMimetype)) &&
                    (supportedTargetMimetype == null || targetMimetype.equals(supportedTargetMimetype)))
                {
                    supported = true;
                    break;
                }
            }
        }
        if (supported && unsupportedTransformations != null)
        {
            for (SupportedTransformation unsuportedTransformation : unsupportedTransformations)
            {
                String unsupportedSourceMimetype = unsuportedTransformation.getSourceMimetype();
                String unsupportedTargetMimetype = unsuportedTransformation.getTargetMimetype();
                if ((unsupportedSourceMimetype == null || sourceMimetype.equals(unsupportedSourceMimetype)) &&
                    (unsupportedTargetMimetype == null || targetMimetype.equals(unsupportedTargetMimetype)))
                {
                    supported = false;
                    break;
                }
            }
        }
        return supported;
    }
}