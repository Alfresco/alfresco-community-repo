/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.repo.dictionary;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.lang.StringUtils;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class ValueDataTypeValidatorImpl implements ValueDataTypeValidator
{

    /** Messages */
    private static final String MSG_DATA_TYPE_UNKNOWN = "cmm.value_datatype_validator.unknown_datatype";
    private static final String MSG_INVALID_VALUE = "cmm.value_datatype_validator.invalid_value";
    private static final String MSG_INVALID_DATE = "cmm.value_datatype_validator.invalid_date";
    private static final String MSG_INVALID_DATETIME = "cmm.value_datatype_validator.invalid_datetime";
    private static final String MSG_INVALID_BOOLEAN_VALUE = "cmm.value_datatype_validator.invalid_boolean_value";

    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Checks that all necessary services have been provided.
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
    }

    @Override
    public void validateValue(String dataType, String value)
    {
        ParameterCheck.mandatoryString("dataType", dataType);
        if (StringUtils.isEmpty(value))
        {
            return;
        }

        QName typeQName = QName.createQName(dataType, this.namespaceService);
        DataTypeDefinition typeDef = this.dictionaryService.getDataType(typeQName);
        if (typeDef == null)
        {
            throw new AlfrescoRuntimeException(MSG_DATA_TYPE_UNKNOWN, new Object[] { typeQName.toPrefixString() });
        }

        if (DataTypeDefinition.BOOLEAN.equals(typeQName))
        {
            checkBooleanValue(value);
        }
        else
        {
            try
            {
                DefaultTypeConverter.INSTANCE.convert(typeDef, value);
            }
            catch (Exception ex)
            {
                if (DataTypeDefinition.DATE.equals(typeQName))
                {
                    throw new AlfrescoRuntimeException(MSG_INVALID_DATE, new Object[] { value });
                }
                if (DataTypeDefinition.DATETIME.equals(typeQName))
                {
                    throw new AlfrescoRuntimeException(MSG_INVALID_DATETIME, new Object[] { value });
                }

                throw new AlfrescoRuntimeException(MSG_INVALID_VALUE, new Object[] { value, typeQName.toPrefixString() });
            }
        }
    }

    protected void checkBooleanValue(String value)
    {
        if (!("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)))
        {
            throw new AlfrescoRuntimeException(MSG_INVALID_BOOLEAN_VALUE, new Object[] { value });
        }
    }

}
