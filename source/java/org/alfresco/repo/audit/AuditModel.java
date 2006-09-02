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
package org.alfresco.repo.audit;


/**
 * API for querying the audit model
 * 
 * @author Andy Hind
 */
public interface AuditModel extends ApplicationAuditModel, MethodAuditModel
{
    /**
     * Constants for reading the xml model definition.
     */

    /* package */final static String NAME_SPACE = "http://www.alfresco.org/model/audit/1.0";

    /* package */final static String EL_AUDIT = "Audit";

    /* package */final static String EL_RECORD_OPTIONS = "RecordOptions";

    /* package */final static String EL_RECORD_PATH = "recordPath";

    /* package */final static String EL_RECORD_FILTERS = "recordFilters";

    /* package */final static String EL_RECORD_SER_RETURN_VAL = "recordSerializedReturnValue";

    /* package */final static String EL_RECORD_SER_EX = "recordSerializedExceptions";

    /* package */final static String EL_RECORD_SER_ARGS = "recordSerializedMethodArguments";

    /* package */final static String EL_RECORD_SER_PROP_BEFORE = "recordSerializedKeyPropertiesBeforeInvocation";

    /* package */final static String EL_RECORD_SER_PROP_AFTER = "recordSerializedKeyPropertiesAferInvocation";

    /* package */final static String EL_FILTER = "Filter";

    /* package */final static String EL_METHOD = "Method";

    /* package */final static String EL_SERVICE = "Service";

    /* package */final static String EL_APPLICATION = "Application";

    /* package */final static String EL_EXPRESSION = "Expression";

    /* package */final static String EL_PARAMETER_NAME = "ParameterName";

    /* package */final static String AT_MODE = "mode";

    /* package */final static String AT_ENABLED = "enabled";

    /* package */final static String AT_AUDIT_INTERNAL = "auditInternal";

    /* package */final static String AT_NAME = "name";

    /* package */final static String AT_INVERT = "invert";

    /* package */final static String AT_TYPE = "type";
}
