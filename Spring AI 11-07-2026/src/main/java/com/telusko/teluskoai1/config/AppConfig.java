package com.telusko.teluskoai1.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import redis.clients.jedis.JedisPooled;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
public class AppConfig
{
    // redis config
    @Value("${spring.data.redis.host}")
    private String redistHost;
    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.ai.vectorstore.redis.index-name}")
    private String indexName;

    @Value("${spring.ai.vectorstore.redis.prefix}")
    private String prefix;
    @Bean
    public JedisPooled jedisPooled()
    {
        return new JedisPooled(redistHost, redisPort);
    }
    @Bean
    public VectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel)
    {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName(indexName)
                .prefix(prefix)
                .initializeSchema(true)
                .build();
    }

//    @Bean
//    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel)
//    {
////        return SimpleVectorStore.builder(embeddingModel).build();
//        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
//                .dimensions(1536)
//                .distanceType(COSINE_DISTANCE)
//                .indexType(HNSW)
//                .build();
//    }
}
