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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import net.sf.jooreports.openoffice.connection.OpenOfficeConnection;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.TempFileProvider;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.document.XDocumentInfoSupplier;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XComponent;
import com.sun.star.ucb.XFileIdentifierConverter;
import com.sun.star.uno.UnoRuntime;

/**
 * Extracts values from Star Office documents into the following:
 * <pre>
 *   <b>author:</b>                 --      cm:author
 *   <b>title:</b>                  --      cm:title
 *   <b>description:</b>            --      cm:description
 * </pre>
 * 
 * @author Jesper Steen Møller
 */
public class OpenOfficeMetadataExtracter extends AbstractMappingMetadataExtracter
{
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";

    public static String[] SUPPORTED_MIMETYPES = new String[] {
        MimetypeMap.MIMETYPE_STAROFFICE5_WRITER,
        MimetypeMap.MIMETYPE_STAROFFICE5_IMPRESS,
        MimetypeMap.MIMETYPE_OPENOFFICE1_WRITER,
        MimetypeMap.MIMETYPE_OPENOFFICE1_IMPRESS
    };

    private OpenOfficeConnection connection;

    public OpenOfficeMetadataExtracter()
    {
        super(new HashSet<String>(Arrays.asList(SUPPORTED_MIMETYPES)));
    }

    public void setConnection(OpenOfficeConnection connection)
    {
        this.connection = connection;
    }
    
    /**
     * Initialises the bean by establishing an UNO connection
     */
    @Override
    public synchronized void init()
    {
        PropertyCheck.mandatory("OpenOfficeMetadataExtracter", "connection", connection);
        
        // Base initialization
        super.init();
    }

    /**
     * @return Returns true if a connection to the Uno server could be
     *         established
     */
    public boolean isConnected()
    {
        return connection.isConnected();
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

    @Override
    public Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        Map<String, Serializable> rawProperties = newRawMap();

        String sourceMimetype = reader.getMimetype();

        // create temporary files to convert from and to
        File tempFromFile = TempFileProvider.createTempFile(
                "OpenOfficeMetadataExtracter-", "."
                + getMimetypeService().getExtension(sourceMimetype));
        // download the content from the source reader
        reader.getContent(tempFromFile);

        String sourceUrl = toUrl(tempFromFile, connection);

        // UNO Interprocess Bridge *should* be thread-safe, but...
        XComponentLoader desktop = connection.getDesktop();
        XComponent document = desktop.loadComponentFromURL(
                sourceUrl,
                "_blank",
                0,
                new PropertyValue[] { property("Hidden", Boolean.TRUE) });
        if (document == null)
        {
            throw new FileNotFoundException("could not open source document: " + sourceUrl);
        }
        try
        {
            XDocumentInfoSupplier infoSupplier = (XDocumentInfoSupplier) UnoRuntime.queryInterface(
                    XDocumentInfoSupplier.class, document);
            XPropertySet propSet = (XPropertySet) UnoRuntime.queryInterface(
                    XPropertySet.class,
                    infoSupplier
                    .getDocumentInfo());

            putRawValue(KEY_TITLE, propSet.getPropertyValue("Title").toString(), rawProperties);
            putRawValue(KEY_DESCRIPTION, propSet.getPropertyValue("Subject").toString(), rawProperties);
            putRawValue(KEY_AUTHOR, propSet.getPropertyValue("Author").toString(), rawProperties);
        }
        finally
        {
            document.dispose();
        }
        // Done
        return rawProperties;
    }

    public String toUrl(File file, OpenOfficeConnection connection) throws ConnectException
    {
        Object fcp = connection.getFileContentProvider();
        XFileIdentifierConverter fic = (XFileIdentifierConverter) UnoRuntime.queryInterface(
                XFileIdentifierConverter.class, fcp);
        return fic.getFileURLFromSystemPath("", file.getAbsolutePath());
    }

    private static PropertyValue property(String name, Object value)
    {
        PropertyValue property = new PropertyValue();
        property.Name = name;
        property.Value = value;
        return property;
    }
}
