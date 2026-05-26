package ru.skypro.homework.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.skypro.homework.dto.ads.AdDto;
import ru.skypro.homework.dto.ads.AdsDto;
import ru.skypro.homework.dto.ads.CreateOrUpdateAdDto;
import ru.skypro.homework.dto.ads.ExtendedAdDto;
import ru.skypro.homework.model.Advertisements;
import ru.skypro.homework.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdMapper {

    @Mapping(source = "author.id", target = "author")
    @Mapping(source = "pk", target = "pk")
    AdDto toAdDto(Advertisements ad);

    List<AdDto> toAdDtoList(List<Advertisements> ads);

    default AdsDto toAdsDto(List<Advertisements> ads) {
        AdsDto adsDto = new AdsDto();
        adsDto.setCount(ads.size());
        adsDto.setResults(toAdDtoList(ads));
        return adsDto;
    }

    @Mapping(source = "author.firstName", target = "authorFirstName")
    @Mapping(source = "author.lastName", target = "authorLastName")
    @Mapping(source = "author.username", target = "email")
    @Mapping(source = "author.phone", target = "phone")
    @Mapping(source = "pk", target = "pk")
    ExtendedAdDto toExtendedAdDto(Advertisements ad);

    @Mapping(target = "pk", ignore = true)
    @Mapping(target = "author", source = "author")
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Advertisements toEntity(CreateOrUpdateAdDto dto, User author);

    @Mapping(target = "pk", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "comments", ignore = true)
    void updateAdFromDto(CreateOrUpdateAdDto dto, @MappingTarget Advertisements ad);


    @AfterMapping
    default void setImageUrl(@MappingTarget AdDto adDto, Advertisements ad) {
        if (ad.getImage() != null && !ad.getImage().isEmpty()) {

            adDto.setImage("/ads/" + ad.getPk() + "/image");
        }
    }

    @AfterMapping
    default void setImageUrl(@MappingTarget ExtendedAdDto extendedAdDto, Advertisements ad) {
        if (ad.getImage() != null && !ad.getImage().isEmpty()) {
            extendedAdDto.setImage("/ads/" + ad.getPk() + "/image");
        }
    }

}
