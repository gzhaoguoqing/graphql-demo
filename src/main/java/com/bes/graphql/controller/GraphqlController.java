package com.bes.graphql.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bes.graphql.bo.Ext;
import com.bes.graphql.bo.GraphqlQuery;
import com.bes.graphql.bo.User;
import com.bes.graphql.directive.AuthDirective;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

@RestController
@RequestMapping("graphql")
public class GraphqlController {
	
	private Map<String, User> userMap = new LinkedHashMap<>();
	
	private Map<String, Ext> extMap = new LinkedHashMap<>();
	
	public GraphqlController() {
		User user = new User(UUID.randomUUID().toString(), "zhao");
		Ext ext = new Ext(user.getId(), "内蒙古");
		userMap.put(user.getId(), user);
		extMap.put(ext.getUserId(), ext);
		user = new User(UUID.randomUUID().toString(), "liu");
		ext = new Ext(user.getId(), "山东");
		userMap.put(user.getId(), user);
		extMap.put(ext.getUserId(), ext);
		user = new User(UUID.randomUUID().toString(), "wu");
		ext = new Ext(user.getId(), "山西");
		userMap.put(user.getId(), user);
		extMap.put(ext.getUserId(), ext);
	}
	
	@PostMapping
	public Map<String, Object> execute(@RequestBody GraphqlQuery query) {
		
		SchemaParser schemaParser = new SchemaParser();
		TypeDefinitionRegistry registry = new TypeDefinitionRegistry();
		registry.merge(schemaParser.parse(GraphqlController.class.getResourceAsStream("/schema.graphqls")));
		TypeDefinitionRegistry parse = schemaParser.parse(GraphqlController.class.getResourceAsStream("/schema2.graphqls"));
		registry.merge(parse);
		RuntimeWiring rungtimeWiring = RuntimeWiring.newRuntimeWiring()
				.type("Query", builder -> builder
						.dataFetcher("role", dataFetcher -> {
							return dataFetcher.getSource();
						})
						.dataFetcher("users", dataFetcher -> {
							Collection<User> users = new ArrayList<>(userMap.values());
							String name = dataFetcher.getArgument("name");
							if (StringUtils.isNotBlank(name)) {								
								Iterator<User> userIt = users.iterator();
								while (userIt.hasNext()) {
									User user = userIt.next();
									if (!user.getName().equals(name)) {
										userIt.remove();
									}
								}
							}
							return users;
						})
						.dataFetcher("exts", dataFetcher -> new ArrayList<>(extMap.values())))
				.type("User", builder -> builder
						.dataFetcher("name", dataFetcher -> ((User) dataFetcher.getSource()).getName())
						.dataFetcher("ext", dataFetcher -> {
							User user = dataFetcher.getSource();
							return extMap.get(user.getId());
						}))
				.type("Ext", builder -> {
					return builder.dataFetcher("address", dataFetcher -> {
						return ((Ext) dataFetcher.getSource()).getAddress();
					});
				})
				.directive("auth", new AuthDirective())
				.build();
		
		Map<String, Object> context = new HashMap<>();
		List<String> roles = new ArrayList<>();
		roles.add("admin");
		context.put("roles", roles);
		
		SchemaGenerator schemaGenerator = new SchemaGenerator();
		GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(registry, rungtimeWiring);
		GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();
		ExecutionInput executionInput = ExecutionInput.newExecutionInput(query.getQuery()).context(context).build();
		ExecutionResult result = graphQL.execute(executionInput);
		return result.toSpecification();
	}
	
}
