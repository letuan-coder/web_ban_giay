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

    @Column(name = "google_id", unique = true, length = 255)
    String googleId;

    @NotBlank(message = "FIRST_NAME_REQUIRED")
    String firstName;

    @NotBlank(message = "LAST_NAME_REQUIRED")
    String lastName;

    @Column(length = 255)
    String email;

    @Column(nullable = false, length = 255)
    String password;

    LocalDate dob;


    String userImage;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    AuthProvider provider;

    @ManyToMany
    Set<Role> roles;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-carts")
    private Cart cart;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonManagedReference("user-orders")
    List<Order> orders;

    @OneToMany(mappedBy = "user"
            , fetch = FetchType.LAZY)
    @JsonManagedReference("user-address")
    List<UserAddress> userAddress;



}
