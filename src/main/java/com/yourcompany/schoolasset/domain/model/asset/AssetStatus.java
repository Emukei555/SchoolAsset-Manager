package com.yourcompany.schoolasset.domain.model.asset;


public enum AssetStatus {
    AVAILABLE,   // 利用可能
    LENT,        // 貸出中
    REPAIR,      // 修理中
    LOST,        // 紛失
    MAINTENANCE;  // メンテナンス中

    // この個体がどのモデル（MacBook等）に属する
    private String model;

    // 管理番号（バーコード値など）
    private String assetCode;
}
