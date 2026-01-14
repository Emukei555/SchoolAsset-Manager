package com.yourcompany.schoolasset.domain.model.loan;

// 後々使う事になるimport
//import com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.domain.model.asset.Model;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// LoanRecordエンティティもまだ無い場合は、一旦Objectや仮クラスで逃げるか、Repositoryだけ定義しておきます
// 今回はエラー回避のため、ジェネリクスを一旦省略するか、LoanRecordクラスを作ってから定義します。
// ここではServiceのコンパイルを通すため、最低限のメソッドを持つインターフェースにします。

@Repository
public interface LoanRecordRepository {
    // JpaRepositoryを継承するとEntityが必要になるので、一旦普通のインターフェースとして定義

    default int countActiveLoansByModelId(Long modelId) {
        return 0; // 仮実装
    }
}
