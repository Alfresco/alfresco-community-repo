/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.forms.processor.task;

/**
 * @author Nick Smith
 */
public abstract class AbstractFormHelper
{
    // /** Protected constants */
    // protected static final String MSG_MIMETYPE_LABEL =
    // "form_service.mimetype.label";
    //
    // protected static final String MSG_MIMETYPE_DESC =
    // "form_service.mimetype.description";
    //
    // protected static final String MSG_ENCODING_LABEL =
    // "form_service.encoding.label";
    //
    // protected static final String MSG_ENCODING_DESC =
    // "form_service.encoding.description";
    //
    // protected static final String MSG_SIZE_LABEL = "form_service.size.label";
    //
    // protected static final String MSG_SIZE_DESC =
    // "form_service.size.description";
    //
    // /**
    // * A regular expression which can be used to match property names. These
    // * names will look like <code>"prop_cm_name"</code>. The pattern can also
    // be
    // * used to extract the "cm" and the "name" parts.
    // */
    // protected Pattern propertyNamePattern = Pattern.compile(PROP_DATA_PREFIX
    // + "(.*){1}?_(.*){1}?");
    //
    // /**
    // * A regular expression which can be used to match tranisent property
    // names.
    // * These names will look like <code>"prop_name"</code>. The pattern can
    // also
    // * be used to extract the "name" part.
    // */
    // protected Pattern transientPropertyPattern =
    // Pattern.compile(PROP_DATA_PREFIX + "(.*){1}?");
    //
    // /**
    // * A regular expression which can be used to match association names.
    // These
    // * names will look like <code>"assoc_cm_references_added"</code>. The
    // * pattern can also be used to extract the "cm", the "name" and the suffix
    // * parts.
    // */
    // protected Pattern associationNamePattern =
    // Pattern.compile(ASSOC_DATA_PREFIX + "(.*){1}?_(.*){1}?(_[a-zA-Z]+)");
    //
    // private NamespaceService namespaceService;
    //
    // private FieldDefinitionFactory factory;
    //
    // /**
    // * Sets up the field definitions for all the requested fields.
    // * <p>
    // * A NodeRef or TypeDefinition can be provided, however, if a NodeRef is
    // * provided all type information will be derived from the NodeRef and the
    // * TypeDefinition will be ignored.
    // * </p>
    // * <p>
    // * If any of the requested fields are not present on the type and they
    // * appear in the forcedFields list an attempt to find a model definition
    // for
    // * those fields is made so they can be included.
    // * </p>
    // *
    // * @param nodeRef The NodeRef of the item being generated
    // * @param typeDef The TypeDefiniton of the item being generated
    // * @param fields Restricted list of fields to include
    // * @param forcedFields List of field names that should be included even if
    // * the field is not currently present
    // * @param form The Form instance to populate
    // */
    // public void generateSelectedField(String fieldName, FieldCreationData
    // data)
    // {
    // FieldInfo fieldInfo = new FieldInfo(fieldName, data);
    // if (fieldInfo.isValid()) fieldInfo.addToForm(form);
    //
    // // try the field as a property
    // if (fieldInfo.tryProperty)
    // {
    // // lookup property def on node
    // PropertyDefinition propDef = propDefs.get(fullQName);
    // if (propDef != null)
    // {
    // // generate the property field
    // generatePropertyField(propDef, propValues.get(fullQName), null);
    //
    // // no need to try and find an association
    // tryAssociation = false;
    // foundField = true;
    // }
    // }
    //
    // // try the field as an association
    // if (fieldInfo.tryAssociation)
    // {
    // AssociationDefinition assocDef = assocDefs.get(fullQName);
    // if (assocDef != null)
    // {
    // // generate the association field
    // generateAssociationField(assocDef, form, (nodeRef != null) ?
    // retrieveAssociationValues(nodeRef,
    // assocDef) : null, this.namespaceService);
    //
    // foundField = true;
    // }
    // }
    //
    // // still not found the field, is it a forced field?
    // if (!foundField)
    // {
    // if (forcedFields != null && forcedFields.size() > 0 &&
    // forcedFields.contains(fieldName))
    // {
    // generateForcedField(fieldName, form);
    // }
    // else if (getLogger().isDebugEnabled())
    // {
    // getLogger().debug(
    // "Ignoring field \"" + fieldName +
    // "\" as it is not defined for the current "
    // + ((nodeRef != null) ? "node" : "type")
    // + " and it does not appear in the 'force' list");
    // }
    // }
    // }
    //
    // /**
    // * Sets up a field definition for the given association.
    // * <p>
    // * NOTE: This method is static so that it can serve as a helper method for
    // * FormFilter implementations as adding additional association fields is
    // * likely to be a common extension.
    // * </p>
    // *
    // * @param assocDef The AssociationDefinition of the field to generate
    // * @param form The Form instance to populate
    // * @param assocValues The values of the association field, can be null
    // * @param group The FieldGroup the association field belongs to, can be
    // null
    // * @param namespaceService NamespaceService instance
    // */
    // public void generateAssociationField(AssociationDefinition assocDef,
    // List<?> assocValues, FieldGroup group)
    // {
    // AssociationFieldDefinition fieldDef =
    // makeAssociationFieldDefinition(assocDef, group);
    //
    // // add definition to the form
    // form.addFieldDefinition(fieldDef);
    //
    // if (assocValues != null)
    // {
    // // add the association value to the form
    // // determine the type of association values data and extract
    // // accordingly
    // List<String> values = new ArrayList<String>(4);
    // for (Object value : assocValues)
    // {
    // if (value instanceof ChildAssociationRef)
    // {
    // values.add(((ChildAssociationRef) value).getChildRef().toString());
    // }
    // else if (value instanceof AssociationRef)
    // {
    // values.add(((AssociationRef) value).getTargetRef().toString());
    // }
    // else
    // {
    // values.add(value.toString());
    // }
    // }
    //
    // // Add the list as the value for the association.
    // form.addData(fieldDef.getDataKeyName(), values);
    // }
    // }
    //
    // /**
    // * Sets up a field definition for the given property.
    // * <p>
    // * NOTE: This method is static so that it can serve as a helper method for
    // * FormFilter implementations as adding additional property fields is
    // likely
    // * to be a common extension.
    // * </p>
    // *
    // * @param propDef The PropertyDefinition of the field to generate
    // * @param form The Form instance to populate
    // * @param propValue The value of the property field
    // * @param group The FieldGroup the property field belongs to, can be null
    // * @param namespaceService NamespaceService instance
    // */
    // @SuppressWarnings("unchecked")
    // public void generatePropertyField(PropertyDefinition propDef,
    // Serializable propValue, FieldGroup group)
    // {
    // PropertyFieldDefinition fieldDef =
    // factory.makePropertyFieldDefinition(propDef, group);
    // form.addFieldDefinition(fieldDef);
    //
    // // add the property value to the form
    // if (propValue != null)
    // {
    // if (propValue instanceof List)
    // {
    // // TODO Currently this adds repeating field data as a comma
    // // separated list, this needs to be replaced with a separate
    // // field for each value once we have full UI support in place.
    // propValue = StringUtils.collectionToCommaDelimitedString((List)
    // propValue);
    // }
    // form.addData(fieldDef.getDataKeyName(), propValue);
    // }
    // }
    //
    // private class FieldInfo
    // {
    // private final QName name;
    //
    // private final FieldDefinition fieldDefinition;
    //
    // private FieldType fieldType;
    //
    // private FieldInfo(String fieldName, FieldCreationData data)
    // {
    // this.name = makeNameAndFieldType(fieldName);
    // this.fieldDefinition = createDefinition(data);
    // }
    //
    // /**
    // * @return
    // */
    // public boolean isValid()
    // {
    // return fieldDefinition != null;
    // }
    //
    // private QName makeNameAndFieldType(String fieldName)
    // {
    // String[] parts = fieldName.split(":");
    // if (parts.length < 2 || parts.length > 3)
    // {
    // this.fieldType = FieldType.INVALID;
    // return QName.createQName(null, fieldName);
    // }
    // int indexer = 0;
    // if (parts.length == 3)
    // {
    // indexer = 1;
    // this.fieldType = FieldType.getType(parts[0]);
    // }
    // else
    // this.fieldType = FieldType.UNKNOWN;
    // String prefix = parts[0 + indexer];
    // String localName = parts[1 + indexer];
    // return QName.createQName(prefix, localName, namespaceService);
    // }
    //
    // /**
    // * @param data
    // */
    // private FieldDefinition createDefinition(FieldCreationData data)
    // {
    // FieldDefinition fieldDef = null;
    // switch (fieldType)
    // {
    // case INVALID:// So fieldDef will stay null.
    // break;
    // case PROPERTY:
    // fieldDef = generatePropertyDefinition(data);
    // break;
    // case ASSOCIATION:
    // fieldDef = generateAssociationDefinition(data);
    // break;
    // case UNKNOWN:
    // fieldDef = generatePropertyDefinition(data);
    // if (fieldDef != null)
    // fieldType = FieldType.PROPERTY;
    // else
    // {
    // fieldDef = generateAssociationDefinition(data);
    // if (fieldDef != null) fieldType = FieldType.ASSOCIATION;
    // }
    // }
    // if (fieldDef == null)
    // {
    // this.fieldType = FieldType.INVALID;
    // }
    // return fieldDef;
    // }
    //
    // /**
    // * @param data
    // * @return
    // */
    // private AssociationFieldDefinition
    // generateAssociationDefinition(FieldCreationData data)
    // {
    // AssociationDefinition assocDef = data.getAssocDefs().get(name);
    // if (assocDef != null)
    // return factory.makeAssociationFieldDefinition(assocDef, data.getGroup());
    // else
    // return null;
    // }
    //
    // /**
    // * @param data
    // */
    // private PropertyFieldDefinition
    // generatePropertyDefinition(FieldCreationData data)
    // {
    // PropertyDefinition propDef = data.getPropDefs().get(name);
    // if (propDef != null) //
    // return factory.makePropertyFieldDefinition(propDef, data.getGroup());
    // else
    // return null;
    // }
    //
    // public void addToForm(Form form, FieldCreationData data)
    // {
    // if (isValid())
    // {
    // form.addFieldDefinition(fieldDefinition);
    // Object value = null;
    // if (fieldType == FieldType.PROPERTY)
    // {
    // value = buildPropertyData(data, (PropertyFieldDefinition)
    // fieldDefinition);
    // }
    // else if (fieldType == FieldType.ASSOCIATION)
    // {
    // value = buildAssociationData(data, (AssociationFieldDefinition)
    // fieldDefinition);
    // }
    // if (value != null)
    // {
    // form.addData(fieldDefinition.getDataKeyName(), value);
    // }
    // }
    // else
    // {
    // throw new IllegalStateException("Cannot add invalid field:" +
    // name.getLocalName() + " to a form!");
    // }
    // }
    // }
    //
    // private enum FieldType
    // {
    // ASSOCIATION, INVALID, PROPERTY, UNKNOWN;
    //
    // public static FieldType getType(String type)
    // {
    // if (PROP.equals(type))
    // {
    // return PROPERTY;
    // }
    // else if (ASSOC.equals(type)) return ASSOCIATION;
    // return UNKNOWN;
    // }
    // }
    //
    // protected abstract Object buildPropertyData(FieldCreationData data,
    // PropertyFieldDefinition fieldDef);
    //
    // protected abstract Object buildAssociationData(FieldCreationData data,
    // AssociationFieldDefinition fieldDef);
}
