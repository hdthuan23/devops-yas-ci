package com.yas.recommendation.vector.common.formatter;

import static org.junit.jupiter.api.Assertions.*;

import tools.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultDocumentFormatterTest {

    private DefaultDocumentFormatter formatter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        formatter = new DefaultDocumentFormatter();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testFormat_withSimpleTemplate_shouldReplaceVariables() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("name", "Product A");
        entityMap.put("price", "100");
        String template = "Name: {name}, Price: {price}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertEquals("Name: Product A, Price: 100", result);
    }

    @Test
    void testFormat_withHtmlTags_shouldRemoveHtmlTags() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("description", "<p>Great product</p>");
        String template = "Description: {description}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertEquals("Description: Great product", result);
    }

    @Test
    void testFormat_withMultipleHtmlTags_shouldRemoveAllHtmlTags() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("content", "<h1>Title</h1><p>Paragraph</p><strong>Bold</strong>");
        String template = "{content}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertEquals("TitleParagraphBold", result);
    }

    @Test
    void testFormat_withEmptyMap_shouldReturnTemplateAsIs() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        String template = "Static text without variables";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertEquals("Static text without variables", result);
    }

    @Test
    void testFormat_withMissingVariable_shouldKeepPlaceholder() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("name", "Product");
        String template = "Name: {name}, Category: {category}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertEquals("Name: Product, Category: {category}", result);
    }

    @Test
    void testFormat_withEmptyTemplate_shouldReturnEmptyString() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("key", "value");
        String template = "";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertEquals("", result);
    }

    @Test
    void testFormat_withNullValues_shouldKeepPlaceholder() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("name", null);
        String template = "Name: {name}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        // StringSubstitutor doesn't replace null values, keeps placeholder
        assertEquals("Name: {name}", result);
    }

    @Test
    void testFormat_withNumericValues_shouldConvertToString() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("count", 42);
        entityMap.put("price", 99.99);
        String template = "Count: {count}, Price: {price}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertEquals("Count: 42, Price: 99.99", result);
    }

    @Test
    void testFormat_withNestedHtmlAndVariables_shouldProcessBoth() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("title", "Product");
        entityMap.put("desc", "<div><p>Description</p></div>");
        String template = "{title}: {desc}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertEquals("Product: Description", result);
    }

    @Test
    void testFormat_withSpecialCharacters_shouldHandleCorrectly() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("text", "Special &amp; chars");
        String template = "Text: {text}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertEquals("Text: Special &amp; chars", result);
    }
}
