/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.content.metadata;

import java.util.List;

import org.apache.tika.extractor.DocumentSelector;
import org.apache.tika.metadata.Metadata;

/**
 * Tika 1.6 has the ability to parse embedded artifacts, such as images in a PDF,
 * but this can be very resource intensive so adding this selector
 * to parsers and transformers that handle formats with embedded artifacts
 * will disable parsing of the specified content types.
 */
public class MediaTypeDisablingDocumentSelector implements DocumentSelector
{
    private List<String> disabledMediaTypes;
    
    public void setDisabledMediaTypes(List<String> disabledMediaTypes)
    {
        this.disabledMediaTypes = disabledMediaTypes;
    }

    @Override
    public boolean select(Metadata metadata)
    {
        String contentType = metadata.get(Metadata.CONTENT_TYPE);
        if (contentType == null || contentType.equals("") || disabledMediaTypes == null)
        {
            return true;
        }
        return !disabledMediaTypes.contains(contentType);
    }
}
