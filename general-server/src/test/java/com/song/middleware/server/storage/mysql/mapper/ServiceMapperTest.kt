package com.song.middleware.server.storage.mysql.mapper

import com.alibaba.fastjson.JSONObject
import com.song.middleware.server.domain.ServiceDO
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

/**
 * Created by song on 2017/8/5.
 */
@RunWith(SpringRunner::class)
@SpringBootTest
class ServiceMapperTest {

    @Autowired
    private val serviceMapper: ServiceMapper? = null

    @Test
    @Throws(Exception::class)
    fun save() {
        val serviceDO = ServiceDO()
        serviceDO.name = "this is a test "
        serviceDO.description = "this is a desc."
        serviceDO.ip = "localhost"
        serviceDO.port = "8080"
        this.serviceMapper!!.save(serviceDO)
    }


    @Test
    @Throws(Exception::class)
    fun getByName() {
        val serviceDOS = serviceMapper!!.findAllByName("this is a test ")
        Assert.assertTrue(serviceDOS.isNotEmpty())
        println(JSONObject.toJSON(serviceDOS).toString())
    }
}