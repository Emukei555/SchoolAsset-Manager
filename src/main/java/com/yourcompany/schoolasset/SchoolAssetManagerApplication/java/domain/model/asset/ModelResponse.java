package com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.domain.model.asset;

public record ModelResponse(Long id, String name, String categoryName, Integer totalQuantity) {
    public static ModelResponse fromEntity(Model model) {
        return new ModelResponse(
                model.getId(),
                model.getName(),
                model.getCategory().getName(),
                model.getTotalQuantity()
        );
    }
}
