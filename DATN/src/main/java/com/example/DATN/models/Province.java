
package com.example.DATN.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "provinces")
public class Province {
    @Id
    private String id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug")
    private String slug;

    @Column(name = "type")
    private String type;

    @Column(name = "name_with_type")
    private String nameWithType;
}
