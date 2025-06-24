/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.util.schemacomp;

import org.alfresco.util.schemacomp.model.*;

/**
 * Constants relating to the XML representation of the schema data model.
 * 
 * @author Matt Ward
 */
public abstract class XML
{
    public static final String EL_SCHEMA = Schema.class.getSimpleName().toLowerCase();
    public static final String EL_TABLE = Table.class.getSimpleName().toLowerCase();
    public static final String EL_COLUMN = Column.class.getSimpleName().toLowerCase();
    public static final String EL_COLUMNS = "columns";
    public static final String EL_PRIMARY_KEY = PrimaryKey.class.getSimpleName().toLowerCase();
    public static final String EL_FOREIGN_KEY = ForeignKey.class.getSimpleName().toLowerCase();
    public static final String EL_FOREIGN_KEYS = "foreignkeys";
    public static final String EL_INDEX = Index.class.getSimpleName().toLowerCase();
    public static final String EL_INDEXES = "indexes";
    public static final String EL_SEQUENCE = Sequence.class.getSimpleName().toLowerCase();
    public static final String EL_TYPE = "type";
    public static final String EL_NULLABLE = "nullable";
    public static final String EL_AUTOINCREMENT = "autoincrement";
    public static final String EL_COLUMN_NAME = "columnname";
    public static final String EL_COLUMN_NAMES = "columnnames";
    public static final String EL_LOCAL_COLUMN = "localcolumn";
    public static final String EL_TARGET_COLUMN = "targetcolumn";
    public static final String EL_TARGET_TABLE = "targettable";
    public static final String EL_VALIDATORS = "validators";
    public static final String EL_VALIDATOR = "validator";
    public static final String EL_OBJECTS = "objects";
    public static final String EL_PROPERTIES = "properties";
    public static final String EL_PROPERTY = "property";

    public static final String ATTR_NAME = "name";
    public static final String ATTR_ORDER = "order";
    public static final String ATTR_UNIQUE = "unique";
    public static final String ATTR_CLASS = "class";
    public static final String ATTR_DB_PREFIX = "dbprefix";
    public static final String ATTR_VERSION = "version";
    public static final String ATTR_TABLE_COLUMN_ORDER = "tablecolumnorder";
}
