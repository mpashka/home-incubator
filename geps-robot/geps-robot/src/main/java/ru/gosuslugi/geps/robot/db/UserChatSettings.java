package ru.gosuslugi.geps.robot.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import java.util.Set;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import io.quarkiverse.hibernate.types.json.JsonBinaryType;
import io.quarkiverse.hibernate.types.json.JsonTypes;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

/**
 * Статус пользователя в чате - например пользователь подписан на уведомления
 */
@Entity
@IdClass(UserChatPK.class)
@TypeDef(name = JsonTypes.JSON_BIN, typeClass = JsonBinaryType.class)
public class UserChatSettings extends PanacheEntityBase {

    public static long NO_ITEM = 0;

    @Id
    public long userId;

    @Id
    public long chatId;

    @Type(type = JsonTypes.JSON_BIN)
    @Column(name = "options", columnDefinition = JsonTypes.JSON_BIN)
    public Set<ChatOption> options;

}
