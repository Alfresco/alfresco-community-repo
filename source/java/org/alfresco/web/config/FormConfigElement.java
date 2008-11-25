/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.element.ConfigElementAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Custom config element that represents form values for the client.
 * 
 * @author Neil McErlean.
 */
public class FormConfigElement extends ConfigElementAdapter
{
    private static final long serialVersionUID = -7008510360503886308L;
    private static Log logger = LogFactory.getLog(FormConfigElement.class);

    public enum Mode
    {
        VIEW, EDIT, CREATE;
        public static Mode fromString(String modeString)
        {
            if (modeString.equalsIgnoreCase("create")) {
                return Mode.CREATE;
            }
            else if (modeString.equalsIgnoreCase("edit"))
            {
                return Mode.EDIT;
            }
            else if (modeString.equalsIgnoreCase("view"))
            {
                return Mode.VIEW;
            }
            else
            {
                return null;
            }
        }
    }
    
    public static final String FORM_ID = "form";
    private String submissionURL;
    private List<StringPair> modelOverrides = new ArrayList<StringPair>();
    
    // We need to maintain the order of templates - per mode.
    private List<String> createTemplates = new ArrayList<String>();
    private List<String> editTemplates = new ArrayList<String>();
    private List<String> viewTemplates = new ArrayList<String>();

    /**
     * Map of the required roles for create-form templates.
     * Key = the template String. Value = the requires-role String.
     */
    private Map<String, String> rolesForCreateTemplates = new HashMap<String, String>();
    private Map<String, String> rolesForEditTemplates = new HashMap<String, String>();
    private Map<String, String> rolesForViewTemplates = new HashMap<String, String>();
    
    private List<FieldVisibilityRule> visibilityRules = new ArrayList<FieldVisibilityRule>();

    private Map<String, FormSet> sets = new HashMap<String, FormSet>();
    private Map<String, FormField> fields = new HashMap<String, FormField>();
    
    public FormConfigElement()
    {
        super(FORM_ID);
    }

    public FormConfigElement(String name)
    {
        super(name);
    }

    /**
     * @see org.alfresco.config.ConfigElement#getChildren()
     */
    public List<ConfigElement> getChildren()
    {
        throw new ConfigException(
                "Reading the default-controls config via the generic interfaces is not supported");
    }

    /**
     * @see org.alfresco.config.ConfigElement#combine(org.alfresco.config.ConfigElement)
     */
    public ConfigElement combine(ConfigElement otherConfigElement)
    {
        FormConfigElement otherFormElem = (FormConfigElement)otherConfigElement;
        FormConfigElement result = new FormConfigElement();
        
        combineSubmissionURL(otherFormElem, result);
        
        combineTemplates(otherFormElem, result);
        
        combineFieldVisibilities(otherFormElem, result);
        
        combineSets(otherFormElem, result);
        
        combineFields(otherFormElem, result);
        
        //TODO field-controls
        
        //TODO constraint-for-field
        
        combineModelOverrides(otherFormElem, result);
        
        return result;
    }

    private void combineFields(FormConfigElement otherFormElem,
            FormConfigElement result)
    {
        for (String nextFieldId : fields.keySet())
        {
            FormField nextField = fields.get(nextFieldId);
            Map<String, String> nextAttributes = nextField.getAttributes();
            
            List<String> attributeNames = new ArrayList<String>(nextAttributes.size());
            List<String> attributeValues = new ArrayList<String>(nextAttributes.size());
            for (String nextAttrName : nextAttributes.keySet())
            {
                attributeNames.add(nextAttrName);
                attributeValues.add(nextAttributes.get(nextAttrName));
            }
            result.addField(nextFieldId, attributeNames, attributeValues);
        }
        for (String nextFieldId : otherFormElem.fields.keySet())
        {
            FormField nextField = otherFormElem.fields.get(nextFieldId);
            Map<String, String> nextAttributes = nextField.getAttributes();
            
            List<String> attributeNames = new ArrayList<String>(nextAttributes.size());
            List<String> attributeValues = new ArrayList<String>(nextAttributes.size());
            for (String nextAttrName : nextAttributes.keySet())
            {
                attributeNames.add(nextAttrName);
                attributeValues.add(nextAttributes.get(nextAttrName));
            }
            result.addField(nextFieldId, attributeNames, attributeValues);
        }
    }

    private void combineModelOverrides(FormConfigElement otherFormElem,
            FormConfigElement result)
    {
        for (StringPair override : modelOverrides)
        {
            result.addModelOverrides(override.getName(), override.getValue());
        }
        for (StringPair override : otherFormElem.modelOverrides)
        {
            result.addModelOverrides(override.getName(), override.getValue());
        }
    }

    private void combineSets(FormConfigElement otherFormElem,
            FormConfigElement result)
    {
        for (String nextOldSet : sets.keySet())
        {
            FormSet nextOldSetData = sets.get(nextOldSet);
            String setId = nextOldSetData.getSetId();
            String parentId = nextOldSetData.getParentId();
            String appearance = nextOldSetData.getAppearance();
            result.addSet(setId, parentId, appearance);
        }
        for (String nextNewSet : otherFormElem.sets.keySet())
        {
            FormSet nextNewSetData = otherFormElem.sets.get(nextNewSet);
            String setId = nextNewSetData.getSetId();
            String parentId = nextNewSetData.getParentId();
            String appearance = nextNewSetData.getAppearance();
            result.addSet(setId, parentId, appearance);
        }
    }

    private void combineFieldVisibilities(FormConfigElement otherFormElem,
            FormConfigElement result)
    {
        List<FieldVisibilityRule> combinedInstructions = new ArrayList<FieldVisibilityRule>();
        combinedInstructions.addAll(this.visibilityRules);

        //If there are already instructions pertaining to a particular
        // field id, we can just leave them in place as the new should override the old.
        //TODO Test this is true.
        combinedInstructions.addAll(otherFormElem.visibilityRules);
        for (FieldVisibilityRule fvi : combinedInstructions)
        {
            result.addFieldVisibility(fvi.isShow() ? "show" : "hide"
                , fvi.getFieldId(), fvi.getModes());
        }
    }

    private void combineTemplates(FormConfigElement otherFormElem,
            FormConfigElement result)
    {
        for (String s : this.createTemplates)
        {
            String reqsRole = this.rolesForCreateTemplates.get(s);
            result.addFormTemplate("create-form", s, reqsRole);
        }
        for (String s : otherFormElem.createTemplates)
        {
            String reqsRole = otherFormElem.rolesForCreateTemplates.get(s);
            result.addFormTemplate("create-form", s, reqsRole);
        }
        
        for (String s : this.editTemplates)
        {
            String reqsRole = this.rolesForEditTemplates.get(s);
            result.addFormTemplate("edit-form", s, reqsRole);
        }
        for (String s : otherFormElem.editTemplates)
        {
            String reqsRole = otherFormElem.rolesForEditTemplates.get(s);
            result.addFormTemplate("edit-form", s, reqsRole);
        }
        
        for (String s : this.viewTemplates)
        {
            String reqsRole = this.rolesForViewTemplates.get(s);
            result.addFormTemplate("view-form", s, reqsRole);
        }
        for (String s : otherFormElem.viewTemplates)
        {
            String reqsRole = otherFormElem.rolesForViewTemplates.get(s);
            result.addFormTemplate("view-form", s, reqsRole);
        }
    }

    private void combineSubmissionURL(FormConfigElement otherFormElem,
            FormConfigElement result)
    {
        String otherSubmissionURL = otherFormElem.getSubmissionURL();
        result.setSubmissionURL(otherSubmissionURL == null ? this.submissionURL : otherSubmissionURL);
    }

    public String getSubmissionURL()
    {
        return this.submissionURL;
    }
    
    public Map<String, FormSet> getSets()
    {
        return Collections.unmodifiableMap(this.sets);
    }
    
    public List<FieldVisibilityRule> getFieldVisibilityRules()
    {
        return Collections.unmodifiableList(this.visibilityRules);
    }
    
    //TODO This is all fields. need getVisFields(Role)
    public Map<String, FormField> getFields()
    {
        return Collections.unmodifiableMap(this.fields);
    }
    
    public Map<String, FormField> getVisibleCreateFields()
    {
        return getFieldsVisibleInMode(Mode.CREATE);
    }
    public Map<String, FormField> getVisibleEditFields()
    {
        return getFieldsVisibleInMode(Mode.EDIT);
    }
    public Map<String, FormField> getVisibleViewFields()
    {
        return getFieldsVisibleInMode(Mode.VIEW);
    }

    public Map<String, String> getCreateTemplates()
    {
        return Collections.unmodifiableMap(this.rolesForCreateTemplates);
    }
    
    public Map<String, String> getEditTemplates()
    {
        return Collections.unmodifiableMap(this.rolesForEditTemplates);
    }
    
    public Map<String, String> getViewTemplates()
    {
        return Collections.unmodifiableMap(this.rolesForViewTemplates);
    }
    
    public List<StringPair> getModelOverrideProperties()
    {
        return Collections.unmodifiableList(modelOverrides);
    }

    /* package */void setSubmissionURL(String newURL)
    {
        this.submissionURL = newURL;
    }

    /* package */void addFormTemplate(String nodeName, String template,
            String requiredRole)
    {
        if (requiredRole == null)
        {
            requiredRole = "";
        }
        
        if (nodeName.equals("create-form"))
        {
            if (!createTemplates.contains(template))
            {
                createTemplates.add(template);
            }
            rolesForCreateTemplates.put(template, requiredRole);
        }
        else if (nodeName.equals("edit-form"))
        {
            if (!editTemplates.contains(template))
            {
                editTemplates.add(template);
            }
            rolesForEditTemplates.put(template, requiredRole);
        }
        else if (nodeName.equals("view-form"))
        {
            if (!viewTemplates.contains(template))
            {
                viewTemplates.add(template);
            }
            rolesForViewTemplates.put(template, requiredRole);
        }
        else
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("Unrecognised mode: " + nodeName);
            }
            return;
        }
    }

    /* package */void addFieldVisibility(String showOrHide, String fieldId,
            String mode)
    {
        FieldVisibilityRule instruc = new FieldVisibilityRule(showOrHide, fieldId, mode);
        this.visibilityRules.add(instruc);
    }

    /* package */void addSet(String setId, String parentSetId, String appearance)
    {
        sets.put(setId, new FormSet(setId, parentSetId, appearance));
    }

    /* package */void addField(String fieldId, List<String> attributeNames,
            List<String> attributeValues)
    {
        Map<String, String> attrs = new HashMap<String, String>();
        for (int i = 0; i < attributeNames.size(); i++)
        {
            attrs.put(attributeNames.get(i), attributeValues.get(i));
        }
        fields.put(fieldId, new FormField(attrs));
    }

    /* package */void addControlForField(String fieldId, String template,
            List<String> controlParamNames, List<String> controlParamValues)
    {
        FormField field = fields.get(fieldId);
        field.setTemplate(template);
        for (int i = 0; i < controlParamNames.size(); i++)
        {
            field.addControlParam(new StringPair(controlParamNames.get(i),
                    controlParamValues.get(i)));
        }
    }

    /* package */void addConstraintForField(String fieldId, String type,
            String message, String messageId)
    {
        FormField field = fields.get(fieldId);
        field.setConstraintType(type);
        field.setConstraintMessage(message);
        field.setConstraintMessageId(messageId);
    }

    /* package */void addModelOverrides(String name, String value)
    {
        StringPair modelOverride = new StringPair(name, value);
        //TODO Consider using a different collection type here.
        for (Iterator<StringPair> iter = modelOverrides.iterator(); iter.hasNext(); )
        {
            if (iter.next().getName().equals(name))
            {
                iter.remove();
            }
        }
        modelOverrides.add(modelOverride);
    }
    
    private String findFirstMatchingTemplate(List<String> templates,
            Map<String, String> templatesToRoles, List<String> currentRoles)
    {
        // If there is no current role, we can only return templates that require no role.
        if (currentRoles == null || currentRoles.isEmpty())
        {
            for (String template : templates)
            {
                String requiredRolesForThisTemplate = templatesToRoles.get(template);
                if (requiredRolesForThisTemplate.trim().length() == 0)
                {
                    return template;
                }
            }
            return null;
        }
        
        // Here there is at least one current role.
        for (String template : templates)
        {
            String requiredRolesForThisTemplate = templatesToRoles.get(template);
            for (String role : currentRoles)
            {
                if (requiredRolesForThisTemplate.contains(role))
                {
                    return template;
                }
            }
        }
        return null;
    }
    
    /**
     * 
     * @param m
     * @param roles a list of roles, can be an empty list or null.
     * @return <code>null</code> if there is no template available for the specified role(s).
     */
    public String getFormTemplate(Mode m, List<String> roles)
    {
        switch (m)
        {
        case CREATE: return findFirstMatchingTemplate(createTemplates, rolesForCreateTemplates, roles);
        case EDIT: return findFirstMatchingTemplate(editTemplates, rolesForEditTemplates, roles);
        case VIEW: return findFirstMatchingTemplate(viewTemplates, rolesForViewTemplates, roles);
        default: return null;
        }
    }

    /**
     * This method checks whether the specified field is visible in the specified mode.
     * The algorithm for determining visibility works as follows
     * <ul>
     *   <li>If there is no field-visibility configuration (show or hide tags) then
     *       all fields are visible in all modes.</li>
     *   <li>If there are one or more hide tags then the specified fields will be hidden
     *       in the specified modes. All other fields remain visible as before.</li>
     *   <li>However, as soon as a single show tag appears in the config xml, this is
     *       taken as a signal that all field visibility is to be manually configured.
     *       At that point, all fields default to hidden and only those explicitly
     *       configured to be shown (with a show tag) will actually be shown.</li>
     *   <li>Show and hide rules will be applied in sequence with later rules
     *       invalidating previous rules.</li>
     *   <li>Show or hide rules which only apply for specified modes have an implicit
     *       element. e.g. <show id="name" for-mode="view"/> would show the name field
     *       in view mode and by implication, hide it in other modes.</li>
     * </ul>
     * @param fieldId
     * @param m
     * @return
     */
    public boolean isFieldVisible(String fieldId, Mode m)
    {
        if (visibilityRules.isEmpty())
        {
            return true;
        }
        else
        {
            boolean listContainsAtLeastOneShow = false;
            // true means show, false means hide, null means 'no answer'.
            Boolean latestInstructionToShow = null;
            for (FieldVisibilityRule instruc : visibilityRules)
            {
                if (instruc.isShow())
                {
                    listContainsAtLeastOneShow = true;
                }
                if (instruc.getFieldId().equals(fieldId))
                {
                    // This matters
                    if (instruc.getListOfModes().contains(m))
                    {
                        latestInstructionToShow = instruc.isShow() ?
                                Boolean.TRUE : Boolean.FALSE;
                    }
                    else
                    {
                        latestInstructionToShow = instruc.isShow() ?
                                Boolean.FALSE : Boolean.TRUE;
                    }
                }
            }
            if (latestInstructionToShow == null)
            {
                // We really on the 'default' behaviour
                return !listContainsAtLeastOneShow;
            }
            else
            {
                return latestInstructionToShow.booleanValue();
            }
        }
    }
    
    private Map<String, FormField> getFieldsVisibleInMode(Mode mode)
    {
        Map<String, FormField> result = new HashMap<String, FormField>();
        for (String fieldId : this.fields.keySet())
        {
            if (this.isFieldVisible(fieldId, mode))
            {
                result.put(fieldId, fields.get(fieldId));
            }
        }
        return result;
    }

    /**
     * This inner class represents a &lt;set&gt; element within a &lt;form&gt;
     * element.
     * 
     * @author Neil McErlean.
     */
    public static class FormSet
    {
        private final String setId;
        private final String parentId;
        private final String appearance;
        public FormSet(String setId, String parentId, String appearance)
        {
            this.setId = setId;
            this.parentId = parentId;
            this.appearance = appearance;
        }
        public String getSetId()
        {
            return setId;
        }
        public String getParentId()
        {
            return parentId;
        }
        public String getAppearance()
        {
            return appearance;
        }

        @Override
        public boolean equals(Object otherObj)
        {
            if (otherObj == null
                    || !otherObj.getClass().equals(this.getClass()))
            {
                return false;
            }
            FormSet otherSet = (FormSet) otherObj;
            return this.setId.equals(otherSet.setId)
                    && this.parentId.equals(otherSet.parentId)
                    && this.appearance.equals(otherSet.appearance);
        }
        
        @Override
        public int hashCode()
        {
            return setId.hashCode()
                    + 7 * parentId.hashCode() + 13 * appearance.hashCode();
        }
    }

    public static class FormField
    {
        
        //TODO I think I'll have to add explicit methods for all params e.g. getRequiredRoles():String
        // Maps of attributes are not enough as they leave too much work up to the client.
        
        private final Map<String, String> attributes;
        private String template;
        private final List<StringPair> controlParams = new ArrayList<StringPair>();
        private String constraintType;
        private String constraintMessage;
        private String constraintMessageId;
        public FormField(Map<String, String> attributes)
        {
            this.attributes = attributes;
        }
        public void setTemplate(String template)
        {
            this.template = template;
        }
        public void setConstraintType(String constraintType)
        {
            this.constraintType = constraintType;
        }
        public void setConstraintMessage(String constraintMessage)
        {
            this.constraintMessage = constraintMessage;
        }
        public void setConstraintMessageId(String constraintMessageId)
        {
            this.constraintMessageId = constraintMessageId;
        }
        public void addControlParam(StringPair nameValue)
        {
            this.controlParams.add(nameValue);
        }
        public Map<String, String> getAttributes()
        {
            return Collections.unmodifiableMap(attributes);
        }
        public String getTemplate()
        {
            return template;
        }
        public List<StringPair> getControlParams()
        {
            return Collections.unmodifiableList(controlParams);
        }
        public String getConstraintType()
        {
            return constraintType;
        }
        public String getConstraintMessage()
        {
            return constraintMessage;
        }
        public String getConstraintMessageId()
        {
            return constraintMessageId;
        }
    }
    
    public static class FieldVisibilityRule
    {
        private final boolean show;
        private final String fieldId;
        private final List<Mode> forModes;
        public FieldVisibilityRule(String showOrHide, String fieldId, String modesString)
        {
            if (showOrHide.equals("show"))
            {
                this.show = true;
            }
            else
            {
                this.show = false;
            }
            this.fieldId = fieldId;
            this.forModes = new ArrayList<Mode>();
            if (modesString == null)
            {
                forModes.addAll(Arrays.asList(Mode.values()));
            }
            else
                {
                StringTokenizer st = new StringTokenizer(modesString, ",");
                while (st.hasMoreTokens())
                {
                    String nextToken = st.nextToken().trim();
                    Mode nextMode = Mode.fromString(nextToken);
                    forModes.add(nextMode);
                }
            }
        }
        
        public boolean isShow()
        {
            return show;
        }
        
        public boolean isHide()
        {
            return !isShow();
        }
        
        public String getFieldId()
        {
            return fieldId;
        }
        
        public List<Mode> getListOfModes()
        {
            return Collections.unmodifiableList(forModes);
        }
        
        public String getModes()
        {
            StringBuilder result = new StringBuilder();
            for (Iterator<Mode> iter = forModes.iterator(); iter.hasNext(); )
            {
                result.append(iter.next());
                if (iter.hasNext())
                {
                    result.append(", ");
                }
            }
            return result.toString();
        }
        
        public String toString()
        {
            StringBuilder result = new StringBuilder();
            result.append(show ? "show" : "hide");
            result.append(" ").append(fieldId).append(" ").append(forModes);
            return result.toString();
        }

        @Override
        public boolean equals(Object otherObj)
        {
            if (otherObj == null
                    || !otherObj.getClass().equals(this.getClass()))
            {
                return false;
            }
            FieldVisibilityRule otherFVI = (FieldVisibilityRule) otherObj;
            return this.show == otherFVI.show
                    && this.fieldId.equals(otherFVI.fieldId)
                    && this.forModes.equals(otherFVI.forModes);
        }
        
        @Override
        public int hashCode()
        {
            return show ? 1 : 0
                    + 7 * fieldId.hashCode() + 13 * forModes.hashCode();
        }
    }
}

