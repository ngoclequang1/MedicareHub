package fit.se2.medicarehub.controller;

import fit.se2.medicarehub.model.UserDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String route() {
        return "redirect:/home";
    }
    @GetMapping("/home")
    public String home(Model model) {
        if (!model.containsAttribute("userDTO")) {
            model.addAttribute("userDTO", new UserDTO());
        }
        return "homepage";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms"; // Trả về template terms.html
    }
}
