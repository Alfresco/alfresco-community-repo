/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.forms.processor.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.dictionary.types.period.Cron;
import org.alfresco.repo.dictionary.types.period.Days;
import org.alfresco.repo.dictionary.types.period.EndOfFinancialMonth;
import org.alfresco.repo.dictionary.types.period.EndOfFinancialQuarter;
import org.alfresco.repo.dictionary.types.period.EndOfFinancialYear;
import org.alfresco.repo.dictionary.types.period.EndOfMonth;
import org.alfresco.repo.dictionary.types.period.EndOfQuarter;
import org.alfresco.repo.dictionary.types.period.EndOfYear;
import org.alfresco.repo.dictionary.types.period.Months;
import org.alfresco.repo.dictionary.types.period.NoPeriod;
import org.alfresco.repo.dictionary.types.period.Quarters;
import org.alfresco.repo.dictionary.types.period.Weeks;
import org.alfresco.repo.dictionary.types.period.XMLDuration;
import org.alfresco.repo.dictionary.types.period.Years;
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
    private static final long serialVersionUID = -6158584544200642857L;
    
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
     * @see org.alfresco.repo.forms.DataTypeParameters#getParameters()
     * @return List of PeriodProvider objects
     */
    public Object getParameters()
    {
        return this.providers;
    }
    
    /**
     * Returns the valid period options as a JSONArray of JSONObject's.
     * 
     * @see org.alfresco.repo.forms.DataTypeParameters#getParametersAsJSON()
     * @return A JSONArray object holding JSONObject's representing the
     *         period definitions
     */
    public Object getParametersAsJSON()
    {
        JSONArray periods = new JSONArray();
        
        try
        {
            for (PeriodProvider pp : this.providers)
            {
                boolean hasExpression = !(pp.getExpressionMutiplicity().equals(PeriodProvider.ExpressionMutiplicity.NONE)); 
                
                JSONObject period = new JSONObject();
                period.put("type", pp.getPeriodType());
                period.put("label", getPeriodTypeLabel(pp));
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
    
    protected String getPeriodTypeLabel(PeriodProvider pp)
    {
        // TODO: The label for the period should be localisable
        //       and returned via the PeriodProvider, for now
        //       we will provide them here.
        
        String type = pp.getPeriodType();
        String label = type;
        
        if (Cron.PERIOD_TYPE.equals(type))
        {
            label = "Cron Expression";
        }
        else if (Days.PERIOD_TYPE.equals(type))
        {
            label = "Days";
        }
        else if (EndOfFinancialMonth.PERIOD_TYPE.equals(type))
        {
            label = "End Of Financial Month";
        }
        else if (EndOfFinancialQuarter.PERIOD_TYPE.equals(type))
        {
            label = "End Of Financial Quarter";
        }
        else if (EndOfFinancialYear.PERIOD_TYPE.equals(type))
        {
            label = "End Of Financial Year";
        }
        else if (EndOfMonth.PERIOD_TYPE.equals(type))
        {
            label = "End Of Month";
        }
        else if (EndOfQuarter.PERIOD_TYPE.equals(type))
        {
            label = "End Of Quarter";
        }
        else if (EndOfYear.PERIOD_TYPE.equals(type))
        {
            label = "End Of Year";
        }
        else if (Months.PERIOD_TYPE.equals(type))
        {
            label = "Month";
        }
        else if (NoPeriod.PERIOD_TYPE.equals(type))
        {
            label = "None";
        }
        else if (Quarters.PERIOD_TYPE.equals(type))
        {
            label = "Quarter";
        }
        else if (Weeks.PERIOD_TYPE.equals(type))
        {
            label = "Week";
        }
        else if (XMLDuration.PERIOD_TYPE.equals(type))
        {
            label = "XML Duration";
        }
        else if (Years.PERIOD_TYPE.equals(type))
        {
            label = "Year";
        }
        
        return label;
    }
}
