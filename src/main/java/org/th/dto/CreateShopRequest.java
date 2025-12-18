package org.th.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.th.entity.enums.PricePreference;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Request object for creating a new shop")
public class CreateShopRequest {

    @NotBlank(message = "Shop name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(example = "Burma Bistro")
    private String name;

    @NotBlank(message = "Myanmar name is required")
    @Schema(example = "မြန်မာဘစ်စထရို")
    private String nameMm;

    @Schema(example = "Burma Bistro")
    private String nameEn;

    @NotBlank(message = "Category is required")
    @Schema(example = "Restaurant")
    private String category;

    @Schema(example = "Asian Fusion")
    private String subCategory;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be valid")
    @DecimalMax(value = "90.0", message = "Latitude must be valid")
    @Schema(example = "16.8409")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be valid")
    @DecimalMax(value = "180.0", message = "Longitude must be valid")
    @Schema(example = "96.1735")
    private BigDecimal longitude;

    @NotBlank(message = "Address is required")
    @Schema(example = "No. 123, Wayzayandar Rd, Bahan")
    private String address;

    @Schema(example = "အမှတ် ၁၂၃, ...")
    private String addressMm;

    @NotNull(message = "District is required")
    @Schema(example = "1", description = "District ID")
    private Long districtId;

    @Schema(example = "09450001122")
    private String phone;

    @Schema(example = "A modern take on traditional Burmese cuisine.")
    private String description;

    @Schema(example = "ရိုးရာ...")
    private String descriptionMm;

    private Boolean hasDelivery = false;
    private Boolean hasParking = false;
    private Boolean hasWifi = false;

    private Boolean isHalal = false;
    private Boolean isVegetarian = false;
    private PricePreference pricePreference;

    @Schema(description = "List of Operating Hours")
    private List<OperatingHourRequest> operatingHours;
}
