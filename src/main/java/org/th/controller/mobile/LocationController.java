package org.th.controller.mobile;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.th.entity.City;
import org.th.entity.District;
import org.th.service.CityService;
import org.th.service.DistrictService;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/locations")
@RequiredArgsConstructor
public class LocationController {

    private final CityService cityService;
    private final DistrictService districtService;

    @GetMapping("/cities")
    public ResponseEntity<List<City>> getCities() {
        return ResponseEntity.ok(cityService.getAllActiveCities());
    }

    @GetMapping("/cities/{cityId}/districts")
    public ResponseEntity<List<District>> getDistrictsByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(districtService.getDistrictsByCity(cityId));
    }
}
