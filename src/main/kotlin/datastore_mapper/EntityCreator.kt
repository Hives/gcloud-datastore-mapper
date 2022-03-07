package datastore_mapper

import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.ListValue
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class EntityCreator<T : Any>(
    private val keyProperty: KProperty1<T, String>,
    kind: String,
    datastore: Datastore
) {
    fun createEntity(valueObject: T): Entity {
        val entity = Entity.newBuilder(newKey(valueObject))

        valueObject.nonKeyProperties().forEach {
            @Suppress("UNCHECKED_CAST")
            val property = it as KProperty1<T, *>
            val name = property.name

            when (val value = property(valueObject)) {
                null -> Unit
                is String -> entity.set(name, value)
                is Number -> {
                    if (value is BigDecimal) entity.set(name, value.toPlainString())
                    else entity.set(name, value.toLong())
                }
                is Boolean -> entity.set(name, value)
                is Collection<*> -> entity.set(name, value.toListValue())
                else -> throw RuntimeException("unknown property type")
            }
        }

        return entity.build()
    }

    private fun Collection<*>.toListValue(): ListValue {
        val listValue = ListValue.newBuilder()

        forEach { value ->
            when(value) {
                is Int -> listValue.addValue(value.toLong())
                is String -> listValue.addValue(value)
                else -> throw RuntimeException("unknown list value type")
            }
        }

        return listValue.build()
    }

    private fun T.nonKeyProperties() =
        this::class.memberProperties.filterNot { it == keyProperty }

    private fun newKey(value: T) = keyFactory.newKey(keyProperty(value))

    private val keyFactory = datastore.newKeyFactory().setKind(kind)
}