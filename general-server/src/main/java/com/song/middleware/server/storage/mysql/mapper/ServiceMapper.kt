package com.song.middleware.server.storage.mysql.mapper

import com.song.middleware.server.domain.ServiceDO
import org.apache.ibatis.annotations.*
import org.springframework.stereotype.Repository

/**
 * Created by song on 2017/8/5.
 */
@Mapper
@Repository
interface ServiceMapper {

    @Insert("Insert into general_service(name,description,ip,port,status,health_check_url,health_check_interval) " + "values(#{name},#{description},#{ip},#{port},#{status},#{healthCheckUrl}),#{healthCheckInterval}")
    fun save(serviceDO: ServiceDO)

    @Update("update general_service set status = #{status} where id = #{id}")
    fun updateStatus(@Param("id") id: Long, status: Int)

    @Select("select id name,description,ip,port,status,health_check_url,health_check_interval from general_service where name = #{name} and ip = #{ip} and port = #{port}")
    fun findOne(name: String, ip: String, port: String): ServiceDO

    @Select("select id name,description,ip,port,status,health_check_url,health_check_interval from general_service where name = #{name}")
    fun findAllByName(@Param("name") name: String): List<ServiceDO>
}
