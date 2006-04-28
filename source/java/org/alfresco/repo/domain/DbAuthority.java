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
package org.alfresco.repo.domain;

import java.io.Serializable;
import java.util.Set;

/** 
 * The interface against which recipients of permission are persisted
 * @author andyh
 */
public interface DbAuthority extends Serializable 
{
    /**
     * @return Returns the recipient
     */
    public String getRecipient();
    
    /**
     * @param recipient the authority recipient
     */
    public void setRecipient(String recipient);
    
    /**
     * @return Returns the external keys associated with this authority
     */
    public Set<String> getExternalKeys();
    
    /**
     * Delete the access control entries related to this authority
     * 
     * @return Returns the number of entries deleted
     */
    public int deleteEntries();
}
