package org.th.entity;

import jakarta.persistence.*;
import lombok.*;
import org.th.entity.shops.MenuItem;

@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_items_cart", columnList = "cart_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 500)
    private String specialInstructions;
}
