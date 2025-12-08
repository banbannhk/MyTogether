package org.th.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private String fullName;
    private String email;
    private Boolean isVegetarian;
    private Boolean isHalal;
    private org.th.entity.enums.PricePreference pricePreference;
    private String pricePreferenceMm;
    private org.th.entity.enums.SpicinessPreference spicinessPreference;
    private String spicinessPreferenceMm;
}
