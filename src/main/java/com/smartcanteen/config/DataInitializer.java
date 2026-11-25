package com.smartcanteen.config;

import com.smartcanteen.model.Category;
import com.smartcanteen.model.MenuItem;
import com.smartcanteen.model.Role;
import com.smartcanteen.model.User;
import com.smartcanteen.repository.MenuItemRepository;
import com.smartcanteen.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           MenuItemRepository menuItemRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.menuItemRepository = menuItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedMenuItems();
    }

    private void seedUsers() {
        createUserIfMissing("System Administrator", "admin", "admin123", Role.ADMIN.name());
        createUserIfMissing("Canteen Counter Staff", "staff", "staff123", Role.STAFF.name());
        createUserIfMissing("Aarav Sharma (Student)", "student", "student123", Role.STUDENT.name());
    }

    private void createUserIfMissing(String fullName, String username, String rawPassword, String role) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User(
                    fullName,
                    username,
                    passwordEncoder.encode(rawPassword),
                    role
            );
            userRepository.save(user);
        }
    }

    private void seedMenuItems() {
        if (menuItemRepository.count() == 0) {
            List<MenuItem> items = List.of(
                new MenuItem("Masala Dosa", Category.VEG.getLabel(), new BigDecimal("65.00"), 35, true),
                new MenuItem("Paneer Butter Masala Wrap", Category.VEG.getLabel(), new BigDecimal("85.00"), 25, true),
                new MenuItem("Veg Hakka Noodles", Category.VEG.getLabel(), new BigDecimal("75.00"), 30, true),
                new MenuItem("Filter Kaapi", Category.VEG.getLabel(), new BigDecimal("30.00"), 60, true),
                new MenuItem("Chicken Kathi Roll", Category.NON_VEG.getLabel(), new BigDecimal("110.00"), 20, true),
                new MenuItem("Chicken Biryani Bowl", Category.NON_VEG.getLabel(), new BigDecimal("140.00"), 18, true)
            );
            menuItemRepository.saveAll(items);
        }
    }
}
