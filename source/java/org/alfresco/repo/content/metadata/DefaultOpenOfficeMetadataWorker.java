/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import net.sf.jooreports.openoffice.connection.OpenOfficeConnection;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.util.TempFileProvider;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.document.XDocumentInfoSupplier;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XComponent;
import com.sun.star.ucb.XFileIdentifierConverter;
import com.sun.star.uno.UnoRuntime;

/**
 * The class doing the actual work of the OpenOfficeMetadataExtracter, based around an OpenOffice connection.
 * 
 * @author dward
 */
public class DefaultOpenOfficeMetadataWorker implements OpenOfficeMetadataWorker
{
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";

    private OpenOfficeConnection connection;
    private MimetypeService mimetypeService;

    public void setConnection(OpenOfficeConnection connection)
    {
        this.connection = connection;
    }

    /*
     * @param mimetypeService the mimetype service. Set this if required.
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    /**
     * @return Returns true if a connection to the Uno server could be established
     */
    public boolean isConnected()
    {
        return connection.isConnected();
    }

    /*
     * (non-Javadoc)
     * @seeorg.alfresco.repo.content.metadata.OpenOfficeMetadataWorker#extractRaw(org.alfresco.service.cmr.repository.
     * ContentReader)
     */
    public Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        Map<String, Serializable> rawProperties = new HashMap<String, Serializable>(17);

        String sourceMimetype = reader.getMimetype();

        // create temporary files to convert from and to
        File tempFromFile = TempFileProvider.createTempFile("OpenOfficeMetadataExtracter-", "."
                + this.mimetypeService.getExtension(sourceMimetype));

        // download the content from the source reader
        reader.getContent(tempFromFile);

        String sourceUrl = toUrl(tempFromFile, connection);

        // UNO Interprocess Bridge *should* be thread-safe, but...
        XComponentLoader desktop = connection.getDesktop();
        XComponent document = desktop.loadComponentFromURL(sourceUrl, "_blank", 0, new PropertyValue[]
        {
            property("Hidden", Boolean.TRUE)
        });
        if (document == null)
        {
            throw new FileNotFoundException("could not open source document: " + sourceUrl);
        }
        try
        {
            XDocumentInfoSupplier infoSupplier = (XDocumentInfoSupplier) UnoRuntime.queryInterface(
                    XDocumentInfoSupplier.class, document);
            XPropertySet propSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, infoSupplier
                    .getDocumentInfo());

            rawProperties.put(KEY_TITLE, propSet.getPropertyValue("Title").toString());
            rawProperties.put(KEY_DESCRIPTION, propSet.getPropertyValue("Subject").toString());
            rawProperties.put(KEY_AUTHOR, propSet.getPropertyValue("Author").toString());
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
