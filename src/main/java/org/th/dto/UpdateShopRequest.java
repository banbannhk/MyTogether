package org.th.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.th.entity.enums.PricePreference;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Request object for updating an existing shop")
public class UpdateShopRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private String nameMm;
    private String nameEn;

    private String category;
    private String subCategory;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal longitude;

    private String address;
    private String addressMm;

    @Schema(description = "District ID")
    private Long districtId;
    private String phone;

    private String description;
    private String descriptionMm;

    private Boolean hasDelivery;
    private Boolean hasParking;
    private Boolean hasWifi;

    private Boolean isHalal;
    private Boolean isVegetarian;
    private PricePreference pricePreference;

    private Boolean isActive;
    private Boolean isVerified;

    @Schema(description = "Update Operating Hours (Replaces existing)")
    private List<OperatingHourRequest> operatingHours;
}
