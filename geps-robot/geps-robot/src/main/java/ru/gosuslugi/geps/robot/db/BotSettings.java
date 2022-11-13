package ru.gosuslugi.geps.robot.db;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

@Entity
public class BotSettings extends PanacheEntityBase {
    @Id
    public int id = 1;

    public int lastUpdate;

}
