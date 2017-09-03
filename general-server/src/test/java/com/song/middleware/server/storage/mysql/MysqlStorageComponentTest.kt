package com.song.middleware.server.storage.mysql

import com.alibaba.fastjson.JSONObject
import com.song.middleware.server.domain.ServiceDO
import java.util.concurrent.Future
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.CollectionUtils

/**
 * Created by song on 2017/8/5.
 */
@RunWith(SpringRunner::class)
@SpringBootTest
class MysqlStorageComponentTest {

    @Autowired
    private val mysqlStorageComponent: MysqlStorageComponent? = null

    @Test
    @Throws(Exception::class)
    fun asyncQuery() {
        val future = mysqlStorageComponent!!.asyncQuery("this is a test ")
        val serviceDOS = future.get()
        Assert.assertTrue(!CollectionUtils.isEmpty(serviceDOS))
        println(JSONObject.toJSON(serviceDOS))
    }

}