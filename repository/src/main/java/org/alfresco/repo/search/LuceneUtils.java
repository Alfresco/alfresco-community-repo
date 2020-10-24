/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.search;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Lucene utils
 *
 * @author Andy
 *
 */
public class LuceneUtils
{
    /**
     * This is the date string format as required by Lucene e.g. "1970\\-01\\-01T00:00:00"
     * @since 4.0
     */
    private static final SimpleDateFormat LUCENE_DATETIME_FORMAT = new SimpleDateFormat("yyyy\\-MM\\-dd'T'HH:mm:ss");

    /**
     * Returns a date string in the format required by Lucene.
     *
     * @since 4.0
     */
    public static String getLuceneDateString(Date date)
    {
        return LUCENE_DATETIME_FORMAT.format(date);
    }
    /**
     * This method creates a Lucene query fragment which constrains the specified dateProperty to a range
     * given by the fromDate and toDate parameters.
     *
     * @param fromDate     the start of the date range (defaults to 1970-01-01 00:00:00 if null).
     * @param toDate       the end of the date range (defaults to 3000-12-31 00:00:00 if null).
     * @param dateProperty the Alfresco property value to check against the range (must be a valid Date or DateTime property).
     *
     * @return the Lucene query fragment.
     *
     * @throws NullPointerException if dateProperty is null or if the dateProperty is not recognised by the system.
     * @throws IllegalArgumentException if dateProperty refers to a property that is not of type {@link DataTypeDefinition#DATE} or {@link DataTypeDefinition#DATETIME}.
     */
    public static String createDateRangeQuery(Date fromDate, Date toDate, QName dateProperty,
                                              DictionaryService dictionaryService, NamespaceService namespaceService)
    {
        // Some sanity checking of the date property.
        if (dateProperty == null)
        {
            throw new NullPointerException("dateProperty cannot be null");
        }
        PropertyDefinition propDef = dictionaryService.getProperty(dateProperty);
        if (propDef == null)
        {
            throw new NullPointerException("dateProperty '" + dateProperty + "' not recognised.");
        }
        else
        {
            final QName propDefType = propDef.getDataType().getName();
            if ( !DataTypeDefinition.DATE.equals(propDefType) &&
                    !DataTypeDefinition.DATETIME.equals(propDefType))
            {
                throw new IllegalArgumentException("Illegal property type '" + dateProperty + "' [" + propDefType + "]");
            }
        }

        QName propertyName = propDef.getName();
        final String shortFormQName = propertyName.toPrefixString(namespaceService);
        final String prefix = shortFormQName.substring(0, shortFormQName.indexOf(QName.NAMESPACE_PREFIX));
        final String localName = propertyName.getLocalName();


        // I can see potential issues with using 1970 and 3000 as default dates, but this is what the previous
        // JavaScript controllers/libs did and I'll reproduce it here.
        final String ZERO_DATE = "1970\\-01\\-01T00:00:00";
        final String FUTURE_DATE = "3000\\-12\\-31T00:00:00";

        StringBuilder luceneQuery = new StringBuilder();
        luceneQuery.append(" +@").append(prefix).append("\\:").append(localName).append(":[");
        if (fromDate != null)
        {
            luceneQuery.append(LuceneUtils.getLuceneDateString(fromDate));
        }
        else
        {
            luceneQuery.append(ZERO_DATE);
        }
        luceneQuery.append(" TO ");
        if (toDate != null)
        {
            luceneQuery.append(LuceneUtils.getLuceneDateString(toDate));
        }
        else
        {
            luceneQuery.append(FUTURE_DATE);
        }
        luceneQuery.append("] ");
        return luceneQuery.toString();
    }
}