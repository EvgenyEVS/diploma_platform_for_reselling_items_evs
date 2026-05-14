package ru.skypro.homework.controller;


import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.homework.dto.ads.AdsDto;

@RestController
@RequestMapping("/ads")
public class AdvertisementsController {

    @GetMapping()
    @Operation(summary = "Получение всех объявлений")
    public AdsDto getAllAds() {
        return new AdsDto();
    }




}
