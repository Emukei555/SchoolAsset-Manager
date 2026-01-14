package com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.domain.model.asset;


import com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.domain.model.asset.Model;
import com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.domain.model.asset.ModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
public class AssetSearchController {

    private final ModelRepository modelRepository;

    @GetMapping
    public ResponseEntity<Page<ModelResponse>> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Model> models = modelRepository.searchModels(categoryId, keyword, pageable);

        // EntityをDTOに変換して返却
        Page<ModelResponse> response = models.map(ModelResponse::fromEntity);
        return ResponseEntity.ok(response);
    }
}