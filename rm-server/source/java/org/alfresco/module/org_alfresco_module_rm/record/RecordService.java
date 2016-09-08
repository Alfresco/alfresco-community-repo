/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

package org.alfresco.module.org_alfresco_module_rm.record;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Record Service Interface.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public interface RecordService
{
    /**
     * Disables the property editable check.
     */
    void disablePropertyEditableCheck();
    
    /**
     * Enables the property editable check.  By default this is always enabled.
     */
    void enablePropertyEditableCheck();
    
    /**
    * Gets a list of all the record meta-data aspects
    *
    * @return {@link Set}<{@link QName}>   list of record meta-data aspects
    */
   Set<QName> getRecordMetaDataAspects();

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
    * Creates a new unfiled record from an existing node.
    * <p>
    * Note that the node reference of the record will be the same as the original
    * document.
    *
    * @param filePlan  The filePlan in which the record should be placed
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
    * Creates a new document as a unfiled record.
    * 
    * @param filePlan
    * @param name
    * @param type
    * @param properties
    */
   NodeRef createRecord(NodeRef filePlan, String name, QName type, Map<QName, Serializable> properties, ContentReader reader);

   /**
    * Indicates whether the record is filed or not
    *
    * @param nodeRef    record
    * @return boolean   true if filed, false otherwise
    */
   boolean isFiled(NodeRef record);

   /**
    * 'File' a new document that arrived in the file plan structure.
    * 
    * @param nodeRef    record
    */
   void file(NodeRef record);
   
   /**
    * Hides a record within a collaboration site
    *
    * @param nodeRef   The record which should be hidden
    */
   void hideRecord(NodeRef nodeRef);

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
}
