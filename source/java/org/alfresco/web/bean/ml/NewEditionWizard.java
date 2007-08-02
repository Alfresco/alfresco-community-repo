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
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.alfresco.i18n.I18NUtil;
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
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.alfresco.web.ui.common.component.UIActionLink;

/**
 * Wizard bean to create a new edition from an existing MLContainer.
 *
 * @author Yanick Pignot
 */
public class NewEditionWizard extends BaseWizardBean
{
    public static final String ID_MESSAGE_MINOR_CHANGE = "minor_change";
    public static final String ID_MESSAGE_MAJOR_CHANGE = "major_change";

    protected EditionService editionService;
    protected MultilingualContentService multilingualContentService;
    protected ContentFilterLanguagesService contentFilterLanguagesService;
    protected LockService lockService;
    protected VersionService versionService;

    protected NodeRef mlContainerToVersion;

    private List<TranslationWrapper> selectableTranslations;
    private List<TranslationWrapper> translationsCheckedOut;

    private String editionNotes;
    private boolean minorChange;
    private boolean otherProperties;
    private boolean skipFirstStep;
    private String language;
    private String title;
    private boolean hasTranslationCheckedOut;
    private NodeRef startingElement;
    private String author;
    private String selectedLanguage;


    @Override
    public void init(Map<String, String> parameters)
    {
       super.init(parameters);

       // reset the fileds
       editionNotes = null;
       minorChange = true;
       otherProperties = false;
       translationsCheckedOut = null;
       selectableTranslations = null;


       if(!skipFirstStep)
       {
           // this properties is set by the skipFirstStep action event method.

           language = null;
           title = null;

           // set the current mlContainer
           setMLContainer();
       }

       // init the list of the available translations and the list of translations checked out
       initTranslationList();

       // true if they are at leat one translation checked out
       hasTranslationCheckedOut = this.translationsCheckedOut.size() > 0;
    }

    /**
     * Force the the lang of the new pivot translation for the new edition and skip the first step
     */
    public void skipFirstStep(ActionEvent event)
    {
       skipFirstStep = true;

       // Get the lang of the new
       UIActionLink link = (UIActionLink)event.getComponent();
       Map<String, String> params = link.getParameterMap();
       String lang = params.get("lang");

       // test if the parameter is valid
       ParameterCheck.mandatoryString("The lang of the node", lang);

       // set the current mlContainer
       setMLContainer();

       setStartingItem(lang.toLowerCase());
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

        // redirect the user at the 'modify translation properties' page if desire.
        if (otherProperties == true)
        {
            this.browseBean.setDocument(new Node(newPivot));
            outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + AlfrescoNavigationHandler.OUTCOME_SEPARATOR + AlfrescoNavigationHandler.DIALOG_PREFIX  + "setContentProperties";
        }
        else
        {
            outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + AlfrescoNavigationHandler.OUTCOME_SEPARATOR + "browse";
        }

        skipFirstStep = false;

        return outcome;
    }

    /**
     * Returns the properties for checked out translations JSF DataModel
     *
     * @return JSF DataModel representing the translations checked out
     */
    public DataModel getTranslationsCheckedOutDataModel()
    {
        return getDataModel(this.translationsCheckedOut);
    }

    /**
     * Returns the properties for available translations JSF DataModel
     *
     * @return JSF DataModel representing the available translation for a new edition
     */
    public DataModel getAvailableTranslationsDataModel()
    {
        return getDataModel(this.selectableTranslations);
    }

    /**
     * Determines whether there are any translation checked out.
     *
     * @return true if there are translation checked out
     */
    public boolean getHasTranslationCheckedOut()
    {
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
     * @param language of the starting translation to set as the new pivot of the new edition
     */
    private void setStartingItem(String language)
    {
        // get the starting point translation with its locale
        startingElement = multilingualContentService.getTranslationForLocale(mlContainerToVersion, I18NUtil.parseLocale(language));

        // set the futur properties of the new starting element (only usefull for the summary step)
        setLanguage(language);
        setTitle((String) nodeService.getProperty(startingElement, ContentModel.PROP_TITLE));
        setAuthor((String) nodeService.getProperty(startingElement, ContentModel.PROP_AUTHOR));
    }

    /**
     * Util metho which construct a data model with rows passed in parameter
     */
    private DataModel getDataModel(Object rows)
    {
        DataModel translationDataModel = new ListDataModel();

        translationDataModel.setWrappedData(rows);

        return translationDataModel;
    }

    /**
     * Util method which init the lists of translations
     */
    private void initTranslationList()
    {
        this.translationsCheckedOut = new ArrayList<TranslationWrapper>();
        this.selectableTranslations = new ArrayList<TranslationWrapper>();

        // get all translations of the mlContainer
        Map<Locale, NodeRef> translations = multilingualContentService.getTranslations(mlContainerToVersion);

        // fill the data table rows list
        for(Map.Entry<Locale, NodeRef> entry : translations.entrySet())
        {
            NodeRef nodeRef = entry.getValue();

            String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            String langCode = ((Locale) nodeService.getProperty(nodeRef, ContentModel.PROP_LOCALE)).getLanguage();
            String langText = contentFilterLanguagesService.getLabelByCode(langCode);
            String lockOwner = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_OWNER);

            // create the row with the current translation
            TranslationWrapper wrapper = new TranslationWrapper(name, langCode, lockOwner, langText);

            if(!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
            {
                // add it to the selectable list if it is not empty
                this.selectableTranslations.add(wrapper);
            }

            if(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE))
            {
                LockStatus lockStatus = lockService.getLockStatus(nodeRef);
                if (lockStatus != LockStatus.NO_LOCK)
                {
                    // if the node is locked, add it to the locked translation list
                    this.translationsCheckedOut.add(wrapper);
                }
            }

        }
    }

    private void setMLContainer()
    {
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

        if(!skipFirstStep)
        {
            // init the pivot language (it will be selected by default)
            selectedLanguage = ((Locale) nodeService.getProperty(mlContainerToVersion, ContentModel.PROP_LOCALE)).getLanguage();
        }
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
     * @return the author
     */
    public String getAuthor()
    {
        return author;
    }


    /**
     * @return the language
     */
    public String getLanguage()
    {
        return contentFilterLanguagesService.getLabelByCode(language);
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
     * @param author the author to set
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }


    /**
     * @return the versionLabel
     */
    public String getVersionLabel()
    {
        String label = versionService.getCurrentVersion(mlContainerToVersion).getVersionLabel();

        String nextLabel = null;

        try
        {
            int dotPosition = label.indexOf('.');

            int major = Integer.parseInt(label.substring(0, dotPosition));
            int minor = Integer.parseInt(label.substring(dotPosition + 1));

            if(minorChange)
            {
                nextLabel = major + "." + (minor + 1);
            }
            else
            {
                nextLabel = (major + 1) + ".0";
            }
        }
        catch(Exception e)
        {
            nextLabel = "";
        }

        if(minorChange)
        {
            String minorString = Application.getMessage(
                                        FacesContext.getCurrentInstance(),
                                        ID_MESSAGE_MINOR_CHANGE);

            return nextLabel + " (" + minorString + ')' ;
        }
        else
        {
            return nextLabel;
        }
    }



    /**
     * @return the selectedTranslationLanguage
     */
    public String getSelectedTranslationLanguage()
    {
        return selectedLanguage;
    }

    /**
     * @param selectedTranslationLanguage the selectedTranslationLanguage to set
     */
    public void setSelectedTranslationLanguage(String language)
    {
        // only the selected language is not set as null
        if(language != null)
        {
            setStartingItem(language);
        }

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
     * @param editionService the Edition Service to set
     */
    public void setEditionService(EditionService editionService)
    {
        this.editionService = editionService;
    }

    /**
     * @param versionService the version Service to set
     */
    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }


    /**
     * Simple wrapper class to represent a translation in the data table
     */
    public static class TranslationWrapper
    {
        private String language;
        private String name;
        private String checkedOutBy;
        private String languageLabel;

        public TranslationWrapper(String name, String language, String checkedOutBy, String languageLabel)
        {
            this.name = name;
            this.language = language;
            this.checkedOutBy = checkedOutBy;
            this.languageLabel = languageLabel;
        }

        public String getCheckedOutBy()
        {
            return checkedOutBy;
        }

        public String getLanguage()
        {
            return language;
        }

        public String getName()
        {
            return name;
        }

        public String getLanguageLabel()
        {
            return this.languageLabel;
        }
    }
}
