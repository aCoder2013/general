package com.song.middleware.server.storage.mysql.mapper;

import com.alibaba.fastjson.JSONObject;
import com.song.middleware.server.domain.ServiceDO;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by song on 2017/8/5.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ServiceMapperTest {

    @Autowired
    private ServiceMapper serviceMapper;

    @Test
    public void save() throws Exception {
        ServiceDO serviceDO = new ServiceDO();
        serviceDO.setName("this is a test ");
        serviceDO.setDescription("this is a desc.");
        serviceDO.setIp("localhost");
        serviceDO.setPort("8080");
        this.serviceMapper.save(serviceDO);
    }


    @Test
    public void getByName() throws Exception {
        List<ServiceDO> serviceDOS = serviceMapper.findAllByName("this is a test ");
        Assert.assertTrue(serviceDOS != null && serviceDOS.size() > 0);
        System.out.println(JSONObject.toJSON(serviceDOS).toString());
    }
}