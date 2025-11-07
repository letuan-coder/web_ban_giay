package com.example.DATN.models;

import com.example.DATN.constant.AuthProvider;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    Long id;

    @Column(nullable = false
            , length = 255
            , unique = true)
    String username;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "FIRST_NAME_REQUIRED")
    String firstName;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "LAST_NAME_REQUIRED")
    String lastName;

    @Column(length = 255)
    String email;

    @Column(nullable = false, length = 255)
    String password;

    LocalDate dob;


    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    AuthProvider provider;

    @Column(length = 255)
    String providerId;

    @ManyToMany
    Set<Role> roles;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Cart cart;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    List<Order> orders;

    @OneToOne(mappedBy = "user"
            , fetch = FetchType.LAZY)
    @JsonManagedReference
    UserAddress userAddress;

}
