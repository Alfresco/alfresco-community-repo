/*
 * Copyright (C) 2005 Jesper Steen Møller
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
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;

/**
 * Office file format Metadata Extracter.  This extracter uses the POI library to extract
 * the following:
 * <pre>
 *   <b>author:</b>             --      cm:author
 *   <b>title:</b>              --      cm:title
 *   <b>subject:</b>            --      cm:description
 *   <b>createDateTime:</b>     --      cm:created
 *   <b>lastSaveDateTime:</b>   --      cm:modified
 *   <b>comments:</b>
 *   <b>editTime:</b>
 *   <b>format:</b>
 *   <b>keywords:</b>
 *   <b>lastAuthor:</b>
 *   <b>lastPrinted:</b>
 *   <b>osVersion:</b>
 *   <b>thumbnail:</b>
 *   <b>pageCount:</b>
 *   <b>wordCount:</b>
 * </pre>
 * 
 * @author Jesper Steen Møller
 * @author Derek Hulley
 */
public class OfficeMetadataExtracter extends AbstractMappingMetadataExtracter
{
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_TITLE = "title";
    public static final String KEY_SUBJECT = "subject";
    public static final String KEY_CREATE_DATETIME = "createDateTime";
    public static final String KEY_LAST_SAVE_DATETIME = "lastSaveDateTime";
    public static final String KEY_COMMENTS = "comments";
    public static final String KEY_EDIT_TIME = "editTime";
    public static final String KEY_FORMAT = "format";
    public static final String KEY_KEYWORDS = "keywords";
    public static final String KEY_LAST_AUTHOR = "lastAuthor";
    public static final String KEY_LAST_PRINTED = "lastPrinted";
    public static final String KEY_OS_VERSION = "osVersion";
    public static final String KEY_THUMBNAIL = "thumbnail";
    public static final String KEY_PAGE_COUNT = "pageCount";
    public static final String KEY_WORD_COUNT = "wordCount";
    
    public static String[] SUPPORTED_MIMETYPES = new String[] {
        MimetypeMap.MIMETYPE_WORD,
        MimetypeMap.MIMETYPE_EXCEL,
        MimetypeMap.MIMETYPE_PPT};

    public OfficeMetadataExtracter()
    {
        super(new HashSet<String>(Arrays.asList(SUPPORTED_MIMETYPES)));
    }

    @Override
    protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        final Map<String, Serializable> rawProperties = newRawMap();
        
        POIFSReaderListener readerListener = new POIFSReaderListener()
        {
            public void processPOIFSReaderEvent(final POIFSReaderEvent event)
            {
                try
                {
                    PropertySet ps = PropertySetFactory.create(event.getStream());
                    if (ps instanceof SummaryInformation)
                    {
                        SummaryInformation si = (SummaryInformation) ps;
                        
                        putRawValue(KEY_AUTHOR, si.getAuthor(), rawProperties);
                        putRawValue(KEY_TITLE, si.getTitle(), rawProperties);
                        putRawValue(KEY_SUBJECT, si.getSubject(), rawProperties);
                        putRawValue(KEY_CREATE_DATETIME, si.getCreateDateTime(), rawProperties);
                        putRawValue(KEY_LAST_SAVE_DATETIME, si.getLastSaveDateTime(), rawProperties);
                        putRawValue(KEY_COMMENTS, si.getComments(), rawProperties);
                        putRawValue(KEY_EDIT_TIME, si.getEditTime(), rawProperties);
                        putRawValue(KEY_FORMAT, si.getFormat(), rawProperties);
                        putRawValue(KEY_KEYWORDS, si.getKeywords(), rawProperties);
                        putRawValue(KEY_LAST_AUTHOR, si.getLastAuthor(), rawProperties);
                        putRawValue(KEY_LAST_PRINTED, si.getLastPrinted(), rawProperties);
                        putRawValue(KEY_OS_VERSION, si.getOSVersion(), rawProperties);
                        putRawValue(KEY_THUMBNAIL, si.getThumbnail(), rawProperties);
                        putRawValue(KEY_PAGE_COUNT, si.getPageCount(), rawProperties);
                        putRawValue(KEY_WORD_COUNT, si.getWordCount(), rawProperties);
                    }
                }
                catch (Exception ex)
                {
                    throw new ContentIOException("Property set stream: " + event.getPath() + event.getName(), ex);
                }
            }
        };
        
        InputStream is = null;
        try
        {
            is = reader.getContentInputStream();
            POIFSReader poiFSReader = new POIFSReader();
            poiFSReader.registerListener(readerListener, SummaryInformation.DEFAULT_STREAM_NAME);
            poiFSReader.read(is);
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (IOException e) {}
            }
        }
        return rawProperties;
    }
}
