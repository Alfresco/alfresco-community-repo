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
package org.alfresco.service.cmr.module;

/**
 * Enum used to indicate the install state of a module
 * 
 * @author Roy Wetherall
 */
public enum ModuleInstallState
{
    INSTALLED,      // indicates that a module is installed
    DISABLED,       // indicates that a module is installed but it functionality is disabled
    UNINSTALLED     // indicates that a module is uninstalled
}
