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

package org.alfresco.module.org_alfresco_module_rm.caveat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * RM Constraint implementation that ensures the value is one of a constrained
 * <i>list of values</i>.  By default, this constraint is case-sensitive.
 *
 * @see #setAllowedValues(List)
 * @see #setCaseSensitive(boolean)
 *
 * @author janv
 */
public class RMListOfValuesConstraint extends ListOfValuesConstraint
{
    private static final String LOV_CONSTRAINT_VALUE = "listconstraint";
    private List<String> allowedValues;
    private List<String> allowedValuesUpper;
    // defined match logic used by caveat matching (default = "AND")
    private MatchLogic matchLogic = MatchLogic.AND;

    public enum MatchLogic
    {
        // closed marking - all values must match
        AND,
        // open marking   - at least one value must match
        OR
    }

    // note: alternative to static init could be to use 'registered' constraint
    private static RMCaveatConfigService caveatConfigService;

    public void setCaveatConfigService(RMCaveatConfigService caveatConfigService)
    {
        RMListOfValuesConstraint.caveatConfigService = caveatConfigService;
    }


    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("RMListOfValuesConstraint")
          .append("[allowedValues=").append(getAllowedValues())
          .append(", caseSensitive=").append(isCaseSensitive())
          .append(", sorted=").append(isSorted())
          .append(", matchLogic=").append(getMatchLogic())
          .append("]");
        return sb.toString();
    }

    public RMListOfValuesConstraint()
    {
        super();

        // Set RM list of value constraints to be sorted by default
        sorted = true;
    }

    /**
     * Get the allowed values.  Note that these are <tt>String</tt> instances, but may
     * represent non-<tt>String</tt> values.  It is up to the caller to distinguish.
     *
     * @return Returns the values allowed
     */
    @Override
    public List<String> getRawAllowedValues()
    {
        String runAsUser = AuthenticationUtil.getRunAsUser();
        if ((runAsUser != null) && (! runAsUser.equals(AuthenticationUtil.getSystemUserName())) && (caveatConfigService != null))
        {
            // get allowed values for current user
            List<String> allowedForUser = caveatConfigService.getRMAllowedValues(getShortName());

            List<String> filteredList = new ArrayList<>(allowedForUser.size());
            for (String allowed : allowedForUser)
            {
                if (this.allowedValues.contains(allowed))
                {
                    filteredList.add(allowed);
                }
            }

            return filteredList;
        }
        else
        {
            return this.allowedValues;
        }
    }

    public String getDisplayLabel(String constraintAllowableValue, MessageLookup messageLookup)
    {
        if (!this.allowedValues.contains(constraintAllowableValue))
        {
            return null;
        }

        String key = LOV_CONSTRAINT_VALUE;
        key += "." + this.getShortName();
        key += "." + constraintAllowableValue;
        key = StringUtils.replace(key, ":", "_");

        String message = messageLookup.getMessage(key, I18NUtil.getLocale());
        return message == null ? constraintAllowableValue : message;
    }

    private List<String> getAllowedValuesUpper()
    {
        String runAsUser = AuthenticationUtil.getRunAsUser();
        if ((runAsUser != null) && (! runAsUser.equals(AuthenticationUtil.getSystemUserName())) && (caveatConfigService != null))
        {
            // get allowed values for current user
            List<String> allowedForUser = caveatConfigService.getRMAllowedValues(getType());

            List<String> filteredList = new ArrayList<>(allowedForUser.size());
            for (String allowed : allowedForUser)
            {
                if (this.allowedValuesUpper.contains(allowed.toUpperCase()))
                {
                    filteredList.add(allowed);
                }
            }

            return filteredList;
        }
        else
        {
            return this.allowedValuesUpper;
        }
    }
    /**
     * Set the values that are allowed by the constraint.
     *
     * @param allowedValues a list of allowed values
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void setAllowedValues(List allowedValues)
    {
        if (allowedValues == null)
        {
            allowedValues = new ArrayList<String>(0);
        }
        int valueCount = allowedValues.size();
        this.allowedValues = Collections.unmodifiableList(allowedValues);

        // make the upper case versions
        this.allowedValuesUpper = new ArrayList<>(valueCount);
        for (String allowedValue : this.allowedValues)
        {
            allowedValuesUpper.add(allowedValue.toUpperCase());
        }
    }

    @Override
    public void initialize()
    {
        checkPropertyNotNull("allowedValues", allowedValues);
    }

    @Override
    public Map<String, Object> getParameters()
    {
        Map<String, Object> params = new HashMap<>(2);

        params.put("caseSensitive", isCaseSensitive());
        params.put("allowedValues", getAllowedValues());
        params.put("sorted", isSorted());
        params.put("matchLogic", getMatchLogic());

        return params;
    }

    public MatchLogic getMatchLogicEnum()
    {
        return matchLogic;
    }

    public String getMatchLogic()
    {
        return matchLogic.toString();
    }

    public void setMatchLogic(String matchLogicStr)
    {
        this.matchLogic = MatchLogic.valueOf(matchLogicStr);
    }

    @Override
    protected void evaluateSingleValue(Object value)
    {
        // convert the value to a String
        String valueStr = null;
        try
        {
            valueStr = DefaultTypeConverter.INSTANCE.convert(String.class, value);
        }
        catch (TypeConversionException e)
        {
            throw new ConstraintException(RMConstraintMessageKeys.ERR_NON_STRING, value, e);
        }
        // check that the value is in the set of allowed values
        if (isCaseSensitive())
        {
            if (!getAllowedValues().contains(valueStr))
            {
                throw new ConstraintException(RMConstraintMessageKeys.ERR_INVALID_VALUE, value);
            }
        }
        else
        {
            if (!getAllowedValuesUpper().contains(valueStr.toUpperCase()))
            {
                throw new ConstraintException(RMConstraintMessageKeys.ERR_INVALID_VALUE, value);
            }
        }
    }
}
