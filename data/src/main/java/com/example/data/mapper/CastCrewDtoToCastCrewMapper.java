package com.example.data.mapper;

import com.example.data.source.remote.model.CastCrewDto;
import com.example.domain.entity.CastCrew;

public class CastCrewDtoToCastCrewMapper {
    public CastCrew map(CastCrewDto dto) {
        return new CastCrew(
                dto.getId(),
                dto.getName(),
                dto.getProfilePath()
        );
    }
}