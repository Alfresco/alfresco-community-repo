package org.alfresco.repo.content.transform;

import java.io.File;

import net.sf.jooreports.converter.DocumentFormat;
import net.sf.jooreports.openoffice.connection.OpenOfficeConnection;
import net.sf.jooreports.openoffice.converter.AbstractOpenOfficeDocumentConverter;
import net.sf.jooreports.openoffice.converter.OpenOfficeDocumentConverter;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.SocketOpenOfficeConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Makes use of the <a href="http://sourceforge.net/projects/joott/JOOConverter">JOOConverter</a> library to perform
 *  OpenOffice-driven conversions.
 * This requires that OpenOffice be running, but delivers a wider range of transformations
 *  than Tika is able to (Tika just translates into Text, HTML and XML)
 * 
 * @author Derek Hulley
 */
public class OpenOfficeContentTransformerWorker extends OOoContentTransformerHelper implements ContentTransformerWorker, InitializingBean
{
    private static Log logger = LogFactory.getLog(OpenOfficeContentTransformerWorker.class);

    private OpenOfficeConnection connection;
    private AbstractOpenOfficeDocumentConverter converter;

    /**
     * @param connection
     *            the connection that the converter uses
     */
    public void setConnection(OpenOfficeConnection connection)
    {
        this.connection = connection;
    }

    /**
     * Explicitly set the converter to be used. The converter must use the same connection set in
     * {@link #setConnection(OpenOfficeConnection)}.
     * <p>
     * If not set, then the <code>OpenOfficeDocumentConverter</code> will be used.
     * 
     * @param converter
     *            the converter to use.
     */
    public void setConverter(AbstractOpenOfficeDocumentConverter converter)
    {
        this.converter = converter;
    }

    @Override
    protected Log getLogger()
    {
        return logger;
    }
    
    @Override
    protected String getTempFilePrefix()
    {
        return "OpenOfficeContentTransformer";
    }
    
    @Override
    public boolean isAvailable()
    {
        return connection.isConnected();
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory("OpenOfficeContentTransformerWorker", "connection", connection);

        super.afterPropertiesSet();

        // set up the converter
        if (converter == null)
        {
            converter = getDefaultConverter(connection);
        }
    }

    protected AbstractOpenOfficeDocumentConverter getDefaultConverter(OpenOfficeConnection connection)
    {
        return (connection instanceof SocketOpenOfficeConnection)       
           ? ((SocketOpenOfficeConnection)connection).getDefaultConverter()
           : new OpenOfficeDocumentConverter(connection);
    }

    @Override
    protected void convert(File tempFromFile, DocumentFormat sourceFormat, File tempToFile,
            DocumentFormat targetFormat)
    {
        converter.convert(tempFromFile, sourceFormat, tempToFile, targetFormat);
    }
    
    public void saveContentInFile(String sourceMimetype, ContentReader reader, File file) throws ContentIOException
    {
        // jooconverter does not handle non western chars by default in text files.
        // Jodconverter does by setting properties it passes to soffice.
        // The following patched method added to jooconverter, addes these properties.
        converter.setTextUtf8(MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(sourceMimetype));
        
        super.saveContentInFile(sourceMimetype, reader, file);
    }
}
