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

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Allows direct streaming from source to target when the respective mimetypes
 * are identical, except where the mimetype is text.
 * <p>
 * Text has to be transformed based on the encoding even if the mimetypes don't
 * reflect it. 
 * 
 * @see org.alfresco.repo.content.transform.StringExtractingContentTransformer
 * 
 * @author Derek Hulley
 */
public class BinaryPassThroughContentTransformer extends AbstractContentTransformer2
{
    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(BinaryPassThroughContentTransformer.class);

    @Override
    protected void transformInternal(ContentReader reader,
            ContentWriter writer, TransformationOptions options)
            throws Exception
    {
        // just stream it
        writer.putContent(reader.getContentInputStream());
        
    }

    @Override
    public boolean isTransformableMimetype(String sourceMimetype,
            String targetMimetype, TransformationOptions options)
    {
        if (sourceMimetype.startsWith(StringExtractingContentTransformer.PREFIX_TEXT))
        {
            // we can only stream binary content through
            return false;
        }
        else if (!sourceMimetype.equals(targetMimetype))
        {
            // no transformation is possible so formats must be exact
            return false;
        }
        else
        {
            if (options == null || TransformationOptions.class.equals(options.getClass()) == true)
            {
                // formats are the same and are not text
                return true;
            }
            else
            {
                // If it has meaningful options then we assume there is another transformer better equiped
                // to deal with it
                return false;
            }
        }
    }
    
    @Override
    public String getComments(boolean available)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getComments(available));
        sb.append("# Only supports streaming to the same type but excludes txt\n");
        return sb.toString();
    }
}
