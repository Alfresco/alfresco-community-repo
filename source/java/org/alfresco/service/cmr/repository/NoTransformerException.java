/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.service.cmr.repository;

import java.text.MessageFormat;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Thrown when a transformation request cannot be honoured due to
 * no transformers being present for the requested transformation.  
 * 
 * @author Derek Hulley
 */
public class NoTransformerException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 3689067335554183222L;

    private static final MessageFormat MSG =
        new MessageFormat("No transformation exists between mimetypes {0} and {1}");

    private String sourceMimetype;
    private String targetMimetype;
    
    /**
     * @param sourceMimetype the attempted source mimetype
     * @param targetMimetype the attempted target mimetype
     */
    public NoTransformerException(String sourceMimetype, String targetMimetype)
    {
        super(MSG.format(new Object[] {sourceMimetype, targetMimetype}));
        this.sourceMimetype = sourceMimetype;
        this.targetMimetype = targetMimetype;
    }

    public String getSourceMimetype()
    {
        return sourceMimetype;
    }
    
    public String getTargetMimetype()
    {
        return targetMimetype;
    }
}
