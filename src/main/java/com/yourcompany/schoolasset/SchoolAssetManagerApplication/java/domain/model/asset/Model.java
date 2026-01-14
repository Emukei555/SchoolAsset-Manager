package com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.domain.model.asset;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "models")
@Getter @Setter
@NoArgsConstructor
@Slf4j
public class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // 個体リスト。実務ではあまり巨大なリストを保持しないよう注意が必要（必要に応じてページング）
    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL)
    private List<Asset> assets = new ArrayList<>();

}
