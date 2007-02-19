/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.ml;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The API to manage multilingual content and related structures.
 * 
 * @author Derek Hulley
 * @author Philippe Dubois
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
     * Create a new edition of an existing <b>cm:mlContainer</b> using any one of the
     * associated <b>cm:mlDocument</b> transalations.
     *
     * @param translationNodeRef        The specific <b>cm:mlDocument</b> to use as the starting point
     *                                  of the new edition.  All other translations will be removed.
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"translationNodeRef"})
    void createEdition(NodeRef translationNodeRef);

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
