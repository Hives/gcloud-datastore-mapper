package datastore_mapper.gcloud

import com.google.cloud.NoCredentials
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.datastore.Query
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import java.time.Duration

class EmulatedDatastore private constructor() {
    val project = "my-test-project"
    private val namespace = "my-emulated-datastore-namespace"

    private val emulatorPort = 8082

    private val datastoreContainer =
        DatastoreEmulatorContainer(emulatorPort)
            .withStartupTimeout(Duration.ofMinutes(10))
            .withExposedPorts(emulatorPort)
            .waitingFor(HttpWaitStrategy().forStatusCode(200).forPath("/").forPort(emulatorPort))

    val client: Datastore by lazy { getEmulatedDatastoreClient() }

    private fun getEmulatedDatastoreClient() = DatastoreOptions.newBuilder()
        .setProjectId(project)
        .setNamespace(namespace)
        .setHost("http://${datastoreContainer.containerIpAddress}:${datastoreContainer.getMappedPort(emulatorPort)}")
        .setCredentials(NoCredentials.getInstance())
        .build()
        .service

    init {
        datastoreContainer.start()
    }

    fun clean() = client.clean()

    companion object {
        val instance by lazy {
            EmulatedDatastore()
        }
    }
}

private fun Datastore.clean() {
    val query =
        Query
            .newEntityQueryBuilder()
            .build()

    run(query)
        .asSequence()
        .forEach { delete(it.key) }
}