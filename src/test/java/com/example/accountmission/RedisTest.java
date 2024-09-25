package com.example.accountmission;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RedisTest {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    public void setUp() {
        redisTemplate.opsForValue().set("key", "value"); // 테스트를 위한 데이터 세팅
    }

    @AfterEach
    public void tearDown() {
        redisTemplate.delete("key"); // 테스트 후 데이터 삭제
    }

    @Test
    public void testRedis() {
        String value = (String) redisTemplate.opsForValue().get("key");
        assertThat(value).isEqualTo("value"); // 값이 예상한 값과 같은지 검증
    }
}
