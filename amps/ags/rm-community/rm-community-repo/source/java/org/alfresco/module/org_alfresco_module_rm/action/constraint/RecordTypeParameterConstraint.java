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

package org.alfresco.module.org_alfresco_module_rm.action.constraint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.action.constraint.BaseParameterConstraint;
import org.alfresco.repo.i18n.StaticMessageLookup;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Record type parameter constraint
 *
 * @author Craig Tan
 * @since 2.1
 */
public class RecordTypeParameterConstraint extends BaseParameterConstraint
{
    /** Name constant */
    public static final String NAME = "rm-ac-record-types";

    /** record service */
    private RecordService recordService;

    /** dictionary service */
    private DictionaryService dictionaryService;

    /** file plan service */
    private FilePlanService filePlanService;

    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getAllowableValues()
     */
    protected Map<String, String> getAllowableValuesImpl()
    {
        return AuthenticationUtil.runAsSystem(new RunAsWork<Map<String, String>>()
        {
            @SuppressWarnings("unchecked")
            public Map<String, String> doWork()
            {
                Map<String, String> result = Collections.EMPTY_MAP;

                // get the file plan
                // TODO we will likely have to re-implement as a custom control so that context of the file
                //      plan can be correctly determined when setting the rule up
                NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);

                if (filePlan != null)
                {
                    Set<QName> recordTypes = recordService.getRecordMetadataAspects(filePlan);

                    result = new HashMap<>(recordTypes.size());
                    for (QName recordType : recordTypes)
                    {
                        AspectDefinition aspectDefinition = dictionaryService.getAspect(recordType);
                        if (aspectDefinition != null)
                        {
                            result.put(aspectDefinition.getName().getLocalName(), aspectDefinition.getTitle(new StaticMessageLookup()));
                        }
                    }
                }

                return result;
            }
        });
    }
}
