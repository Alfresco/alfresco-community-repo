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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileNotFoundException;
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
     * Checks whether an existing document is part of a translation group.
     *
     * @param contentNodeRef            An existing <b>cm:content</b>
     * @return                          Returns <tt>true</tt> if the document has a <b>cm:mlContainer</b> parent
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"contentNodeRef"})
    boolean isTranslation(NodeRef contentNodeRef);

    /**
     * Make an existing document into a translation by adding the <b>cm:mlDocument</b> aspect and
     * creating a <b>cm:mlContainer</b> parent.  If it is already a translation, then nothing is done.
     *
     * @param contentNodeRef            An existing <b>cm:content</b>
     *
     * @see org.alfresco.model.ContentModel#ASPECT_MULTILINGUAL_DOCUMENT
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"contentNodeRef", "locale"})
    void makeTranslation(NodeRef contentNodeRef, Locale locale);

    /**
     * Removes the node from any associated translations.  If the translation is the
     * pivot translation, then the entire set of translations will be unhooked.
     *
     * @param translationNodeRef        an existing <b>cm:mlDocument</b>
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"translationNodeRef"})
    void unmakeTranslation(NodeRef translationNodeRef);

    /**
     * Make a translation out of an existing document.  The necessary translation structures will be created
     * as necessary.
     *
     * @param newTranslationNodeRef     An existing <b>cm:content</b>
     * @param translationOfNodeRef      An existing <b>cm:mlDocument</b>
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"newTranslationNodeRef", "translationOfNodeRef", "locale"})
    void addTranslation(NodeRef newTranslationNodeRef, NodeRef translationOfNodeRef, Locale locale);

    /**
     * Convenience method for super user.
     *
     * @param translationNodeRef        An existing <b>cm:mlDocument</b>
     * @return                          Returns the <b>cm:mlContainer</b> translation parent
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"translationNodeRef"})
    NodeRef getTranslationContainer(NodeRef translationNodeRef);

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
     * {@link org.alfresco.i18n.I18NUtil#getNearestLocale(Locale, Set) partial match}, then the
     * {@link #getPivotTranslation(NodeRef) pivot translation} is used.  If that also gives no results
     * then the translation itself is returned.
     *
     * @param translationNodeRef        the <b>cm:mlDocument</b>
     * @param locale                    the target locale
     * @return                          Returns the best match for the locale (never <tt>null</tt>)
     *
     * @see #getTranslations(NodeRef)
     * @see org.alfresco.i18n.I18NUtil#getNearestLocale(Locale, Set)
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"translationNodeRef", "locale"})
    NodeRef getTranslationForLocale(NodeRef translationNodeRef, Locale locale);


    /**
     * Given a <b>cm:mlDocument</b> or <b>cm:mlContainer</b> this node returns each locale for
     * which there isn't a translation.
     *
     * @param localizedNodeRef          the <b>cm:mlDocument</b> or <b>cm:mlContainer</b>
     * @param addThisNodeLocale         if true, add the locale of the given <b>cm:mlDocument</b> in the list.
     * @return                          Returns a list of missng locales
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"localizedNodeRef", "addThisNodeLocale"})
    List<Locale> getMissingTranslations(NodeRef localizedNodeRef, boolean addThisNodeLocale);

    /**
     * Given any node, this returns the pivot translation.  All multilingual documents belong to
     * a group linked by a hidden parent node of type <b>cm:mlContainer</b>.  The pivot language
     * for the translations is stored on the parent, and the child that has the same locale is the
     * pivot translation.
     *
     * @param nodeRef       a <b>cm:mlDocument</b> translation or <b>cm:mlContainer</b> translation
     *                      container
     * @return              Returns a corresponding <b>cm:mlDocument</b> that matches the locale of
     *                      of the <b>cm:mlContainer</b>.  <tt>null</tt> is returned if there is no
     *                      pivot translation.
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    NodeRef getPivotTranslation(NodeRef nodeRef);

    /**
     * Make a empty translation out of an existing pivot translation.  The given translation or
     * container will be used to find the pivot translation.  Failing this, the given translation
     * will be used directly.  If no name is provided or if the name is the same as the original's
     * then the locale will be added to the main portion of the filename, i.e.
     * <pre>
     *    Document.txt --> Document_fr.txt
     * </pre>
     * <p/>
     * The necessary translation structures will be created as necessary.
     *
     * @param translationOfNodeRef      An existing <b>cm:mlDocument</b>
     * @param name                      The name of the file to create, or <tt>null</tt> to use
     *                                  the default naming convention.
     * @return Returns                  the new created <b>cm:mlEmptyTranslation</b>
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"translationOfNodeRef", "name", "locale"})
    NodeRef addEmptyTranslation(NodeRef translationOfNodeRef, String name, Locale locale);

    /**
     * Copies the given <b>cm:mlContainer</b>.
     * <p>
     * This involves the copy of the <b>cm:mlContainer</b> node and the copy of its <b>cm:mlDocument</b>.
     * <p>
     *
     * @param translationNodeRef            The <b>cm:mlContainer</b> to copy
     * @param newParentRef                  The new parent of the copied <b>cm:mlDocument</b>
     * @param prefixName                    The prefix of the name of the copied translations. Can be null.
     * @return                              The copied <b>cm:mlContainer</b>
     * @throws FileNotFoundException
     * @throws FileExistsException
     * @throws Exception
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"mlContainerNodeRef", "newParentRef"})
    NodeRef copyTranslationContainer(NodeRef mlContainerNodeRef, NodeRef newParentRef, String prefixName) throws FileExistsException, FileNotFoundException, Exception;

    /**
     * Moves the location of the given <b>cm:mlContainer</b>.
     * <p>
     * This not involves changing the <b>cm:mlContainer</b> node but moves its <b>cm:mlDocument</b>.
     * <p>
     *
     * @param translationNodeRef            The <b>cm:mlContainer</b> to move
     * @param newParentRef                  The new parent of the moved <b>cm:mlDocument</b>
     * @throws FileExistsException
     * @throws FileNotFoundException
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"mlContainerNodeRef", "newParentRef"})
    void moveTranslationContainer(NodeRef mlContainerNodeRef, NodeRef newParentRef) throws FileExistsException, FileNotFoundException;

    /**
     * Delete the given mlContainer and its translations. The translations will lost their <b>cm:mlDocument</b> aspect and
     * will be archved. The empty translations will be permanently deleted.
     *
     * @param mlContainerNodeRef        The <b>cm:mlContainer</b> to remove
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"mlContainerNodeRef"})
    void deleteTranslationContainer(NodeRef mlContainerNodeRef);
}
