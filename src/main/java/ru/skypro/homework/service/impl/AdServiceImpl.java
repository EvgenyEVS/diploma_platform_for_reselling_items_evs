package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.skypro.homework.dto.ads.AdDto;
import ru.skypro.homework.dto.ads.AdsDto;
import ru.skypro.homework.dto.ads.CreateOrUpdateAdDto;
import ru.skypro.homework.dto.ads.ExtendedAdDto;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.model.Advertisements;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.service.AdService;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AdServiceImpl implements AdService {

    public final AdRepository adRepository;
    public final AdMapper adMapper;
    private static final String UPLOAD_DIR = "uploads/images/ads/";

    public AdServiceImpl(AdRepository adRepository, AdMapper adMapper) {
        this.adRepository = adRepository;
        this.adMapper = adMapper;
    }


    @Override
    public AdsDto getAllAds() {
        log.debug("Getting all ads");
        List<Advertisements> ads = adRepository.findAll();
        return adMapper.toAdsDto(ads);
    }


    @Override
    public AdDto createAd(CreateOrUpdateAdDto dto, MultipartFile image, User author) {
        log.debug("Creating new ad for user: {}", author.getUsername());

        Advertisements ad = adMapper.toEntity(dto, author);

        if (image != null && !image.isEmpty()) {
            String imagePath = saveImage(image);
            ad.setImage(imagePath);
        }

        Advertisements saved = adRepository.save(ad);
        log.info("Created ad with id: {}", saved.getPk());
        return adMapper.toAdDto(saved);
    }


    @Override
    public ExtendedAdDto getAds(int id) {
        log.debug("Getting ad with id: {}", id);

        Advertisements adv = adRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ad not found with id: " + id));
        return adMapper.toExtendedAdDto(adv);
    }

    @Override
    public void deleteAd(int id, User currentUser) {
        log.debug("Deleting ad with id: {} by user: {}", id, currentUser.getUsername());

        Advertisements ad = getAdById(id);
        checkAuthorization(ad, currentUser);

        if (ad.getImage() != null && !ad.getImage().isEmpty()) {
            deleteImageFile(ad.getImage());
        }

        adRepository.delete(ad);
        log.info("Deleted ad with id: {}", id);
    }


    @Override
    public AdDto updateAd(int id, CreateOrUpdateAdDto dto, User currentUser) {
        log.debug("Updating ad with id: {} by user: {}", id, currentUser.getUsername());

        Advertisements ad = getAdById(id);
        checkAuthorization(ad, currentUser);

        adMapper.updateAdFromDto(dto, ad);
        Advertisements updated = adRepository.save(ad);

        log.info("Updated ad with id: {}", id);
        return adMapper.toAdDto(updated);
    }



    @Override
    public AdsDto getMyAds(User currentUser) {
        log.debug("Getting ads for user: {}", currentUser.getUsername());

        List<Advertisements> ads = adRepository.findByAuthorId(currentUser.getId());
        return adMapper.toAdsDto(ads);
    }



    @Override
    public String updateAdImage(int id, MultipartFile image, User currentUser) {
        log.debug("Updating image for ad with id: {} by user: {}", id, currentUser.getUsername());

        Advertisements ad = getAdById(id);
        checkAuthorization(ad, currentUser);

        if (ad.getImage() != null && !ad.getImage().isEmpty()) {
            deleteImageFile(ad.getImage());
        }

        String imagePath = saveImage(image);
        ad.setImage(imagePath);
        adRepository.save(ad);

        log.info("Updated image for ad with id: {}", id);
        return imagePath;
    }



    //private methods

    public Advertisements getAdById(int id) {
        return adRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }


    private void checkAuthorization(Advertisements ad, User currentUser) {
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
        boolean isAuthor = ad.getAuthor().getId().equals(currentUser.getId());

        if (!isAdmin && !isAuthor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the author of this ad");
        }
    }


    private String saveImage(MultipartFile image) {

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }


            String originalFileName = image.getOriginalFilename();
            String extensions = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extensions = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID().toString() + extensions;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/" + UPLOAD_DIR + fileName;


        } catch (IOException e) {
            throw new RuntimeException("Failed to save image", e);
        }
    }


    private void deleteImageFile(String imagePath) {
        try {
            Path path = Paths.get(imagePath.startsWith("/") ? imagePath.substring(1) : imagePath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
            log.debug("Deleted image file: {}", imagePath);

        } catch (IOException e) {
            log.warn("Failed to delete image file: {}", imagePath, e);
        }
    }

}
