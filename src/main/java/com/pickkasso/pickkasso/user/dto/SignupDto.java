package com.pickkasso.pickkasso.user.dto;

import com.pickkasso.pickkasso.user.entity.Gender;
import com.pickkasso.pickkasso.user.entity.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupDto {
    private String username;
    private String password;
    private String passwordConfirm;

    private String email;
    private String name;

    private Gender gender;
    private Role role;

    private String phone;
}
