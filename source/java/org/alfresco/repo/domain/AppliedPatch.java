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

import java.util.Date;

import org.alfresco.repo.admin.patch.PatchInfo;

/**
 * Interface for persistent patch application information.
 * 
 * @author Derek Hulley
 */
public interface AppliedPatch extends PatchInfo
{
    public void setId(String id);

    public void setDescription(String description);
    
    public void setFixesFromSchema(int version);
    
    public void setFixesToSchema(int version);
    
    public void setTargetSchema(int version);
    
    public void setAppliedToSchema(int version);
    
    public void setAppliedToServer(String server);
    
    public void setAppliedOnDate(Date date);
    
    public void setWasExecuted(boolean executed);
    
    public void setSucceeded(boolean succeeded);
    
    public void setReport(String report);
}
