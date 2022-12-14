package com.nowcoder.community.actuator;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Endpoint(id = "database")
public class DatabaseEndpoint {

    private Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);

    @Resource
    private DataSource dataSource;
    @ReadOperation
    public String checkConnection(){
        try (
                Connection conn = dataSource.getConnection();
                ) {
            return CommunityUtil.getJSONString(0,"获取链接成功！");
        } catch (SQLException e) {
            logger.error("获取链接失败:"+e.getMessage());
            return CommunityUtil.getJSONString(1,"获取链接失败！");
        }
    }
}
