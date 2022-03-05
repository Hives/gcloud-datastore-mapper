package datastore_mapper

import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.Entity
import java.math.BigDecimal
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
                is Int -> entity.set(name, value.toLong())
                is Long -> entity.set(name, value)
                is Boolean -> entity.set(name, value)
                is BigDecimal -> entity.set(name, value.toPlainString())
                else -> throw RuntimeException("Property '$name' of class '${valueObject::class.simpleName}' has unknown value type: ${property.returnType}")
            }
        }

        return entity.build()
    }

    private fun T.nonKeyProperties() =
        this::class.memberProperties.filterNot { it == keyProperty }

    private fun newKey(value: T) = keyFactory.newKey(keyProperty(value))

    private val keyFactory = datastore.newKeyFactory().setKind(kind)
}