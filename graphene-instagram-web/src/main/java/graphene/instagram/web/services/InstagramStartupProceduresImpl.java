package graphene.instagram.web.services;

import graphene.dao.StartupProcedures;

public class InstagramStartupProceduresImpl implements StartupProcedures {

	@Override
	public boolean initialize() {
		return true;
	}
}