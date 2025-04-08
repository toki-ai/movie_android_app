package com.example.data.mapper;

import com.example.data.source.local.entity.ReminderEntity;
import com.example.domain.entity.Reminder;

public class ReminderEntityToReminderMapper {
    public Reminder map (ReminderEntity entity){
        return new Reminder(
                entity.getMovieId(),
                entity.getMovieTitle(),
                entity.getPosterUrl(),
                entity.getYear(),
                entity.getRating(),
                entity.getReminderTime()
        );
    }
}
