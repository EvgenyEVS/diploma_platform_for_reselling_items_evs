package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.ads.AdDto;
import ru.skypro.homework.dto.ads.AdsDto;
import ru.skypro.homework.dto.ads.CreateOrUpdateAdDto;
import ru.skypro.homework.dto.ads.ExtendedAdDto;
import ru.skypro.homework.model.Advertisements;
import ru.skypro.homework.model.User;

public interface AdService {

    AdsDto getAllAds();

    AdDto createAd(CreateOrUpdateAdDto dto, MultipartFile image, User author);

    ExtendedAdDto getAds(int id);

}
