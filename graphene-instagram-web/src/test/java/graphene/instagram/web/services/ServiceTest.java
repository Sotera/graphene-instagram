package graphene.instagram.web.services;

import graphene.dao.HyperGraphBuilder;
import graphene.dao.es.ESRestAPIConnection;
import graphene.dao.es.JestModule;
import graphene.util.db.DBConnectionPoolService;
import io.searchbox.client.JestClient;
import mil.darpa.vande.generic.V_GenericEdge;
import mil.darpa.vande.generic.V_GenericGraph;
import mil.darpa.vande.generic.V_GenericNode;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.slf4j.Logger;
import org.testng.annotations.BeforeSuite;

public class ServiceTest {

	protected Registry registry;
	protected DBConnectionPoolService cp;
	protected ESRestAPIConnection c;
	protected Logger logger;
	protected HyperGraphBuilder pgb;
	// protected InteractionGraphBuilder igb;
	protected JestClient client;

	// protected InteractionFinder interactionFinder;

	protected void printGraph(final V_GenericGraph g) {
		System.out.println("=====================");
		for (final V_GenericNode x : g.getNodes()) {
			System.out.println(x);
		}
		for (final V_GenericEdge x : g.getEdges()) {
			System.out.println(x);
		}
		System.out.println("=====================");
	}

	@BeforeSuite
	public void setup() {

		final RegistryBuilder builder = new RegistryBuilder();
		builder.add(TestModule.class);
		builder.add(JestModule.class);
		registry = builder.build();
		registry.performRegistryStartup();

		logger = registry.getService(Logger.class);

		client = registry.getService(JestClient.class);
		pgb = registry.getService("HyperProperty", HyperGraphBuilder.class);
		c = registry.getService(ESRestAPIConnection.class);

	}
}
