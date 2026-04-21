package com.pickkasso.pickkasso.user.dto;

import com.pickkasso.pickkasso.user.entity.Gender;
import com.pickkasso.pickkasso.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupDto {
    private String username;

    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*\\d).{8,30}$",
            message = "비밀번호는 소문자, 숫자를 포함해야 합니다."
    )
    private String password;
    private String passwordConfirm;

    private String email;
    private String name;

    private Gender gender;
    private Role role;

    private String phone;
}
