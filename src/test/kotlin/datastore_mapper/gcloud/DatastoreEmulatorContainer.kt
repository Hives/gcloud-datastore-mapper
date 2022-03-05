package datastore_mapper.gcloud

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

class DatastoreEmulatorContainer(port: Int) :
        GenericContainer<DatastoreEmulatorContainer>(DockerImageName.parse("eu.gcr.io/jl-container-images/shared/gcloud-emulators/datastore:310.0.0")) {
    init {
        exposedPorts = listOf(port)
        setCommand("--port=$port")
    }
}
