/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.repo.content.transform;

import org.alfresco.repo.content.JodConverter;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;

/**
 * Makes use of the {@link http://code.google.com/p/jodconverter/} library and an installed
 * OpenOffice application to perform OpenOffice-driven conversions.
 * 
 * @author Neil McErlean
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class JodContentTransformer extends OOoContentTransformerHelper implements ContentTransformerWorker, InitializingBean
{
    private static Log logger = LogFactory.getLog(JodContentTransformer.class);

    private boolean enabled = true;

    private JodConverter jodconverter;

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setJodConverter(JodConverter jodc)
    {
        this.jodconverter = jodc;
    }

    @Override
    protected Log getLogger()
    {
        return logger;
    }
    
    @Override
    protected String getTempFilePrefix()
    {
        return "JodContentTransformer";
    }

    @Override
    public boolean isAvailable()
    {
        if (remoteTransformerClientConfigured() && !remoteTransformerClient.isAvailable())
        {
            afterPropertiesSet();
        }

        return remoteTransformerClientConfigured()
            ? remoteTransformerClient.isAvailable()
            : jodconverter.isAvailable();
    }

    @Override
    public void afterPropertiesSet()
    {
        if (enabled)
        {
            super.afterPropertiesSet();
            if (remoteTransformerClientConfigured())
            {
                Pair<Boolean, String> result = remoteTransformerClient.check(logger);
                Boolean isAvailable = result.getFirst();
                if (isAvailable != null && isAvailable)
                {
                    String versionString = result.getSecond().trim();
                    logger.debug("Using legacy JodCoverter: " + versionString);
                }
                else
                {
                    String message = "Legacy JodConverter is not available for transformations. " + result.getSecond();
                    if (isAvailable == null)
                    {
                        logger.debug(message);
                    }
                    else
                    {
                        logger.error(message);
                    }
                }
            }
        }
    }

    @Override
    protected void convert(File tempFromFile, DocumentFormat sourceFormat, File tempToFile,
            DocumentFormat targetFormat)
    {
        OfficeDocumentConverter converter = new OfficeDocumentConverter(jodconverter.getOfficeManager());
        converter.convert(tempFromFile, tempToFile);
    }
}
