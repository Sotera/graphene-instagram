package graphene.instagram.web.services;

import graphene.instagram.dao.InstagramDAOModule;
import graphene.instagram.model.graphserver.GraphServerModule;
import graphene.util.UtilModule;

import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.services.TapestryModule;

@SubModule({ TapestryModule.class, AppModule.class, GraphServerModule.class, InstagramDAOModule.class, UtilModule.class })
public class TestModule {

}