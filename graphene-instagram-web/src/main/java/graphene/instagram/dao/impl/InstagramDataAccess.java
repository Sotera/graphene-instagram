package graphene.instagram.dao.impl;

import graphene.dao.es.ESRestAPIConnection;
import graphene.dao.es.impl.CombinedDAOESImpl;
import graphene.model.idl.G_Constraint;
import graphene.model.idl.G_DataAccess;
import graphene.model.idl.G_Entity;
import graphene.model.idl.G_EntitySearch;
import graphene.model.idl.G_LevelOfDetail;
import graphene.model.idl.G_PropertyDescriptor;
import graphene.model.idl.G_PropertyDescriptors;
import graphene.model.idl.G_PropertyMatchDescriptor;
import graphene.model.idl.G_PropertyType;
import graphene.model.idl.G_SearchResult;
import graphene.model.idl.G_SearchResults;
import graphene.model.idl.G_TypeDescriptor;
import graphene.model.idl.G_TypeMapping;
import graphene.model.idlhelper.SingletonRangeHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avro.AvroRemoteException;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

public class InstagramDataAccess extends CombinedDAOESImpl implements G_DataAccess {
	private static final String SINGLE = "single";
	private static final String MULTIPLE = "multiple";

	@Inject
	protected G_EntitySearch _search;

	@Inject
	private Logger logger;

	G_PropertyDescriptors _descriptors;

	@Inject
	public InstagramDataAccess(final ESRestAPIConnection c, final Logger logger) {
		super(c, logger);
		auth = null;
	}

	@Override
	public G_PropertyDescriptors getDescriptors() throws AvroRemoteException {
		if (_descriptors == null) {
			_descriptors = new G_PropertyDescriptors();

			final List<G_TypeDescriptor> typeList = new ArrayList<G_TypeDescriptor>();
			typeList.add(G_TypeDescriptor.newBuilder().setKey("media").setFriendlyText("Media").setGroup(SINGLE)
					.setExclusive(false).build());
			typeList.add(G_TypeDescriptor.newBuilder().setKey("user").setFriendlyText("User").setGroup(SINGLE)
					.setExclusive(false).build());

			// -- Properties
			final List<G_PropertyDescriptor> propertiesList = new ArrayList<G_PropertyDescriptor>();
			final List<G_TypeMapping> typeMappings = new ArrayList<G_TypeMapping>();

			// ID
			typeMappings.add(G_TypeMapping.newBuilder().setType("media").setMemberKey("id").build());

			// final _descriptors.
			_descriptors.setTypes(typeList);

			_descriptors.setProperties(propertiesList);
		}
		return _descriptors;
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
					.setSingletonRange(new SingletonRangeHelper(entity, G_PropertyType.STRING))
					.setConstraint(G_Constraint.EQUALS).build();
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

}
