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

package org.alfresco.repo.workflow.activiti.listener;

import java.util.Calendar;
import java.util.Date;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;

import org.alfresco.util.ISO8601DateFormat;

/**
 * An {@link ExecutionListener} that converts a variable of type date to a ISO-8601 date. The variable that should be converted can be set using field "source", the output string will be written in field configured in "target".
 *
 * @author Frederik Heremans
 * @since 4.0
 */
public class ConvertDateToISO8601 implements ExecutionListener
{
    protected Expression source;
    protected Expression target;

    public void setSource(Expression source)
    {
        this.source = source;
    }

    public void setTarget(Expression target)
    {
        this.target = target;
    }

    @Override
    public void notify(DelegateExecution execution) throws Exception
    {
        String sourceVarName = getExpressionString(source, execution);
        String targetVarName = getExpressionString(target, execution);

        if (sourceVarName == null || targetVarName == null)
        {
            throw new IllegalArgumentException("Both fields 'source' and 'target' shoudl be set");
        }

        Object dateVar = execution.getVariable(sourceVarName);
        Date dateToConvert = null;

        // Accept null, Date or Calendar as value
        if (dateVar != null)
        {
            if (dateVar instanceof Date)
            {
                dateToConvert = (Date) execution.getVariable(sourceVarName);
            }
            else if (dateVar instanceof Calendar)
            {
                dateToConvert = ((Calendar) execution.getVariable(sourceVarName)).getTime();
            }
            else
            {
                throw new IllegalArgumentException("Variable with name: " + sourceVarName + " must be a Date or a Calendar");
            }
        }
        if (dateToConvert != null)
        {
            // Convert the date to ISO-8601 format
            String convertedDate = ISO8601DateFormat.format(dateToConvert);
            execution.setVariable(targetVarName, convertedDate);
        }
    }

    protected String getExpressionString(Expression expression, VariableScope variableScope)
    {
        if (expression != null)
        {
            return (String) expression.getValue(variableScope);
        }
        return null;
    }
}
