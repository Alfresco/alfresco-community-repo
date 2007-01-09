/*
 * Copyright (C) 2007 Alfresco, Inc.
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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
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
     * Rename an existing <b>sys:localized</b> by adding locale suffixes to the base name.
     * Where there are name clashes with existing documents, a numerical naming scheme will be
     * adopted.
     *
     * @param localizedNodeRef          An existing <b>sys:localized</b>
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"localizedNodeRef"})
    void renameWithMLExtension(NodeRef localizedNodeRef);

    /**
     * Make an existing document into a translation by adding the <b>cm:mlDocument</b> aspect and
     * creating a <b>cm:mlContainer</b> parent.  If it is already a translation, then nothing is done.
     *
     * @param contentNodeRef            An existing <b>cm:content</b>
     * @return                          Returns the <b>cm:mlContainer</b> translation parent
     * 
     * @see org.alfresco.model.ContentModel#ASPECT_MULTILINGUAL_DOCUMENT
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"contentNodeRef", "locale"})
    NodeRef makeTranslation(NodeRef contentNodeRef, Locale locale);

    /**
     * Make a translation out of an existing document.  The necessary translation structures will be created
     * as necessary.
     *
     * @param newTranslationNodeRef     An existing <b>cm:content</b>
     * @param translationOfNodeRef      An existing <b>cm:mlDocument</b> or <b>cm:mlContainer</b>
     * @return                          Returns the <b>cm:mlContainer</b> translation parent
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"newTranslationNodeRef", "translationOfNodeRef", "locale"})
    NodeRef addTranslation(NodeRef newTranslationNodeRef, NodeRef translationOfNodeRef, Locale locale);

    /**
     *
     * @return                          Returns the <b>cm:mlContainer</b> translation parent
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"translationNodeRef"})
    NodeRef getTranslationContainer(NodeRef translationNodeRef);

    /**
     * Create a new edition of an existing <b>cm:mlContainer</b>.
     *
     * @param mlContainerNodeRef        An existing <b>cm:mlContainer</b>
     * @param translationNodeRef        The specific <b>cm:mlDocument</b> to use as the starting point
     *                                  of the new edition.  All other translations will be removed.
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"mlContainerNodeRef", "translationNodeRef"})
    void createEdition(NodeRef mlContainerNodeRef, NodeRef translationNodeRef);

    /**
     * Gets the set of sibling translations associated with the given <b>cm:mlDocument</b> or
     * <b>cm:mlContainer</b>.
     * 
     * @param translationOfNodeRef      An existing <b>cm:mlDocument</b> or <b>cm:mlContainer</b>
     * @return                          Returns a map of translation nodes keyed by locale
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"translationOfNodeRef"})
    Map<Locale, NodeRef> getTranslations(NodeRef translationOfNodeRef);
    
    /**
     * Given a <b>cm:mlDocument</b>, this method attempts to find the best translation for the given
     * locale.  If there is not even a
     * {@link org.alfresco.i18n.I18NUtil#getNearestLocale(Locale, Set) partial match}, then <tt>null</tt>
     * is returned.
     * 
     * @param translationNodeRef        the <b>cm:mlDocument</b>
     * @param locale                    the target locale
     * @return Returns                  Returns the best match for the locale, or <tt>null</tt> if there
     *                                  is no near match.
     * 
     * @see #getTranslations(NodeRef)
     * @see org.alfresco.i18n.I18NUtil#getNearestLocale(Locale, Set)
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"translationNodeRef", "locale"})
    NodeRef getTranslationForLocale(NodeRef translationNodeRef, Locale locale);
}
