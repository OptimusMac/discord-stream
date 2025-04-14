package ru.optimus.discord.channelstream.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.UUID;

@Configuration
public class CassandraWebConfig {

    @Bean
    public CqlSession cqlSession(){
        return CqlSession.builder()
                .addContactPoint(new InetSocketAddress("cassandra", 9042))
                .withLocalDatacenter("datacenter1")
                .withKeyspace("discord")
                .withClientId(UUID.randomUUID())
                .build();
    }

}
