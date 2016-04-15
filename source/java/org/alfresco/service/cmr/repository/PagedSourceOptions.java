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
package org.alfresco.service.cmr.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.AbstractTransformationSourceOptions;

/**
 * Paged content conversion options to specify a page number range.
 * <p>
 * The page numbering index starts with 1.
 * <p>
 * If only the start page number is specified transformers should attempt
 * a page range from that page number to the end if possible.
 * <p>
 * If only an end page number is specified transformers should attempt
 * a page range from the start to that page if possible.
 * 
 * @author Ray Gauss II
 */
public class PagedSourceOptions extends AbstractTransformationSourceOptions
{
    public static final Integer PAGE_1 = new Integer(1);

    /** The start of the page range in the source document */
    private Integer startPageNumber;
    
    /** The end of the page range in the source document */
    private Integer endPageNumber;
    
    protected static List<String> getDefaultApplicableMimetypes()
    {
        List<String> defaults = new ArrayList<String>(17);
        defaults.add(MimetypeMap.MIMETYPE_PDF);
        defaults.add(MimetypeMap.MIMETYPE_WORD);
        defaults.add(MimetypeMap.MIMETYPE_PPT);
        defaults.add(MimetypeMap.MIMETYPE_IMAGE_TIFF);
        defaults.add(MimetypeMap.MIMETYPE_OPENDOCUMENT_PRESENTATION);
        defaults.add(MimetypeMap.MIMETYPE_OPENDOCUMENT_PRESENTATION_TEMPLATE);
        defaults.add(MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT_TEMPLATE);
        defaults.add(MimetypeMap.MIMETYPE_OPENOFFICE1_WRITER);
        defaults.add(MimetypeMap.MIMETYPE_OPENOFFICE1_IMPRESS);
        defaults.add(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION);
        defaults.add(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING);
        defaults.add(MimetypeMap.MIMETYPE_STAROFFICE5_IMPRESS);
        defaults.add(MimetypeMap.MIMETYPE_STAROFFICE5_IMPRESS_PACKED);
        defaults.add(MimetypeMap.MIMETYPE_STAROFFICE5_WRITER);
        defaults.add(MimetypeMap.MIMETYPE_STAROFFICE5_WRITER_GLOBAL);
        defaults.add(MimetypeMap.MIMETYPE_IWORK_KEYNOTE);
        defaults.add(MimetypeMap.MIMETYPE_IWORK_PAGES);
        defaults.add(MimetypeMap.MIMETYPE_WORDPERFECT);
        return defaults;
    }
    
    public PagedSourceOptions()
    {
        super();
        setApplicableMimetypes(PagedSourceOptions.getDefaultApplicableMimetypes());
    }
    
    /**
     * Gets the page number to start from in the source document
     * 
     * @return the start page number
     */
    public Integer getStartPageNumber()
    {
        return startPageNumber;
    }
    
    /**
     * Sets the page number to start from in the source document
     * 
     * @param startPageNumber the start page number
     */
    public void setStartPageNumber(Integer startPageNumber)
    {
        this.startPageNumber = startPageNumber;
    }

    /**
     * Gets the page number to end at in the source document
     * 
     * @return the start page number
     */
    public Integer getEndPageNumber()
    {
        return endPageNumber;
    }

    /**
     * Sets the page number to end at in the source document
     * 
     * @param endPageNumber the end page number
     */
    public void setEndPageNumber(Integer endPageNumber)
    {
        this.endPageNumber = endPageNumber;
    }

    @Override
    public TransformationSourceOptions mergedOptions(TransformationSourceOptions overridingOptions)
    {
        if (overridingOptions instanceof PagedSourceOptions)
        {
            PagedSourceOptions mergedOptions = (PagedSourceOptions) super.mergedOptions(overridingOptions);

            if (((PagedSourceOptions) overridingOptions).getStartPageNumber() != null)
            {
                mergedOptions.setStartPageNumber(((PagedSourceOptions) overridingOptions).getStartPageNumber());
            }
            if (((PagedSourceOptions) overridingOptions).getEndPageNumber() != null)
            {
                mergedOptions.setEndPageNumber(((PagedSourceOptions) overridingOptions).getEndPageNumber());
            }
            return mergedOptions;
        }
        return null;
    }
    
    /**
     * Gets paged source options which specify just the first page.
     * 
     * @return the page one source options
     */
    public static PagedSourceOptions getPage1Instance() {
        PagedSourceOptions sourceOptions = new PagedSourceOptions();
        sourceOptions.setStartPageNumber(PAGE_1);
        sourceOptions.setEndPageNumber(PAGE_1);
        return sourceOptions;
    }
    
    @Override
    public TransformationSourceOptionsSerializer getSerializer()
    {
        return PagedSourceOptions.createSerializerInstance();
    }
    
    /**
     * Creates an instance of the options serializer
     * 
     * @return the options serializer
     */
    public static TransformationSourceOptionsSerializer createSerializerInstance()
    {
        return (new PagedSourceOptions()).new PagedSourceOptionsSerializer();
    }
    
    /**
     * Serializer for paged source options
     */
    public class PagedSourceOptionsSerializer implements TransformationSourceOptionsSerializer
    {
        public static final String PARAM_SOURCE_START_PAGE = "source_start_page";
        public static final String PARAM_SOURCE_END_PAGE = "source_end_page";
        
        @Override
        public TransformationSourceOptions deserialize(SerializedTransformationOptionsAccessor serializedOptions)
        {
            int startPageNumber = serializedOptions.getIntegerParam(PARAM_SOURCE_START_PAGE, 1);
            int endPageNumber = serializedOptions.getIntegerParam(PARAM_SOURCE_END_PAGE, 1);
            
            PagedSourceOptions sourceOptions = new PagedSourceOptions();
            sourceOptions.setStartPageNumber(startPageNumber);
            sourceOptions.setEndPageNumber(endPageNumber);
            return sourceOptions;
        }

        @Override
        public void serialize(TransformationSourceOptions sourceOptions, 
                Map<String, Serializable> parameters)
        {
            if (parameters == null || sourceOptions == null)
            {
                return;
            }
            PagedSourceOptions pagedSourceOptions = (PagedSourceOptions) sourceOptions;
            parameters.put(PARAM_SOURCE_START_PAGE, pagedSourceOptions.getStartPageNumber());
            parameters.put(PARAM_SOURCE_END_PAGE, pagedSourceOptions.getEndPageNumber());
        }
    }
}
