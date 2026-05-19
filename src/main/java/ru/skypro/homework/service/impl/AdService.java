package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.ads.AdDto;
import ru.skypro.homework.dto.ads.AdsDto;
import ru.skypro.homework.dto.ads.CreateOrUpdateAdDto;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.model.Advertisements;
import ru.skypro.homework.model.User;

import java.util.List;

@Service
public class AdService {

public final AdRepository adRepository;
public final AdMapper adMapper;

    public AdService(AdRepository adRepository, AdMapper adMapper) {
        this.adRepository = adRepository;
        this.adMapper = adMapper;
    }

    public AdsDto getAllAds() {
        List<Advertisements> ads = adRepository.findAll();
        return adMapper.toAdsDto(ads);
    }

    public AdDto createAd (CreateOrUpdateAdDto dto, MultipartFile image, User author) {

        Advertisements ad = adMapper.toEntity(dto, author);

        if(image != null && !image.isEmpty()){
            String imagePath = saveImage(image);
            ad.setImage(imagePath);
        }

        Advertisements saved = adRepository.save(ad);
        return adMapper.toAdDto(saved);
    }




    private String saveImage(MultipartFile image) {
        return "/images/ads/default.jpg";
    }
}
