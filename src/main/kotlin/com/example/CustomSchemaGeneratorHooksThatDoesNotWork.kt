package com.example

import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLType
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType

class CustomSchemaGeneratorHooksThatDoesNotWork(
    private val rootSuffix: String,
    private val typeSuffix: String
) : SchemaGeneratorHooks {

    override fun willAddGraphQLTypeToSchema(type: KType, generatedType: GraphQLType): GraphQLType {
        return when (generatedType) {
            is GraphQLInputObjectType -> {
                val newBuilder = GraphQLInputObjectType.newInputObject(generatedType)
                newBuilder.name(customizeName(generatedType.name))
                newBuilder.build()
            }

            is GraphQLEnumType -> {
                val newBuilder = GraphQLEnumType.newEnum(generatedType)
                newBuilder.name(customizeName(generatedType.name))
                newBuilder.build()
            }

            is GraphQLObjectType -> {
                val newBuilder = GraphQLObjectType.newObject(generatedType)
                newBuilder.name(customizeName(generatedType.name))
                newBuilder.build()
            }

            else -> generatedType
        }
    }

    override fun didGenerateQueryField(
        kClass: KClass<*>,
        function: KFunction<*>,
        fieldDefinition: GraphQLFieldDefinition
    ): GraphQLFieldDefinition {
        return createCustomFieldDefinition(fieldDefinition)
    }

    private fun customizeName(generatedTypeName: String): String {
        return generatedTypeName + typeSuffix
    }

    private fun createCustomFieldDefinition(fieldDefinition: GraphQLFieldDefinition): GraphQLFieldDefinition {
        val customName = fieldDefinition.name + rootSuffix

        return GraphQLFieldDefinition.newFieldDefinition(fieldDefinition)
            .name(customName)
            .build()
    }
}
