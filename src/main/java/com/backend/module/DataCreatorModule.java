package com.backend.module;

import com.backend.datacreator.*;
import com.google.inject.AbstractModule;

public class DataCreatorModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(DataCreator.class);

	}
}
