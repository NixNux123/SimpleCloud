package eu.thesimplecloud.base.wrapper.process.serviceconfigurator.configurators

import eu.thesimplecloud.base.wrapper.process.serviceconfigurator.IServiceConfigurator
import eu.thesimplecloud.base.core.utils.FileCopier
import eu.thesimplecloud.api.service.ICloudService
import eu.thesimplecloud.api.utils.FileEditor
import java.io.File

class DefaultProxyConfigurator : IServiceConfigurator {

    override fun configureService(cloudService: ICloudService, serviceTmpDirectory: File) {
        val bungeeConfigFile = File(serviceTmpDirectory, "config.yml")
        if (!bungeeConfigFile.exists()) {
            FileCopier.copyFileOutOfJar(bungeeConfigFile, "/files/config.yml")
        }
        val fileEditor = FileEditor(bungeeConfigFile)
        fileEditor.replaceLine("  host: 0.0.0.0:25565", "  host: 0.0.0.0:${cloudService.getPort()}")
        fileEditor.replaceLine("  max_players: 1", "  max_players: ${cloudService.getMaxPlayers()}")
        fileEditor.save(bungeeConfigFile)
    }


}