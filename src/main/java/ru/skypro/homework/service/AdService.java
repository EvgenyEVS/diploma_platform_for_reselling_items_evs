package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.ads.AdDto;
import ru.skypro.homework.dto.ads.AdsDto;
import ru.skypro.homework.dto.ads.CreateOrUpdateAdDto;
import ru.skypro.homework.model.User;

public interface AdService {

    public AdsDto getAllAds();

    public AdDto createAd (CreateOrUpdateAdDto dto, MultipartFile image, User author);
}
