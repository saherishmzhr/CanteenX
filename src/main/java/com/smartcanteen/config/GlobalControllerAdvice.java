package com.smartcanteen.config;

import com.smartcanteen.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Value("${canteen.currency-symbol:₹}")
    private String currencySymbol;

    @Value("${canteen.app-name:Smart Canteen}")
    private String appName;

    @ModelAttribute
    public void addGlobalAttributes(Model model, Authentication authentication) {
        model.addAttribute("currencySymbol", currencySymbol);
        model.addAttribute("appName", appName);

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            model.addAttribute("currentUser", userDetails.getUser());
            model.addAttribute("currentFullName", userDetails.getFullName());
            model.addAttribute("currentUsername", userDetails.getUsername());
            model.addAttribute("currentRole", userDetails.getRole());
        } else {
            model.addAttribute("currentUser", null);
            model.addAttribute("currentFullName", null);
            model.addAttribute("currentUsername", null);
            model.addAttribute("currentRole", null);
        }
    }
}
