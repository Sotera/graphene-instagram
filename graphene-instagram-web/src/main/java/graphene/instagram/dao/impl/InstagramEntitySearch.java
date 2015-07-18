/**
 * 
 */
package graphene.instagram.dao.impl;

import graphene.dao.es.ESRestAPIConnection;
import graphene.model.idl.G_EntityQuery;
import graphene.model.idl.G_EntitySearch;
import graphene.model.idl.G_Geocoding;
import graphene.model.idl.G_PropertyDescriptors;
import graphene.model.idl.G_PropertyMatchDescriptor;
import graphene.model.idl.G_SearchResults;

import java.util.List;
import java.util.Properties;

import org.apache.avro.AvroRemoteException;

/**
 * @author djue
 * 
 */

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
	public G_PropertyDescriptors getDescriptors() throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public G_SearchResults search(final List<G_PropertyMatchDescriptor> terms, final long start, final long max)
			throws AvroRemoteException {
		// XXX:fix this
		G_EntityQuery.newBuilder().setPropertyMatchDescriptors(terms).build();

		return null;
	}

}
