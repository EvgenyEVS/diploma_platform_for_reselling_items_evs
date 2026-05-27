package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdServiceImplTest {

    @Mock
    private AdRepository adRepository;

    @Mock
    private AdMapper adMapper;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private AdServiceImpl adService;

    @Test
    void getAllAds_ShouldReturnAllAds() {
        List<Advertisements> ads = Arrays.asList(new Advertisements(), new Advertisements());
        AdsDto expectedAdsDto = new AdsDto();
        expectedAdsDto.setCount(2);

        when(adRepository.findAll()).thenReturn(ads);
        when(adMapper.toAdsDto(ads)).thenReturn(expectedAdsDto);

        AdsDto result = adService.getAllAds();

        assertThat(result).isNotNull();
        assertThat(result.getCount()).isEqualTo(2);
        verify(adRepository).findAll();
    }

    @Test
    void getAdById_ShouldReturnAd_WhenExists() {
        int adId = 1;
        Advertisements ad = new Advertisements();
        ad.setPk(adId);

        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));

        Advertisements result = adService.getAdById(adId);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(adId);
    }

    @Test
    void getAdById_ShouldThrowNotFound_WhenNotExists() {
        int adId = 999;
        when(adRepository.findById(adId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adService.getAdById(adId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAds_ShouldReturnExtendedAdDto_WhenExists() {
        int adId = 1;
        Advertisements ad = new Advertisements();
        ad.setPk(adId);

        ExtendedAdDto expectedDto = new ExtendedAdDto();
        expectedDto.setPk(adId);

        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(adMapper.toExtendedAdDto(ad)).thenReturn(expectedDto);

        ExtendedAdDto result = adService.getAds(adId);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(adId);
    }

    @Test
    void deleteAd_ShouldDelete_WhenUserIsAuthor() {
        int adId = 1;
        User user = new User();
        user.setId(1);

        Advertisements ad = new Advertisements();
        ad.setPk(adId);
        ad.setAuthor(user);
        ad.setImage(null);

        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));
        doNothing().when(adRepository).delete(ad);

        adService.deleteAd(adId, user);

        verify(adRepository).delete(ad);
    }

    @Test
    void deleteAd_ShouldThrowNotFound_WhenAdNotExists() {
        int adId = 999;
        User user = new User();
        when(adRepository.findById(adId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adService.deleteAd(adId, user))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateAd_ShouldUpdate_WhenUserIsAuthor() {
        int adId = 1;
        CreateOrUpdateAdDto dto = new CreateOrUpdateAdDto();
        dto.setTitle("Updated Title");
        dto.setPrice(2000);
        dto.setDescription("Updated Description");

        User user = new User();
        user.setId(1);

        Advertisements existingAd = new Advertisements();
        existingAd.setPk(adId);
        existingAd.setAuthor(user);

        Advertisements updatedAd = new Advertisements();
        updatedAd.setPk(adId);
        updatedAd.setTitle("Updated Title");

        AdDto expectedDto = new AdDto();
        expectedDto.setPk(adId);
        expectedDto.setTitle("Updated Title");

        when(adRepository.findById(adId)).thenReturn(Optional.of(existingAd));
        when(adRepository.save(any(Advertisements.class))).thenReturn(updatedAd);
        when(adMapper.toAdDto(updatedAd)).thenReturn(expectedDto);

        AdDto result = adService.updateAd(adId, dto, user);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        verify(adMapper).updateAdFromDto(dto, existingAd);
        verify(adRepository).save(existingAd);
    }

    @Test
    void getMyAds_ShouldReturnUserAds() {
        User user = new User();
        user.setId(1);
        user.setUsername("test@example.com");

        List<Advertisements> userAds = Arrays.asList(
                createAd(1, user),
                createAd(2, user)
        );

        AdsDto expectedDto = new AdsDto();
        expectedDto.setCount(2);

        when(adRepository.findByAuthorId(1)).thenReturn(userAds);
        when(adMapper.toAdsDto(userAds)).thenReturn(expectedDto);

        AdsDto result = adService.getMyAds(user);

        assertThat(result).isNotNull();
        assertThat(result.getCount()).isEqualTo(2);
        verify(adRepository).findByAuthorId(1);
    }

    @Test
    void isAdAuthor_ShouldReturnTrue_WhenUserIsAuthor() {
        int adId = 1;
        String username = "author@example.com";

        User author = new User();
        author.setUsername(username);

        Advertisements ad = new Advertisements();
        ad.setPk(adId);
        ad.setAuthor(author);

        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));

        boolean result = adService.isAdAuthor(adId, username);

        assertThat(result).isTrue();
    }

    @Test
    void isAdAuthor_ShouldReturnFalse_WhenUserIsNotAuthor() {
        int adId = 1;
        String username = "other@example.com";

        User author = new User();
        author.setUsername("author@example.com");

        Advertisements ad = new Advertisements();
        ad.setPk(adId);
        ad.setAuthor(author);

        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));

        boolean result = adService.isAdAuthor(adId, username);

        assertThat(result).isFalse();
    }

    @Test
    void createAd_ShouldCreateAd_WhenValidData() throws IOException {
        CreateOrUpdateAdDto dto = new CreateOrUpdateAdDto();
        dto.setTitle("New Ad");
        dto.setPrice(1000);
        dto.setDescription("Description");

        User author = new User();
        author.setId(1);
        author.setUsername("author@example.com");

        Advertisements ad = new Advertisements();
        ad.setAuthor(author);
        ad.setTitle("New Ad");

        Advertisements savedAd = new Advertisements();
        savedAd.setPk(1);
        savedAd.setAuthor(author);
        savedAd.setTitle("New Ad");

        AdDto expectedDto = new AdDto();
        expectedDto.setPk(1);
        expectedDto.setTitle("New Ad");

        when(adMapper.toEntity(dto, author)).thenReturn(ad);
        when(adRepository.save(ad)).thenReturn(savedAd);
        when(adMapper.toAdDto(savedAd)).thenReturn(expectedDto);
        when(multipartFile.isEmpty()).thenReturn(true);

        AdDto result = adService.createAd(dto, multipartFile, author);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1);
        verify(adRepository).save(ad);
    }

    private Advertisements createAd(int id, User author) {
        Advertisements ad = new Advertisements();
        ad.setPk(id);
        ad.setAuthor(author);
        ad.setTitle("Ad " + id);
        ad.setPrice(1000);
        ad.setDescription("Description " + id);
        return ad;
    }
}
