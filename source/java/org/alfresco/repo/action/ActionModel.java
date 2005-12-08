package org.alfresco.repo.action;

import org.alfresco.service.namespace.QName;

public interface ActionModel
{
    static final String ACTION_MODEL_URI            = "http://www.alfresco.org/model/action/1.0";
    static final String ACTION_MODEL_PREFIX         = "act";
    static final QName TYPE_ACTION                  = QName.createQName(ACTION_MODEL_URI, "action");
    static final QName PROP_DEFINITION_NAME         = QName.createQName(ACTION_MODEL_URI, "definitionName");
    static final QName PROP_ACTION_TITLE            = QName.createQName(ACTION_MODEL_URI, "actionTitle");
    static final QName PROP_ACTION_DESCRIPTION      = QName.createQName(ACTION_MODEL_URI, "actionDescription");
    static final QName PROP_EXECUTE_ASYNCHRONOUSLY  = QName.createQName(ACTION_MODEL_URI, "executeAsynchronously");
    static final QName ASSOC_CONDITIONS             = QName.createQName(ACTION_MODEL_URI, "conditions");
    static final QName ASSOC_COMPENSATING_ACTION    = QName.createQName(ACTION_MODEL_URI, "compensatingAction");
    static final QName ASSOC_PARAMETERS             = QName.createQName(ACTION_MODEL_URI, "parameters");
    static final QName TYPE_ACTION_CONDITION        = QName.createQName(ACTION_MODEL_URI, "actioncondition");
    static final QName TYPE_ACTION_PARAMETER        = QName.createQName(ACTION_MODEL_URI, "actionparameter");
    static final QName PROP_PARAMETER_NAME          = QName.createQName(ACTION_MODEL_URI, "parameterName");
    static final QName PROP_PARAMETER_VALUE         = QName.createQName(ACTION_MODEL_URI, "parameterValue");
    static final QName TYPE_COMPOSITE_ACTION        = QName.createQName(ACTION_MODEL_URI, "compositeaction");
    static final QName ASSOC_ACTIONS                = QName.createQName(ACTION_MODEL_URI, "actions");

    static final QName ASPECT_ACTIONS               = QName.createQName(ACTION_MODEL_URI, "actions");
    static final QName ASSOC_ACTION_FOLDER          = QName.createQName(ACTION_MODEL_URI, "actionFolder");
    
    //static final QName ASPECT_ACTIONABLE = QName.createQName(ACTION_MODEL_URI, "actionable");
    //static final QName ASSOC_SAVED_ACTION_FOLDERS = QName.createQName(ACTION_MODEL_URI, "savedActionFolders");
    //static final QName TYPE_SAVED_ACTION_FOLDER = QName.createQName(ACTION_MODEL_URI, "savedactionfolder");
    //static final QName ASSOC_SAVED_ACTIONS = QName.createQName(ACTION_MODEL_URI, "savedActions");
    
    static final QName PROP_CONDITION_INVERT = QName.createQName(ACTION_MODEL_URI, "invert");

}