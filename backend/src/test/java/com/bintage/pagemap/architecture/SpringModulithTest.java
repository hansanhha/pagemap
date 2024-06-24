package com.bintage.pagemap.architecture;

import com.bintage.pagemap.PageMapApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;


class SpringModulithTest {

    ApplicationModules application = ApplicationModules.of(PageMapApplication.class);

    @Test
    void verifyApplicationModules() throws Exception {
        application.verify();
    }

    @Test
    void writeDocumentationSnippets() {
        new Documenter(application)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }

    @Test
    void writeModuleCanvases() {
        new Documenter(application)
                .writeModuleCanvases();
    }
}
