/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
    public static final String EL_PRIMARY_KEY = PrimaryKey.class.getSimpleName().toLowerCase();
    public static final String EL_FOREIGN_KEY = ForeignKey.class.getSimpleName().toLowerCase();
    public static final String EL_INDEX = Index.class.getSimpleName().toLowerCase();
    public static final String EL_SEQUENCE = Sequence.class.getSimpleName().toLowerCase();    
    public static final String EL_TYPE = "type";
    public static final String EL_NULLABLE = "nullable";
    public static final String EL_COLUMN_NAME = "columnname";
    public static final String EL_COLUMN_NAMES = "columnnames";    
    public static final String EL_LOCAL_COLUMN = "localcolumn";
    public static final String EL_TARGET_COLUMN = "targetcolumn";
    public static final String EL_TARGET_TABLE = "targettable";
    
    public static final String ATTR_NAME = "name";
}
