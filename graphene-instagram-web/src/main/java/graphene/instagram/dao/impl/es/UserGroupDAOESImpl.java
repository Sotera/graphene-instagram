package graphene.instagram.dao.impl.es;

import graphene.dao.GroupDAO;
import graphene.dao.UserDAO;
import graphene.dao.UserGroupDAO;
import graphene.dao.es.BasicESDAO;
import graphene.dao.es.ESRestAPIConnection;
import graphene.dao.es.JestModule;
import graphene.model.idl.G_Group;
import graphene.model.idl.G_User;
import graphene.model.idl.G_UserGroup;
import graphene.util.validator.ValidationUtils;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
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

public class UserGroupDAOESImpl extends BasicESDAO implements UserGroupDAO {
	@Inject
	@Symbol(JestModule.ES_USERGROUP_INDEX)
	private String indexName;

	@Inject
	private UserDAO userDAO;

	@Inject
	private GroupDAO groupDAO;

	public UserGroupDAOESImpl(final ESRestAPIConnection c, final JestClient jestClient, final Logger logger) {
		auth = null;
		this.c = c;
		this.jestClient = jestClient;
		mapper = new ObjectMapper(); // can reuse, share globally
		this.logger = logger;

	}

	@Override
	public boolean addToGroup(final String username, final String groupname) {
		final G_Group group = groupDAO.getGroupByGroupname(groupname);
		final G_User user = userDAO.getByUsername(username);

		G_UserGroup ug = new G_UserGroup(null, group.getId(), user.getId(), getModifiedTime());
		ug = save(ug);

		return ug == null ? false : true;
	}

	@Override
	public List<G_Group> getGroupsForUser(final String username) {
		final G_User user = userDAO.getByUsername(username);
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("userId", user.getId())));
		final Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(indexName).addType(type)
				.build();

		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		final List<G_Group> returnValue = new ArrayList<G_Group>(0);
		try {
			result = jestClient.execute(search);
			final List<G_UserGroup> resultObject = result.getSourceAsObjectList(G_UserGroup.class);
			System.out.println(resultObject);
			for (final G_UserGroup r : resultObject) {
				final G_Group foundObject = groupDAO.getGroupById(r.getGroupId());
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
	public List<G_User> getUsersByGroup(final String groupName) {
		final G_Group group = groupDAO.getGroupByGroupname(groupName);
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("groupId", group.getId())));
		final Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(indexName).addType(type)
				.build();

		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		final List<G_User> returnValue = new ArrayList<G_User>(0);
		try {
			result = jestClient.execute(search);
			final List<G_UserGroup> resultObject = result.getSourceAsObjectList(G_UserGroup.class);
			System.out.println(resultObject);
			for (final G_UserGroup r : resultObject) {
				final G_User foundObject = userDAO.getById(r.getUserId());
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
	@PostInjection
	public void initialize() {
		setIndex(indexName);
		setType("usergroup");
		super.initialize();
	}

	@Override
	public boolean removeFromGroup(final String userId, final String groupId) {
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("userId", userId))
				.must(QueryBuilders.matchQuery("groupId", groupId)));
		final Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(indexName).addType(type)
				.build();

		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		boolean success = false;
		try {
			result = jestClient.execute(search);
			final G_UserGroup resultObject = result.getSourceAsObject(G_UserGroup.class);
			System.out.println(resultObject);
			success = delete(resultObject.getId());

		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;
	}

	@Override
	public G_UserGroup save(final G_UserGroup g) {
		G_UserGroup returnVal = g;
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
