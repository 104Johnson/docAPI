package com.e104.util;

import java.io.IOException;
import java.net.InetSocketAddress;

import redis.clients.jedis.Jedis;

import com.e104.Errorhandling.DocApplicationException;

import net.spy.memcached.MemcachedClient;

public class RedisService {
	public MemcachedClient redisClient() throws DocApplicationException{
		String configEndpoint = "docurlcache.abjn5b.0001.apne1.cache.amazonaws.com";
        Integer clusterPort = 6379;

        MemcachedClient redis =null;
        try {
			redis = new MemcachedClient(
			        new InetSocketAddress(configEndpoint, 
			                              clusterPort));
		} catch (IOException e) {
			
			e.printStackTrace();
			throw new DocApplicationException("redis Error", 14);
		}
        return redis;
		
	}
	public Jedis jedisClient(){
		
		Jedis jedis = new Jedis("localhost");
		//Johnson
		return jedis;
	}
}