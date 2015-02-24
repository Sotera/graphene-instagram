/**
 * 
 */
package graphene.instagram.dao.impl;

import graphene.dao.es.ESRestAPIConnection;
import graphene.model.idl.G_EntitySearch;
import graphene.model.idl.G_Geocoding;
import graphene.model.idl.G_PropertyDescriptor;
import graphene.model.idl.G_PropertyMatchDescriptor;
import graphene.model.idl.G_SearchResults;
import graphene.model.idl.G_SearchTuple;
import graphene.model.idl.G_SearchType;
import graphene.model.query.EntityQuery;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.avro.AvroRemoteException;

/**
 * @author djue
 * 
 */
@Deprecated
public class InstagramEntitySearch implements G_EntitySearch {
	private G_Geocoding _geocoding;
	private Properties _config;
	private final String auth;
	ESRestAPIConnection c;

	public InstagramEntitySearch(final ESRestAPIConnection c, final String authEncoding) {
		auth = authEncoding;
		this.c = c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see graphene.model.idl.G_EntitySearch#getDescriptors()
	 */
	@Override
	public Map<String, List<G_PropertyDescriptor>> getDescriptors() throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see graphene.model.idl.G_EntitySearch#search(java.lang.String,
	 * java.util.List, long, long, java.lang.String)
	 */
	@Override
	public G_SearchResults search(final String query, final List<G_PropertyMatchDescriptor> terms, final long start,
			final long max, final String type) throws AvroRemoteException {

		final EntityQuery sq = new EntityQuery();
		sq.addAttribute(new G_SearchTuple(query, G_SearchType.COMPARE_EQUALS));

		return null;
	}

}
