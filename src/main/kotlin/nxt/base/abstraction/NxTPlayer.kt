package nxt.base.abstraction

import java.util.*

abstract class NxTPlayer(open val uuid: UUID) {

    abstract var playerObject: Any

    abstract var name: String
    abstract var isOnline: Boolean

    abstract fun sendMessage(message: String)

    override fun toString(): String {
        return "NxTPlayer(name='$name', uuid='$uuid', isOnline=$isOnline)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as NxTPlayer
        if (uuid != other.uuid) return false
        return true
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }
}