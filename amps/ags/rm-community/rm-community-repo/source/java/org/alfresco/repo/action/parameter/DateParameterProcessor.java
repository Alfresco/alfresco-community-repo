/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.action.parameter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;

/**
 * Date parameter processor.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class DateParameterProcessor extends ParameterProcessor implements ParameterSubstitutionSuggester
{
    private static final String DAY = "day";
    private static final String WEEK = "week";
    private static final String MONTH = "month";
    private static final String YEAR = "year";
    private static final String SHORT = "short";
    private static final String LONG = "long";
    private static final String NUMBER = "number";

    private static final String SEP = ".";

    private static final String[] ALL_FIELDS_FOR_SUBSTITUTION_QUERY = {
        DAY + SEP + SHORT,
        DAY,
        DAY + SEP + LONG,
        DAY + SEP + NUMBER,
        DAY + SEP + MONTH,
        DAY + SEP + YEAR,
        MONTH + SEP + SHORT,
        MONTH,
        MONTH + SEP + LONG,
        MONTH + SEP + NUMBER,
        YEAR + SEP + SHORT,
        YEAR,
        YEAR + SEP + LONG,
        YEAR + SEP + WEEK
    };

    private int maximumNumberSuggestions = DEFAULT_MAXIMUM_NUMBER_SUGGESTIONS;

    /**
     * @see org.alfresco.repo.action.parameter.ParameterProcessor#process(java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public String process(String value, NodeRef actionedUponNodeRef)
    {
        // the default position is to return the value un-changed
        String result = value;

        // strip the processor name from the value
        value = stripName(value);

        if (!value.isEmpty())
        {
            String[] values = value.split("\\.", 2);
            String field = values[0].trim();

            if (DAY.equalsIgnoreCase(field))
            {
                result = handleDay(values);
            }
            else if (MONTH.equalsIgnoreCase(field))
            {
                result = handleMonth(values);
            }
            else if (YEAR.equalsIgnoreCase(field))
            {
                result = handleYear(values);
            }
            else
            {
                throw new AlfrescoRuntimeException("Cannot process the field '" + field + "'.");
            }
        }

        return result;
    }

    private String handleDay(String[] values)
    {
        String style = getStyle(values);
        String pattern;

        if (SHORT.equalsIgnoreCase(style))
        {
            pattern = "EE";
        }
        else if (LONG.equalsIgnoreCase(style))
        {
            pattern = "EEEE";
        }
        else if (NUMBER.equalsIgnoreCase(style))
        {
            pattern = "uu";
        }
        else if (MONTH.equalsIgnoreCase(style))
        {
            pattern = "dd";
        }
        else if (YEAR.equalsIgnoreCase(style))
        {
            pattern = "DDD";
        }
        else
        {
            throw new AlfrescoRuntimeException("The pattern 'date.day." + style + "' is not supported!");
        }

        return new SimpleDateFormat(pattern).format(new Date());
    }

    private String handleMonth(String[] values)
    {
        String style = getStyle(values);
        String pattern;

        if (SHORT.equalsIgnoreCase(style))
        {
            pattern = "MMM";
        }
        else if (LONG.equalsIgnoreCase(style))
        {
            pattern = "MMMM";
        }
        else if (NUMBER.equalsIgnoreCase(style))
        {
            pattern = "MM";
        }
        else
        {
            throw new AlfrescoRuntimeException("The pattern 'date.month." + style + "' is not supported!");
        }

        return new SimpleDateFormat(pattern).format(new Date());
    }

    private String handleYear(String[] values)
    {
        String style = getStyle(values);
        String pattern;

        if (SHORT.equalsIgnoreCase(style))
        {
            pattern = "yy";
        }
        else if (LONG.equalsIgnoreCase(style))
        {
            pattern = "yyyy";
        }
        else if (WEEK.equalsIgnoreCase(style))
        {
            pattern = "ww";
        }
        else
        {
            throw new AlfrescoRuntimeException("The pattern 'date.year." + style + "' is not supported!");
        }

        return new SimpleDateFormat(pattern).format(new Date());
    }

    private String getStyle(String[] values)
    {
        String style;

        if (values.length == 1)
        {
            style = SHORT;
        }
        else
        {
            style = values[1].trim();
        }

        return style;
    }

    /**
     * Set the maxmimum number of suggestions returned  from the global property
     *
     * @param maximumNumberSuggestions
     */
    public void setMaximumNumberSuggestions(int maximumNumberSuggestions)
    {
        this.maximumNumberSuggestions = (maximumNumberSuggestions <= 0 ? DEFAULT_MAXIMUM_NUMBER_SUGGESTIONS: maximumNumberSuggestions);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.action.parameter.ParameterSubstitutionSuggester#getSubstitutionSuggestions(java.lang.String)
     */
    @Override
    public List<String> getSubstitutionSuggestions(String substitutionFragment)
    {
        List<String> suggestions = new ArrayList<>();
        String namePrefix = this.getName() + ".";
        if(StringUtils.isBlank(substitutionFragment)  || this.getName().toLowerCase().contains(substitutionFragment.toLowerCase()))
        {
            for(String field: ALL_FIELDS_FOR_SUBSTITUTION_QUERY) 
            {
                suggestions.add(namePrefix + field);
                if(suggestions.size() >= maximumNumberSuggestions)
                {
                    break;
                }
            }
        }
        else
        {
            for(String field: ALL_FIELDS_FOR_SUBSTITUTION_QUERY) 
            {
                String prefixFieldName = namePrefix + field;
                if(prefixFieldName.toLowerCase().contains(substitutionFragment.toLowerCase()))
                {
                    suggestions.add(namePrefix + field);
                    if(suggestions.size() >= maximumNumberSuggestions)
                    {
                        break;
                    }
                }
            }
        }
        return suggestions;
    }
}
