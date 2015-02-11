package graphene.instagram.dao.impl.es;

import graphene.dao.UserDAO;
import graphene.dao.UserWorkspaceDAO;
import graphene.dao.WorkspaceDAO;
import graphene.dao.es.BasicESDAO;
import graphene.dao.es.ESRestAPIConnection;
import graphene.dao.es.JestModule;
import graphene.model.idl.G_User;
import graphene.model.idl.G_UserSpaceRelationshipType;
import graphene.model.idl.G_UserWorkspace;
import graphene.model.idl.G_Workspace;
import graphene.util.validator.ValidationUtils;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Count;
import io.searchbox.core.CountResult;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import io.searchbox.core.Search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UserWorkspaceDAOESImpl extends BasicESDAO implements UserWorkspaceDAO {
	@Inject
	@Symbol(JestModule.ES_USERWORKSPACE_INDEX)
	private String indexName;
	@Inject
	private UserDAO userDAO;
	@Inject
	private WorkspaceDAO workspaceDAO;

	public UserWorkspaceDAOESImpl(final ESRestAPIConnection c, final JestClient jestClient, final Logger logger) {
		auth = null;
		this.c = c;
		this.jestClient = jestClient;
		mapper = new ObjectMapper(); // can reuse, share globally
		this.logger = logger;
	}

	@Override
	public boolean addRelationToWorkspace(final String userId, final G_UserSpaceRelationshipType rel,
			final String workspaceid) {
		// G_Workspace workspace = workspaceDAO.getById(workspaceid);
		// G_User user = userDAO.getById(id);
		G_UserWorkspace ug = null;
		// if (ValidationUtils.isValid(workspace, user)) {
		ug = new G_UserWorkspace(null, workspaceid, userId, getModifiedTime(), rel);
		ug = save(ug);

		if (ug != null) {
			logger.debug("Added user " + userId + " as " + rel.name() + " of workspace " + workspaceid);
			return true;
		} else {
			logger.error("Could not create relationship for user " + userId + " as " + rel.name() + " of workspace "
					+ workspaceid);
			return false;
		}
	}

	@Override
	public int countUsersForWorkspace(final String workspaceId) {
		final String query = new SearchSourceBuilder().query(QueryBuilders.matchQuery("id", workspaceId)).toString();
		try {
			final CountResult result = jestClient.execute(new Count.Builder().query(query).addIndex(indexName)
					.addType(type).build());
			return (int) result.getCount().longValue();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean deleteWorkspaceRelations(final String workspaceId) {
		boolean success = false;
		try {
			jestClient.execute((new Delete.Builder(QueryBuilders.matchQuery("workspaceId", workspaceId).toString()))
					.index(getIndex()).type(type).build());
			success = true;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return success;
	}

	@Override
	public List<G_User> getUsersForWorkspace(final String workspaceId) {
		final G_Workspace w = workspaceDAO.getById(workspaceId);
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("workspaceId", w.getId())));
		final Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(indexName).addType(type)
				.build();

		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		final List<G_User> returnValue = new ArrayList<G_User>(0);
		try {
			result = jestClient.execute(search);
			final List<G_UserWorkspace> resultObject = result.getSourceAsObjectList(G_UserWorkspace.class);
			System.out.println(resultObject);
			for (final G_UserWorkspace r : resultObject) {
				final G_User foundObject = userDAO.getById(r.getWorkspaceId());
				if (foundObject != null) {
					returnValue.add(foundObject);
				}
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnValue;
	}

	@Override
	public List<G_Workspace> getWorkspacesForUser(final String userId) {
		// G_User user = userDAO.getById(userId);
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("userId", userId)));
		final Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(indexName).addType(type)
				.build();

		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		final List<G_Workspace> returnValue = new ArrayList<G_Workspace>(0);
		try {
			result = jestClient.execute(search);
			final List<G_UserWorkspace> resultObject = result.getSourceAsObjectList(G_UserWorkspace.class);
			System.out.println(resultObject);
			for (final G_UserWorkspace r : resultObject) {
				final G_Workspace foundObject = workspaceDAO.getById(r.getWorkspaceId());
				if (foundObject != null) {
					returnValue.add(foundObject);
				} else {
					logger.error("No workspace found for " + r.toString());
				}
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnValue;
	}

	@Override
	public boolean hasRelationship(final String userId, final String workspaceId,
			final G_UserSpaceRelationshipType... relations) {
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("userId", userId))
				.must(QueryBuilders.matchQuery("workspaceId", workspaceId)));
		final Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(indexName).addType(type)
				.build();

		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		boolean success = false;
		try {
			result = jestClient.execute(search);
			final List<G_UserWorkspace> resultObject = result.getSourceAsObjectList(G_UserWorkspace.class);
			System.out.println("hasRelationship result:" + result);
			for (final G_UserWorkspace r : resultObject) {
				for (final G_UserSpaceRelationshipType relation : relations) {
					if (r.getRole().equals(relation)) {
						success = true;
					}
				}
			}

		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;
	}

	@Override
	@PostInjection
	public void initialize() {
		setIndex(indexName);
		setType("userworkspace");
		super.initialize();
	}

	@Override
	public boolean removeUserFromWorkspace(final String userId, final String workspaceId) {
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("userId", userId))
				.must(QueryBuilders.matchQuery("workspaceId", workspaceId)));
		final Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(indexName).addType(type)
				.build();

		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		boolean success = false;
		try {
			result = jestClient.execute(search);
			final List<G_UserWorkspace> resultObject = result.getSourceAsObjectList(G_UserWorkspace.class);
			System.out.println(resultObject);
			for (final G_UserWorkspace r : resultObject) {
				// remove each user binding that matched, should only be one or
				// two.
				logger.debug("Deleting user-workspace relation " + r.getId());
				success = delete(r.getId());
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;
	}

	@Override
	public boolean removeUserPermissionFromWorkspace(final String userId, final String permission,
			final String workspaceId) {
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("userId", userId))
				.must(QueryBuilders.matchQuery("workspaceId", workspaceId)));
		final Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(indexName).addType(type)
				.build();

		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		boolean success = false;
		try {
			result = jestClient.execute(search);
			final List<G_UserWorkspace> resultObject = result.getSourceAsObjectList(G_UserWorkspace.class);
			System.out.println(resultObject);
			for (final G_UserWorkspace r : resultObject) {
				if (r.getRole().name().equals(permission)) {
					success = delete(r.getId());
				}
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;
	}

	@Override
	public G_UserWorkspace save(final G_UserWorkspace g) {
		G_UserWorkspace returnVal = g;
		returnVal.setModified(getModifiedTime());
		Index index;
		if (returnVal.getId() == null) {
			// auto id
			index = new Index.Builder(returnVal).index(indexName).type(type).build();
		} else {
			index = new Index.Builder(returnVal).index(indexName).id(returnVal.getId()).type(type).build();
		}
		try {

			final JestResult result = jestClient.execute(index);
			if (!ValidationUtils.isValid(returnVal.getId()) && ValidationUtils.isValid(result.getValue("_id"))) {
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

}
