package com.example.soop.domain.knowledge.repository;

import com.example.soop.domain.knowledge.entity.SavedReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SavedReferenceRepository extends JpaRepository<SavedReference, Long> {

    @Query(value = """
        SELECT *, embedding_vector <-> CAST(:queryVector AS vector) AS similarity
        FROM saved_reference
        ORDER BY similarity ASC
        LIMIT :limit
    """, nativeQuery = true)
    List<SavedReference> findTopKBySimilarity(
            @Param("queryVector") String queryVector,
            @Param("limit") int limit
    );
}
