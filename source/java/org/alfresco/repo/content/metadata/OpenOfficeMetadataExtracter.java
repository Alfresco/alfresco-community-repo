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
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.springframework.extensions.surf.util.PropertyCheck;

/**
 * Extracts values from Star Office documents into the following:
 * <pre>
 *   <b>author:</b>                 --      cm:author
 *   <b>title:</b>                  --      cm:title
 *   <b>description:</b>            --      cm:description
 * </pre>
 * 
 * TIKA Note - this probably won't be ported to TIKA. There's currently
 *  no support for these old formats in tika.
 * 
 * @author Jesper Steen Møller
 */
public class OpenOfficeMetadataExtracter extends AbstractMappingMetadataExtracter implements OpenOfficeMetadataWorker
{
    public static String[] SUPPORTED_MIMETYPES = new String[] {
        MimetypeMap.MIMETYPE_STAROFFICE5_WRITER,
        MimetypeMap.MIMETYPE_STAROFFICE5_IMPRESS,
        MimetypeMap.MIMETYPE_OPENOFFICE1_WRITER,
        MimetypeMap.MIMETYPE_OPENOFFICE1_IMPRESS,
        MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING,
        MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET,
        MimetypeMap.MIMETYPE_OPENXML_PRESENTATION
    };

    private OpenOfficeMetadataWorker worker;

    public OpenOfficeMetadataExtracter()
    {
        super(new HashSet<String>(Arrays.asList(SUPPORTED_MIMETYPES)));
    }

    public void setWorker(OpenOfficeMetadataWorker worker)
    {
        this.worker = worker;
    }
    
    /**
     * Initialises the bean by establishing an UNO connection
     */
    @Override
    public synchronized void init()
    {
        PropertyCheck.mandatory("OpenOfficeMetadataExtracter", "worker", worker);
        
        // Base initialization
        super.init();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConnected()
    {
        return worker.isConnected();
    }

    /**
     * Perform the default check, but also check if the OpenOffice connection is good.
     */
    @Override
    public boolean isSupported(String sourceMimetype)
    {
        if (!isConnected())
        {
            return false;
        }
        return super.isSupported(sourceMimetype);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        Map<String, Serializable> rawProperties = newRawMap();
        Map<String, Serializable> result = this.worker.extractRaw(reader);
        for (Map.Entry<String, Serializable> entry : result.entrySet())
        {
            putRawValue(entry.getKey(), entry.getValue(), rawProperties);
        }
        return rawProperties;
    }
}
