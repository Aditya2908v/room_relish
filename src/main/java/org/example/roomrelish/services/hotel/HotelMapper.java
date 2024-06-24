package org.example.roomrelish.services.hotel;

import org.example.roomrelish.dto.HotelDTO;
import org.example.roomrelish.models.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface HotelMapper {
    HotelMapper INSTANCE = Mappers.getMapper(HotelMapper.class);

    @Mapping(target = "id", ignore = true)
    Hotel toHotel(HotelDTO hotelDTO);

    @Mapping(target = "id", ignore = true)
    void updateHotelFromDTO(HotelDTO hotelDTO, @MappingTarget Hotel hotel);

}
