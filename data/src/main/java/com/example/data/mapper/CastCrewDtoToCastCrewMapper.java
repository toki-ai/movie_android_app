package com.example.data.mapper;

import com.example.data.source.remote.model.CastCrewDto;
import com.example.domain.entity.CastCrew;

public class CastCrewDtoToCastCrewMapper {
    public CastCrew mapCast(CastCrewDto dto) {
        return new CastCrew(
                dto.getId(),
                dto.getName(),
                dto.getProfilePath(),
                dto.getCharacter(),
                null
        );
    }

    public CastCrew mapCrew(CastCrewDto dto) {
        return new CastCrew(
                dto.getId(),
                dto.getName(),
                dto.getProfilePath(),
                null,
                dto.getJob()
        );
    }
}