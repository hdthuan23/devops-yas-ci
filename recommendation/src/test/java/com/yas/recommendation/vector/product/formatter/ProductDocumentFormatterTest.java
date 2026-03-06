package com.yas.recommendation.vector.product.formatter;

import static org.junit.jupiter.api.Assertions.*;

import tools.jackson.databind.ObjectMapper;
import com.yas.recommendation.viewmodel.CategoryVm;
import com.yas.recommendation.viewmodel.ProductAttributeValueVm;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductDocumentFormatterTest {

    private ProductDocumentFormatter formatter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        formatter = new ProductDocumentFormatter();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testFormat_withBasicProduct_shouldFormatCorrectly() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("name", "Product A");
        entityMap.put("description", "Test product");
        String template = "Product: {name}, Description: {description}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertEquals("Product: Product A, Description: Test product", result);
    }

    @Test
    void testFormat_withNullAttributeValues_shouldReturnEmptyBrackets() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("name", "Product");
        entityMap.put("attributeValues", null);
        String template = "Name: {name}, Attributes: {attributeValues}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertTrue(result.contains("Attributes: []"));
    }

    @Test
    void testFormat_withAttributeValues_shouldFormatAttributes() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        Map<String, Object> attr1 = new HashMap<>();
        attr1.put("id", 1L);
        attr1.put("nameProductAttribute", "Color");
        attr1.put("value", "Red");
        
        Map<String, Object> attr2 = new HashMap<>();
        attr2.put("id", 2L);
        attr2.put("nameProductAttribute", "Size");
        attr2.put("value", "Large");
        
        List<Map<String, Object>> attributes = Arrays.asList(attr1, attr2);
        entityMap.put("attributeValues", attributes);
        String template = "Attributes: {attributeValues}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertTrue(result.contains("[Color: Red, Size: Large]"));
    }

    @Test
    void testFormat_withSingleAttribute_shouldFormatSingleAttribute() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        Map<String, Object> attr = new HashMap<>();
        attr.put("id", 1L);
        attr.put("nameProductAttribute", "Brand");
        attr.put("value", "Nike");
        
        List<Map<String, Object>> attributes = Collections.singletonList(attr);
        entityMap.put("attributeValues", attributes);
        String template = "{attributeValues}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertEquals("[Brand: Nike]", result);
    }

    @Test
    void testFormat_withNullCategories_shouldReturnEmptyBrackets() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("name", "Product");
        entityMap.put("categories", null);
        String template = "Categories: {categories}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertTrue(result.contains("Categories: []"));
    }

    @Test
    void testFormat_withCategories_shouldFormatCategories() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        Map<String, Object> cat1 = new HashMap<>();
        cat1.put("id", 1L);
        cat1.put("name", "Electronics");
        cat1.put("slug", "electronics");
        
        Map<String, Object> cat2 = new HashMap<>();
        cat2.put("id", 2L);
        cat2.put("name", "Computers");
        cat2.put("slug", "computers");
        
        List<Map<String, Object>> categories = Arrays.asList(cat1, cat2);
        entityMap.put("categories", categories);
        String template = "Categories: {categories}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertTrue(result.contains("[Electronics, Computers]"));
    }

    @Test
    void testFormat_withHtmlTags_shouldRemoveHtmlTags() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("description", "<p>Great <strong>product</strong></p>");
        String template = "{description}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertEquals("Great product", result);
    }

    @Test
    void testFormat_withAttributesAndCategories_shouldFormatBoth() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();        
        Map<String, Object> attr1 = new HashMap<>();
        attr1.put("id", 1L);
        attr1.put("nameProductAttribute", "RAM");
        attr1.put("value", "16GB");
        entityMap.put("attributeValues", Collections.singletonList(attr1));
        
        Map<String, Object> cat1 = new HashMap<>();
        cat1.put("id", 1L);
        cat1.put("name", "Electronics");
        cat1.put("slug", "electronics");
        entityMap.put("categories", Collections.singletonList(cat1));
        
        String template = "Attrs: {attributeValues}, Cats: {categories}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("RAM"));
        assertTrue(result.contains("Electronics"));
    }

    @Test
    void testFormat_withEmptyAttributesList_shouldReturnEmptyBrackets() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("attributeValues", Collections.emptyList());
        String template = "Attributes: {attributeValues}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertEquals("Attributes: []", result);
    }

    @Test
    void testFormat_withEmptyCategoriesList_shouldReturnEmptyBrackets() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("categories", Collections.emptyList());
        String template = "Categories: {categories}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertEquals("Categories: []", result);
    }

    @Test
    void testFormat_withMultipleCategories_shouldJoinWithComma() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        Map<String, Object> cat1 = new HashMap<>();
        cat1.put("id", 1L);
        cat1.put("name", "Category1");
        cat1.put("slug", "cat1");
        
        Map<String, Object> cat2 = new HashMap<>();
        cat2.put("id", 2L);
        cat2.put("name", "Category2");
        cat2.put("slug", "cat2");
        
        Map<String, Object> cat3 = new HashMap<>();
        cat3.put("id", 3L);
        cat3.put("name", "Category3");
        cat3.put("slug", "cat3");
        
        entityMap.put("categories", Arrays.asList(cat1, cat2, cat3));
        String template = "{categories}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertEquals("[Category1, Category2, Category3]", result);
    }

    @Test
    void testFormat_withComplexHtmlContent_shouldCleanAll() {
        // Given
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("content", "<div><h1>Title</h1><p>Paragraph with <a href='#'>link</a> and <img src='img.jpg'/></p></div>");
        String template = "{content}";

        // When
        String result = formatter.format(entityMap, template, objectMapper);

        // Then
        assertFalse(result.contains("<"));
        assertFalse(result.contains(">"));
        assertTrue(result.contains("Title"));
        assertTrue(result.contains("Paragraph"));
    }
}
