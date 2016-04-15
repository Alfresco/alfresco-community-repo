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
package org.alfresco.repo.forms.processor.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.dictionary.types.period.Cron;
import org.alfresco.repo.dictionary.types.period.XMLDuration;
import org.alfresco.repo.forms.DataTypeParameters;
import org.alfresco.service.cmr.repository.PeriodProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents the parameters for the period data type.
 *
 * @author Gavin Cornwell
 */
public class PeriodDataTypeParameters implements DataTypeParameters, Serializable
{
    private static final long serialVersionUID = -3654041242831123509L;
    
    protected List<PeriodProvider> providers;
    
    /**
     * Default constructor
     */
    public PeriodDataTypeParameters()
    {
        this.providers = new ArrayList<PeriodProvider>(8);
    }
    
    /**
     * Adds a PeriodProvider
     * 
     * @param pp The PeriodProvider to add
     */
    public void addPeriodProvider(PeriodProvider pp)
    {
        // the XML and cron period types are for future use so don't 
        // return them for now
        if (pp.getPeriodType() != XMLDuration.PERIOD_TYPE &&
            pp.getPeriodType() != Cron.PERIOD_TYPE)
        {
            this.providers.add(pp);
        }
    }
    
    /**
     * Retrieves a List of PeriodProvider objects representing
     * the valid period options for the property 
     * 
     * @see org.alfresco.repo.forms.DataTypeParameters#getAsObject()
     * @return List of PeriodProvider objects
     */
    public Object getAsObject()
    {
        return this.providers;
    }
    
    /**
     * Returns the valid period options as a JSONArray of JSONObject's.
     * 
     * @see org.alfresco.repo.forms.DataTypeParameters#getAsJSON()
     * @return A JSONArray object holding JSONObject's representing the
     *         period definitions
     */
    public Object getAsJSON()
    {
        JSONArray periods = new JSONArray();
        
        try
        {
            for (PeriodProvider pp : this.providers)
            {
                boolean hasExpression = !(pp.getExpressionMutiplicity().equals(PeriodProvider.ExpressionMutiplicity.NONE)); 
                
                JSONObject period = new JSONObject();
                period.put("type", pp.getPeriodType());
                period.put("label", pp.getDisplayLabel());
                period.put("hasExpression", hasExpression);
                
                if (hasExpression)
                {
                    period.put("expressionMandatory", 
                                pp.getExpressionMutiplicity().equals(PeriodProvider.ExpressionMutiplicity.MANDATORY));
                    period.put("expressionType", pp.getExpressionDataType().toPrefixString());
                    period.put("defaultExpression", pp.getDefaultExpression());
                }
                
                periods.put(period);
            }
        }
        catch (JSONException je)
        {
            // return an empty array
            periods = new JSONArray();
        }
        
        return periods;
    }
}
