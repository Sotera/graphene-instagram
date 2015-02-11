package graphene.instagram.dao.impl.es;

import graphene.dao.UserWorkspaceDAO;
import graphene.dao.WorkspaceDAO;
import graphene.dao.es.BasicESDAO;
import graphene.dao.es.ESRestAPIConnection;
import graphene.dao.es.JestModule;
import graphene.model.graph.G_PersistedGraph;
import graphene.model.idl.G_Workspace;
import graphene.util.validator.ValidationUtils;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Count;
import io.searchbox.core.CountResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WorkspaceDAOESImpl extends BasicESDAO implements WorkspaceDAO {

	@Inject
	@Symbol(JestModule.ES_WORKSPACE_INDEX)
	private String indexName;
	@Inject
	@Symbol(JestModule.ES_PERSISTED_GRAPH_INDEX)
	private String persistedGraphIndexName;

	@Inject
	@Symbol(JestModule.ES_PERSISTED_GRAPH_TYPE)
	private String persistedGraphType;

	@Inject
	UserWorkspaceDAO uwDAO;

	public WorkspaceDAOESImpl(final ESRestAPIConnection c,
			final JestClient jestClient, final Logger logger) {
		auth = null;
		this.c = c;
		this.jestClient = jestClient;
		mapper = new ObjectMapper(); // can reuse, share globally
		this.logger = logger;

	}

	@Override
	public long countWorkspaces(final String partialName) {
		final String query = new SearchSourceBuilder().query(
				QueryBuilders.wildcardQuery("title", "*" + partialName + "*"))
				.toString();
		try {
			final CountResult result = jestClient.execute(new Count.Builder()
					.query(query).addIndex(indexName).addType(type).build());
			return result.getCount().longValue();
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public long countWorkspaces(final String id, final String partialName) {
		final List<G_Workspace> workspacesForUser = uwDAO
				.getWorkspacesForUser(id);
		long count = 0;
		for (final G_Workspace w : workspacesForUser) {
			if (StringUtils.containsIgnoreCase(partialName, w.getTitle())) {
				count++;
			}
		}
		return count;
	}

	@Override
	public List<G_Workspace> findWorkspaces(final String partialName,
			final int offset, final int limit) {
		if (ValidationUtils.isValid(partialName)) {
			final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder.query(QueryBuilders.fuzzyQuery("title",
					partialName));

			final Search search = new Search.Builder(
					searchSourceBuilder.toString()).addIndex(indexName)
					.addType(type).build();
			System.out.println(searchSourceBuilder.toString());
			JestResult result;
			List<G_Workspace> returnValue = new ArrayList<G_Workspace>(0);
			try {
				result = jestClient.execute(search);
				returnValue = result.getSourceAsObjectList(G_Workspace.class);
				for (final G_Workspace u : returnValue) {
					System.out.println(u);
				}
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return returnValue;
		} else {
			return getAllWorkspaces();
		}
	}

	@Override
	public List<G_Workspace> findWorkspaces(final String userId,
			final String partialName, final int offset, final int limit) {
		if (ValidationUtils.isValid(userId)) {
			final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			if (ValidationUtils.isValid(partialName)) {
				// use the partial name to filter
				searchSourceBuilder.query(QueryBuilders.fuzzyQuery("title",
						partialName));
			} else {
				// don't filter on name, get all of them.
				searchSourceBuilder.query(QueryBuilders.matchAllQuery());
			}
			final Search search = new Search.Builder(
					searchSourceBuilder.toString()).addIndex(indexName)
					.addType(type).build();
			System.out.println(searchSourceBuilder.toString());
			JestResult result;
			List<G_Workspace> returnValue = new ArrayList<G_Workspace>(0);
			try {
				result = jestClient.execute(search);
				returnValue = result.getSourceAsObjectList(G_Workspace.class);
				for (final G_Workspace u : returnValue) {
					System.out.println(u);
				}
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return returnValue;
		} else {
			return getAllWorkspaces();
		}
	}

	@Override
	public List<G_Workspace> getAllWorkspaces() {
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());

		final Search search = new Search.Builder(searchSourceBuilder.toString())
				.addIndex(indexName).addType(type).build();
		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		List<G_Workspace> returnValue = new ArrayList<G_Workspace>(0);
		try {
			result = jestClient.execute(search);
			returnValue = result.getSourceAsObjectList(G_Workspace.class);
			for (final G_Workspace u : returnValue) {
				System.out.println(u);
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnValue;
	}

	@Override
	public G_Workspace getById(final String id) {
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchQuery("_id", id));

		final Search search = new Search.Builder(searchSourceBuilder.toString())
				.addIndex(indexName).addType(type).build();
		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		G_Workspace resultObject = null;
		try {
			result = jestClient.execute(search);
			resultObject = result.getSourceAsObject(G_Workspace.class);
			resultObject.setId(id);
			System.out.println(resultObject);

		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultObject;
	}

	@Override
	public G_PersistedGraph getExistingGraph(final String graphSeed,
			final String userName, final String timeStamp) {
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// For all users at this point!!!
		searchSourceBuilder.query(QueryBuilders.matchQuery("graphSeed",
				graphSeed));

		final Search search = new Search.Builder(searchSourceBuilder.toString())
				.addIndex(persistedGraphIndexName).addType(persistedGraphType)
				.build();
		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		G_PersistedGraph resultObject = null;
		try {
			result = jestClient.execute(search);
			if (result != null) {
				System.out.println(result);
				resultObject = result.getSourceAsObject(G_PersistedGraph.class);
				if (resultObject != null) {
					resultObject.setId((String) result.getValue("_id"));
					System.out.println(resultObject);
				} else {
					logger.error("No result was found for query.");
				}
			} else {
				logger.error("Existing Graph Result was null!");
			}
		} catch (final Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return resultObject;
	}

	// @PostInjection
	@Override
	public void initialize() {
		setIndex(indexName);
		setType("workspace");
		super.initialize();
	}

	@Override
	public G_Workspace save(final G_Workspace g) {
		G_Workspace returnVal = g;
		returnVal.setModified(getModifiedTime());
		Index index;
		if (!ValidationUtils.isValid(returnVal.getId())) {
			// auto id
			index = new Index.Builder(returnVal).index(indexName).type(type)
					.build();
		} else {
			index = new Index.Builder(returnVal).index(indexName)
					.id(returnVal.getId()).type(type).build();
		}
		try {

			final JestResult result = jestClient.execute(index);
			if (!ValidationUtils.isValid(returnVal.getId())
					&& ValidationUtils.isValid(result.getValue("_id"))) {
				returnVal.setId((String) result.getValue("_id"));
				// this should only happen once
				returnVal = save(returnVal);
			}
		} catch (ExecutionException | InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnVal;
	}

	@Override
	public G_PersistedGraph saveGraph(final G_PersistedGraph pg) {
		final G_PersistedGraph returnVal = pg;
		returnVal.setModified(getModifiedTime());
		Index index;
		// This makes it unique only to the seed
		returnVal.setId(pg.getGraphSeed());
		index = new Index.Builder(returnVal).index(persistedGraphIndexName)
				.id(returnVal.getId()).type(persistedGraphType).build();
		try {
			final JestResult result = jestClient.execute(index);
			System.out.println(result.getJsonString());
		} catch (ExecutionException | InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnVal;
	}

}
