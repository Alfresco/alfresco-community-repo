/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.mail.Header;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @deprecated OOTB extractors have being moved to T-Engines.
 *
 * This class originally provided metadata extraction of RFC822 mimetype emails. It will no longer be used for that
 * purpose as that work has been off loaded to a T-Engine via the AsynchronousExtractor. It still exists because the
 * governance services (RM) AMP overrides it to provide alternate property mappings and to filter out some of
 * these properties if the node does not have the "record" or "dod5015record" aspects.<p>
 *
 * We still also have the Default configuration file (RFC822MetadataExtracter.properties) file which contains the
 * default set of properties, which may be manipulated by RM.
 *
 * <pre>
 *   <b>messageFrom:</b>              --      imap:messageFrom, cm:originator
 *   <b>messageTo:</b>                --      imap:messageTo
 *   <b>messageCc:</b>                --      imap:messageCc
 *   <b>messageSubject:</b>           --      imap:messageSubject, cm:title, cm:description, cm:subjectline
 *   <b>messageSent:</b>              --      imap:dateSent, cm:sentdate
 *   <b>messageReceived:</b>          --      imap:dateReceived
 *   <b>All {@link Header#getName() header names}:</b>
 *      <b>Thread-Index:</b>          --      imap:threadIndex
 *      <b>Message-ID:</b>            --      imap:messageId
 * </pre>
 *
 * This class now provides an alternative property mapping in the request to the T-Engine. Unlike the previous
 * implementation the filtering of properties takes place before rather than after the extraction. This is done in
 * this class making the code within the org.alfresco.module.org_alfresco_module_rm.email.RFC822MetadataExtracter
 * filterSystemProperties method redundant.
 *
 * @author adavis
 */
@Deprecated
public class RFC822MetadataExtracter extends AbstractMappingMetadataExtracter
        implements MetadataExtractorPropertyMappingOverride
{
    static String RM_URI = "http://www.alfresco.org/model/recordsmanagement/1.0";
    static String DOD_URI = "http://www.alfresco.org/model/dod5015/1.0";

    static final String RECORD = "record";
    static final String DOD_5015_RECORD = "dod5015record";

    static final QName ASPECT_RECORD = QName.createQName(RM_URI, RECORD);
    static final QName ASPECT_DOD_5015_RECORD = QName.createQName(DOD_URI, DOD_5015_RECORD);

    private static Log logger = LogFactory.getLog(RFC822MetadataExtracter.class);

    private static final HashSet<String> SUPPORTED_MIMETYPES =
            new HashSet<>(Arrays.asList(new String[] { MimetypeMap.MIMETYPE_RFC822 }));

    public RFC822MetadataExtracter()
    {
        super(SUPPORTED_MIMETYPES);
    }

    private NodeService nodeService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        logger.error("RFC822MetadataExtracter.extractRaw should not have been called, " +
                "as the extraction should have taken place in a T-Engine.");
        return Collections.emptyMap(); // will result in no updates.
    }

    /**
     * Back door for RM
     * @return Map
     */
    public final Map<String, Set<QName>> getCurrentMapping()
    {
        return super.getMapping();
    }

    @Override
    public boolean match(String sourceMimetype)
    {
        // When RM overrides the "extracter.RFC822" bean with its own class 'this' will be a sub class.
        return SUPPORTED_MIMETYPES.contains(sourceMimetype) && this.getClass() != RFC822MetadataExtracter.class;
    }

    @Override
    // Only include system properties depending on RM / DOD aspects on this nodeRef
    public Map<String, Set<String>> getExtractMapping(NodeRef nodeRef)
    {
        Map<String, Set<QName>> customMapping = getMapping();
        HashMap<String, Set<String>> mapping = new HashMap<>(customMapping.size());

        boolean isARecord = nodeService.hasAspect(nodeRef, ASPECT_RECORD);
        boolean isADodRecord = nodeService.hasAspect(nodeRef, ASPECT_DOD_5015_RECORD);

        for (Map.Entry<String, Set<QName>> entry : customMapping.entrySet())
        {
            Set<QName> customSystemProperties = entry.getValue();
            HashSet<String> systemProperties = new HashSet<>(customSystemProperties.size());
            String documentProperty = entry.getKey();

            for (QName customSystemProperty : customSystemProperties)
            {
                String uri = customSystemProperty.getNamespaceURI();
                boolean rmProperty = RM_URI.equals(uri);
                boolean dodProperty = DOD_URI.equals(uri);
                if ((rmProperty && isARecord) || (dodProperty && isADodRecord) || (!rmProperty && !dodProperty))
                {
                    systemProperties.add(customSystemProperty.toString());
                }
            }
            if (!systemProperties.isEmpty())
            {
                mapping.put(documentProperty, systemProperties);
            }
        }

        return mapping;
    }
}
