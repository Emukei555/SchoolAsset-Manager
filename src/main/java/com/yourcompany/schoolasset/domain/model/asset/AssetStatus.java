package com.yourcompany.schoolasset.domain.model.asset;


public enum AssetStatus {
    AVAILABLE,   // 利用可能
    LENT,        // 貸出中
    REPAIR,      // 修理中
    LOST,        // 紛失
    MAINTENANCE;  // メンテナンス中

    // TODO [ASSET-101] この個体がどのモデル（MacBook等）に属するか
    // 本来は EquipmentModel エンティティ等と紐付けますが、今は String で通します
    private String model;

    // TODO [ASSET-102] 管理番号（バーコード値など）
    private String assetCode;
}