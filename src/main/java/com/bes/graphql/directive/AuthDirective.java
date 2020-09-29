package com.bes.graphql.directive;

import java.util.List;
import java.util.Map;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;

public class AuthDirective implements SchemaDirectiveWiring {

	@Override
	public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
		String targetAuthRole = (String) environment.getDirective().getArgument("role").getValue();
		
		GraphQLFieldDefinition field = environment.getElement();
        GraphQLFieldsContainer parentType = environment.getFieldsContainer();
        DataFetcher originalDataFetcher = environment.getCodeRegistry().getDataFetcher(parentType, field);
        
        
        environment.getCodeRegistry().dataFetcher(parentType, field, dataFetcher -> {
        	Map<String, Object> contextMap = dataFetcher.getContext();
        	List<String> originalRoles = (List<String>) contextMap.get("roles");
        	if (originalRoles.contains(targetAuthRole)) {
        		return originalDataFetcher.get(dataFetcher);
        	} else {
        		return null;
        	}
        });
        
        return field;
	}
	
}
