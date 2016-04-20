
package org.alfresco.repo.virtual.bundle;

import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.AlfrescoCollator;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.I18NUtil;

public class FileInfoPropsComparator implements Comparator<FileInfo>
{
    private List<Pair<QName, Boolean>> sortProps;

    private Collator collator;

    public static final String IS_FOLDER = "IS_FOLDER";

    public FileInfoPropsComparator(List<Pair<QName, Boolean>> sortProps)
    {
        this.sortProps = sortProps;
        this.collator = AlfrescoCollator.getInstance(I18NUtil.getContentLocale());
    }

    @Override
    public int compare(FileInfo n1, FileInfo n2)
    {
        return compareImpl(n1,
                           n2,
                           sortProps);
    }

    private int compareImpl(FileInfo node1In, FileInfo node2In, List<Pair<QName, Boolean>> sortProps)
    {
        Object pv1 = null;
        Object pv2 = null;

        QName sortPropQName = (QName) sortProps.get(0).getFirst();
        boolean sortAscending = sortProps.get(0).getSecond();

        FileInfo node1 = node1In;
        FileInfo node2 = node2In;

        if (sortAscending == false)
        {
            node1 = node2In;
            node2 = node1In;
        }

        int result = 0;

        pv1 = node1.getProperties().get(sortPropQName);
        pv2 = node2.getProperties().get(sortPropQName);

        if (sortPropQName.getLocalName().equals(IS_FOLDER))
        {
            pv1 = node1.isFolder();
            pv2 = node2.isFolder();
        }

        if (pv1 == null)
        {
            if (pv2 == null && sortProps.size() > 1)
            {
                return compareImpl(node1In,
                                   node2In,
                                   sortProps.subList(1,
                                                     sortProps.size()));
            }
            else
            {
                return (pv2 == null ? 0 : -1);
            }
        }
        else if (pv2 == null)
        {
            return 1;
        }

        if (pv1 instanceof String)
        {
            result = collator.compare((String) pv1,
                                      (String) pv2); // TODO: use collation keys
                                                     // (re: performance)
        }
        else if (pv1 instanceof Date)
        {
            result = (((Date) pv1).compareTo((Date) pv2));
        }
        else if (pv1 instanceof Long)
        {
            result = (((Long) pv1).compareTo((Long) pv2));
        }
        else if (pv1 instanceof Integer)
        {
            result = (((Integer) pv1).compareTo((Integer) pv2));
        }
        else if (pv1 instanceof QName)
        {
            result = (((QName) pv1).compareTo((QName) pv2));
        }
        else if (pv1 instanceof Boolean)
        {
            result = (((Boolean) pv1).compareTo((Boolean) pv2));
        }
        else
        {
            throw new RuntimeException("Unsupported sort type: " + pv1.getClass().getName());
        }

        if ((result == 0) && (sortProps.size() > 1))
        {
            return compareImpl(node1In,
                               node2In,
                               sortProps.subList(1,
                                                 sortProps.size()));
        }

        return result;
    }
}