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

package org.alfresco.module.org_alfresco_module_rm.record;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Record Service Interface.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@AlfrescoPublicApi
public interface RecordService
{
    /**
     * Register a record metadata aspect.
     * <p>
     * The file plan type indicates which file plan type the aspect applied to.  Null indicates that
     * the aspect applies to rma:filePlan.
     * <p>
     * A record metadata aspect can be registered more than once if it applies to more than one
     * file plan type.
     *
     * @param recordMetadataAspect  record metadata aspect qualified name
     * @param filePlanType          file plan type
     *
     * @since 2.2
     */
    void registerRecordMetadataAspect(QName recordMetadataAspect, QName filePlanType);

    /**
     * Disables the property editable check.
     *
     * @since 2.2
     */
    void disablePropertyEditableCheck();

    /**
     * Disables the property editable check for a given node in this transaction only.
     *
     * @param nodeRef   node reference
     *
     * @since 2.2
     */
    void disablePropertyEditableCheck(NodeRef nodeRef);

    /**
     * Enables the property editable check.  By default this is always enabled.
     */
    void enablePropertyEditableCheck();

    /**
    * Gets a list of all the record meta-data aspects
    *
    * @return {@link Set}&lt;{@link QName}&gt;   list of record meta-data aspects
    *
    * @deprecated since 2.2, file plan component required to provide context
    */
   @Deprecated
   Set<QName> getRecordMetaDataAspects();

   /**
    * Indicates whether the provided aspect is a registered record meta-data
    * aspect.
    *
    * @param aspect     aspect {@link QName}
    * @return boolean   true if the aspect is a registered record meta-data aspect, false otherwise
    *
    * @since 2.3
    */
   boolean isRecordMetadataAspect(QName aspect);

   /**
    * Indicates whther the provided property is declared on a registered record
    * meta-data aspect.
    *
    * @param  property  property {@link QName}
    * @return boolean   true if the property is declared on a registered record meta-data aspect,
    *                   false otherwise
    *
    * @since 2.3
    */
   boolean isRecordMetadataProperty(QName property);

   /**
    * Gets a list of all the record metadata aspects relevant to the file plan type of the
    * file plan component provided.
    * <p>
    * If a null context is provided all record meta-data aspects are returned, but this is not
    * recommended.
    *
    * @param  nodeRef                      node reference to file plan component providing context
    * @return {@link Set}&lt;{@link QName} &gt;   list of record meta-data aspects
    *
    * @since 2.2
    */
   Set<QName> getRecordMetadataAspects(NodeRef nodeRef);

   /**
    * Gets a list of all the record metadata aspect that relate to the provided file plan type.
    * <p>
    * If null is provided for the file plan type then record metadata aspects for the default
    * file plan type (rma:filePlan) are returned.
    *
    * @param filePlanType                   file plan type
    * @return {@link Set} &lt;{@link QName} &gt;     list of record meta-data aspects
    *
    * @since 2.2
    */
   Set<QName> getRecordMetadataAspects(QName filePlanType);

   /**
    * Checks whether if the given node reference is a record or not
    *
    * @param nodeRef    node reference to be checked
    * @return boolean   true if the node reference is a record, false otherwise
    */
   boolean isRecord(NodeRef nodeRef);

   /**
    * Indicates whether the record is declared
    *
    * @param nodeRef   node reference of the record for which the check would be performed
    * @return boolean  true if record is declared, false otherwise
    */
   boolean isDeclared(NodeRef nodeRef);

    /**
     * Creates a new record from an existing node and files it into the specified location.
     * <p>
     * Note that the node reference of the record will be the same as the original
     * document.
     *
     * @param filePlan  The filePlan in which the record should be placed. filePlan can be <code>null</code> in this case the default RM site will be used.
     * @param nodeRef   The node from which the record will be created
     * @param locationNodeRef   The container in which the record will be created
     * @param isLinked  indicates if the newly created record is linked to it's original location or not.
     */
    void createRecord(final NodeRef filePlan, final NodeRef nodeRef, final NodeRef locationNodeRef, final boolean isLinked);

    /**
     * Creates a new record from an existing node and files it into the specified location.
     * <p>
     * Note that the node reference of the record will be the same as the original
     * document.
     *
     * @param filePlan  The filePlan in which the record should be placed. filePlan can be <code>null</code> in this case the default RM site will be used.
     * @param nodeRef   The node from which the record will be created
     * @param locationNodeRef   The container in which the record will be created
     */
    void createRecord(final NodeRef filePlan, final NodeRef nodeRef, final NodeRef locationNodeRef);

   /**
    * Creates a new unfiled record from an existing node.
    * <p>
    * Note that the node reference of the record will be the same as the original
    * document.
    *
    * @param filePlan  The filePlan in which the record should be placed. filePlan can be <code>null</code> in this case the default RM site will be used.
    * @param nodeRef   The node from which the record will be created
    * @param isLinked  indicates if the newly created record is linked to it's original location or not.
    */
   void createRecord(NodeRef filePlan, NodeRef nodeRef, boolean isLinked);

   /**
    * Links the newly created record to it's original location.
    *
    * @see #createRecord(NodeRef, NodeRef, boolean)
    */
   void createRecord(NodeRef filePlan, NodeRef nodeRef);

   /**
    * Creates a record from a copy of the node reference provided.
    *
    * @param filePlan   file plan
    * @param nodeRef    node reference
    */
   NodeRef createRecordFromCopy(NodeRef filePlan, NodeRef nodeRef);

   /**
    * Creates a new document in the unfiled records container if the given node reference is a file plan
    * node reference otherwise the node reference will be used as the destination for the new record.
    *
    * @param parent     parent node reference
    * @param name       name of the new record
    * @param type       content type, cm:content if null
    * @param properties properties
    * @param reader     content reader
    */
   NodeRef createRecordFromContent(NodeRef parent, String name, QName type, Map<QName, Serializable> properties, ContentReader reader);

   /**
    * Indicates whether the record is filed or not
    *
    * @param record    nodeRef of record
    * @return boolean   true if filed, false otherwise
    */
   boolean isFiled(NodeRef record);

   /**
    * 'File' a new document that arrived in the file plan structure.
    *
    * @param record    noderef of record
    */
   void file(NodeRef record);

   /**
    * Rejects a record with the provided reason
    *
    * @param nodeRef   The record which will be rejected
    * @param reason    The reason for rejection
    */
   void rejectRecord(NodeRef nodeRef, String reason);

   /**
    * Indicates whether a property of a record is editable for the current user or not.
    *
    * @param record     record
    * @param property   property
    * @return boolean   true if editable, false otherwise.
    */
   boolean isPropertyEditable(NodeRef record, QName property);

   /**
    * Indicates whether the given node (record or record folder) is a metadata stub or not.
    *
    * @param nodeRef   node reference
    * @return boolean  true if a metadata stub, false otherwise
    */
   boolean isMetadataStub(NodeRef nodeRef);

   /**
    * Gets a list of all the records within a record folder
    *
    * @param recordFolder      record folder
    * @return List&lt;NodeRef&gt;    list of records in the record folder
    */
   List<NodeRef> getRecords(NodeRef recordFolder);

   /**
    * Adds the specified type to the record
    *
    * @param nodeRef    Record node reference
    * @param typeQName  Type to add
    */
   void addRecordType(NodeRef nodeRef, QName typeQName);

   /**
    * Creates a record from the given document
    *
    * @param nodeRef    The document node reference from which a record will be created
    */
   void makeRecord(NodeRef nodeRef);

   /**
    * Links a record to a record folder
    *
    * @param record         the record to link
    * @param recordFolder   the record folder to link it to
    */
   void link(NodeRef record, NodeRef recordFolder);

   /**
    * Unlinks a record from a specified record folder.
    *
    * @param record         the record to unlink
    * @param recordFolder   the record folder to unlink it from
    *
    * @since 2.3
    */
   void unlink(NodeRef record, NodeRef recordFolder);

    /**
     * Completes a record
     *
     * @param nodeRef Record node reference
     */
    void complete(NodeRef nodeRef);
}
