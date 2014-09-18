/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.content.metadata;

import java.util.ArrayList;
import java.util.Set;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.patch.AlfrescoPoiPatchUtils;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.springframework.beans.factory.InitializingBean;

/**
 * POI-based metadata extractor for Office 07 documents.
 * See http://poi.apache.org/ for information on POI.
 * <pre>
 *   <b>author:</b>                 --      cm:author
 *   <b>title:</b>                  --      cm:title
 *   <b>subject:</b>                --      cm:description
 *   <b>created:</b>                --      cm:created
 *   <b>Any custom property:</b>    --      [not mapped]
 * </pre>
 * 
 * Uses Apache Tika<br />
 * <br />
 * Configures {@link AlfrescoPoiPatchUtils} to resolve the following issues:
 * <ul>
 * <li><a href="https://issues.alfresco.com/jira/browse/MNT-577">MNT-577</a></li>
 * <li><a href="https://issues.alfresco.com/jira/browse/MNT-11823">MNT-11823</a></li>
 * </ul>
 * 
 * @author Nick Burch
 * @author Neil McErlean
 * @author Dmitry Velichkevich
 */
public class PoiMetadataExtracter extends TikaPoweredMetadataExtracter implements InitializingBean
{
    protected static Log logger = LogFactory.getLog(PoiMetadataExtracter.class);

    public static ArrayList<String> SUPPORTED_MIMETYPES = buildSupportedMimetypes( 
       new String[] {MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING,
    	               MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET,
    	               MimetypeMap.MIMETYPE_OPENXML_PRESENTATION},
    	 new OOXMLParser() 
    );

    private Integer poiFootnotesLimit;

    private Boolean poiExtractPropertiesOnly = false;

    private Set<String> poiAllowableXslfRelationshipTypes;

    public PoiMetadataExtracter()
    {
        super(PoiMetadataExtracter.class.getName(), SUPPORTED_MIMETYPES);
    }

    @Override
    protected Parser getParser() 
    {
        return new OOXMLParser();
    }

    /**
     * MNT-577: Alfresco is running 100% CPU for over 10 minutes while extracting metadata for Word office document <br />
     * <br />
     * 
     * @param poiFootnotesLimit - {@link Integer} value which specifies limit of amount of footnotes of XWPF documents
     */
    public void setPoiFootnotesLimit(Integer poiFootnotesLimit)
    {
        this.poiFootnotesLimit = poiFootnotesLimit;
    }

    /**
     * MNT-11823: Upload of PPTX causes very high memory usage leading to system instability<br />
     * <br />
     * 
     * @param poiExtractPropertiesOnly - {@link Boolean} value which indicates that POI extractor must avoid building of the full document parts hierarchy and reading content of
     *        the parts
     */
    public void setPoiExtractPropertiesOnly(Boolean poiExtractPropertiesOnly)
    {
        this.poiExtractPropertiesOnly = poiExtractPropertiesOnly;
    }

    public Boolean isPoiExtractPropertiesOnly()
    {
        return (poiExtractPropertiesOnly == null) ? (false) : (poiExtractPropertiesOnly);
    }

    /**
     * MNT-11823: Upload of PPTX causes very high memory usage leading to system instability<br />
     * <br />
     * 
     * @param poiAllowableXslfRelationshipTypes - {@link Set}&lt;{@link String}&gt; instance which determines the list of allowable relationship types for traversing during
     *        analyzing of XSLF document
     */
    public void setPoiAllowableXslfRelationshipTypes(Set<String> poiAllowableXslfRelationshipTypes)
    {
        this.poiAllowableXslfRelationshipTypes = poiAllowableXslfRelationshipTypes;
    }

    public Set<String> getPoiAllowableXslfRelationshipTypes()
    {
        return poiAllowableXslfRelationshipTypes;
    }

    /**
     * MNT-11823: Upload of PPTX causes very high memory usage leading to system instability<br />
     * <br />
     * Initialization of {@link AlfrescoPoiPatchUtils} properties for {@link PoiMetadataExtracter#getExtractorContext()} context
     */
    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (null == poiExtractPropertiesOnly)
        {
            poiExtractPropertiesOnly = false;
        }

        String context = getExtractorContext();

        if (null != poiFootnotesLimit)
        {
            AlfrescoPoiPatchUtils.setPoiFootnotesLimit(context, poiFootnotesLimit);
        }

        AlfrescoPoiPatchUtils.setPoiExtractPropertiesOnly(context, poiExtractPropertiesOnly);
        AlfrescoPoiPatchUtils.setPoiAllowableXslfRelationshipTypes(context, poiAllowableXslfRelationshipTypes);
    }
}
