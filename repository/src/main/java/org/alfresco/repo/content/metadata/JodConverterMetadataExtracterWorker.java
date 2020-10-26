/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.content.metadata;

import static org.artofsolving.jodconverter.office.OfficeUtils.SERVICE_DESKTOP;
import static org.artofsolving.jodconverter.office.OfficeUtils.cast;
import static org.artofsolving.jodconverter.office.OfficeUtils.toUrl;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.content.JodConverter;
import org.alfresco.repo.content.metadata.OpenOfficeMetadataWorker;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeTask;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.document.XDocumentInfoSupplier;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;
import com.sun.star.util.XRefreshable;

/**
 * @deprecated OOTB extractors are being moved to T-Engines.
 */
@Deprecated
public class JodConverterMetadataExtracterWorker implements
        OpenOfficeMetadataWorker
{
    /** Logger */
    private static Log logger = LogFactory.getLog(JodConverterMetadataExtracterWorker.class);

    private JodConverter jodc;
    private MimetypeService mimetypeService;

    /*
     * @param mimetypeService the mimetype service. Set this if required.
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public void setJodConverter(JodConverter jodc)
    {
        this.jodc = jodc;
    }

    /*
     * @see org.alfresco.repo.content.metadata.OpenOfficeMetadataWorker#extractRaw
     * (org.alfresco.service.cmr.repository. ContentReader)
     */
    public Map<String, Serializable> extractRaw(ContentReader reader)
            throws Throwable
    {
    	String sourceMimetype = reader.getMimetype();
    	
    	if (logger.isDebugEnabled())
    	{
    		StringBuilder msg = new StringBuilder();
    		msg.append("Extracting metadata content from ")
    		    .append(sourceMimetype);
    		logger.debug(msg.toString());
    	}

        // create temporary files to convert from and to
        File tempFile = TempFileProvider.createTempFile(this.getClass()
                .getSimpleName()
                + "-", "." + mimetypeService.getExtension(sourceMimetype));

        // download the content from the source reader
        reader.getContent(tempFile);

        ResultsCallback callback = new ResultsCallback();
        jodc.getOfficeManager().execute(new ExtractMetadataOfficeTask(tempFile, callback));

        return callback.getResults();
    }

    public boolean isConnected()
    {
        // the JodConverter library ensures that the connection is always there.
        // If the extracter is not available then the isAvailable call should ensure that it is not used.
        return true;
    }
}

@Deprecated
class ExtractMetadataOfficeTask implements OfficeTask
{
    /*
     * These keys are used by Alfresco to map properties into a content model and do need to
     * have lower-case initial letters.
     */
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";

    private static Log logger = LogFactory.getLog(ExtractMetadataOfficeTask.class);
    private File inputFile;
    private ResultsCallback callback;

    public ExtractMetadataOfficeTask(File inputFile, ResultsCallback callback)
    {
        this.inputFile = inputFile;
        this.callback = callback;
    }

    public void execute(OfficeContext context)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Extracting metadata from file " + inputFile);
        }

        XComponent document = null;
        try
        {
            if (!inputFile.exists())
            {
                throw new OfficeException("input document not found");
            }
            XComponentLoader loader = cast(XComponentLoader.class, context
                    .getService(SERVICE_DESKTOP));
            
            // Need to set the Hidden property to ensure that OOo GUI does not appear.
            PropertyValue hiddenOOo = new PropertyValue();
            hiddenOOo.Name = "Hidden";
            hiddenOOo.Value = Boolean.TRUE;
            PropertyValue readOnly = new PropertyValue();
            readOnly.Name = "ReadOnly";
            readOnly.Value = Boolean.TRUE;

            try
            {
                document = loader.loadComponentFromURL(toUrl(inputFile), "_blank", 0,
                        new PropertyValue[]{hiddenOOo, readOnly});
            } catch (IllegalArgumentException illegalArgumentException)
            {
                throw new OfficeException("could not load document: "
                        + inputFile.getName(), illegalArgumentException);
            } catch (ErrorCodeIOException errorCodeIOException)
            {
                throw new OfficeException("could not load document: "
                        + inputFile.getName() + "; errorCode: "
                        + errorCodeIOException.ErrCode, errorCodeIOException);
            } catch (IOException ioException)
            {
                throw new OfficeException("could not load document: "
                        + inputFile.getName(), ioException);
            }
            if (document == null)
            {
                throw new OfficeException("could not load document: "
                        + inputFile.getName());
            }
            XRefreshable refreshable = cast(XRefreshable.class, document);
            if (refreshable != null)
            {
                refreshable.refresh();
            }

            XDocumentInfoSupplier docInfoSupplier = cast(XDocumentInfoSupplier.class, document);
            XPropertySet propSet = cast(XPropertySet.class, docInfoSupplier.getDocumentInfo());

            // The strings below are property names as used by OOo. They need upper-case
            // initial letters.
            Object author = getPropertyValueIfAvailable(propSet, "Author");
            Object description = getPropertyValueIfAvailable(propSet, "Subject");
            Object title = getPropertyValueIfAvailable(propSet, "Title");
            
            Map<String, Serializable> results = new HashMap<String, Serializable>(3);
            results.put(KEY_AUTHOR, author == null ? null : author.toString());
            results.put(KEY_DESCRIPTION, description == null ? null : description.toString());
            results.put(KEY_TITLE, title == null ? null : title.toString());
            callback.setResults(results);
        } catch (OfficeException officeException)
        {
            throw officeException;
        } catch (Exception exception)
        {
            throw new OfficeException("conversion failed", exception);
        } finally
        {
            if (document != null)
            {
                XCloseable closeable = cast(XCloseable.class, document);
                if (closeable != null)
                {
                    try
                    {
                        closeable.close(true);
                    } catch (CloseVetoException closeVetoException)
                    {
                        // whoever raised the veto should close the document
                    }
                } else
                {
                    document.dispose();
                }
            }
        }
    }

    /**
     * OOo throws exceptions if we ask for properties that aren't there, so we'll tread carefully.
     * 
     * @param propSet
     * @param propertyName property name as used by the OOo API.
     * @return the propertyValue if it's there, else null.
     * @throws UnknownPropertyException
     * @throws WrappedTargetException
     */
    private Object getPropertyValueIfAvailable(XPropertySet propSet, String propertyName)
            throws UnknownPropertyException, WrappedTargetException
    {
        if (propSet.getPropertySetInfo().hasPropertyByName(propertyName))
        {
            return propSet.getPropertyValue(propertyName);
        }
        else
        {
            return null;
        }
    }
}

@Deprecated
class ResultsCallback
{
    private Map<String, Serializable> results = new HashMap<String, Serializable>();

    public Map<String, Serializable> getResults()
    {
        return results;
    }

    public void setResults(Map<String, Serializable> results)
    {
        this.results = results;
    }
}
