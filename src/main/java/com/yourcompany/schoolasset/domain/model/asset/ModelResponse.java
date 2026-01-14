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
     * AssetSearchController.java からメソッド参照 (ModelResponse::fromEntity) で呼ばれます
     */
    public static ModelResponse fromEntity(Model model) {
        return new ModelResponse(
                model.getId(),
                model.getName(),
                // カテゴリがnullの場合のNullPointerExceptionを防止
                model.getCategory() != null ? "TODO: Category Name" : "未分類",
                model.getTotalQuantity()
        );
    }
}