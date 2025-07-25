package com.gov.core.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Converter
@Slf4j
public class JpaConverterJson implements AttributeConverter<Map<String, Object>, String> {

    private static final ObjectMapper objectMapper = createObjectMapper();

    /**
     * ObjectMapper 설정
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Java 8 시간 API 지원
        mapper.registerModule(new JavaTimeModule());

        // 날짜를 타임스탬프가 아닌 ISO 문자열로 직렬화
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // null 값 무시
        mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);

        return mapper;
    }

    /**
     * 엔티티 속성을 데이터베이스 컬럼으로 변환 (Map -> JSON String)
     */
    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        try {
            String json = objectMapper.writeValueAsString(attribute);
            log.debug("JSON 직렬화: {} -> {}", attribute, json);
            return json;

        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패: {}", attribute, e);
            throw new IllegalArgumentException("JSON 직렬화 중 오류 발생", e);
        }
    }

    /**
     * 데이터베이스 컬럼을 엔티티 속성으로 변환 (JSON String -> Map)
     */
    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
            Map<String, Object> result = objectMapper.readValue(dbData, typeRef);

            log.debug("JSON 역직렬화: {} -> {}", dbData, result);
            return result != null ? result : new HashMap<>();

        } catch (JsonProcessingException e) {
            log.error("JSON 역직렬화 실패: {}", dbData, e);

            // 파싱 실패 시 빈 Map 반환 (장애 방지)
            log.warn("JSON 파싱 실패로 빈 Map 반환: {}", dbData);
            return new HashMap<>();
        }
    }
}
