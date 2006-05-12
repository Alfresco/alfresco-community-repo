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
package org.alfresco.repo.importer.system;

import java.util.Date;

/**
 * Data holder of patch information that's to be exported and imported
 *
 * @author davidc
 */
public class PatchInfo
{
    public String id = null;
    public String description = null;
    public Integer fixesFromSchema = null;
    public Integer fixesToSchema = null;
    public Integer targetSchema = null;
    public Integer appliedToSchema = null;
    public String appliedToServer = null;
    public Date appliedOnDate = null;
    public Boolean wasExecuted = null;
    public Boolean succeeded = null;
    public String report = null;
}
