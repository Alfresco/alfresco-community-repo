package org.alfresco.repo.domain.patch.ibatis;

import java.util.List;

public class IdListOfIdsParam
{
    private Long id;
    private List<Long> listOfIds;
    
    public IdListOfIdsParam()
    {
    }
    
    public Long getId()
    {
        return id;
    }
    public void setId(Long id)
    {
        this.id = id;
    }
    public List<Long> getListOfIds()
    {
        return listOfIds;
    }
    public void setListOfIds(List<Long> listOfIds)
    {
        this.listOfIds = listOfIds;
    }
}
