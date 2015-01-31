/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.forms.processor.workflow;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.repo.workflow.PropertyValueSizeIsMoreMaxLengthException;

public class WorkflowFormFilter<ItemType, PersistType> extends AbstractFilter<ItemType, PersistType>
{

    private static final String PROP_BPM_COMMENT = "prop_bpm_comment";

    private int maxLengthBpmCommentProperty = 4000;

    public void setMaxLengthBpmCommentProperty(int maxLengthBpmCommentProperty)
    {
        this.maxLengthBpmCommentProperty = maxLengthBpmCommentProperty;
    }

    @Override
    public void beforeGenerate(ItemType item, List<String> fields, List<String> forcedFields, Form form, Map<String, Object> context)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterGenerate(ItemType item, List<String> fields, List<String> forcedFields, Form form, Map<String, Object> context)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforePersist(ItemType item, FormData data)
    {
        FieldData bpmComment = data.getFieldData(PROP_BPM_COMMENT);
        if (bpmComment != null)
        {
            int value = ((String) bpmComment.getValue()).getBytes().length;

            if (maxLengthBpmCommentProperty < value)
            {
                throw new PropertyValueSizeIsMoreMaxLengthException(PROP_BPM_COMMENT);
            }
        }

    }

    @Override
    public void afterPersist(ItemType item, FormData data, PersistType persistedObject)
    {
        // TODO Auto-generated method stub

    }

}
