package org.alfresco.web.templating.xforms.flux;

import org.chiba.xml.util.DOMUtil;
import org.chiba.xml.xforms.XFormsConstants;
import org.chiba.xml.xforms.events.XFormsEvent;
import org.chiba.xml.xforms.events.XFormsEventFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * EventLog logs all events happening in XForms processor and build a DOM
 * document which represent those events.
 *
 * @author Joern Turner
 * @version $Id: EventLog.java,v 1.6 2005/12/21 22:59:27 unl Exp $
 */
public class EventLog {
    private static List HELPER_ELEMENTS = 
	Arrays.asList(new String[] 
	    {
		XFormsConstants.LABEL, 
		XFormsConstants.HELP, 
		XFormsConstants.HINT, 
		XFormsConstants.ALERT, 
		XFormsConstants.VALUE
	    });

    private static List SELECTOR_ELEMENTS = 
	Arrays.asList(new String[]
	    {
		XFormsConstants.SELECT1, 
		XFormsConstants.SELECT
	    });

    private final Document doc;
    private final Element root;
    private Element selector;

    public EventLog() {
        this.doc = DOMUtil.newDocument(false, false);
        this.root = this.doc.createElement("eventlog");
        this.root.setAttribute("id", "eventlog");
        this.doc.appendChild(this.root);
    }

    public Element getLog() {
        return (Element) this.root.cloneNode(true);
    }

    public void add(XFormsEvent event) {
        // get target properties
        String type = event.getType();
        Element target = (Element) event.getTarget();
        String targetId = target.getAttributeNS(null, "id");
        String targetName = target.getLocalName();

        // create event element
        Element element;

        if (XFormsEventFactory.CHIBA_STATE_CHANGED.equals(type) && SELECTOR_ELEMENTS.contains(targetName)) {
            // selector events are always appended to the end of the log
            // to ensure their items' labels and values are updated before
            element = insert(null, type, targetId, targetName);
            if (this.selector == null)
                this.selector = element;
        }
        else 
	{
            // all other events are inserted before any selector events
            element = insert(this.selector, type, targetId, targetName);
        }

        if (XFormsEventFactory.CHIBA_STATE_CHANGED.equals(type) && HELPER_ELEMENTS.contains(targetName)) 
	{
            // parent id is needed for updating all helper elements cause they
            // are identified by '<parentId>-label' etc. rather than their own id
            String parentId = ((Element) target.getParentNode()).getAttributeNS(null, "id");
            addProperty(element, "parentId", parentId);
        }

        // add event params
        Iterator iterator = event.getPropertyNames().iterator();
        while (iterator.hasNext()) 
	{
            String name = (String) iterator.next();
            addProperty(element, name, event.getContextInfo(name).toString());
        }
    }

    public Element add(String type, String targetId, String targetName){
        return insert(this.selector, type, targetId, targetName);
    }

    public Element addProperty(Element element, String name, String value) {
        Element property = this.doc.createElement("property");
        property.setAttribute("name", name);
        property.appendChild(this.doc.createTextNode(value));
        element.appendChild(property);

        return element;
    }

    private Element insert(Element ref, String type, String targetId, String targetName)
    {
        // create event element
        Element element = this.doc.createElement("event");
        this.root.insertBefore(element, ref);

        // add target properties
        element.setAttribute("type", type);
        element.setAttribute("targetId", targetId);
        element.setAttribute("targetName", targetName);
        return element;
    }


    // clean the log
    public void flush() 
    {
        DOMUtil.removeAllChildren(this.root);
        this.selector = null;
    }
}
