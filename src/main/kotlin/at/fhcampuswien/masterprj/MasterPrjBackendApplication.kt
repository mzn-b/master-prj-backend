package at.fhcampuswien.masterprj

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MasterPrjBackendApplication

fun main(args: Array<String>) {
    runApplication<MasterPrjBackendApplication>(*args)
}
