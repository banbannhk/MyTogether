package org.th.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.entity.City;
import org.th.repository.CityRepository;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    @PostConstruct
    @Transactional
    public void init() {
        seedCities();
    }

    private void seedCities() {
        if (cityRepository.count() == 0) {
            List<City> cities = Arrays.asList(
                    City.builder().nameEn("Bangkok").nameMm("ဘန်ကောက်").nameTh("กรุงเทพมหานคร").slug("bangkok")
                            .active(true).build());
            cityRepository.saveAll(cities);
        }
    }

    public List<City> getAllActiveCities() {
        return cityRepository.findByActiveTrue();
    }

    public City getCityBySlug(String slug) {
        return cityRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("City not found: " + slug));
    }
}
