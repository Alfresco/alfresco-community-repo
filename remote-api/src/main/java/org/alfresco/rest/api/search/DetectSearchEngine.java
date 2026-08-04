package org.alfresco.rest.api.search;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class DetectSearchEngine implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event){

    }

    private static SearchEngineInfo searchEngineInfo(){
        // make http request to find search engine info
        return new SearchEngineInfo();
    }
}
