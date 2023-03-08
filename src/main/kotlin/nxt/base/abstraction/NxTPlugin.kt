package nxt.base.abstraction

import java.io.File

abstract class NxTPlugin {

    abstract val server: NxTServer

    abstract val dataFolder: File

}