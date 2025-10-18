
package com.example.DATN.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "districts")
public class District {
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

    @Column(name = "path")
    private String path;

    @Column(name = "path_with_type")
    private String pathWithType;

    @Column(name = "parent_code")
    private String parentCode; // Mã tỉnh/thành phố cha

    // Liên kết với bảng Province
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id")
    @JsonIgnore
    private Province province;
}
