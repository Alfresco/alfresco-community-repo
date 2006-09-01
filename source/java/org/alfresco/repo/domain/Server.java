/*
 * Copyright (C) 2006 Alfresco, Inc.
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

/**
 * Interface for persistent <b>server</b> objects.  These persist
 * details of the servers that have committed transactions to the
 * database, for instance.
 * 
 * @author Derek Hulley
 */
public interface Server
{
    public Long getId();
    
    public String getIpAddress();
    
    public void setIpAddress(String ipAddress);
}
