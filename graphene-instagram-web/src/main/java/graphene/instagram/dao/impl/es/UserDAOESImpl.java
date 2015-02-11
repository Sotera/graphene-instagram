package graphene.instagram.dao.impl.es;

import graphene.dao.UserDAO;
import graphene.dao.es.BasicESDAO;
import graphene.dao.es.ESRestAPIConnection;
import graphene.dao.es.JestModule;
import graphene.model.idl.AuthenticationException;
import graphene.model.idl.G_User;
import graphene.util.crypto.PasswordHash;
import graphene.util.validator.ValidationUtils;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Count;
import io.searchbox.core.CountResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UserDAOESImpl extends BasicESDAO implements UserDAO {

	@Inject
	@Symbol(JestModule.ES_USER_INDEX)
	private String indexName;

	PasswordHash passwordHasher = new PasswordHash();

	@Inject
	public UserDAOESImpl(final ESRestAPIConnection c,
			final JestClient jestClient, final Logger logger) {
		auth = null;
		this.c = c;
		this.jestClient = jestClient;
		mapper = new ObjectMapper(); // can reuse, share globally
		this.logger = logger;

	}

	@Override
	public long countUsers(final String partialName) {
		final String query = new SearchSourceBuilder().query(
				QueryBuilders
						.wildcardQuery("username", "*" + partialName + "*"))
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

	// @Override
	// public G_User createOrUpdate(final G_User user) {
	// G_User existingUser = getById(user.getId());
	// G_User u = null;
	// if (existingUser != null) {
	// u = save(user);
	// } else {
	// u = save(user);
	// }
	// return u;
	//
	// }

	@Override
	public boolean disable(final String id) {
		final G_User user = getById(id);
		if (user == null) {
			return false;
		} else {
			user.setActive(false);
			final G_User save = save(user);
			if (save == null) {
				return false;
			} else {
				return true;
			}
		}
	}

	@Override
	public boolean enable(final String id) {
		final G_User user = getById(id);
		if (user == null) {
			return false;
		} else {
			user.setActive(true);
			final G_User save = save(user);
			if (save == null) {
				return false;
			} else {
				return true;
			}
		}
	}

	@Override
	public List<G_User> getAllUsers() {
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());

		final Search search = new Search.Builder(searchSourceBuilder.toString())
				.addIndex(indexName).addType(type).build();
		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		List<G_User> users = new ArrayList<G_User>(0);
		try {
			result = jestClient.execute(search);
			users = result.getSourceAsObjectList(G_User.class);
			for (final G_User u : users) {
				System.out.println(u);
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return users;
	}

	@Override
	public G_User getById(final String id) {
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchQuery("_id", id));

		final Search search = new Search.Builder(searchSourceBuilder.toString())
				.addIndex(indexName).addType(type).build();
		JestResult result;
		G_User user = null;
		try {
			result = jestClient.execute(search);
			logger.debug("getById: " + result.getJsonString());
			user = result.getSourceAsObject(G_User.class);
			if (user == null) {
				logger.error("Error getting user by id: " + id
						+ " results were " + result.getJsonString());
			} else {
				// This is super important, because the _id and id fields are
				// different!!!
				user.setId(id);
			}
			// System.out.println(user);

		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
	}

	@Override
	public List<G_User> getByPartialUsername(final String partialName,
			final int offset, final int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public G_User getByUsername(final String userName) {
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders
				.matchQuery("username", userName));

		final Search search = new Search.Builder(searchSourceBuilder.toString())
				.addIndex(indexName).addType(type).build();
		System.out.println(searchSourceBuilder.toString());
		JestResult result;
		G_User resultObject = null;
		try {
			result = jestClient.execute(search);
			resultObject = result.getSourceAsObject(G_User.class);
			logger.debug("getByUsername: " + result.getJsonString());
			if (ValidationUtils.isValid(resultObject)) {
				logger.debug("Found: " + resultObject.toString());
			} else {
				logger.error("Could not get user with username " + userName);
			}
		} catch (final Exception e) {
			logger.error("Problem getting user: " + e.getMessage());
			e.printStackTrace();
		}
		return resultObject;
	}

	@Override
	public String getPasswordHash(final String id, final String password) {
		String hash = null;
		try {
			hash = passwordHasher.createHash(password);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error("Error getting password hash for id " + id);
			e.printStackTrace();
		}
		return hash;
	}

	// @PostInjection
	@Override
	public void initialize() {
		setIndex(indexName);
		setType("users");
		super.initialize();
	}

	@Override
	public boolean isExisting(final String username) {
		logger.debug("Checking to see if username exists");
		if (getByUsername(username) != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isExistingId(final String id) {
		logger.debug("Checking to see if user id exists");
		if (getById(id) != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public G_User loginAuthenticatedUser(final String id) {
		G_User user = getById(id);
		if (user != null) {
			user.setLastlogin(DateTime.now(DateTimeZone.UTC).getMillis());
			user = save(user);
		}
		return user;
	}

	@Override
	public G_User loginUser(final String id, final String password)
			throws AuthenticationException {
		final G_User user = getById(id);
		if (user != null) {
			final String hash = user.getHashedpassword();
			try {
				if (passwordHasher.validatePassword(password, hash)) {
					user.setLastlogin(DateTime.now(DateTimeZone.UTC)
							.getMillis());

				}
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				logger.error("Error logging in, could not validate password for "
						+ id);
				e.printStackTrace();
			}

		} else {
			logger.warn("Could not get user with id " + id);
		}
		return user;
	}

	@Override
	public G_User save(final G_User g) {
		G_User returnVal = g;
		returnVal.setModified(getModifiedTime());
		Index saveAction;
		if (ValidationUtils.isValid(returnVal)) {
			if (!ValidationUtils.isValid(returnVal.getId())) {
				// auto id
				saveAction = new Index.Builder(returnVal).index(indexName)
						.type(type).build();
			} else {
				// use id that was provided
				saveAction = new Index.Builder(returnVal).index(indexName)
						.id(returnVal.getId()).type(type).build();
			}
			try {
				final JestResult result = jestClient.execute(saveAction);
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
		} else {
			logger.error("Attempted to save a null user object!");
		}
		return returnVal;
	}

	@Override
	public boolean updatePasswordHash(final String id, final String hash) {
		final G_User user = getById(id);
		if (user == null) {
			logger.error("Could not find user for id " + id);
			return false;
		} else {
			logger.debug("Updating password hash for " + user.getUsername());
			user.setHashedpassword(hash);
			final G_User s = save(user);
			if (s == null) {
				logger.error("Problem saving updated password hash");
				return false;
			} else {
				logger.debug("Password hash saved.");
				return true;
			}
		}
	}

}
