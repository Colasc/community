package com.nowcoder.community;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;

@SpringBootTest
//注意这个注解(classes,必须这样写，要不然component扫描不到)
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void testString(){
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey,1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        redisTemplate.opsForValue().increment(redisKey);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        redisTemplate.opsForValue().decrement(redisKey);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
    }

    @Test
    public void testHash(){
        String redisKey = "redisKey";
        BoundHashOperations operations = redisTemplate.boundHashOps(redisKey);
        redisTemplate.opsForHash().put(redisKey, "id",10);
        operations.put("name","张三");
        System.out.println(operations.get("id"));
    }

    @Test
    public void testList(){
        String redisKey = "test:list";
        BoundListOperations operations = redisTemplate.boundListOps(redisKey);

        /*operations.leftPush(101);
        operations.leftPush(102);
        operations.leftPush(103);*/
        System.out.println(operations.size());
        System.out.println(operations.index(0));
        System.out.println(operations.rightPop());
        System.out.println(operations.leftPop());
        System.out.println(operations.range(0,2));
    }
    //统计20万个重复数据的独立整数
    @Test
    public void testHyperLL(){
        String redisKey = "test:hll:01";

        for (int i = 0;i < 100000;i++){
            redisTemplate.opsForHyperLogLog().add(redisKey,i);
        }

        for (int i = 0;i < 100000;i++){
            int r = (int)(Math.random() * 10000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey,r);
        }
        long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);
    }

    @Test
    public void testHyperLLUnion(){
        String redisKey2 = "test:hll:02";
        for (int i = 0;i < 10000;i++){
            redisTemplate.opsForHyperLogLog().add(redisKey2,i);
        }
        String redisKey3 = "test:hll:03";
        for (int i = 5001;i < 15001;i++){
            redisTemplate.opsForHyperLogLog().add(redisKey3,i);
        }
        String redisKey4 = "test:hll:04";
        for (int i = 10001;i < 20001;i++){
            redisTemplate.opsForHyperLogLog().add(redisKey4,i);
        }
        String redisKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(redisKey,redisKey2,redisKey3,redisKey4);

        long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);

    }

    @Test
    public void testBitmap(){
        String redisKey = "test:bm:01";
        //记录
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        redisTemplate.opsForValue().setBit(redisKey,4,true);
        redisTemplate.opsForValue().setBit(redisKey,7,true);
        //查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,2));
        //统计
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);

    }

    @Test
    public void testBitmapOR(){
        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2,0,true);
        redisTemplate.opsForValue().setBit(redisKey2,1,true);
        redisTemplate.opsForValue().setBit(redisKey2,2,true);

        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey3,2,true);
        redisTemplate.opsForValue().setBit(redisKey3,3,true);
        redisTemplate.opsForValue().setBit(redisKey3,4,true);

        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey4,4,true);
        redisTemplate.opsForValue().setBit(redisKey4,5,true);
        redisTemplate.opsForValue().setBit(redisKey4,6,true);

        String redisKey = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),redisKey2.getBytes(),redisKey3.getBytes(),redisKey4.getBytes());
                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);

    }

    
}
