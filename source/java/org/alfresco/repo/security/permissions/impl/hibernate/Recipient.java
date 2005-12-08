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
package org.alfresco.repo.security.permissions.impl.hibernate;

import java.io.Serializable;
import java.util.Set;

/** 
 * The interface against which recipients of permission are persisted
 * @author andyh
 */
public interface Recipient extends Serializable 
{
    /**
     * Get the recipient.
     * 
     * @return
     */
    public String getRecipient();
    
    /**
     * Set the recipient
     * 
     * @param recipient
     */
    public void setRecipient(String recipient);
    
    /**
     * Get the external keys that map to this recipient.
     * 
     * @return
     */
    public Set<String> getExternalKeys();
}
