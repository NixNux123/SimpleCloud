package eu.thesimplecloud.lib.network.packets.servicegroup

import eu.thesimplecloud.clientserverapi.lib.connection.IConnection
import eu.thesimplecloud.clientserverapi.lib.packet.IPacket
import eu.thesimplecloud.clientserverapi.lib.packet.packettype.JsonPacket
import eu.thesimplecloud.lib.service.ServiceType
import eu.thesimplecloud.lib.servicegroup.ICloudServiceGroup
import eu.thesimplecloud.lib.servicegroup.impl.DefaultLobbyGroup
import eu.thesimplecloud.lib.servicegroup.impl.DefaultProxyGroup
import eu.thesimplecloud.lib.servicegroup.impl.DefaultServerGroup

abstract class PacketIOCloudServiceGroupData() : JsonPacket() {

    constructor(cloudServiceGroup: ICloudServiceGroup): this() {
        this.jsonData.append("serviceType", cloudServiceGroup.getServiceType()).append("group", cloudServiceGroup)
    }

    override suspend fun handle(connection: IConnection): IPacket? {
        val serviceType = this.jsonData.getObject("serviceType", ServiceType::class.java) ?: return null
        val serviceGroupClass = when(serviceType){
            ServiceType.LOBBY -> DefaultLobbyGroup::class.java
            ServiceType.SERVER -> DefaultServerGroup::class.java
            ServiceType.PROXY -> DefaultProxyGroup::class.java
        }
        val serviceGroup = this.jsonData.getObject("group", serviceGroupClass) ?: return null
        return handleData(serviceGroup)
    }

    abstract fun handleData(cloudServiceGroup: ICloudServiceGroup): IPacket?

}