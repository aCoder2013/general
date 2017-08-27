package com.song.middleware.server.storage.mysql;

import com.alibaba.fastjson.JSONObject;
import com.song.middleware.server.domain.ServiceDO;
import java.util.List;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

/**
 * Created by song on 2017/8/5.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MysqlStorageComponentTest {

    @Autowired
    private MysqlStorageComponent mysqlStorageComponent;

    @Test
    public void asyncQuery() throws Exception {
        Future<List<ServiceDO>> future = mysqlStorageComponent.asyncQuery("this is a test ");
        List<ServiceDO> serviceDOS = future.get();
        Assert.assertTrue(!CollectionUtils.isEmpty(serviceDOS));
        System.out.println(JSONObject.toJSON(serviceDOS));
    }

}