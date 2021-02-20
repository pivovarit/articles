package com.pivovarit.movies;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_ARCHIVES;
import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_JARS;
import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class ArchitectureTest {

    private static final JavaClasses classes = new ClassFileImporter()
      .withImportOption(DO_NOT_INCLUDE_TESTS)
      .withImportOption(DO_NOT_INCLUDE_JARS)
      .withImportOption(DO_NOT_INCLUDE_ARCHIVES)
      .importPackages("com.pivovarit");

    @Test
    void com_pivovarit_movies_shouldNotExposeInternalClasses() {
        classes()
          .that().resideInAPackage("com.pivovarit.movies.*")
          .should()
          .onlyBeAccessed().byClassesThat()
          .resideInAPackage("com.pivovarit.movies..")
          .check(classes);
    }
}
