package com.haui.lms.entity;

import jakarta.persistence.*;
import com.haui.lms.constant.CommonConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = CommonConstant.EMAIL_LENGTH)
    private String email;

    @Column(length = CommonConstant.PASSWORD_LENGTH)
    private String password;

    @Column(name = "full_name", length = CommonConstant.FULLNAME_LENGTH)
    private String fullName;

    @Column(length = CommonConstant.PHONE_LENGTH)
    private String phone;

    @Column(length = CommonConstant.ADDRESS_LENGTH)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Thoi diem doi mat khau gan nhat, dung de vo hieu hoa token cu phat truoc do
    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;
}
