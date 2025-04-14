package ru.optimus.discord.channelstream.repository;

import ru.optimus.discord.channelstream.model.MessageDiscord;

import java.util.UUID;

public interface CassandraRepository extends org.springframework.data.cassandra.repository.CassandraRepository<MessageDiscord, UUID> {

    boolean existsByContentID(String contentID);
}
