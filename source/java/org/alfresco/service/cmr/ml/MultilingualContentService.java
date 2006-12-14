/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.ml;

import java.util.Locale;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The API to manage multilingual content and related structures.
 * 
 * @author Derek Hulley
 */
@PublicService
public interface MultilingualContentService
{
    /**
     * Rename an existing <b>cm:translation</b> by adding locale suffixes to the base name.
     * Where there are name clashes with existing documents, a numerical naming scheme will be
     * adopted.
     *
     * @param translationNodeRef       An existing <b>cm:translation</b>
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"translationNodeRef"})
    void renameWithMLExtension(NodeRef translationNodeRef);

    /**
     * Make an existing document translatable.  If it is already translatable, then nothing is done.
     *
     * @param contentNodeRef           An existing <b>cm:content</b>
     * @return                         Returns the <b>cm:mlContainer</b> translation parent
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"contentNodeRef", "locale"})
    NodeRef makeTranslatable(NodeRef contentNodeRef, Locale locale);

    /**
     * Make a translation out of an existing document.  The necessary translation structures will be created
     * as necessary.
     *
     * @param newTranslationNodeRef    An existing <b>cm:content</b>
     * @param translationOfNodeRef     An existing <b>cm:translation</b> or <b>cm:mlContainer</b>
     * @return                         Returns the <b>cm:mlContainer</b> translation parent
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"newTranslationNodeRef", "translationOfNodeRef", "locale"})
    NodeRef addTranslation(NodeRef newTranslationNodeRef, NodeRef translationOfNodeRef, Locale locale);

    /**
     *
     * @return                         Returns the <b>cm:mlContainer</b> translation parent
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"translationNodeRef"})
    NodeRef getTranslationContainer(NodeRef translationNodeRef);

    /**
     * Create a new edition of an existing <b>cm:mlContainer</b>.
     *
     * @param mlContainerNodeRef       An existing <b>cm:mlContainer</b>
     * @param translationNodeRef       The specific <b>cm:translation</b> to use as the starting point
     *                                 of the new edition.
     * @return                         Returns the <b>cm:mlContainer</b>
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"mlContainerNodeRef", "translationNodeRef"})
    NodeRef createEdition(NodeRef mlContainerNodeRef, NodeRef translationNodeRef);
}
