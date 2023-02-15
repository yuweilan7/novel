package io.github.xxyopen.novel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author Lenovo
 * @date 2023-02-12 14:54
 */
@SpringBootTest
public class HyperLogLog {
    @Autowired
    public StringRedisTemplate stringRedisTemplate;
    @Test
    void testHyperLogLog() {
        String[] users = new String[1000];
        int index = 0;
        for(int i = 1; i <= 1000000; i++) {
            users[index++] = "user_" + i;
            if(i % 1000 == 0) {
                index = 0;
                stringRedisTemplate.opsForHyperLogLog().add("hll1",users);
            }
        }
        Long size = stringRedisTemplate.opsForHyperLogLog().size("hll1");
        //结果为997593
        System.out.println("size="+size);
    }
}
