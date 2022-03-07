package datastore_mapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import datastore_mapper.gcloud.EmulatedDatastore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.BigInteger

class Test {
    private val datastore = EmulatedDatastore.instance.client
    private val kind = "TestKind"

    private val keyFactory = datastore.newKeyFactory().setKind(kind)

    @BeforeEach
    fun setup() {
        EmulatedDatastore.instance.clean()
    }

    @Test
    fun `a test`() {
        data class TestClass(
            val id: String,
            val string: String,
            val optionalString: String?,
            val int: Int,
            val long: Long,
            val bigDecimal: BigDecimal,
            val boolean: Boolean,
            val listOfInts: List<Int>,
            val listOfStrings: List<String>,
        )

        val input = TestClass(
            id = "1234",
            string = "Hello Mom",
            optionalString = null,
            int = 123,
            long = 123L,
            bigDecimal = BigDecimal(9.95),
            boolean = true,
            listOfInts = listOf(1, 2, 3),
            listOfStrings = listOf("one", "two", "three"),
        )

        val entityCreator = EntityCreator(
            keyProperty = TestClass::id,
            kind = kind,
            datastore = datastore
        )

        val entityReader = EntityReader(
            targetClass = TestClass::class,
            keyProperty = TestClass::id
        )

        val inputEntity = entityCreator.createEntity(input)

        datastore.put(inputEntity)
        val outputEntity = datastore.get(keyFactory.newKey(input.id))

        val final = entityReader.readEntity(outputEntity)

        assertThat(final).isEqualTo(input)
    }
}