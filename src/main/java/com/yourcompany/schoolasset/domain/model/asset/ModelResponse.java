package com.yourcompany.schoolasset.domain.model.asset;

/**
 * ModelエンティティをAPIレスポンス用の形式に変換するDTO
 */
public record ModelResponse(
        Long id,
        String name,
        String categoryName,
        Integer totalQuantity
) {
    /**
     * EntityからDTOへ変換する静的メソッド
     */
    public static ModelResponse fromEntity(Model model) {
        return new ModelResponse(
                model.getId(),
                model.getName(),
                // 修正: 実際のカテゴリ名を取得するように変更
                model.getCategory() != null ? model.getCategory().getName() : "未分類",
                model.getTotalQuantity()
        );
    }
}