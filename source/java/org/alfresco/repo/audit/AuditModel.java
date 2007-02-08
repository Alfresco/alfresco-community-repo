/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
