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

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.Header;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @deprecated OOTB extractors are being moved to T-Engines.
 *
 * This class originally provided metadata extraction of RFC822 mimetype emails. It will no longer be used for that
 * purpose as that work has been off loaded to a T-Engine via the AsynchronousExtractor. It still exists because the
 * governance services (RM) AMP overrides it to provide alternate property mappings and to filter out some of
 * these properties if the node does not has the "record" or "dod5015record" aspects.<p>
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
public class RFC822MetadataExtracter extends AbstractMappingMetadataExtracter, MetadataExtractorPropertyMappingOverride
{
    private static Log logger = LogFactory.getLog(RFC822MetadataExtracter.class);

    private static String[] SUPPORTED_MIMETYPES = new String[] { MimetypeMap.MIMETYPE_RFC822 };

    public RFC822MetadataExtracter()
    {
        super(new HashSet<>(Arrays.asList(SUPPORTED_MIMETYPES)));
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
}
