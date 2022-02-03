/*
 * MIT License
 *
 * Copyright (C) 2020 The SimpleCloud authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package eu.thesimplecloud.module.notify

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.event.service.CloudServiceRegisteredEvent
import eu.thesimplecloud.api.event.service.CloudServiceStartedEvent
import eu.thesimplecloud.api.event.service.CloudServiceUnregisteredEvent
import eu.thesimplecloud.api.eventapi.CloudEventHandler
import eu.thesimplecloud.api.eventapi.IListener
import eu.thesimplecloud.api.player.text.CloudText
import eu.thesimplecloud.api.service.ICloudService

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Created by IntelliJ IDEA.
 * User: Philipp.Eistrach
 * Date: 13.04.2020
 * Time: 17:03
 */
class CloudListener(val module: NotifyModule) : IListener {

    private var message: String = ""
    private lateinit var cloudText: CloudText

    private val permission = "cloud.module.notify.messages"

    @CloudEventHandler
    fun on(event: CloudServiceRegisteredEvent) {
        setText(module.config.serviceStartingMessage)
        sendCloudMessage(event.cloudService, false)
    }

    @CloudEventHandler
    fun on(event: CloudServiceStartedEvent) {
        setText(module.config.serviceStartedMessage)
        val cpuUsage = event.cloudService.getWrapper().getCpuUsage() * 100
        val decimal = BigDecimal(cpuUsage.toString()).setScale(2, RoundingMode.HALF_EVEN)
        setText(
            getMessage().replace("%WRAPPER%", event.cloudService.getWrapperName().toString())
                .replace("%CPUUSAGE%", "$decimal%")
        )
        sendCloudMessage(event.cloudService, true)
    }

    @CloudEventHandler
    fun on(event: CloudServiceUnregisteredEvent) {
        setText(module.config.serviceStoppedMessage)
        try {
            val cpuUsage = event.cloudService.getWrapper().getCpuUsage() * 100
            val decimal = BigDecimal(cpuUsage.toString()).setScale(2, RoundingMode.HALF_EVEN)
            setText(
                getMessage().replace("%WRAPPER%", event.cloudService.getWrapperName().toString())
                    .replace("%CPUUSAGE%", "$decimal%")
            )
            sendCloudMessage(event.cloudService, false)
        } catch (e: Exception) {
            setText("§8[§c»§8] §c%SERVICE%")
            //e.printStackTrace() ignored
        }
    }

    private fun setText(message: String) {
        this.message = message
    }

    private fun getMessage(): String {
        return this.message
    }

    private fun sendCloudMessage(service: ICloudService, addClick: Boolean) {

        val replacedMessage = getMessage().replace("%SERVICE%", service.getName())
        this.cloudText = CloudText(replacedMessage)

        if (addClick) {
            cloudText.addHover(module.config.hoverMessage)
            cloudText.addClickEvent(CloudText.ClickEventType.RUN_COMMAND, "/server " + service.getName())
        }
        CloudAPI.instance.getCloudPlayerManager().getAllCachedObjects().forEach { cloudPlayer ->
            cloudPlayer.hasPermission(permission).then {
                if (it) {
                    cloudPlayer.sendMessage(cloudText)
                }
            }
        }
    }
}