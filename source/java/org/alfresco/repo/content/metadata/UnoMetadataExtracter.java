/*
 * Copyright (C) 2005 Jesper Steen Møller
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.content.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import net.sf.joott.uno.UnoConnection;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.document.XDocumentInfoSupplier;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XComponent;
import com.sun.star.ucb.XFileIdentifierConverter;
import com.sun.star.uno.UnoRuntime;

/**
 * 
 * @author Jesper Steen Møller
 */
public class UnoMetadataExtracter extends AbstractMetadataExtracter
{

    private static final Log logger = LogFactory.getLog(UnoMetadataExtracter.class);

    private static String[] mimeTypes = new String[] {
        MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT,
        MimetypeMap.MIMETYPE_OPENOFFICE_WRITER,
    // Add the other OpenOffice.org stuff here
    // In fact, other types may apply as well, but should be counted as lower
    // quality since they involve conversion.
    };

    public UnoMetadataExtracter(MimetypeMap mimetypeMap, String connectionUrl)
    {
        super(new HashSet<String>(Arrays.asList(mimeTypes)), 1.00, 10000);
        this.mimetypeMap = mimetypeMap;
        init(connectionUrl);
    }

    public UnoMetadataExtracter(MimetypeMap mimetypeMap)
    {
        this(mimetypeMap, UnoConnection.DEFAULT_CONNECTION_STRING);
    }

    private MimetypeMap mimetypeMap;
    private MyUnoConnection connection;
    private boolean isConnected;

    /**
     * @param unoConnectionUrl the URL of the Uno server
     */
    private synchronized void init(String unoConnectionUrl)
    {
        connection = new MyUnoConnection(unoConnectionUrl);
        // attempt to make an connection
        try
        {
            connection.connect();
            isConnected = true;
        }
        catch (ConnectException e)
        {
            isConnected = false;
        }
    }

    /**
     * @return Returns true if a connection to the Uno server could be
     *         established
     */
    public boolean isConnected()
    {
        return isConnected;
    }

    public void extract(ContentReader reader, final Map<QName, Serializable> destination) throws ContentIOException
    {
        String sourceMimetype = reader.getMimetype();

        // create temporary files to convert from and to
        File tempFromFile = TempFileProvider.createTempFile("UnoContentTransformer", "."
                + mimetypeMap.getExtension(sourceMimetype));
        // download the content from the source reader
        reader.getContent(tempFromFile);
        String sourceUrl = tempFromFile.toString();
        try
        {
            sourceUrl = toUrl(tempFromFile, connection);

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
                    trimPut(ContentModel.PROP_CREATOR, propSet.getPropertyValue("Author"), destination);
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
        catch (Throwable e)
        {
            throw new ContentIOException("Conversion failed: \n" +
                    "   source: " + sourceUrl + "\n",
                    e);
        }
    }

    public String toUrl(File file, MyUnoConnection connection) throws ConnectException
    {
        Object fcp = connection.getFileContentService();
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

    static class MyUnoConnection extends UnoConnection
    {
        public MyUnoConnection(String url)
        {
            super(url);
        }

        public Object getFileContentService() throws ConnectException
        {
            return getService("com.sun.star.ucb.FileContentProvider");
        }
    }
}
