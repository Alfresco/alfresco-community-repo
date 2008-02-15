package org.alfresco.web.bean.ml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.web.bean.repository.MapNode;

/**
 * Util class which represents a light weight representation of an edition history for the
 * client side needs.
 *
 * @author pignoya
 */
public class SingleEditionBean implements Serializable
{
   private static final long serialVersionUID = 9145202732094403340L;

   /** The edition in a list */
    private List<MapNode> edition = null;

    /** The translation list of the edition */
    private List<MapNode> translations = null;

    /**
     * @return
     */
    public String getEditionLabel()
    {
        return (String) this.getEdition().get(0).get("editionLabel");
    }

    /**
     * @return the edition
     */
    public List<MapNode> getEdition()
    {
        return edition;
    }

    /**
     * @param edition the edition to set
     */
    public void setEdition(MapNode edition)
    {
        this.edition = new ArrayList<MapNode>(1);
        translations = null;

        this.edition.add(edition);
    }

    /**
     * @return the translations
     */
    public List<MapNode> getTranslations()
    {
        return translations;
    }

    /**
     * @param translation the translations to add to the list
     */
    public void addTranslations(MapNode translation)
    {
        if(this.translations == null)
        {
            this.translations = new ArrayList<MapNode>();
        }

        this.translations.add(translation);
    }
}


