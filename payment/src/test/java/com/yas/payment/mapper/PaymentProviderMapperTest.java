package com.yas.payment.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yas.payment.model.PaymentProvider;
import com.yas.payment.viewmodel.paymentprovider.CreatePaymentVm;
import com.yas.payment.viewmodel.paymentprovider.PaymentProviderVm;
import com.yas.payment.viewmodel.paymentprovider.UpdatePaymentVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("Payment Provider Mapper Tests")
class PaymentProviderMapperTest {

    private PaymentProviderMapper paymentProviderMapper;
    private CreatePaymentProviderMapper createPaymentProviderMapper;
    private UpdatePaymentProviderMapper updatePaymentProviderMapper;

    @BeforeEach
    void setUp() {
        paymentProviderMapper = Mappers.getMapper(PaymentProviderMapper.class);
        createPaymentProviderMapper = Mappers.getMapper(CreatePaymentProviderMapper.class);
        updatePaymentProviderMapper = Mappers.getMapper(UpdatePaymentProviderMapper.class);
    }

    @Nested
    @DisplayName("PaymentProvider to PaymentProviderVm Mapping Tests")
    class ToVmTests {
        @Test
        @DisplayName("Should map PaymentProvider to PaymentProviderVm")
        void toVm_mappingSuccess() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("PAYPAL");
            provider.setName("PayPal");
            provider.setEnabled(true);
            provider.setAdditionalSettings("api-key=secret");
            provider.setLandingViewComponentName("paypal-component");
            provider.setConfigureUrl("https://paypal.com/config");

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertNotNull(result);
            assertEquals("PAYPAL", result.getId());
            assertEquals("PayPal", result.getName());
            assertEquals("https://paypal.com/config", result.getConfigureUrl());
        }

        @Test
        @DisplayName("Should handle null additional settings")
        void toVm_nullSettings() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("STRIPE");
            provider.setName("Stripe");
            provider.setEnabled(false);
            provider.setAdditionalSettings(null);

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertNotNull(result);
            assertEquals("STRIPE", result.getId());
            assertEquals("Stripe", result.getName());
        }

        @Test
        @DisplayName("Should map all provider fields")
        void toVm_allFieldsMapped() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("TEST-PROVIDER");
            provider.setName("Test Provider");
            provider.setEnabled(true);
            provider.setConfigureUrl("https://test.com");
            provider.setLandingViewComponentName("test-landing");
            provider.setAdditionalSettings("test-settings");

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertNotNull(result);
            assertThat(result.getId()).isEqualTo("TEST-PROVIDER");
            assertThat(result.getName()).isEqualTo("Test Provider");
            assertThat(result.getConfigureUrl()).isEqualTo("https://test.com");
        }

        @Test
        @DisplayName("Should handle empty additional settings")
        void toVm_emptyAdditionalSettings() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("EMPTY-SETTINGS");
            provider.setName("Empty Settings Provider");
            provider.setEnabled(true);
            provider.setAdditionalSettings("");

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertNotNull(result);
            assertEquals("EMPTY-SETTINGS", result.getId());
            assertEquals("Empty Settings Provider", result.getName());
        }

        @Test
        @DisplayName("Should preserve configure URL")
        void toVm_preserveConfigureUrl() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("CONFIG-URL-TEST");
            provider.setName("Config URL Provider");
            provider.setConfigureUrl("https://example.com/configure");

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertNotNull(result);
            assertEquals("https://example.com/configure", result.getConfigureUrl());
        }
    }

    @Nested
    @DisplayName("CreatePaymentVm to PaymentProvider Mapping Tests")
    class CreateMapperTests {
        @Test
        @DisplayName("Should map CreatePaymentVm to PaymentProvider with isNew=true")
        void createVmToModel_mappingSuccess() {
            CreatePaymentVm vm = new CreatePaymentVm();
            vm.setId("NEW-PAYPAL");
            vm.setName("New PayPal");
            vm.setEnabled(true);
            vm.setAdditionalSettings("new-settings");
            vm.setConfigureUrl("https://new.paypal.com");
            vm.setLandingViewComponentName("new-landing");

            PaymentProvider result = createPaymentProviderMapper.toModel(vm);

            assertNotNull(result);
            assertEquals("NEW-PAYPAL", result.getId());
            assertEquals("New PayPal", result.getName());
            assertTrue(result.isEnabled());
        }

        @Test
        @DisplayName("Should create PaymentProviderVm from PaymentProvider")
        void createMapperToVm_success() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("CREATED");
            provider.setName("Created Provider");
            provider.setEnabled(true);
            provider.setConfigureUrl("https://created.example.com");

            PaymentProviderVm result = createPaymentProviderMapper.toVmResponse(provider);

            assertNotNull(result);
            assertEquals("CREATED", result.getId());
            assertEquals("Created Provider", result.getName());
        }

        @Test
        @DisplayName("Should handle partial update on create mapper")
        void createMapperPartialUpdate_success() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("PARTIAL");
            provider.setName("Partial Provider");

            CreatePaymentVm vm = new CreatePaymentVm();
            vm.setName("Partial Updated");

            createPaymentProviderMapper.partialUpdate(provider, vm);

            assertNotNull(provider);
            assertEquals("PARTIAL", provider.getId());
            assertEquals("Partial Updated", provider.getName());
        }

        @Test
        @DisplayName("Should map CreatePaymentVm with all fields populated")
        void createVmToModel_allFieldsPopulated() {
            CreatePaymentVm vm = new CreatePaymentVm();
            vm.setId("FULL-PAYMENT");
            vm.setName("Full Payment Provider");
            vm.setEnabled(true);
            vm.setAdditionalSettings("complete-settings");
            vm.setConfigureUrl("https://full.example.com");
            vm.setLandingViewComponentName("full-landing-component");

            PaymentProvider result = createPaymentProviderMapper.toModel(vm);

            assertNotNull(result);
            assertThat(result.getId()).isEqualTo("FULL-PAYMENT");
            assertThat(result.getName()).isEqualTo("Full Payment Provider");
            assertTrue(result.isEnabled());
        }

        @Test
        @DisplayName("Should handle disabled payment provider creation")
        void createVmToModel_disabledProvider() {
            CreatePaymentVm vm = new CreatePaymentVm();
            vm.setId("DISABLED");
            vm.setName("Disabled Provider");
            vm.setEnabled(false);

            PaymentProvider result = createPaymentProviderMapper.toModel(vm);

            assertNotNull(result);
            assertEquals("DISABLED", result.getId());
            assertFalse(result.isEnabled());
        }

        @Test
        @DisplayName("Should map provider with additional settings")
        void createVmToModel_withAdditionalSettings() {
            CreatePaymentVm vm = new CreatePaymentVm();
            vm.setId("WITH-SETTINGS");
            vm.setName("Provider With Settings");
            vm.setAdditionalSettings("key1=value1;key2=value2");

            PaymentProvider result = createPaymentProviderMapper.toModel(vm);

            assertNotNull(result);
            assertEquals("WITH-SETTINGS", result.getId());
            assertEquals("key1=value1;key2=value2", result.getAdditionalSettings());
        }
    }

    @Nested
    @DisplayName("UpdatePaymentVm to PaymentProvider Mapping Tests")
    class UpdateMapperTests {
        @Test
        @DisplayName("Should map UpdatePaymentVm to PaymentProvider")
        void updateVmToModel_mappingSuccess() {
            UpdatePaymentVm vm = new UpdatePaymentVm();
            vm.setId("UPDATE-PAYPAL");
            vm.setName("Updated PayPal");
            vm.setEnabled(false);
            vm.setAdditionalSettings("updated-settings");
            vm.setConfigureUrl("https://updated.paypal.com");
            vm.setLandingViewComponentName("updated-landing");

            PaymentProvider result = updatePaymentProviderMapper.toModel(vm);

            assertNotNull(result);
            assertEquals("UPDATE-PAYPAL", result.getId());
            assertEquals("Updated PayPal", result.getName());
            assertFalse(result.isEnabled());
        }

        @Test
        @DisplayName("Should create PaymentProviderVm from updated PaymentProvider")
        void updateMapperToVm_success() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("UPDATED");
            provider.setName("Updated Provider");
            provider.setEnabled(false);
            provider.setConfigureUrl("https://updated.example.com");

            PaymentProviderVm result = updatePaymentProviderMapper.toVmResponse(provider);

            assertNotNull(result);
            assertEquals("UPDATED", result.getId());
            assertEquals("Updated Provider", result.getName());
            assertEquals("https://updated.example.com", result.getConfigureUrl());
        }

        @Test
        @DisplayName("Should handle partial update on update mapper")
        void updateMapperPartialUpdate_success() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("PARTIAL-UPDATE");
            provider.setName("Original Name");

            UpdatePaymentVm vm = new UpdatePaymentVm();
            vm.setId("PARTIAL-UPDATE");
            vm.setName("Modified Name");

            updatePaymentProviderMapper.partialUpdate(provider, vm);

            assertNotNull(provider);
        }

        @Test
        @DisplayName("Should update payment provider configuration URL")
        void updateVmToModel_configUrlUpdate() {
            UpdatePaymentVm vm = new UpdatePaymentVm();
            vm.setId("UPDATE-CONFIG");
            vm.setName("Update Config Provider");
            vm.setEnabled(true);
            vm.setConfigureUrl("https://new-config.example.com");

            PaymentProvider result = updatePaymentProviderMapper.toModel(vm);

            assertNotNull(result);
            assertEquals("UPDATE-CONFIG", result.getId());
            assertEquals("https://new-config.example.com", result.getConfigureUrl());
        }

        @Test
        @DisplayName("Should update payment provider additional settings")
        void updateVmToModel_settingsUpdate() {
            UpdatePaymentVm vm = new UpdatePaymentVm();
            vm.setId("UPDATE-SETTINGS");
            vm.setName("Update Settings Provider");
            vm.setAdditionalSettings("new-api-key=abc123");

            PaymentProvider result = updatePaymentProviderMapper.toModel(vm);

            assertNotNull(result);
            assertEquals("UPDATE-SETTINGS", result.getId());
            assertEquals("new-api-key=abc123", result.getAdditionalSettings());
        }
    }

    @Nested
    @DisplayName("Mapper Multiple Provider Tests")
    class MultipleProviderMapperTests {
        @Test
        @DisplayName("Should map multiple providers to VMs")
        void toVm_multipleProviders() {
            PaymentProvider provider1 = createProvider("PAYPAL", "PayPal");
            PaymentProvider provider2 = createProvider("STRIPE", "Stripe");

            PaymentProviderVm vm1 = paymentProviderMapper.toVm(provider1);
            PaymentProviderVm vm2 = paymentProviderMapper.toVm(provider2);

            assertNotNull(vm1);
            assertNotNull(vm2);
            assertEquals("PAYPAL", vm1.getId());
            assertEquals("STRIPE", vm2.getId());
        }

        @Test
        @DisplayName("Should preserve enabled status during mapping to model")
        void toModel_enabledStatusPreserved() {
            CreatePaymentVm enabledVm = createCreateVm("ENABLED", "Enabled");
            enabledVm.setEnabled(true);

            CreatePaymentVm disabledVm = createCreateVm("DISABLED", "Disabled");
            disabledVm.setEnabled(false);

            PaymentProvider enabledProvider = createPaymentProviderMapper.toModel(enabledVm);
            PaymentProvider disabledProvider = createPaymentProviderMapper.toModel(disabledVm);

            assertNotNull(enabledProvider);
            assertNotNull(disabledProvider);
            assertTrue(enabledProvider.isEnabled());
            assertFalse(disabledProvider.isEnabled());
        }

        @Test
        @DisplayName("Should map provider with special characters in settings")
        void toVm_specialCharactersInSettings() {
            PaymentProvider provider = createProvider("SPECIAL", "Special");
            provider.setAdditionalSettings("key=value!@#$%^&*()_+-=[]{}|;':\",./<>?");

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertNotNull(result);
            assertEquals("SPECIAL", result.getId());
        }

        @Test
        @DisplayName("Should map multiple providers with different configurations")
        void toVm_differentConfigurations() {
            PaymentProvider provider1 = createProvider("CONFIG1", "Configuration 1");
            provider1.setConfigureUrl("https://config1.example.com");
            provider1.setAdditionalSettings("api-v1");

            PaymentProvider provider2 = createProvider("CONFIG2", "Configuration 2");
            provider2.setConfigureUrl("https://config2.example.com");
            provider2.setAdditionalSettings("api-v2");

            PaymentProviderVm vm1 = paymentProviderMapper.toVm(provider1);
            PaymentProviderVm vm2 = paymentProviderMapper.toVm(provider2);

            assertNotNull(vm1);
            assertNotNull(vm2);
            assertEquals("CONFIG1", vm1.getId());
            assertEquals("CONFIG2", vm2.getId());
            assertEquals("https://config1.example.com", vm1.getConfigureUrl());
            assertEquals("https://config2.example.com", vm2.getConfigureUrl());
        }

        @Test
        @DisplayName("Should map multiple create VMs to providers")
        void createVmToModel_multipleProviders() {
            CreatePaymentVm vm1 = createCreateVm("CREATE1", "Create Provider 1");
            vm1.setEnabled(true);

            CreatePaymentVm vm2 = createCreateVm("CREATE2", "Create Provider 2");
            vm2.setEnabled(false);

            PaymentProvider result1 = createPaymentProviderMapper.toModel(vm1);
            PaymentProvider result2 = createPaymentProviderMapper.toModel(vm2);

            assertNotNull(result1);
            assertNotNull(result2);
            assertEquals("CREATE1", result1.getId());
            assertEquals("CREATE2", result2.getId());
            assertTrue(result1.isEnabled());
            assertFalse(result2.isEnabled());
        }

        @Test
        @DisplayName("Should map multiple update VMs to providers")
        void updateVmToModel_multipleProviders() {
            UpdatePaymentVm vm1 = createUpdateVm("UPDATE1", "Update Provider 1");
            vm1.setEnabled(true);

            UpdatePaymentVm vm2 = createUpdateVm("UPDATE2", "Update Provider 2");
            vm2.setEnabled(false);

            PaymentProvider result1 = updatePaymentProviderMapper.toModel(vm1);
            PaymentProvider result2 = updatePaymentProviderMapper.toModel(vm2);

            assertNotNull(result1);
            assertNotNull(result2);
            assertEquals("UPDATE1", result1.getId());
            assertEquals("UPDATE2", result2.getId());
            assertTrue(result1.isEnabled());
            assertFalse(result2.isEnabled());
        }

        @Test
        @DisplayName("Should handle null fields in mapping")
        void toVm_nullFields() {
            PaymentProvider provider = createProvider("NULL-TEST", "Null Test Provider");
            provider.setAdditionalSettings(null);
            provider.setLandingViewComponentName(null);

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertNotNull(result);
            assertEquals("NULL-TEST", result.getId());
        }
    }

    private PaymentProvider createProvider(String id, String name) {
        PaymentProvider provider = new PaymentProvider();
        provider.setId(id);
        provider.setName(name);
        provider.setEnabled(true);
        return provider;
    }

    private CreatePaymentVm createCreateVm(String id, String name) {
        CreatePaymentVm vm = new CreatePaymentVm();
        vm.setId(id);
        vm.setName(name);
        vm.setConfigureUrl("https://" + id.toLowerCase() + ".example.com");
        return vm;
    }

    private UpdatePaymentVm createUpdateVm(String id, String name) {
        UpdatePaymentVm vm = new UpdatePaymentVm();
        vm.setId(id);
        vm.setName(name);
        vm.setConfigureUrl("https://" + id.toLowerCase() + ".example.com");
        return vm;
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesTests {
        @Test
        @DisplayName("Should map provider with null configure URL")
        void toVm_nullConfigureUrl() {
            PaymentProvider provider = createProvider("NULL-URL", "Null URL");
            provider.setConfigureUrl(null);

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertNotNull(result);
            assertEquals("NULL-URL", result.getId());
        }

        @Test
        @DisplayName("Should map provider with empty string fields")
        void toVm_emptyStringFields() {
            PaymentProvider provider = createProvider("EMPTY", "Empty Fields");
            provider.setAdditionalSettings("");
            provider.setConfigureUrl("");
            provider.setLandingViewComponentName("");

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertNotNull(result);
            assertEquals("EMPTY", result.getId());
        }

        @Test
        @DisplayName("Should handle create mapper with null values")
        void createVmToModel_nullValues() {
            CreatePaymentVm vm = new CreatePaymentVm();
            vm.setId("NULL-VALUES");
            vm.setName("Null Values");
            vm.setAdditionalSettings(null);
            vm.setConfigureUrl(null);
            vm.setLandingViewComponentName(null);

            PaymentProvider result = createPaymentProviderMapper.toModel(vm);

            assertNotNull(result);
            assertEquals("NULL-VALUES", result.getId());
        }

        @Test
        @DisplayName("Should handle update mapper with null values")
        void updateVmToModel_nullValues() {
            UpdatePaymentVm vm = new UpdatePaymentVm();
            vm.setId("UPDATE-NULL");
            vm.setName("Update Null");
            vm.setAdditionalSettings(null);
            vm.setConfigureUrl(null);

            PaymentProvider result = updatePaymentProviderMapper.toModel(vm);

            assertNotNull(result);
            assertEquals("UPDATE-NULL", result.getId());
        }

        @Test
        @DisplayName("Should map provider with media ID")
        void toVm_withMediaId() {
            PaymentProvider provider = createProvider("WITH-MEDIA", "With Media");
            provider.setMediaId(12345L);

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertNotNull(result);
            assertEquals("WITH-MEDIA", result.getId());
            assertEquals(12345L, result.getMediaId());
        }

        @Test
        @DisplayName("Should map provider with version")
        void toVm_withVersion() {
            PaymentProvider provider = createProvider("VERSION", "Version Test");
            provider.setVersion(5);

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertNotNull(result);
            assertEquals("VERSION", result.getId());
            assertEquals(5, result.getVersion());
        }

        @Test
        @DisplayName("Should handle very long configure URL")
        void toVm_veryLongConfigureUrl() {
            PaymentProvider provider = createProvider("LONG-URL", "Long URL");
            String longUrl = "https://example.com/" + "a".repeat(500);
            provider.setConfigureUrl(longUrl);

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertNotNull(result);
            assertEquals(longUrl, result.getConfigureUrl());
        }

        @Test
        @DisplayName("Should handle special characters in ID and name")
        void toVm_specialCharactersInIdAndName() {
            PaymentProvider provider = createProvider("SPECIAL-ID-123", "Name with Spaces & Special");
            provider.setLandingViewComponentName("component-with-dashes");

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertNotNull(result);
            assertEquals("SPECIAL-ID-123", result.getId());
            assertEquals("Name with Spaces & Special", result.getName());
        }

        @Test
        @DisplayName("Should map create VM with long additional settings")
        void createVmToModel_longAdditionalSettings() {
            CreatePaymentVm vm = new CreatePaymentVm();
            vm.setId("LONG-SETTINGS");
            vm.setName("Long Settings");
            vm.setAdditionalSettings("key1=value1;key2=value2;key3=value3;" + "key4=value4;".repeat(100));

            PaymentProvider result = createPaymentProviderMapper.toModel(vm);

            assertNotNull(result);
            assertTrue(result.getAdditionalSettings().length() > 500);
        }

        @Test
        @DisplayName("Should update mapper handle landing component name")
        void updateVmToModel_landingComponentName() {
            UpdatePaymentVm vm = new UpdatePaymentVm();
            vm.setId("UPDATE-COMPONENT");
            vm.setName("Update Component");
            vm.setLandingViewComponentName("custom-landing-component");

            PaymentProvider result = updatePaymentProviderMapper.toModel(vm);

            assertNotNull(result);
            assertEquals("UPDATE-COMPONENT", result.getId());
            assertEquals("custom-landing-component", result.getLandingViewComponentName());
        }

        @Test
        @DisplayName("Should handle partial update with only name change")
        void updateMapperPartialUpdate_onlyNameChange() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("UPDATE-NAME");
            provider.setName("Original Name");
            provider.setEnabled(true);
            provider.setConfigureUrl("https://original.com");

            UpdatePaymentVm vm = new UpdatePaymentVm();
            vm.setName("Updated Name");
            vm.setEnabled(true);

            updatePaymentProviderMapper.partialUpdate(provider, vm);

            assertEquals("Updated Name", provider.getName());
            assertEquals("https://original.com", provider.getConfigureUrl());
            assertTrue(provider.isEnabled());
        }

        @Test
        @DisplayName("Should handle mapping with enabled status transitions")
        void toVm_enabledStatusTransitions() {
            PaymentProvider enabledProvider = createProvider("ENABLED-TEST", "Enabled");
            enabledProvider.setEnabled(true);

            PaymentProvider disabledProvider = createProvider("DISABLED-TEST", "Disabled");
            disabledProvider.setEnabled(false);

            PaymentProviderVm enabledVm = paymentProviderMapper.toVm(enabledProvider);
            PaymentProviderVm disabledVm = paymentProviderMapper.toVm(disabledProvider);

            assertNotNull(enabledVm);
            assertNotNull(disabledVm);
        }

        @Test
        @DisplayName("Should create mapper handle enable/disable flow")
        void createVmToModel_enableDisableFlow() {
            CreatePaymentVm enableVm = createCreateVm("ENABLE", "Enable Provider");
            enableVm.setEnabled(true);

            CreatePaymentVm disableVm = createCreateVm("DISABLE", "Disable Provider");
            disableVm.setEnabled(false);

            PaymentProvider enableResult = createPaymentProviderMapper.toModel(enableVm);
            PaymentProvider disableResult = createPaymentProviderMapper.toModel(disableVm);

            assertTrue(enableResult.isEnabled());
            assertFalse(disableResult.isEnabled());
        }

        @Test
        @DisplayName("Should map response with all fields")
        void toVmResponse_allFieldsPresent() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("RESPONSE-FULL");
            provider.setName("Full Response Provider");
            provider.setEnabled(true);
            provider.setVersion(3);
            provider.setMediaId(999L);
            provider.setConfigureUrl("https://response.example.com");
            provider.setAdditionalSettings("response-settings");
            provider.setLandingViewComponentName("response-landing");

            PaymentProviderVm result = createPaymentProviderMapper.toVmResponse(provider);

            assertNotNull(result);
            assertEquals("RESPONSE-FULL", result.getId());
            assertEquals("Full Response Provider", result.getName());
        }

        @Test
        @DisplayName("Should handle partial updates in mapper")
        void updateMapperPartialUpdate_propertyUpdate() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("UPDATE-PROPS");
            provider.setName("Original");
            provider.setConfigureUrl("https://original.com");

            UpdatePaymentVm vm = new UpdatePaymentVm();
            vm.setId("NEW-ID");
            vm.setName("Updated");
            vm.setConfigureUrl("https://updated.com");

            updatePaymentProviderMapper.partialUpdate(provider, vm);

            assertNotNull(provider);
            assertEquals("Updated", provider.getName());
        }
    }

    @Nested
    @DisplayName("PaymentProviderMapper Detailed Field Coverage Tests")
    class DetailedMapperTests {
        @Test
        @DisplayName("Should map id field correctly")
        void toVm_idField() {
            PaymentProvider provider = createProvider("TEST-ID", "Test");
            PaymentProviderVm result = paymentProviderMapper.toVm(provider);
            assertEquals("TEST-ID", result.getId());
        }

        @Test
        @DisplayName("Should map name field correctly")
        void toVm_nameField() {
            PaymentProvider provider = createProvider("ID", "TEST-NAME");
            PaymentProviderVm result = paymentProviderMapper.toVm(provider);
            assertEquals("TEST-NAME", result.getName());
        }

        @Test
        @DisplayName("Should map configureUrl field correctly")
        void toVm_configureUrlField() {
            PaymentProvider provider = createProvider("ID", "Name");
            provider.setConfigureUrl("https://test.example.com");
            PaymentProviderVm result = paymentProviderMapper.toVm(provider);
            assertEquals("https://test.example.com", result.getConfigureUrl());
        }

        @Test
        @DisplayName("Should map version field correctly")
        void toVm_versionField() {
            PaymentProvider provider = createProvider("ID", "Name");
            provider.setVersion(7);
            PaymentProviderVm result = paymentProviderMapper.toVm(provider);
            assertEquals(7, result.getVersion());
        }

        @Test
        @DisplayName("Should map mediaId field correctly")
        void toVm_mediaIdField() {
            PaymentProvider provider = createProvider("ID", "Name");
            provider.setMediaId(12345L);
            PaymentProviderVm result = paymentProviderMapper.toVm(provider);
            assertEquals(12345L, result.getMediaId());
        }

        @Test
        @DisplayName("Should map all fields in one object")
        void toVm_allFieldsTogether() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("COMPLETE-ID");
            provider.setName("Complete Name");
            provider.setConfigureUrl("https://complete.example.com");
            provider.setVersion(10);
            provider.setMediaId(55555L);
            provider.setEnabled(true);
            provider.setAdditionalSettings("complete-settings");
            provider.setLandingViewComponentName("complete-landing");

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertNotNull(result);
            assertEquals("COMPLETE-ID", result.getId());
            assertEquals("Complete Name", result.getName());
            assertEquals("https://complete.example.com", result.getConfigureUrl());
            assertEquals(10, result.getVersion());
            assertEquals(55555L, result.getMediaId());
        }

        @Test
        @DisplayName("Should handle provider with minimum required fields")
        void toVm_minimumFields() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("MIN-ID");
            provider.setName("Min Name");

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertNotNull(result);
            assertEquals("MIN-ID", result.getId());
            assertEquals("Min Name", result.getName());
        }

        @Test
        @DisplayName("Should map provider with zero version")
        void toVm_zeroVersion() {
            PaymentProvider provider = createProvider("ZERO", "Zero Version");
            provider.setVersion(0);
            PaymentProviderVm result = paymentProviderMapper.toVm(provider);
            assertEquals(0, result.getVersion());
        }

        @Test
        @DisplayName("Should map provider with null mediaId")
        void toVm_nullMediaId() {
            PaymentProvider provider = createProvider("NULL-MEDIA", "Null Media");
            provider.setMediaId(null);
            PaymentProviderVm result = paymentProviderMapper.toVm(provider);
            assertEquals(null, result.getMediaId());
        }

        @Test
        @DisplayName("Should map multiple providers field by field")
        void toVm_multipleProvidersFieldByField() {
            PaymentProvider provider1 = new PaymentProvider();
            provider1.setId("PROVIDER1");
            provider1.setName("Provider 1");
            provider1.setConfigureUrl("https://provider1.com");
            provider1.setVersion(1);

            PaymentProvider provider2 = new PaymentProvider();
            provider2.setId("PROVIDER2");
            provider2.setName("Provider 2");
            provider2.setConfigureUrl("https://provider2.com");
            provider2.setVersion(2);
            provider2.setMediaId(2L);

            PaymentProviderVm vm1 = paymentProviderMapper.toVm(provider1);
            PaymentProviderVm vm2 = paymentProviderMapper.toVm(provider2);

            assertThat(vm1.getId()).isEqualTo("PROVIDER1");
            assertThat(vm2.getId()).isEqualTo("PROVIDER2");
            assertThat(vm1.getVersion()).isEqualTo(1);
            assertThat(vm2.getVersion()).isEqualTo(2);
            assertThat(vm2.getMediaId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should create VM preserving all non-null fields")
        void toVm_preserveAllNonNullFields() {
            PaymentProvider provider = createProvider("PRESERVE", "Preserve Fields");
            provider.setConfigureUrl("https://preserve.example.com");
            provider.setVersion(5);
            provider.setMediaId(77777L);

            PaymentProviderVm result = paymentProviderMapper.toVm(provider);

            assertThat(result)
                    .isNotNull()
                    .extracting("id", "name", "configureUrl", "version", "mediaId")
                    .containsExactly("PRESERVE", "Preserve Fields", 
                            "https://preserve.example.com", 5, 77777L);
        }

        @Test
        @DisplayName("Should map create VM to provider with all fields")
        void createMapperToModel_allFieldsComplete() {
            CreatePaymentVm vm = new CreatePaymentVm();
            vm.setId("CREATE-COMPLETE");
            vm.setName("Create Complete");
            vm.setEnabled(true);
            vm.setConfigureUrl("https://create.example.com");
            vm.setAdditionalSettings("create-settings");
            vm.setLandingViewComponentName("create-landing");
            vm.setMediaId(111L);

            PaymentProvider result = createPaymentProviderMapper.toModel(vm);

            assertNotNull(result);
            assertEquals("CREATE-COMPLETE", result.getId());
            assertEquals("Create Complete", result.getName());
            assertTrue(result.isEnabled());
            assertEquals("https://create.example.com", result.getConfigureUrl());
            assertEquals("create-settings", result.getAdditionalSettings());
            assertEquals("create-landing", result.getLandingViewComponentName());
            assertEquals(111L, result.getMediaId());
        }

        @Test
        @DisplayName("Should map update VM to provider with all fields")
        void updateMapperToModel_allFieldsComplete() {
            UpdatePaymentVm vm = new UpdatePaymentVm();
            vm.setId("UPDATE-COMPLETE");
            vm.setName("Update Complete");
            vm.setEnabled(false);
            vm.setConfigureUrl("https://update.example.com");
            vm.setAdditionalSettings("update-settings");
            vm.setLandingViewComponentName("update-landing");
            vm.setMediaId(222L);

            PaymentProvider result = updatePaymentProviderMapper.toModel(vm);

            assertNotNull(result);
            assertEquals("UPDATE-COMPLETE", result.getId());
            assertEquals("Update Complete", result.getName());
            assertFalse(result.isEnabled());
            assertEquals("https://update.example.com", result.getConfigureUrl());
            assertEquals("update-settings", result.getAdditionalSettings());
            assertEquals("update-landing", result.getLandingViewComponentName());
            assertEquals(222L, result.getMediaId());
        }
    }
}
