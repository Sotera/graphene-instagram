package graphene.instagram.dao.impl;

import graphene.model.idl.G_CallBack;
import graphene.model.idl.G_Constraint;
import graphene.model.idl.G_DataAccess;
import graphene.model.idl.G_DateRange;
import graphene.model.idl.G_DirectionFilter;
import graphene.model.idl.G_Entity;
import graphene.model.idl.G_EntityQuery;
import graphene.model.idl.G_EntitySearch;
import graphene.model.idl.G_LevelOfDetail;
import graphene.model.idl.G_Link;
import graphene.model.idl.G_LinkEntityTypeFilter;
import graphene.model.idl.G_LinkTag;
import graphene.model.idl.G_PropertyDescriptors;
import graphene.model.idl.G_PropertyMatchDescriptor;
import graphene.model.idl.G_PropertyType;
import graphene.model.idl.G_SearchResult;
import graphene.model.idl.G_SearchResults;
import graphene.model.idl.G_SortBy;
import graphene.model.idl.G_TransactionResults;
import graphene.model.idlhelper.SingletonRangeHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avro.AvroRemoteException;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

public class InstagramDataAccess implements G_DataAccess {
	protected final G_EntitySearch _search;
	@Inject
	private Logger logger;

	@Inject
	public InstagramDataAccess(final G_EntitySearch s) {
		_search = s;
	}

	@Override
	public long count(final G_EntityQuery q) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public G_SearchResults findByQuery(final G_EntityQuery pq) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, List<G_Entity>> getAccounts(final List<String> entities) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public G_SearchResults getAll(final long offset, final long maxResults) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public G_TransactionResults getAllTransactions(final List<String> entities, final G_LinkTag tag,
			final G_DateRange date, final G_SortBy sort, final List<String> linkFilter, final long start, final long max)
			throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public G_PropertyDescriptors getDescriptors() throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<G_Entity> getEntities(final List<String> entities, final G_LevelOfDetail levelOfDetail)
			throws AvroRemoteException {

		final List<G_Entity> results = new ArrayList<G_Entity>();

		final List<G_PropertyMatchDescriptor> idList = new ArrayList<G_PropertyMatchDescriptor>();

		final int maxFetch = 100;
		int qCount = 0; // How many entities to query at once
		final Iterator<String> idIter = entities.iterator();
		while (idIter.hasNext()) {
			final String entity = idIter.next();

			final G_PropertyMatchDescriptor idMatch = G_PropertyMatchDescriptor.newBuilder().setKey("uid")
					.setRange(new SingletonRangeHelper(entity, G_PropertyType.STRING))
					.setConstraint(G_Constraint.REQUIRED_EQUALS).build();
			idList.add(idMatch);
			qCount++;

			if ((qCount == (maxFetch - 1)) || !idIter.hasNext()) {
				final G_SearchResults searchResult = _search.search(idList, 0, 100);
				if (searchResult != null) {
					logger.debug("Searched for " + qCount + " ids, found " + searchResult.getTotal());

					for (final G_SearchResult r : searchResult.getResults()) {
						final G_Entity fle = (G_Entity) r.getResult();
						results.add(fle);
					}
				} else {
					logger.warn("Null search results!");
				}

				qCount = 0;
				idList.clear();
			}
		}

		return results;
	}

	@Override
	public Map<String, List<G_Link>> getFlowAggregation(final List<String> entities, final List<String> focusEntities,
			final G_DirectionFilter direction, final G_LinkEntityTypeFilter entityType, final G_LinkTag tag,
			final G_DateRange date) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, List<G_Link>> getTimeSeriesAggregation(final List<String> entities,
			final List<String> focusEntities, final G_LinkTag tag, final G_DateRange date) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean performCallback(final long offset, final long maxResults, final G_CallBack cb, final G_EntityQuery q)
			throws AvroRemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public G_SearchResults search(final List<G_PropertyMatchDescriptor> terms, final long start, final long max)
			throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
