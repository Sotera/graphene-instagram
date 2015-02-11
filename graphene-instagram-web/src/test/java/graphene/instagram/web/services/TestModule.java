package graphene.instagram.web.services;

import graphene.instagram.dao.InstagramDAOModule;
import graphene.instagram.model.graphserver.GraphServerModule;
import graphene.util.PropertiesFileSymbolProvider;
import graphene.util.UtilModule;

import org.apache.tapestry5.internal.services.URLEncoderImpl;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.services.URLEncoder;
import org.slf4j.Logger;

@SubModule({ GraphServerModule.class, InstagramDAOModule.class, UtilModule.class })
public class TestModule {

	public static void bind(ServiceBinder binder) {
		binder.bind(URLEncoder.class, URLEncoderImpl.class);
	}

	public PropertiesFileSymbolProvider buildColorsSymbolProvider(Logger logger) {
		return new PropertiesFileSymbolProvider(logger,
				"graphene_optional_colors01.properties", true);
	}

	public static void contributeSymbolSource(
			OrderedConfiguration<SymbolProvider> configuration,
			@InjectService("ColorsSymbolProvider") SymbolProvider c) {
		configuration.add("ColorsPropertiesFile", c, "after:SystemProperties",
				"before:ApplicationDefaults");
	}


}