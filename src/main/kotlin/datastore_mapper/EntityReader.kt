package datastore_mapper

import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.Value
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

class EntityReader<T : Any>(
    targetClass: KClass<T>,
    private val keyProperty: KProperty1<T, String>,
) {
    private val constructor = targetClass.primaryConstructor!!

    fun readEntity(entity: Entity): T {
        val constructorArguments = constructor.parameters
            .map { parameter ->
                if (parameter.name == keyProperty.name) entity.getKeyValue()
                else entity.getValue(parameter)
            }.toTypedArray()

        return constructor.call(*constructorArguments)
    }

    private fun Entity.getKeyValue(): String =
        key.name

    private fun Entity.getValue(parameter: KParameter): Any? {
        val name = parameter.name
        val type = parameter.type

        return if (!this.contains(name)) null
        else {
            when (type.classifier) {
                String::class -> getString(name)
                Int::class -> getLong(name).toInt()
                Long::class -> getLong(name)
                BigDecimal::class -> BigDecimal(getString(name))
                Boolean::class -> getBoolean(name)

                List::class -> getList2(name, type)

                else -> throw RuntimeException("Unknown parameter type: $type")
            }
        }
    }

    private fun Entity.getList2(name: String?, type: KType): List<Any?> {
        val elemType = type.arguments[0].type!!
        val values = this.getList<Value<*>>(name)
        return values.map {
            when(elemType.classifier) {
                Int::class -> (it.get() as Long).toInt()
                String::class -> it.get()

                else -> throw RuntimeException("Unknown list type: $elemType")
            }
        }
    }
}