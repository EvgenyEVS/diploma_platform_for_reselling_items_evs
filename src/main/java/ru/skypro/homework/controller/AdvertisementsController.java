package ru.skypro.homework.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.ads.AdDto;
import ru.skypro.homework.dto.ads.AdsDto;
import ru.skypro.homework.dto.ads.CreateOrUpdateAdDto;
import ru.skypro.homework.dto.ads.ExtendedAdDto;
import ru.skypro.homework.model.User;
import ru.skypro.homework.service.AdService;
import ru.skypro.homework.service.UserService;


@RestController
@Tag(name = "Объявления")
@RequestMapping("/ads")
public class AdvertisementsController {

    private final AdService adService;
    private final UserService userService;

    public AdvertisementsController(AdService adService, UserService userService) {
        this.adService = adService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Получение всех объявлений")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = AdsDto.class)))
    })
    public ResponseEntity<AdsDto> getAllAds() {

        AdsDto adsDto = adService.getAllAds();
        return ResponseEntity.ok(adsDto);
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Добавление объявления")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(schema = @Schema(implementation = AdDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AdDto> addAd(
            @RequestPart("properties") CreateOrUpdateAdDto properties,
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) {

      User author = userService.findByUsername(userDetails.getUsername());
      AdDto createAd = adService.createAd(properties, image, author);
        return ResponseEntity.status(HttpStatus.CREATED).body(createAd);
    }


    @GetMapping("/{id}")
    @Operation(summary = "Получение информации об объявлении")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ExtendedAdDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ExtendedAdDto> getAds(@PathVariable int id) {
        ExtendedAdDto ad = adService.getAds(id);
        return ResponseEntity.ok(ad);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление объявления")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> removeAd(@PathVariable int id,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        adService.deleteAd(id, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Обновление информации об объявлении")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = AdDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<AdDto> updateAds(@PathVariable int id,
                                           @RequestBody CreateOrUpdateAdDto dto,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        AdDto updatedAd = adService.updateAd(id, dto, currentUser);
        return ResponseEntity.ok(updatedAd);
    }

    @GetMapping("/me")
    @Operation(summary = "Получение объявлений авторизованного пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = AdsDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AdsDto> getAdsMe(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        AdsDto myAds = adService.getMyAds(currentUser);
        return ResponseEntity.ok(myAds);
    }

    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Обновление картинки объявления")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = byte[].class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<byte[]> updateImage(@PathVariable int id,
                                              @RequestParam("image") MultipartFile image,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        String imagePath = adService.updateAdImage(id, image, currentUser);
        return ResponseEntity.ok(new byte[0]);
    }
}