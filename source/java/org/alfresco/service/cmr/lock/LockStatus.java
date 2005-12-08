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
package org.alfresco.service.cmr.lock;

/**
 * Enum used to indicate lock status.
 * 
 * @author Roy Wetherall
 */
public enum LockStatus 
{
    NO_LOCK,        // Indicates that there is no lock present 
    LOCKED,         // Indicates that the node is locked
    LOCK_OWNER,     // Indicates that the node is locked and you have lock ownership rights 
    LOCK_EXPIRED    // Indicates that the lock has expired and the node can be considered to be unlocked
}