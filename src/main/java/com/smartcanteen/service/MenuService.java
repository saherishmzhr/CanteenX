package com.smartcanteen.service;

import com.smartcanteen.model.Category;
import com.smartcanteen.model.MenuItem;
import com.smartcanteen.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    public MenuService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuItem> browseMenu(String categoryParam, String searchParam) {
        String normalizedCategory = null;
        if (categoryParam != null && !categoryParam.isBlank() && !"ALL".equalsIgnoreCase(categoryParam.trim())) {
            Category cat = Category.fromString(categoryParam);
            if (cat != null) {
                normalizedCategory = cat.getLabel();
            }
        }
        String normalizedSearch = (searchParam != null && !searchParam.isBlank()) ? searchParam.trim() : null;
        return menuItemRepository.searchActiveMenu(normalizedCategory, normalizedSearch);
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getAllMenuItemsForAdmin() {
        return menuItemRepository.findAllOrdered();
    }

    @Transactional(readOnly = true)
    public MenuItem getMenuItemById(Integer itemId) {
        return menuItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found with ID: " + itemId));
    }

    @Transactional
    public MenuItem saveMenuItem(MenuItem formItem) {
        validateMenuItem(formItem);
        Category parsedCategory = Category.fromString(formItem.getCategory());
        String categoryLabel = parsedCategory != null ? parsedCategory.getLabel() : Category.VEG.getLabel();

        if (formItem.getItem_id() == null) {
            MenuItem newItem = new MenuItem();
            newItem.setItem_name(formItem.getItem_name().trim());
            newItem.setCategory(categoryLabel);
            newItem.setPrice(formItem.getPrice());
            newItem.setStock(formItem.getStock());
            newItem.setActive(formItem.getActive() != null ? formItem.getActive() : true);
            return menuItemRepository.save(newItem);
        } else {
            MenuItem existing = getMenuItemById(formItem.getItem_id());
            existing.setItem_name(formItem.getItem_name().trim());
            existing.setCategory(categoryLabel);
            existing.setPrice(formItem.getPrice());
            existing.setStock(formItem.getStock());
            if (formItem.getActive() != null) {
                existing.setActive(formItem.getActive());
            }
            return menuItemRepository.save(existing);
        }
    }

    @Transactional
    public void softDeleteMenuItem(Integer itemId) {
        MenuItem item = getMenuItemById(itemId);
        item.setActive(false);
        menuItemRepository.save(item);
    }

    @Transactional
    public void restoreMenuItem(Integer itemId) {
        MenuItem item = getMenuItemById(itemId);
        item.setActive(true);
        menuItemRepository.save(item);
    }

    @Transactional
    public void toggleCategory(Integer itemId) {
        MenuItem item = getMenuItemById(itemId);
        if (Category.VEG.getLabel().equalsIgnoreCase(item.getCategory())) {
            item.setCategory(Category.NON_VEG.getLabel());
        } else {
            item.setCategory(Category.VEG.getLabel());
        }
        menuItemRepository.save(item);
    }

    private void validateMenuItem(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Menu item data cannot be null.");
        }
        if (item.getItem_name() == null || item.getItem_name().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name is required.");
        }
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.valueOf(0.50)) < 0) {
            throw new IllegalArgumentException("Price must be at least 0.50.");
        }
        if (item.getStock() == null || item.getStock() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
        Category.fromString(item.getCategory());
    }
}
