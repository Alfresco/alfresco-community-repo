/*
 * Copyright (C) 2005 Antti Jokipii
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;

import com.catcode.odf.ODFMetaFileAnalyzer;
import com.catcode.odf.OpenDocumentMetadata;

/**
 * Metadata extractor for the
 * {@link org.alfresco.repo.content.MimetypeMap#MIMETYPE_OPENDOCUMENT_TEXT MIMETYPE_OPENDOCUMENT_XXX}
 * mimetypes.
 * <pre>
 *   <b>creationDate:</b>           --      cm:created
 *   <b>creator:</b>                --      cm:author
 *   <b>date:</b>
 *   <b>description:</b>            --      cm:description
 *   <b>generator:</b>
 *   <b>initialCreator:</b>
 *   <b>keyword:</b>
 *   <b>language:</b>
 *   <b>printDate:</b>
 *   <b>printedBy:</b>
 *   <b>subject:</b>
 *   <b>title:</b>                  --      cm:title
 *   <b>All user properties</b>
 * </pre>
 * 
 * @author Antti Jokipii
 * @author Derek Hulley
 */
public class OpenDocumentMetadataExtracter extends AbstractMappingMetadataExtracter
{
    private static final String KEY_CREATION_DATE = "creationDate";
    private static final String KEY_CREATOR = "creator";
    private static final String KEY_DATE = "date";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_GENERATOR = "generator";
    private static final String KEY_INITIAL_CREATOR = "initialCreator";
    private static final String KEY_KEYWORD = "keyword";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_PRINT_DATE = "printDate";
    private static final String KEY_PRINTED_BY = "printedBy";
    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_TITLE = "title";
    
    public static String[] SUPPORTED_MIMETYPES = new String[] {
            MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_GRAPHICS,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_GRAPHICS_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_PRESENTATION,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_PRESENTATION_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_SPREADSHEET,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_SPREADSHEET_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_CHART,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_CHART_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_IMAGE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_IMAGE_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_FORMULA,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_FORMULA_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT_MASTER,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT_WEB,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_DATABASE };

    public OpenDocumentMetadataExtracter()
    {
        super(new HashSet<String>(Arrays.asList(SUPPORTED_MIMETYPES)));
    }

    @Override
    public Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        Map<String, Serializable> rawProperties = newRawMap();
        
        ODFMetaFileAnalyzer analyzer = new ODFMetaFileAnalyzer();
        InputStream is = null;
        try
        {
            is = reader.getContentInputStream();
            // stream the document in
            OpenDocumentMetadata docInfo = analyzer.analyzeZip(is);

            if (docInfo != null)
            {
                putRawValue(KEY_CREATION_DATE, docInfo.getCreationDate(), rawProperties);
                putRawValue(KEY_CREATOR, docInfo.getCreator(), rawProperties);
                putRawValue(KEY_DATE, docInfo.getDate(), rawProperties);
                putRawValue(KEY_DESCRIPTION, docInfo.getDescription(), rawProperties);
                putRawValue(KEY_GENERATOR, docInfo.getGenerator(), rawProperties);
                putRawValue(KEY_INITIAL_CREATOR, docInfo.getInitialCreator(), rawProperties);
                putRawValue(KEY_KEYWORD, docInfo.getKeyword(), rawProperties);
                putRawValue(KEY_LANGUAGE, docInfo.getLanguage(), rawProperties);
                putRawValue(KEY_PRINT_DATE, docInfo.getPrintDate(), rawProperties);
                putRawValue(KEY_PRINTED_BY, docInfo.getPrintedBy(), rawProperties);
                putRawValue(KEY_SUBJECT, docInfo.getSubject(), rawProperties);
                putRawValue(KEY_TITLE, docInfo.getTitle(), rawProperties);
                
                // Handle user-defined properties dynamically
                Map<String, Set<QName>> mapping = super.getMapping();
                Hashtable userDefinedProperties = docInfo.getUserDefined();
                // Extract those user properties for which there is a mapping
                for (String key : mapping.keySet())
                {
                    if (userDefinedProperties.containsKey(key))
                    {
                        Object value = userDefinedProperties.get(key);
                        if (value != null && value instanceof Serializable)
                        {
                            putRawValue(key, (Serializable) value, rawProperties);
                        }
                    }
                }
            }
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (IOException e) {}
            }
        }
        // Done
        return rawProperties;
    }
}