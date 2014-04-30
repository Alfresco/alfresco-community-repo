package org.alfresco.repo.web.util;

import org.springframework.context.ConfigurableApplicationContext;

public interface JettyComponent
{
	int getPort();
	ConfigurableApplicationContext getApplicationContext();
	void start();
	void shutdown();
}
