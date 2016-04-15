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
package org.alfresco.repo.rule;

import org.alfresco.service.namespace.QName;

/**
 * Interface containing rule model constants
 * 
 * @author Roy Wetherall
 */
public interface RuleModel
{
    /** Rule model constants */
    static final String RULE_MODEL_URI           = "http://www.alfresco.org/model/rule/1.0";
    static final String RULE_MODEL_PREFIX        = "rule";
    
    static final QName TYPE_RULE                 = QName.createQName(RULE_MODEL_URI, "rule");
    static final QName PROP_RULE_TYPE            = QName.createQName(RULE_MODEL_URI, "ruleType");
    static final QName PROP_APPLY_TO_CHILDREN    = QName.createQName(RULE_MODEL_URI, "applyToChildren");
    static final QName PROP_EXECUTE_ASYNC        = QName.createQName(RULE_MODEL_URI, "executeAsynchronously");
    static final QName ASSOC_ACTION              = QName.createQName(RULE_MODEL_URI, "action");    
    static final QName PROP_DISABLED             = QName.createQName(RULE_MODEL_URI, "disabled");
    
    static final QName ASPECT_RULES              = QName.createQName(RULE_MODEL_URI, "rules");
    static final QName ASSOC_RULE_FOLDER         = QName.createQName(RULE_MODEL_URI, "ruleFolder");
    
    static final QName ASPECT_IGNORE_INHERITED_RULES = QName.createQName(RULE_MODEL_URI, "ignoreInheritedRules");
}