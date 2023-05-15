package com.example

import com.expediagroup.graphql.server.operations.Query

data class PetName(val value: String)
data class PetAttribute(val name: String)
enum class PetColor {
    BLACK,
    WHITE,
    BROWN
}

data class Dog(
    val name: PetName,
    val stringName: String,
    val colors: List<PetColor>,
    private val attributes: List<PetAttribute>
) {
    fun attributes(): List<PetAttribute> = attributes
}

data class PetFamily(val familyName: String, val dogs: List<Dog>)

class RootQuery : Query {
    fun allPets(): PetFamily {

        val dogs = listOf(
            Dog(
                PetName("Oreo"),
                "Oreo",
                listOf(PetColor.BLACK, PetColor.WHITE),
                listOf(PetAttribute("cute"), PetAttribute("fluffy"))
            ),
            Dog(
                PetName("Thor"),
                "Thor",
                listOf(PetColor.BROWN),
                listOf(PetAttribute("lazy"))
            )
        )

        return PetFamily("Dogs", dogs)
    }
}
