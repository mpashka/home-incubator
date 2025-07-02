package ru.gosuslugi.geps.robot.db;

import javax.persistence.Id;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserChatPK implements Serializable {
    @Id
    public long userId;

    @Id
    public long chatId;
}
