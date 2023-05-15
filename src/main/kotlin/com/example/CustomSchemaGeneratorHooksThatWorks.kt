package com.example

import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import graphql.schema.FieldCoordinates
import graphql.schema.GraphQLCodeRegistry
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchema
import graphql.schema.GraphQLType
import kotlin.reflect.KType

class CustomSchemaGeneratorHooksThatWorks(
    private val rootSuffix: String,
    private val typeSuffix: String
) : SchemaGeneratorHooks {

    private val fieldCoordinatesToFieldDefinition:
        MutableMap<FieldCoordinates, GraphQLFieldDefinition> = mutableMapOf()

    private val fieldCoordinatesMap:
        MutableMap<FieldCoordinates, FieldCoordinates> = mutableMapOf()

    override fun willBuildSchema(builder: GraphQLSchema.Builder): GraphQLSchema.Builder {
        val originalBuilder = super.willBuildSchema(builder)
        val originalSchema = originalBuilder.build()
        val codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry(originalSchema.codeRegistry)

        // Add the new field coordinates to the code registry with the original data fetchers
        // Without this all the fields will return null or throw an exception as the data fetchers are not set for them
        fieldCoordinatesToFieldDefinition.forEach { (coordinates, fieldDefinition) ->
            val newCoordinates = fieldCoordinatesMap[coordinates]
            codeRegistryBuilder.dataFetcher(
                newCoordinates,
                originalSchema.codeRegistry.getDataFetcher(coordinates, fieldDefinition)
            )
        }

        return GraphQLSchema.newSchema(originalSchema)
            .codeRegistry(codeRegistryBuilder.build())
    }

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

            is GraphQLObjectType -> customizeObjectType(generatedType)

            else -> generatedType
        }
    }

    override fun didGenerateQueryObject(type: GraphQLObjectType): GraphQLObjectType {
        val fieldDefinitionsMap = type.fieldDefinitions.associateWith { createCustomFieldDefinition(it) }

        val newType = GraphQLObjectType.newObject(type)
            .replaceFields(type.fieldDefinitions.map { fieldDefinitionsMap[it] })
            .build()

        fieldCoordinatesToFieldDefinition.putAll(
            type.fieldDefinitions.associateBy { fieldDefinition ->
                FieldCoordinates.coordinates(type, fieldDefinition)
            }
        )

        type.fieldDefinitions.forEach { fieldDefinition ->
            fieldCoordinatesMap[FieldCoordinates.coordinates(type, fieldDefinition)] =
                FieldCoordinates.coordinates(newType, fieldDefinitionsMap[fieldDefinition])
        }

        return newType
    }

    private fun customizeObjectType(generatedType: GraphQLObjectType): GraphQLObjectType {
        val builder = GraphQLObjectType.newObject(generatedType)
        builder.name(customizeName(generatedType.name))
        val newType = builder.build()

        // Map all original field coordinates to their field definitions, so we can extract the data fetchers later
        fieldCoordinatesToFieldDefinition.putAll(
            generatedType.fieldDefinitions.associateBy {
                    fieldDefinition ->
                FieldCoordinates.coordinates(generatedType, fieldDefinition)
            }
        )

        // Map all original field coordinates to their new field coordinates,
        // so we can add the data fetchers to the prefixed types later
        newType.fieldDefinitions.forEach {
                fieldDefinition ->
            fieldCoordinatesMap[FieldCoordinates.coordinates(generatedType, fieldDefinition)] =
                FieldCoordinates.coordinates(newType, fieldDefinition)
        }

        return newType
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
