package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

        Advertisements ad = adMapper.toEntity(dto, author);
        ad.setImage(null);
        Advertisements saved = adRepository.save(ad);


        if (image != null && !image.isEmpty()) {
            try {
                String imagePath = saveImage(image, saved.getPk());
                saved.setImage(imagePath);
                saved = adRepository.save(saved);
            } catch (IOException e) {
                log.error("Failed to save image for ad: {}", saved.getPk(), e);
                adRepository.delete(saved);
                throw new RuntimeException("Failed to save ad image", e);
            }
        }

        log.info("Created ad with id: {}", saved.getPk());
        return adMapper.toAdDto(saved);
    }


    @Override
    public ExtendedAdDto getAds(int id) {
        log.debug("Getting ad with id: {}", id);

        Advertisements adv = adRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ad not found with id: " + id));
        return adMapper.toExtendedAdDto(adv);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @adServiceImpl.isAdAuthor(#id, authentication.name)")
    public void deleteAd(int id, User currentUser) {
        log.debug("Deleting ad with id: {} by user: {}", id, currentUser.getUsername());

        Advertisements ad = getAdById(id);

        if (ad.getImage() != null && !ad.getImage().isEmpty()) {
            deleteImageFile(ad.getImage());
        }

        adRepository.delete(ad);
        log.info("Deleted ad with id: {}", id);
    }


    @Override
    @PreAuthorize("hasRole('ADMIN') or @adServiceImpl.isAdAuthor(#id, authentication.name)")
    public AdDto updateAd(int id, CreateOrUpdateAdDto dto, User currentUser) {
        log.debug("Updating ad with id: {} by user: {}", id, currentUser.getUsername());

        Advertisements ad = getAdById(id);

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
    @PreAuthorize("hasRole('ADMIN') or @adServiceImpl.isAdAuthor(#id, authentication.name)")
    public byte[] updateAdImage(int id, MultipartFile image, User currentUser) {  // параметр сохранен
        log.debug("Updating image for ad with id: {} by user: {}", id, currentUser.getUsername());

        Advertisements ad = getAdById(id);

        if (ad.getImage() != null && !ad.getImage().isEmpty()) {
            deleteImageFile(ad.getImage());
        }

        try {
            String imagePath = saveImage(image, id);
            ad.setImage(imagePath);
            adRepository.save(ad);

            log.info("Updated image for ad with id: {}", id);
            return getImageBytes(imagePath);
        } catch (IOException e) {
            log.error("Failed to update image for ad: {}", id, e);
            throw new RuntimeException("Failed to update ad image", e);
        }
    }


    @Override
    public byte[] getAdImage(int id) {
        Advertisements ad = getAdById(id);
        if (ad.getImage() == null || ad.getImage().isEmpty()) {
            return new byte[0];
        }
        return getImageBytes(ad.getImage());
    }


    //private methods

    public Advertisements getAdById(int id) {
        return adRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }


    private String saveImage(MultipartFile image, int adId) throws IOException {
        String fileName = adId + ".jpg";
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }


    private byte[] getImageBytes(String imagePath) {
        try {

            Path path = Paths.get(UPLOAD_DIR + imagePath);
            if (Files.exists(path)) {
                return Files.readAllBytes(path);
            }
            log.warn("Image file not found: {}", path);
        } catch (IOException e) {
            log.error("Failed to read image file: {}", imagePath, e);
        }
        return new byte[0];
    }

    private void deleteImageFile(String imagePath) {
        try {
            Path path = Paths.get(UPLOAD_DIR + imagePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.debug("Deleted image file: {}", imagePath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete image file: {}", imagePath, e);
        }
    }

    @SuppressWarnings("unused")
    public boolean isAdAuthor(int adId, String username) {
        return adRepository.findById(adId)
                .map(ad -> ad.getAuthor().getUsername().equals(username))
                .orElse(false);
    }


}
