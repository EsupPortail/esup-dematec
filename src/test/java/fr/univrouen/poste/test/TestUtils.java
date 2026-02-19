package fr.univrouen.poste.test;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

public class TestUtils {

    public static MockMultipartFile getPdfFile() throws IOException {
        String filePath = "src/test/resources/doc-dummy.pdf";
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("Le fichier de test n'existe pas : " + filePath);
        }
        FileInputStream fis = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "doc-dummy.pdf",
                "application/pdf",
                fis
        );
        return  multipartFile;
    }

    public static MultiValueMap<String, String> getParamsAsStringMap(Object obj, String... params2exclude) {
        // ObjectMapper pour sérialisation/désérialisation form-urlencoded parameters
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDateFormat(new SimpleDateFormat("dd/MM/yyyy HH:mm"));

        // --- MixIn : ajoute @JsonFilter sur la classe sans la toucher ---
        objectMapper.addMixIn(obj.getClass(), ExcludeFilterMixin.class);
        SimpleFilterProvider excludeFilter = new SimpleFilterProvider();
        excludeFilter.addFilter("excludeParams", SimpleBeanPropertyFilter.serializeAllExcept(params2exclude));
        objectMapper.setFilterProvider(excludeFilter);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        Map<String, Object> fields = objectMapper.convertValue(obj, Map.class);
        fields.forEach((k, v) -> {
            if (v != null) {
                map.add(k, v.toString());
            }
        });
        return map;
    }

    // Interface interne suffisant, pas besoin de classe à part
    @JsonFilter("excludeParams")
    private interface ExcludeFilterMixin {}

}
