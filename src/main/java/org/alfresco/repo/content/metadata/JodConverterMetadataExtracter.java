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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter;
import org.alfresco.repo.content.metadata.OpenOfficeMetadataWorker;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.PropertyCheck;

/**
 * @deprecated OOTB extractors are being moved to T-Engines.
 *
 * Extracts values from Open Office documents into the following:
 * <pre>
 *   <b>author:</b>                 --      cm:author
 *   <b>title:</b>                  --      cm:title
 *   <b>description:</b>            --      cm:description
 * </pre>
 * 
 * @author Neil McErlean
 */
@Deprecated
public class JodConverterMetadataExtracter extends AbstractMappingMetadataExtracter implements OpenOfficeMetadataWorker
{
    private OpenOfficeMetadataWorker worker;
    private static final Set<String> typedEmptySet = Collections.emptySet();
    
    public JodConverterMetadataExtracter()
    {
        this(typedEmptySet);
    }
    
    public JodConverterMetadataExtracter(Set<String> supportedMimetypes)
    {
        super(supportedMimetypes);
    }
    
    public void setWorker(OpenOfficeMetadataWorker worker)
    {
        this.worker = worker;
    }
    
    @Override
    public synchronized void init()
    {
        PropertyCheck.mandatory("JodConverterMetadataExtracter", "worker", worker);
        
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
