package graphene.instagram.dao.impl.es;

import graphene.dao.GroupDAO;
import graphene.dao.es.BasicESDAO;
import graphene.dao.es.ESRestAPIConnection;
import graphene.dao.es.JestModule;
import graphene.model.idl.G_Group;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GroupDAOESImpl extends BasicESDAO implements GroupDAO {

	@Inject
	@Symbol(JestModule.ES_GROUP_INDEX)
	private String indexName;

	public GroupDAOESImpl(ESRestAPIConnection c, JestClient jestClient,
			Logger logger) {
		this.auth = null;
		this.c = c;
		this.jestClient = jestClient;
		this.mapper = new ObjectMapper(); // can reuse, share globally
		this.logger = logger;

	}

	//@PostInjection
	public void initialize() {
		this.setIndex(indexName);
		this.setType("groups");
		super.initialize();
	}

	@Override
	public G_Group createGroup(G_Group g) {
		return save(g);
	}

	@Override
	public G_Group save(final G_Group g) {
		// Asynch index
		g.setModified(getModifiedTime());
		Index index;
		if (g.getId() == null) {
			// auto id
			index = new Index.Builder(g).index(indexName).type(type).build();
		} else {
			index = new Index.Builder(g).index(indexName)
					.id(g.getId()).type(type).build();
		}
		try {
//			jestClient.executeAsync(index, new JestResultHandler<JestResult>() {
//				public void completed(JestResult result) {
//					g.setId((String) result.getValue("_id"));
//					System.out.println("completed==>>" + g);
//				}
//
//				public void failed(Exception ex) {
//					logger.error("Could not create or update: "
//							+ ex.getMessage());
//				}
//			});
			JestResult result = jestClient.execute(index);
			g.setId((String) result.getValue("_id"));
		} catch (ExecutionException | InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return g;
	}

	@Override
	public void deleteGroup(G_Group g) {
		delete(g.getId());
	}

	@Override
	public G_Group getGroupByGroupname(String groupname) {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchQuery("name", groupname));

		Search search = new Search.Builder(searchSourceBuilder.toString())
				.addIndex(indexName).addType(type).build();
		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		G_Group resultObject = null;
		try {
			result = jestClient.execute(search);
			resultObject = result.getSourceAsObject(G_Group.class);
			System.out.println(resultObject);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultObject;
	}

	@Override
	public G_Group getGroupById(String id) {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchQuery("_id", id));

		Search search = new Search.Builder(searchSourceBuilder.toString())
				.addIndex(indexName).addType(type).build();
		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		G_Group g = null;
		try {
			result = jestClient.execute(search);
			g = result.getSourceAsObject(G_Group.class);
			g.setId(id);
			System.out.println(g);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return g;
	}

	@Override
	public List<G_Group> getAllGroups() {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());

		Search search = new Search.Builder(searchSourceBuilder.toString())
				.addIndex(indexName).addType(type).build();
		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		List<G_Group> users = new ArrayList<G_Group>(0);
		try {
			result = jestClient.execute(search);
			users = result.getSourceAsObjectList(G_Group.class);
			for (G_Group u : users) {
				System.out.println(u);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return users;
	}

}
