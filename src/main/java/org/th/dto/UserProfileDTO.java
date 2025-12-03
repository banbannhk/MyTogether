package org.th.dto;

import lombok.Data;
import org.th.entity.User.PricePreference;
import org.th.entity.User.SpicinessPreference;

@Data
public class UserProfileDTO {
    private String fullName;
    private String email;
    private Boolean isVegetarian;
    private Boolean isHalal;
    private PricePreference pricePreference;
    private SpicinessPreference spicinessPreference;
}
