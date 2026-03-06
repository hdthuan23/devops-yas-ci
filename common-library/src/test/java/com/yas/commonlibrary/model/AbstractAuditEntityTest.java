package com.yas.commonlibrary.model;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class AbstractAuditEntityTest {

    // Concrete subclass to allow instantiation
    static class ConcreteAuditEntity extends AbstractAuditEntity {
    }

    @Test
    void testAbstractAuditEntity_instantiate_fieldsAreNullByDefault() {
        ConcreteAuditEntity entity = new ConcreteAuditEntity();

        assertNull(entity.getCreatedBy());
        assertNull(entity.getLastModifiedBy());
        assertNull(entity.getCreatedOn());
        assertNull(entity.getLastModifiedOn());
    }

    @Test
    void testAbstractAuditEntity_setCreatedBy_returnsCorrectValue() {
        ConcreteAuditEntity entity = new ConcreteAuditEntity();
        entity.setCreatedBy("user1");

        org.junit.jupiter.api.Assertions.assertEquals("user1", entity.getCreatedBy());
    }

    @Test
    void testAbstractAuditEntity_setLastModifiedBy_returnsCorrectValue() {
        ConcreteAuditEntity entity = new ConcreteAuditEntity();
        entity.setLastModifiedBy("admin");

        org.junit.jupiter.api.Assertions.assertEquals("admin", entity.getLastModifiedBy());
    }
}
