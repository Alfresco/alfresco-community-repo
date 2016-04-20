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
