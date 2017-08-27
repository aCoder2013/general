package com.song.middleware.server.storage.mysql.mapper;

import com.song.middleware.server.domain.ServiceDO;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * Created by song on 2017/8/5.
 */
@Mapper
@Repository
public interface ServiceMapper {

    @Insert(
        "Insert into general_service(name,description,ip,port,status,health_check_url,health_check_interval) "
            + "values(#{name},#{description},#{ip},#{port},#{status},#{healthCheckUrl}),#{healthCheckInterval}")
    void save(ServiceDO serviceDO);

    @Update("update general_service set status = #{status} where id = #{id}")
    void updateStatus(@Param("id") long id, int status);

    @Select("select id name,description,ip,port,status,health_check_url,health_check_interval from general_service where name = #{name} and ip = #{ip} and port = #{port}")
    ServiceDO findOne(String name, String ip, String port);

    @Select("select id name,description,ip,port,status,health_check_url,health_check_interval from general_service where name = #{name}")
    List<ServiceDO> findAllByName(@Param("name") String name);
}
