package graphene.instagram.dao.impl.es;

public class ESUrlConstants {
	public static final String defaultAuth = "bsauser:bs@U$er41ife";
	public static final String oldhost = "http://lesnet-solr3/es/";
	public static final String defaultHost = "http://localhost:9200/";
	public static final String defaultIndex1 = "instagram";
	// public static final String urlBase = defaultHost + defaultIndex1;

	public static final String search = "/_search?q=";
	public static final String count = "/_count?q=";
	public static final String mapping = "/_mapping";
	public static final String typeAll = "";
	public static final String typeMedia = "media";
	public static final String typeUser = "user";
	public static final String urlStatsAll = defaultHost + "_stats";

	// public static final String searchAll = urlBase + search;
	// public static final String searchBSAR = urlBase + typeBSAR + search;
	// public static final String searchBCTR = urlBase + typeBCTR + search;
	// public static final String search8300 = urlBase + type8300 + search;
	// public static final String searchCMIR = urlBase + typeCMIR + search;
	// public static final String searchSARDI = urlBase + typeSARDI + search;
	// public static final String searchSARC = urlBase + typeSARC + search;
	// public static final String searchSARMSB = urlBase + typeSARMSB + search;
	//
	// public static final String countAll = urlBase + typeAll + count;
	// public static final String countBSAR = urlBase + typeBSAR + count;
	// public static final String countBCTR = urlBase + typeBCTR + count;
	// public static final String count8300 = urlBase + type8300 + count;
	// public static final String countCMIR = urlBase + typeCMIR + count;
	// public static final String countSARDI = urlBase + typeSARDI + count;
	// public static final String countSARC = urlBase + typeSARC + count;
	// public static final String countSARMSB = urlBase + typeSARMSB + count;

	public static final String mappingAll = defaultHost + mapping;
	public static final String mappingMedia = defaultHost + defaultIndex1
			+ typeMedia + mapping;
	public static final String mappingUser = defaultHost + defaultIndex1
			+ typeUser + mapping;

	public static final String fieldAll = "_all";

	/**
	 * These are the default ES indicies if no es.properties file or overriding
	 * system/catalina property is provided. Note that elastic search index
	 * names MUST be lower case!!!!
	 */
	public static final String defaultUserIndexName = "grapheneuser";
	public static final String defaultGroupIndexName = "graphenegroup";
	public static final String defaultWorkspaceIndexName = "grapheneworkspace";
	public static final String defaultPersistedGraphIndexName = "graphenepersistedgraphs";
	public static final String defaultPersistedGraphType = "csgraph";

	public static String defaultUserWorkspaceIndexName = "grapheneuserworkspace";
	public static String defaultUserGroupIndexName = "grapheneusergroup";
	public static String defaultLoggingIndexName = "graphenelogging";

}
