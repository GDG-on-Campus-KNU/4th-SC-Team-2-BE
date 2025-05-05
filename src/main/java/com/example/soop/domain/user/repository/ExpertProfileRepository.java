package com.example.soop.domain.user.repository;

import com.example.soop.domain.user.ExpertProfile;
import com.example.soop.domain.user.type.Category;
import com.example.soop.domain.user.type.Language;
import com.example.soop.domain.user.type.Style;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpertProfileRepository extends JpaRepository<ExpertProfile, Long> {
    @Query("""
    SELECT ep FROM ExpertProfile ep
    WHERE (:category IS NULL OR ep.category = :category)
      AND (:language IS NULL OR ep.language = :language)
      AND (:minExperience IS NULL OR ep.experience >= :minExperience)
      AND (:styles IS NULL OR EXISTS (
          SELECT s FROM ep.styles s WHERE s IN :styles
      ))
    """)
    List<ExpertProfile> findByFilters(
        @Param("category") Category category,
        @Param("styles") List<Style> styles,
        @Param("language") Language language,
        @Param("minExperience") Integer minExperience
    );


}
