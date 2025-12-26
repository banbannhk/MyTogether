package org.th.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import org.th.dto.ShopListDTO;
import org.th.dto.MenuCategoryDTO;
import org.th.dto.MenuItemDTO;

/**
 * Combined search response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDTO {

    private List<ShopListDTO> shops;
    private List<MenuCategoryDTO> categories;
    private List<MenuItemDTO> menus;
}
