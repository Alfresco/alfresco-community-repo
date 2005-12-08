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
package org.alfresco.repo.node.integrity;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Thrown when an integrity check fails
 * 
 * @author Derek Hulley
 */
public class IntegrityException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -5036557255854195669L;

    private List<IntegrityRecord> records;
    
    public IntegrityException(List<IntegrityRecord> records)
    {
        super("Integrity failure");
        this.records = records;
    }

    /**
     * @return Returns a list of all the integrity violations
     */
    public List<IntegrityRecord> getRecords()
    {
        return records;
    }
}
