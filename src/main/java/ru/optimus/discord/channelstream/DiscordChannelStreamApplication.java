package ru.optimus.discord.channelstream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;

import javax.xml.crypto.dsig.XMLSignContext;

@SpringBootApplication
public class DiscordChannelStreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscordChannelStreamApplication.class, args);
	}

}
