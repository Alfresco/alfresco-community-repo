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
    static final String RULE_MODEL_URI          = "http://www.alfresco.org/model/rule/1.0";
    static final String RULE_MODEL_PREFIX       = "rule";
    static final QName TYPE_RULE                = QName.createQName(RULE_MODEL_URI, "rule");
    static final QName PROP_RULE_TYPE           = QName.createQName(RULE_MODEL_URI, "ruleType");
    static final QName TYPE_RULE_CONTENT        = QName.createQName(RULE_MODEL_URI, "rulecontent");
    static final QName PROP_APPLY_TO_CHILDREN   = QName.createQName(RULE_MODEL_URI, "applyToChildren");
    static final QName ASPECT_RULES             = QName.createQName(RULE_MODEL_URI, "rules");
    static final QName ASSOC_RULE_FOLDER        = QName.createQName(RULE_MODEL_URI, "ruleFolder");
}