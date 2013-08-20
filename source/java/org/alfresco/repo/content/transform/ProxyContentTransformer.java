/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import net.sf.jooreports.converter.DocumentFormatRegistry;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Makes use of a {@link ContentTransformerWorker} to perform conversions.
 * 
 * @author dward
 */
public class ProxyContentTransformer extends AbstractContentTransformer2
{
    private ContentTransformerWorker worker;

    public ProxyContentTransformer()
    {
    }

    /**
     * @param worker
     *            the worker that the converter uses
     */
    public void setWorker(ContentTransformerWorker worker)
    {
        this.worker = worker;
    }
    
    /**
     * Returns the worker that the converter uses
     */
    public ContentTransformerWorker getWorker()
    {
        return this.worker;
    }

    /**
     * THIS IS A CUSTOM SPRING INIT METHOD 
     */
    public void register()
    {
        if (worker instanceof ContentTransformerHelper)
        {
            logDeprecatedSetter(((ContentTransformerHelper)worker).deprecatedSetterMessages);
        }
        super.register();
    }

    /**
     * @see DocumentFormatRegistry
     */
    @Override
    public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        return this.worker.isTransformable(sourceMimetype, targetMimetype, options);
    }

    @Override
    public String getComments(boolean available)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getComments(available));
        sb.append(worker.getComments(false));
        return sb.toString();
    }

    protected void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions options)
            throws Exception
    {
        TransformationOptionLimits original = options.getLimits();
        try
        {
            // Combine the transformer's limit values into the options so they are available to the worker
            options.setLimits(getLimits(reader, writer, options));

            // Perform the transformation
            this.worker.transform(reader, writer, options);
        }
        finally
        {
            options.setLimits(original);
        }
   }
}
