package com.example

import com.expediagroup.graphql.generator.SchemaGenerator
import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import graphql.GraphQL
import graphql.schema.GraphQLSchema
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test

class CustomSchemaGeneratorHooksThatWorksTest {

    private lateinit var schema: GraphQLSchema
    private lateinit var graphQL: GraphQL

    @BeforeEach
    fun setUp() {

        val config = SchemaGeneratorConfig(
            supportedPackages = listOf("com.example"),
            hooks = CustomSchemaGeneratorHooksThatWorks(ROOT_SUFFIX, TYPE_SUFFIX)
        )
        val generator = SchemaGenerator(config)

        schema = generator.generateSchema(listOf(TopLevelObject(RootQuery())))

        graphQL = GraphQL.newGraphQL(schema).build()
    }

    @Test
    @Order(1)
    fun `query with custom name returns correct data`() {
        val query =
            """
                query {
                    allPetsRootSuffix {
                        familyName
                        dogs {
                            name {
                                value
                            }
                            stringName
                            colors
                            attributes {
                                name
                            }
                        }
                    }
                }
            """.trimIndent()

        val result = graphQL.execute(query)
        println(result)

        assertTrue(result.isDataPresent)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    @Order(2)
    fun `all type names are customized`() {
        val typeNames = schema.typeMap.keys.toMutableSet()
        typeNames.removeAll { it.startsWith("_") }
        typeNames.removeAll(listOf("Query", "Mutation", "Boolean", "String"))

        typeNames.forEach {
            assertTrue(it.endsWith(TYPE_SUFFIX))
        }
    }

    @Test
    @Order(3)
    fun `all top level fields are customized`() {
        val queryFields = schema.queryType.fieldDefinitions

        queryFields.forEach {
            assertTrue(it.name.endsWith(ROOT_SUFFIX))
        }
    }

    companion object {
        const val ROOT_SUFFIX = "RootSuffix"
        const val TYPE_SUFFIX = "TypeSuffix"
    }
}