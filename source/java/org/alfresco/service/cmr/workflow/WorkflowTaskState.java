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
package org.alfresco.service.cmr.workflow;


/**
 * Workflow Task State
 * 
 * Represents the high-level state of Workflow Task (in relation to "in-flight" 
 * workflow instance).
 *
 * A user-defined task state may be represented as Task Property (and described
 * by the Alfresco Data Dictionary).
 * 
 * @author davidc
 */
public enum WorkflowTaskState
{
    IN_PROGRESS,
    COMPLETED;
}
