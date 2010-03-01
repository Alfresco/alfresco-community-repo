/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

    /**
     * 
     */
    public ContentTransformerHelper()
    {
        setExplicitTransformations(Collections.<ExplictTransformationDetails> emptyList());
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

    public void setExplicitTransformations(List<ExplictTransformationDetails> explicitTransformations)
    {
        this.explicitTransformations = explicitTransformations;
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

}