package com.song.general.gossip

/**
 * Created by song on 2017/9/2.
 */
class ApplicationState {

    var json: String
    var status: StatusEnum
    var version: Int

    /**
     *  @param json application state in json format
     *  @param version of application state
     */
    constructor(json: String, version: Int = 0) {
        this.json = json
        this.status = StatusEnum.DOWN
        this.version = version
    }

    fun markAlive() {
        this.status = StatusEnum.UP
    }

    fun markDown() {
        this.status = StatusEnum.DOWN
    }

    fun updateVersion() {
        this.version = GlobalVersionGenerator.nextVersion()
    }

    enum class StatusEnum {
        UP, DOWN
    }
}