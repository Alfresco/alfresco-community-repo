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
package org.alfresco.service.cmr.dictionary;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Base Exception of Data Dictionary Exceptions.
 * 
 * @author David Caruana
 */
public class DictionaryException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 3257008761007847733L;

    public DictionaryException(String msgId)
    {
       super(msgId);
    }
    
    public DictionaryException(String msgId, Throwable cause)
    {
       super(msgId, cause);
    }

    public DictionaryException(String msgId, Object ... args)
    {
        super(msgId, args);
    }

    public DictionaryException(String msgId, Throwable cause, Object ... args)
    {
        super(msgId, args, cause);
    }
}
