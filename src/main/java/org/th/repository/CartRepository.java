package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.th.entity.Cart;
import org.th.entity.User;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "items", "items.menuItem", "shop" })
    Optional<Cart> findByUser(User user);
}
