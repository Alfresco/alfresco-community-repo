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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.content.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import net.sf.jooreports.openoffice.connection.OpenOfficeConnection;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
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
 * @author Jesper Steen Møller
 */
public class OpenOfficeMetadataExtracter extends AbstractMetadataExtracter
{
    public static String[] SUPPORTED_MIMETYPES = new String[] {
        MimetypeMap.MIMETYPE_STAROFFICE5_WRITER,
        MimetypeMap.MIMETYPE_STAROFFICE5_IMPRESS,
        MimetypeMap.MIMETYPE_OPENOFFICE1_WRITER,
        MimetypeMap.MIMETYPE_OPENOFFICE1_IMPRESS
    // Add the other OpenOffice.org stuff here
    // In fact, other types may apply as well, but should be counted as lower
    // quality since they involve conversion.
    };

    private OpenOfficeConnection connection;

    public OpenOfficeMetadataExtracter()
    {
        super(new HashSet<String>(Arrays.asList(SUPPORTED_MIMETYPES)), 1.00, 10000);
    }

    public void setConnection(OpenOfficeConnection connection)
    {
        this.connection = connection;
    }
    
    private synchronized void connect()
    {
        if (isConnected())
        {
            // just leave it
        }
        else
        {
            try
            {
                connection.connect();
            }
            catch (ConnectException e)
            {
                logger.warn(e.getMessage());
            }
        }
    }

    /**
     * Initialises the bean by establishing an UNO connection
     */
    public synchronized void init()
    {
        PropertyCheck.mandatory("OpenOfficeMetadataExtracter", "connection", connection);

        // attempt a connection
        connect();
        if (isConnected())
        {
            // Only register if the connection is available initially.  Reconnections are only supported
            // if the server is able to connection initially.
            super.register();
        }
    }

    /**
     * @return Returns true if a connection to the Uno server could be
     *         established
     */
    public boolean isConnected()
    {
        return connection.isConnected();
    }

    public void extractInternal(ContentReader reader, final Map<QName, Serializable> destination) throws Throwable
    {
        String sourceMimetype = reader.getMimetype();

        // create temporary files to convert from and to
        File tempFromFile = TempFileProvider.createTempFile(
                "OpenOfficeMetadataExtracter-", "."
                + getMimetypeService().getExtension(sourceMimetype));
        // download the content from the source reader
        reader.getContent(tempFromFile);

        String sourceUrl = toUrl(tempFromFile, connection);

        // UNO Interprocess Bridge *should* be thread-safe, but...
        synchronized (connection)
        {
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

                // Titled aspect
                trimPut(ContentModel.PROP_TITLE, propSet.getPropertyValue("Title"), destination);
                trimPut(ContentModel.PROP_DESCRIPTION, propSet.getPropertyValue("Subject"), destination);

                // Auditable aspect
                // trimPut(ContentModel.PROP_CREATED,
                // si.getCreateDateTime(), destination);
                trimPut(ContentModel.PROP_AUTHOR, propSet.getPropertyValue("Author"), destination);
                // trimPut(ContentModel.PROP_MODIFIED,
                // si.getLastSaveDateTime(), destination);
                // trimPut(ContentModel.PROP_MODIFIER, si.getLastAuthor(),
                // destination);
            }
            finally
            {
                document.dispose();
            }
        }
    }

    public String toUrl(File file, OpenOfficeConnection connection) throws ConnectException
    {
        Object fcp = connection.getFileContentProvider();
        XFileIdentifierConverter fic = (XFileIdentifierConverter) UnoRuntime.queryInterface(
                XFileIdentifierConverter.class, fcp);
        return fic.getFileURLFromSystemPath("", file.getAbsolutePath());
    }

    public double getReliability(String sourceMimetype)
    {
        if (isConnected())
            return super.getReliability(sourceMimetype);
        else
            return 0.0;
    }

    private static PropertyValue property(String name, Object value)
    {
        PropertyValue property = new PropertyValue();
        property.Name = name;
        property.Value = value;
        return property;
    }
}
