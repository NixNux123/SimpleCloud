package eu.thesimplecloud.api.player

import eu.thesimplecloud.clientserverapi.lib.promise.CommunicationPromise
import eu.thesimplecloud.clientserverapi.lib.promise.ICommunicationPromise
import eu.thesimplecloud.clientserverapi.lib.promise.flatten
import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.exception.NoSuchPlayerException
import eu.thesimplecloud.api.exception.NoSuchServiceException
import eu.thesimplecloud.api.exception.NoSuchWorldException
import eu.thesimplecloud.api.exception.UnreachableServiceException
import eu.thesimplecloud.api.location.ServiceLocation
import eu.thesimplecloud.api.location.SimpleLocation
import eu.thesimplecloud.api.player.connection.IPlayerConnection
import eu.thesimplecloud.api.player.text.CloudText
import eu.thesimplecloud.api.service.ICloudService

interface ICloudPlayer : IOfflineCloudPlayer {

    /**
     * Returns the [IPlayerConnection] of this player.
     */
    fun getPlayerConnection(): IPlayerConnection

    /**
     * Sends a message to this player.
     */
    fun sendMessage(cloudText: CloudText) = CloudAPI.instance.getCloudPlayerManager().sendMessageToPlayer(this, cloudText)

    /**
     * Sends a message to this player.
     */
    fun sendMessage(message: String) = sendMessage(CloudText(message))

    /**
     * Sends this player to the specified [cloudService]
     * @param cloudService the service the player shall be sent to.
     * @return a promise that is completed when the connection is complete, or
     * when an exception is encountered. [ICommunicationPromise.isSuccess] indicates success
     * or failure.
     * The promise will fail with:
     * - [UnreachableServiceException] if the proxy service the player is connected to is not reachable
     * - [IllegalArgumentException] if the specified [cloudService] is a proxy service.
     */
    fun connect(cloudService: ICloudService): ICommunicationPromise<Unit> = CloudAPI.instance.getCloudPlayerManager().connectPlayer(this, cloudService)

    /**
     * Kicks this player form the network.
     */
    fun kick(message: String) = CloudAPI.instance.getCloudPlayerManager().kickPlayer(this, message)

    /**
     * Kicks this player from the network.
     */
    fun kick() = kick("§cYou were kicked from the network.")

    /**
     * Sends a title to this player.
     */
    fun sendTitle(title: String, subTitle: String, fadeIn: Int, stay: Int, fadeOut: Int) = CloudAPI.instance.getCloudPlayerManager().sendTitle(this, title, subTitle, fadeIn, stay, fadeOut)

    /**
     * Sends a action bar to this player
     */
    fun sendActionBar(actionbar: String) = CloudAPI.instance.getCloudPlayerManager().sendActionbar(this, actionbar)

    /**
     * Returns the name of the proxy the player is connected to.
     */
    fun getConnectedProxyName(): String

    /**
     * Returns the name of the server the player is connected to.
     */
    fun getConnectedServerName(): String?

    /**
     * Returns the proxy this player is connected to.
     */
    fun getConnectedProxy(): ICloudService? = CloudAPI.instance.getCloudServiceManger().getCloudServiceByName(getConnectedProxyName())

    /**
     * Returns the server this player is connected to.
     */
    fun getConnectedServer(): ICloudService? = getConnectedServerName()?.let { CloudAPI.instance.getCloudServiceManger().getCloudServiceByName(it) }

    /**
     * Tells the manager that this client wants to receive updates of this player.
     */
    fun enableUpdates() = CloudAPI.instance.getCloudPlayerManager().setUpdates(this, true, CloudAPI.instance.getThisSidesName())

    /**
     * Tells the manager that this client no longer wants to receive updates of this player.
     */
    fun disableUpdates() = CloudAPI.instance.getCloudPlayerManager().setUpdates(this, false, CloudAPI.instance.getThisSidesName())

    /**
     * Lets this player executes the specified [command]
     */
    fun forceCommandExecution(command: String) = CloudAPI.instance.getCloudPlayerManager().forcePlayerCommandExecution(this, command)

    /**
     * Teleports this player to the specified [location].
     * @return a promise that is completed when the teleportation is complete, or
     * when an exception is encountered. [ICommunicationPromise.isSuccess] indicates success
     * or failure.
     * The promise will fail with:
     * - [UnreachableServiceException] if the minecraft server the player is connected is not reachable
     * - [NoSuchWorldException] if the world to teleport the player to does not exist or is not loaded.
     */
    fun teleport(location: SimpleLocation): ICommunicationPromise<Unit> = CloudAPI.instance.getCloudPlayerManager().teleportPlayer(this, location)

    /**
     * Teleports this player to the specified [location]
     * If the player is not connected to the service specified in the [location] he will be sent to the service.
     * @return a promise that is completed when the teleportation is complete, or
     * when an exception is encountered. [ICommunicationPromise.isSuccess] indicates success
     * or failure.
     * The promise will fail with:
     * - [UnreachableServiceException] if the proxy service or the minecraft server the player is connected to is not reachable
     * - [IllegalArgumentException] if [ServiceLocation.getService] is a proxy service.
     * - [NoSuchWorldException] if the world to teleport the player to does not exist or is not loaded.
     */
    fun teleport(location: ServiceLocation): ICommunicationPromise<Unit> {
        val locationService = location.getService()
                ?: return CommunicationPromise.failed(UnreachableServiceException("Service not found"))
        return this.connect(locationService).then { this.teleport(location) }.flatten().addFailureListener { this.sendMessage("§cTeleportation failed.") }
    }

    /**
     * Checks whether this player has the specified [permission]
     * @return a promise that is completed when the permission is checked, or
     * when an exception is encountered. [ICommunicationPromise.isSuccess] indicates success
     * or failure.
     * The promise will fail with:
     * - [UnreachableServiceException] if the proxy server the player is connected is not reachable.
     * - [NoSuchPlayerException] if the player cannot be found on the proxy.
     */
    fun hasPermission(permission: String): ICommunicationPromise<Boolean> = CloudAPI.instance.getCloudPlayerManager().hasPermission(this, permission)

    /**
     *
     * @return a promise that is completed when the [ServiceLocation] is available, or
     * when an exception is encountered. [ICommunicationPromise.isSuccess] indicates success
     * or failure.
     * The promise will fail with:
     * - [UnreachableServiceException] if the player is not connected to a server or the server is not connected to the manager.
     */
    fun getLocation(): ICommunicationPromise<ServiceLocation> = CloudAPI.instance.getCloudPlayerManager().getLocationOfPlayer(this)

    /**
     * Sends this player to a lobby server
     * @return a promise that is completed when this player is connected to the lobby server, or
     * when an exception is encountered. [ICommunicationPromise.isSuccess] indicates success
     * or failure.
     * The promise will fail with:
     * - [NoSuchPlayerException] if the player cannot be found on the proxy.
     * - [UnreachableServiceException] if the proxy server the player is connected is not reachable.
     * - [NoSuchServiceException] if no lobby was available to send the player to.
     */
    fun sendToLobby(): ICommunicationPromise<Unit> = CloudAPI.instance.getCloudPlayerManager().sendPlayerToLobby(this)

    /**
     * Returns a new [IOfflineCloudPlayer] with the data of this player
     */
    fun toOfflinePlayer(): IOfflineCloudPlayer

}