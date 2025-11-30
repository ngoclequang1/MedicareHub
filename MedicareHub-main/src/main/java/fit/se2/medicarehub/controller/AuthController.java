package fit.se2.medicarehub.controller;

import fit.se2.medicarehub.model.Role;
import fit.se2.medicarehub.model.UserDTO;
import fit.se2.medicarehub.repository.RoleRepository;
import fit.se2.medicarehub.repository.UserRepository;
import fit.se2.medicarehub.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import fit.se2.medicarehub.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmailService emailService;


    // Endpoint đăng ký: tạo user với role PATIENT
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute UserDTO userDTO,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {

        if (userRepository.findByUsername(userDTO.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "", "Email đã được sử dụng");
        }
        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "", "Mật khẩu xác nhận không khớp");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("userDTO", userDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userDTO", bindingResult);
            redirectAttributes.addFlashAttribute("showForm", true);
            return "redirect:/home?register=false";
        }

        // Lưu user vào database
        User newUser = new User();
        newUser.setUsername(userDTO.getEmail());
        newUser.setEmail(userDTO.getEmail());
        newUser.setFullName(userDTO.getLastName() + " " + userDTO.getFirstName());
        Role patientRole = roleRepository.findByRoleName("ROLE_PATIENT")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role bệnh nhân"));
        newUser.setRoleID(patientRole);
        newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        newUser.setEnabled(true);
        newUser.setCreatedAt(new Date());

        userRepository.save(newUser);

        return "redirect:/home?showLogin=true";
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        HttpServletRequest request) {
        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, password);
            Authentication authentication = authenticationManager.authenticate(authToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.getSession(true)
                    .setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            Optional<User> currentUser = userRepository.findByEmail(email);
            if (currentUser.isPresent() && "123".equals(password)) {
                String token = UUID.randomUUID().toString();
                currentUser.get().setUUID(token);
                userRepository.save(currentUser.get());

                return "redirect:/auth/reset-password?token=" + token;
            }

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/admin/dashboard";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
                return "redirect:/doctor/home";
            } else {
                return "redirect:/patient/home";
            }
        } catch (AuthenticationException e) {
            return "redirect:/home?loginError=true";
        }
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token,
                                Model model) {
        Optional<User> userOptional = userRepository.findByUUID(token);
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "Invalid or expired reset token.");
            return "redirect:/home";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String changePassword(@RequestParam("password") String password,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 @RequestParam("token") String token,
                                 Model model) {
        Optional<User> user = userRepository.findByUUID(token);
        if (user.isEmpty()) {
            model.addAttribute("error", "Token invalid");
            return "reset-password";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "reset-password";
        }
        if (password.length() < 8) {
            model.addAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự");
            return "reset-password";
        }
        user.get().setPassword(passwordEncoder.encode(password));
        user.get().setUUID(null);

        userRepository.save(user.get());

        return "redirect:/home?showLogin=true";
    }

    @PostMapping("/forgot-password")
    public String resetPassword(@RequestParam("email") String email,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tim thấy tài khoản! Vui lòng nhập đúng email");
            return "redirect:/home?showForgot=true";
        }
        String token = UUID.randomUUID().toString();

        User user = userOptional.get();

        user.setUUID(token);
        userRepository.save(user);

        String resetPasswordUrl = getAppUrl(request) + "/auth/reset-password?token=" + token;

        emailService.sendEmail(user.getEmail(), "Reset Password Link", "To reset your password, please click on the link below:\n" + resetPasswordUrl);

        redirectAttributes.addFlashAttribute("successMessage", "Bạn vui lòng kiểm tra email để cấp lại mật khẩu!");
        return "redirect:/home?showForgot=true";
    }

    private String getAppUrl(HttpServletRequest request) {
        return request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        // Hủy session hiện tại
        request.getSession().invalidate();
        // Xóa context của Spring Security
        SecurityContextHolder.clearContext();
        // Redirect về trang chủ (hoặc trang nào khác bạn mong muốn)
        return "redirect:/home?logout=true";
    }
}
