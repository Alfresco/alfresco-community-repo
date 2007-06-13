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
package org.alfresco.web.bean.ml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.EditionService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.wizard.BaseWizardBean;

/**
 * Wizard bean to create a new edition from an existing MLContainer.
 *
 * @author yanipig
 */
public class NewEditionWizard extends BaseWizardBean
{
    protected EditionService editionService;
    protected MultilingualContentService multilingualContentService;
    protected ContentFilterLanguagesService contentFilterLanguagesService;
    protected LockService lockService;

    protected NodeRef mlContainerToVersion;

    private List<SelectItem> selectableTranslations;
    private String startingItemNodeString;
    private String editionNotes;
    private boolean minorChange;
    private boolean otherProperties;
    private List<SelectItem> translationCheckedOut;
    private String language;
    private String title;
    private String author;
    private boolean hasTranslationCheckedOut;
    private NodeRef startingElement;

    @Override
    public void init(Map<String, String> parameters)
    {
       super.init(parameters);

       // reset the fileds

       startingItemNodeString = null;
       editionNotes = null;
       minorChange = true;
       otherProperties = false;
       translationCheckedOut = null;
       language = "lang";
       title = "title";
       author = "author";
       selectableTranslations = null;

       // set the mlContainer to version
       NodeRef currentNodeRef = this.browseBean.getDocument().getNodeRef();

       if(ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(nodeService.getType(currentNodeRef)))
       {
           mlContainerToVersion = currentNodeRef;
       }
       else
       {
           mlContainerToVersion = multilingualContentService.getTranslationContainer(currentNodeRef);
       }

       translationCheckedOut = getTranslationCheckedOut();
       hasTranslationCheckedOut = getHasTranslationCheckedOut();

    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        // fill the edition properties
        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(1, 1.0f);
        versionProperties.put(Version.PROP_DESCRIPTION, editionNotes);
        if (minorChange)
        {
            versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
        }
        else
        {
            versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
        }

        // create the edition and get the reference of the new starting translation
        NodeRef newPivot = editionService.createEdition(startingElement, versionProperties);

        if (otherProperties == true)
        {
            this.browseBean.setDocument(new Node(newPivot));
            outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + AlfrescoNavigationHandler.OUTCOME_SEPARATOR + AlfrescoNavigationHandler.DIALOG_PREFIX  + "setContentProperties";
        }
        else
        {
            outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + AlfrescoNavigationHandler.OUTCOME_SEPARATOR + "browse";
        }

        return outcome;
    }


    /**
     * Determines whether there are any translation checked out.
     *
     * @return true if there are translation checked out
     */
    public boolean getHasTranslationCheckedOut()
    {
        hasTranslationCheckedOut = getTranslationCheckedOut().size() > 0;

        return hasTranslationCheckedOut;
    }

    @Override
    public boolean getFinishButtonDisabled()
    {
        return super.getFinishButtonDisabled() || hasTranslationCheckedOut;
    }

    @Override
    public boolean getNextButtonDisabled()
    {
        return super.getNextButtonDisabled() || hasTranslationCheckedOut;
    }


    /**
     * Return the list of cecked out document found in the mlContainer.
     *
     * @return the list of checked out translation
     */
    public List<SelectItem> getTranslationCheckedOut()
    {
        if(translationCheckedOut == null )
        {
            // first call, init the list

            this.translationCheckedOut = new ArrayList<SelectItem>();

            // get all translations of the mlContainer
            Map<Locale, NodeRef> translations = multilingualContentService.getTranslations(mlContainerToVersion);

            // fill the select itms
            for(Map.Entry<Locale, NodeRef> entry : translations.entrySet())
            {
                NodeRef nodeRef = entry.getValue();

                if(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE))

                {
                    LockStatus lockStatus = lockService.getLockStatus(nodeRef);
                    if (lockStatus != LockStatus.NO_LOCK)
                    {
                        // if the node is locked, add it to the locked translation list
                        String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                        Locale lang = (Locale) nodeService.getProperty(nodeRef, ContentModel.PROP_LOCALE);
                        String lockOwner = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_WORKING_COPY_OWNER);

                        this.translationCheckedOut.add(new SelectItem(
                                "(" + lang.getLanguage() + ")",
                                name,
                                lockOwner
                            ));
                    }
                }
            }
        }

        return this.translationCheckedOut;
    }


    /**
     * Return the list of available translations to begin the starting translations of the new edition.
     *
     * @return the list of available translations
     */
    public List<SelectItem> getSelectableTranslations()
    {
        if(selectableTranslations == null)
        {
            // first call, init the list

            selectableTranslations = new ArrayList<SelectItem>();

            // get all translations of the mlContainer
            Map<Locale, NodeRef> translations = multilingualContentService.getTranslations(mlContainerToVersion);

            // fill the select items
            for(Map.Entry<Locale, NodeRef> entry : translations.entrySet())
            {
                NodeRef nodeRef = entry.getValue();

                //add each non empty translation
                if(!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
                {
                    String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                    Locale lang = (Locale) nodeService.getProperty(nodeRef, ContentModel.PROP_LOCALE);
                    selectableTranslations.add(new SelectItem(
                            nodeRef.toString(),
                            name + " - " + contentFilterLanguagesService.getLabelByCode(lang.getLanguage())
                        ));
                }
            }
        }

        return selectableTranslations;
    }




    /**
     * @param multilingualContentService the Multilingual Content Service to set
     */
    public void setMultilingualContentService(MultilingualContentService multilingualContentService)
    {
        this.multilingualContentService = multilingualContentService;
    }


    /**
     * @param nodeService the Node Service to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }


    /**
     * @param editionService the Edition Service to set
     */
    public void setEditionService(EditionService editionService)
    {
        this.editionService = editionService;
    }

    /**
     * @return the edition notes
     */
    public String getEditionNotes()
    {
        return editionNotes;
    }

    /**
     * @param editionNotes the edition notes to set
     */
    public void setEditionNotes(String editionNotes)
    {
        this.editionNotes = editionNotes;
    }

    /**
     * @return the minorChange get if the new edition is minor or not.
     */
    public boolean isMinorChange()
    {
        return minorChange;
    }

    /**
     * @param minorChange set if the new edition is minor or not.
     */
    public void setMinorChange(boolean minorChange)
    {
        this.minorChange = minorChange;
    }

    /**
     * @return the otherProperties, get if the edit details dialog must be set at the end of the wizard
     */
    public boolean isOtherProperties()
    {
        return otherProperties;
    }

    /**
     * @param otherProperties set as true, the edit details dialog must be set at the end of the wizard
     */
    public void setOtherProperties(boolean otherProperties)
    {
        this.otherProperties = otherProperties;
    }

    /**
     * @return the starting translation being the new pivot of tne new edition
     */
    public String getStartingItemNodeString()
    {
        return startingItemNodeString;
    }

    /**
     * @param startingItemNodeString the starting translation to set as the new pivot of tne new edition
     */
    public void setStartingItemNodeString(String startingItemNodeString)
    {
        // get the starting point translation with its id
        startingElement = new NodeRef(startingItemNodeString);

        // set the futur properties of the new starting element (only usefull for the summary step)
        setLanguage((Locale) nodeService.getProperty(startingElement, ContentModel.PROP_LOCALE));
        setAuthor((String) nodeService.getProperty(startingElement, ContentModel.PROP_AUTHOR));
        setTitle((String) nodeService.getProperty(startingElement, ContentModel.PROP_TITLE));

        this.startingItemNodeString = startingItemNodeString;
    }


    /**
     * @param contentFilterLanguagesService the Content Filter Languages Service to set
     */
    public void setContentFilterLanguagesService(ContentFilterLanguagesService contentFilterLanguagesService)
    {
        this.contentFilterLanguagesService = contentFilterLanguagesService;
    }


    /**
     * @param lockService the Lock Service to set
     */
    public void setLockService(LockService lockService)
    {
        this.lockService = lockService;
    }


    /**
     * @return the author
     */
    public String getAuthor()
    {
        return author;
    }


    /**
     * @param author the author to set
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }


    /**
     * @return the language
     */
    public String getLanguage()
    {
        return language;
    }


    /**
     * @param language the language to set
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(Locale locale)
    {
        this.language = locale.getLanguage();
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }


    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }


    /**
     * @return the versionLabel
     */
    public String getVersionLabel()
    {
        String toReturn = "Version Label";

        if(minorChange)
        {
            toReturn += " (minor change)";
        }

        return toReturn;
    }
}
