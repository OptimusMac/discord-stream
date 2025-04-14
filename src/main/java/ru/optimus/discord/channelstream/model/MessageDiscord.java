package ru.optimus.discord.channelstream.model;


import com.datastax.oss.driver.api.core.uuid.Uuids;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Table("messages")
@Data
public class MessageDiscord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @PrimaryKey
    private UUID id = Uuids.timeBased();

    @Column("content")
    private String content;

    @Column("content_id")
    private String contentID;

    @Column("create_at")
    private Date createAt = new Date();
}