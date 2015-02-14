package graphene.instagram.dao;

import graphene.augment.snlp.services.SentimentAnalyzer;
import graphene.augment.snlp.services.SentimentAnalyzerImpl;
import graphene.business.commons.exception.DataAccessException;
import graphene.dao.CombinedDAO;
import graphene.dao.DAOModule;
import graphene.dao.DataSourceListDAO;
import graphene.dao.EntityDAO;
import graphene.dao.GroupDAO;
import graphene.dao.IconService;
import graphene.dao.LoggingDAO;
import graphene.dao.PermissionDAO;
import graphene.dao.RoleDAO;
import graphene.dao.StyleService;
import graphene.dao.TransactionDAO;
import graphene.dao.UserDAO;
import graphene.dao.UserGroupDAO;
import graphene.dao.UserWorkspaceDAO;
import graphene.dao.WorkspaceDAO;
import graphene.dao.annotations.EntityLightFunnelMarker;
import graphene.dao.es.ESRestAPIConnection;
import graphene.dao.es.ESRestAPIConnectionImpl;
import graphene.dao.es.JestModule;
import graphene.dao.es.LoggingDAODefaultESImpl;
import graphene.instagram.dao.impl.InstagramDataAccess;
import graphene.instagram.dao.impl.InstagramEntitySearch;
import graphene.instagram.dao.impl.GraphTraversalRuleServiceImpl;
import graphene.instagram.dao.impl.IconServiceImpl;
import graphene.instagram.dao.impl.es.CombinedDAOESImpl;
import graphene.instagram.dao.impl.es.DataSourceListDAOESImpl;
import graphene.instagram.dao.impl.es.ESUrlConstants;
import graphene.instagram.dao.impl.es.EntityDAOESImpl;
import graphene.instagram.dao.impl.es.GroupDAOESImpl;
import graphene.instagram.dao.impl.es.TransactionDAOESImpl;
import graphene.instagram.dao.impl.es.UserDAOESImpl;
import graphene.instagram.dao.impl.es.UserGroupDAOESImpl;
import graphene.instagram.dao.impl.es.UserWorkspaceDAOESImpl;
import graphene.instagram.dao.impl.es.WorkspaceDAOESImpl;
import graphene.instagram.model.funnels.InstagramEntityLightFunnel;
import graphene.hts.entityextraction.Extractor;
import graphene.hts.keywords.KeywordExtractorImpl;
import graphene.hts.sentences.SentenceExtractorImpl;
import graphene.model.funnels.Funnel;
import graphene.model.idl.G_DataAccess;
import graphene.model.idl.G_EntitySearch;
import graphene.services.SimplePermissionDAOImpl;
import graphene.services.SimpleRoleDAOImpl;
import graphene.services.StopWordService;
import graphene.services.StopWordServiceImpl;
import graphene.services.StyleServiceImpl;
import graphene.util.PropertiesFileSymbolProvider;
import graphene.util.net.HttpUtil;

import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Startup;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.ioc.services.ParallelExecutor;
import org.graphene.augment.mitie.MITIEModule;
import org.graphene.augment.mitie.dao.MitieDAO;
import org.graphene.augment.mitie.dao.MitieDAOImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Map the interfaces to the implementations you want to use. By default these
 * are singletons.
 * 
 * @author djue
 * 
 */
@SubModule({ JestModule.class, DAOModule.class, MITIEModule.class })
public class InstagramDAOModule {
	private static Logger logger = LoggerFactory.getLogger(InstagramDAOModule.class);

	public static void bind(final ServiceBinder binder) {
		binder.bind(GraphTraversalRuleService.class, GraphTraversalRuleServiceImpl.class);
		binder.bind(RoleDAO.class, SimpleRoleDAOImpl.class).eagerLoad();
		binder.bind(PermissionDAO.class, SimplePermissionDAOImpl.class).eagerLoad();
		binder.bind(EntityDAO.class, EntityDAOESImpl.class).eagerLoad();
		binder.bind(G_DataAccess.class, InstagramDataAccess.class);

		// Graphene-web needs this for the coercion model
		binder.bind(TransactionDAO.class, TransactionDAOESImpl.class).withId("Primary");

		// TODO: Make this into a service in the core we can contribute to (for
		// distributed configuration!)
		binder.bind(DataSourceListDAO.class, DataSourceListDAOESImpl.class).eagerLoad();
		binder.bind(StyleService.class, StyleServiceImpl.class);

		binder.bind(GroupDAO.class, GroupDAOESImpl.class).eagerLoad();
		binder.bind(WorkspaceDAO.class, WorkspaceDAOESImpl.class).eagerLoad();
		binder.bind(UserDAO.class, UserDAOESImpl.class).eagerLoad();
		binder.bind(UserGroupDAO.class, UserGroupDAOESImpl.class);
		binder.bind(UserWorkspaceDAO.class, UserWorkspaceDAOESImpl.class);

		binder.bind(CombinedDAO.class, CombinedDAOESImpl.class);
		binder.bind(ESRestAPIConnection.class, ESRestAPIConnectionImpl.class).eagerLoad();

		binder.bind(Extractor.class, KeywordExtractorImpl.class).withId("keyword");
		binder.bind(Extractor.class, SentenceExtractorImpl.class).withId("sentence");
		binder.bind(Funnel.class, InstagramEntityLightFunnel.class).withMarker(EntityLightFunnelMarker.class);

		binder.bind(MitieDAO.class, MitieDAOImpl.class);
		binder.bind(IconService.class, IconServiceImpl.class);
		binder.bind(StopWordService.class, StopWordServiceImpl.class);

		binder.bind(SentimentAnalyzer.class, SentimentAnalyzerImpl.class);

		binder.bind(LoggingDAO.class, LoggingDAODefaultESImpl.class).eagerLoad();
	}

	@Contribute(StopWordService.class)
	public static void contributeStopWords(final Configuration<String> stopwords) {
		stopwords.add("Test");
		stopwords.add("XX");
		stopwords.add("XX/XX");
		stopwords.add("XXX");
		stopwords.add("Unknown");
		stopwords.add("NULL NULL");
		stopwords.add("Unavailable");
		stopwords.add("lerequest@wellsfargo.com");
		stopwords.add("backupdocs@bankofamerica.com");
		stopwords.add("le.request@jpmchase.com");
		stopwords.add("all locations");
		stopwords.add("sarescalations@paypal.com");
		stopwords.add("OTHER");
		stopwords.add("NONE");
		stopwords.add("NONE NONE NONE");

		stopwords.add("supportingdocrequest@td.com");
		stopwords.add("figbackupdocs@bankofamerica.com");
	}

	@Startup
	public static void scheduleJobs(final ParallelExecutor executor, @Inject final UserDAO userDAO,
			@Inject final GroupDAO groupDAO, @Inject final WorkspaceDAO workspaceDAO, @Inject final UserGroupDAO ugDAO,
			@Inject final UserWorkspaceDAO uwDAO, @Inject final LoggingDAO lDAO) {

		executor.invoke(UserDAO.class, new Invokable<UserDAO>() {
			@Override
			public UserDAO invoke() {
				try {
					userDAO.initialize();
				} catch (final DataAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return userDAO;
			}
		});

		executor.invoke(GroupDAO.class, new Invokable<GroupDAO>() {
			@Override
			public GroupDAO invoke() {
				try {
					groupDAO.initialize();
				} catch (final DataAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return groupDAO;
			}
		});

		executor.invoke(WorkspaceDAO.class, new Invokable<WorkspaceDAO>() {
			@Override
			public WorkspaceDAO invoke() {
				try {
					workspaceDAO.initialize();
				} catch (final DataAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return workspaceDAO;
			}
		});

		executor.invoke(UserGroupDAO.class, new Invokable<UserGroupDAO>() {
			@Override
			public UserGroupDAO invoke() {
				try {
					ugDAO.initialize();
				} catch (final DataAccessException e) {
					e.printStackTrace();
				}
				return ugDAO;
			}
		});

		executor.invoke(UserWorkspaceDAO.class, new Invokable<UserWorkspaceDAO>() {
			@Override
			public UserWorkspaceDAO invoke() {
				try {
					uwDAO.initialize();
				} catch (final DataAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return uwDAO;
			}
		});
	}

	@Deprecated
	public G_EntitySearch buildInstagramEntitySearch(@Inject final ESRestAPIConnection connection) {
		final String authorizationEncoding = HttpUtil
				.getAuthorizationEncoding("bsauser", "bs@U$er41ife1".toCharArray());
		return new InstagramEntitySearch(connection, authorizationEncoding);
	}

	public PropertiesFileSymbolProvider buildTableNameSymbolProvider(final Logger logger) {
		return new PropertiesFileSymbolProvider(logger, "tablenames.properties", true);
	}

	public void contributeApplicationDefaults(final MappedConfiguration<String, String> configuration) {
		configuration.add(MITIEModule.ENABLED, "true");
		// Elastic Search defaults (if no es.properties file is provided)
		configuration.add(JestModule.ES_SERVER, ESUrlConstants.defaultHost);
		configuration.add(JestModule.ES_SEARCH_INDEX, ESUrlConstants.defaultIndex1);

		configuration.add(JestModule.ES_USER_INDEX, ESUrlConstants.defaultUserIndexName);
		configuration.add(JestModule.ES_GROUP_INDEX, ESUrlConstants.defaultGroupIndexName);
		configuration.add(JestModule.ES_WORKSPACE_INDEX, ESUrlConstants.defaultWorkspaceIndexName);
		configuration.add(JestModule.ES_USERWORKSPACE_INDEX, ESUrlConstants.defaultUserWorkspaceIndexName);
		configuration.add(JestModule.ES_USERGROUP_INDEX, ESUrlConstants.defaultUserGroupIndexName);
		configuration.add(JestModule.ES_LOGGING_INDEX, ESUrlConstants.defaultLoggingIndexName);
		configuration.add(JestModule.ES_PERSISTED_GRAPH_INDEX, ESUrlConstants.defaultPersistedGraphIndexName);
		configuration.add(JestModule.ES_PERSISTED_GRAPH_TYPE, ESUrlConstants.defaultPersistedGraphType);
		configuration.add(JestModule.ES_DEFAULT_TIMEOUT, "30s");

	}

}
