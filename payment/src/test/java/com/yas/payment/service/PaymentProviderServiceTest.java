package com.yas.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.payment.mapper.CreatePaymentProviderMapper;
import com.yas.payment.mapper.PaymentProviderMapper;
import com.yas.payment.mapper.UpdatePaymentProviderMapper;
import com.yas.payment.model.PaymentProvider;
import com.yas.payment.repository.PaymentProviderRepository;
import com.yas.payment.viewmodel.paymentprovider.CreatePaymentVm;
import com.yas.payment.viewmodel.paymentprovider.MediaVm;
import com.yas.payment.viewmodel.paymentprovider.PaymentProviderVm;
import com.yas.payment.viewmodel.paymentprovider.UpdatePaymentVm;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.domain.Pageable;

class PaymentProviderServiceTest {

    public static final String[] IGNORED_FIELDS = {"version", "iconUrl"};

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private PaymentProviderService paymentProviderService;

    @Mock
    private PaymentProviderRepository paymentProviderRepository;

    @Spy
    private PaymentProviderMapper paymentProviderMapper = Mappers.getMapper(
        PaymentProviderMapper.class
    );

    @Spy
    private CreatePaymentProviderMapper createPaymentProviderMapper = Mappers.getMapper(
        CreatePaymentProviderMapper.class
    );

    @Spy
    private UpdatePaymentProviderMapper updatePaymentProviderMapper = Mappers.getMapper(
        UpdatePaymentProviderMapper.class
    );

    private PaymentProvider paymentProvider;

    private Pageable defaultPageable = Pageable.ofSize(10);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentProvider = new PaymentProvider();
        paymentProvider.setId("providerId");
        paymentProvider.setAdditionalSettings("additional settings");
        paymentProvider.setEnabled(true);
        paymentProvider.setMediaId(1L);
    }

    @Nested
    @DisplayName("Create Payment Provider Tests")
    class CreatePaymentProviderTests {
        @Test
        @DisplayName("Create Payment Provider successfully")
        void createPaymentProvider() {
            // Given
            var randomVal = UUID.randomUUID().toString();
            CreatePaymentVm createPaymentRequest = getCreatePaymentVm(randomVal);
            PaymentProvider provider = getPaymentProvider(randomVal);
            when(paymentProviderRepository.save(any())).thenReturn(provider);

            // When
            var result = paymentProviderService.create(createPaymentRequest);

            // Then
            verify(paymentProviderRepository, times(1)).save(any());
            assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields(IGNORED_FIELDS)
                .isEqualTo(createPaymentRequest);
        }

        @Test
        @DisplayName("Create Payment Provider with all fields")
        void createPaymentProvider_withAllFields() {
            CreatePaymentVm createPaymentRequest = new CreatePaymentVm();
            createPaymentRequest.setId("test-provider");
            createPaymentRequest.setName("Test Provider");
            createPaymentRequest.setEnabled(true);
            createPaymentRequest.setConfigureUrl("http://configure.test");
            createPaymentRequest.setLandingViewComponentName("test-component");

            PaymentProvider savedProvider = new PaymentProvider();
            savedProvider.setId("test-provider");
            savedProvider.setName("Test Provider");
            savedProvider.setEnabled(true);

            when(paymentProviderRepository.save(any())).thenReturn(savedProvider);

            var result = paymentProviderService.create(createPaymentRequest);

            assertNotNull(result);
            assertEquals("test-provider", result.getId());
            verify(paymentProviderRepository, times(1)).save(any());
        }
    }

    @Nested
    @DisplayName("Update Payment Provider Tests")
    class UpdatePaymentProviderTests {
        @Test
        @DisplayName("Update Payment Provider successfully")
        void updatePaymentProvider() {
            // Given
            var randomVal = UUID.randomUUID().toString();
            UpdatePaymentVm updatePaymentRequest = getUpdatePaymentVm(randomVal);
            PaymentProvider provider = getPaymentProvider(randomVal);
            when(paymentProviderRepository.findById(randomVal)).thenReturn(Optional.of(provider));
            when(paymentProviderRepository.save(any())).thenReturn(provider);

            // When
            var result = paymentProviderService.update(updatePaymentRequest);

            // Then
            verify(paymentProviderRepository, times(1)).save(any());
            assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields(IGNORED_FIELDS)
                .isEqualTo(updatePaymentRequest);
        }

        @Test
        @DisplayName("Update non-existing Payment Provider, Service should throw NotFoundException")
        void updateNonExistPaymentProvider() {
            // Given
            var randomVal = UUID.randomUUID().toString();
            UpdatePaymentVm createPaymentRequest = getUpdatePaymentVm(randomVal);
            PaymentProvider provider = getPaymentProvider(randomVal);
            when(paymentProviderRepository.save(any())).thenReturn(provider);

            //When & Then
            assertThrows(
                NotFoundException.class,
                () -> paymentProviderService.update(createPaymentRequest)
            );
        }

        @Test
        @DisplayName("Update Payment Provider should update enabled status")
        void updatePaymentProvider_shouldUpdateEnabledStatus() {
            String providerId = "update-test";
            UpdatePaymentVm updateRequest = getUpdatePaymentVm(providerId);

            PaymentProvider existingProvider = getPaymentProvider(providerId);
            when(paymentProviderRepository.findById(providerId)).thenReturn(Optional.of(existingProvider));
            when(paymentProviderRepository.save(any())).thenReturn(existingProvider);

            paymentProviderService.update(updateRequest);

            verify(paymentProviderRepository).save(any());
        }
    }

    @Nested
    @DisplayName("Additional Settings Tests")
    class AdditionalSettingsTests {
        @Test
        void getAdditionalSettingsByPaymentProviderId_ShouldReturnAdditionalSettings_WhenPaymentProviderExists() {
            when(paymentProviderRepository.findById("providerId")).thenReturn(Optional.of(paymentProvider));

            String result = paymentProviderService.getAdditionalSettingsByPaymentProviderId("providerId");

            assertThat(result).isEqualTo("additional settings");
            verify(paymentProviderRepository, times(1)).findById("providerId");
        }

        @Test
        void getAdditionalSettingsByPaymentProviderId_ShouldThrowNotFoundException_WhenPaymentProviderDoesNotExist() {
            when(paymentProviderRepository.findById("invalidId")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentProviderService.getAdditionalSettingsByPaymentProviderId("invalidId"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("invalidId");

            verify(paymentProviderRepository, times(1)).findById("invalidId");
        }

        @Test
        @DisplayName("Get additional settings for different providers")
        void getAdditionalSettings_forMultipleProviders() {
            PaymentProvider provider1 = new PaymentProvider();
            provider1.setId("provider1");
            provider1.setAdditionalSettings("settings1");

            PaymentProvider provider2 = new PaymentProvider();
            provider2.setId("provider2");
            provider2.setAdditionalSettings("settings2");

            when(paymentProviderRepository.findById("provider1")).thenReturn(Optional.of(provider1));
            when(paymentProviderRepository.findById("provider2")).thenReturn(Optional.of(provider2));

            String result1 = paymentProviderService.getAdditionalSettingsByPaymentProviderId("provider1");
            String result2 = paymentProviderService.getAdditionalSettingsByPaymentProviderId("provider2");

            assertEquals("settings1", result1);
            assertEquals("settings2", result2);
        }
    }

    @Nested
    @DisplayName("Get Enabled Payment Providers Tests")
    class EnabledPaymentProvidersTests {
        @Test
        void getEnabledPaymentProviders_ShouldReturnListOfEnabledPaymentProviders() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("providerId");
            provider.setEnabled(true);
            provider.setMediaId(1L);
            
            List<PaymentProvider> enabledProviders = List.of(provider);
            MediaVm mediaVm = MediaVm.builder()
                    .id(1L)
                    .url("http://icon.png")
                    .build();
            
            when(paymentProviderRepository.findByEnabledTrue(defaultPageable)).thenReturn(enabledProviders);
            when(mediaService.getMediaVmMap(enabledProviders)).thenReturn(Map.of(1L, mediaVm));

            List<PaymentProviderVm> result = paymentProviderService.getEnabledPaymentProviders(defaultPageable);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getId()).isEqualTo("providerId");
            verify(paymentProviderRepository, times(1)).findByEnabledTrue(defaultPageable);
        }

        @Test
        void getEnabledPaymentProviders_ShouldReturnEmptyList_WhenNoEnabledPaymentProvidersExist() {
            when(paymentProviderRepository.findByEnabledTrue(defaultPageable)).thenReturn(List.of());

            List<PaymentProviderVm> result = paymentProviderService.getEnabledPaymentProviders(defaultPageable);

            assertThat(result).isEmpty();
            verify(paymentProviderRepository, times(1)).findByEnabledTrue(defaultPageable);
        }

        @Test
        @DisplayName("Get enabled providers with multiple providers")
        void getEnabledPaymentProviders_withMultipleProviders() {
            PaymentProvider provider1 = new PaymentProvider();
            provider1.setId("paypal");
            provider1.setEnabled(true);
            provider1.setMediaId(1L);

            PaymentProvider provider2 = new PaymentProvider();
            provider2.setId("banking");
            provider2.setEnabled(true);
            provider2.setMediaId(2L);

            List<PaymentProvider> enabledProviders = List.of(provider1, provider2);
            when(paymentProviderRepository.findByEnabledTrue(defaultPageable)).thenReturn(enabledProviders);
            when(mediaService.getMediaVmMap(enabledProviders)).thenReturn(Map.of());

            List<PaymentProviderVm> result = paymentProviderService.getEnabledPaymentProviders(defaultPageable);

            assertThat(result).hasSize(2);
            verify(paymentProviderRepository, times(1)).findByEnabledTrue(defaultPageable);
            verify(mediaService, times(1)).getMediaVmMap(enabledProviders);
        }

        @Test
        @DisplayName("Get enabled providers should call media service")
        void getEnabledPaymentProviders_shouldCallMediaService() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("test-provider");
            provider.setEnabled(true);
            provider.setMediaId(1L);
            
            List<PaymentProvider> providers = List.of(provider);
            MediaVm mediaVm = MediaVm.builder()
                    .id(1L)
                    .url("http://icon.png")
                    .build();
            
            when(paymentProviderRepository.findByEnabledTrue(defaultPageable)).thenReturn(providers);
            when(mediaService.getMediaVmMap(providers)).thenReturn(Map.of(1L, mediaVm));

            paymentProviderService.getEnabledPaymentProviders(defaultPageable);

            verify(mediaService, times(1)).getMediaVmMap(providers);
        }

        @Test
        @DisplayName("Get enabled providers with icon URL from media service")
        void getEnabledPaymentProviders_withIconUrl() {
            PaymentProvider provider = new PaymentProvider();
            provider.setId("paypal");
            provider.setEnabled(true);
            provider.setMediaId(1L);

            List<PaymentProvider> providers = List.of(provider);
            com.yas.payment.viewmodel.paymentprovider.MediaVm mediaVm = MediaVm.builder()
                    .id(1L)
                    .url("http://icon-url.com/paypal.png")
                    .build();

            when(paymentProviderRepository.findByEnabledTrue(defaultPageable)).thenReturn(providers);
            when(mediaService.getMediaVmMap(providers)).thenReturn(Map.of(1L, mediaVm));

            List<PaymentProviderVm> result = paymentProviderService.getEnabledPaymentProviders(defaultPageable);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getId()).isEqualTo("paypal");
        }

        @Test
        @DisplayName("Get enabled providers with different pagination sizes")
        void getEnabledPaymentProviders_withDifferentPageSizes() {
            Pageable page5 = Pageable.ofSize(5);
            Pageable page20 = Pageable.ofSize(20);

            List<PaymentProvider> providers = List.of(paymentProvider);
            MediaVm mediaVm = MediaVm.builder()
                    .id(1L)
                    .url("http://icon.png")
                    .build();
            
            when(paymentProviderRepository.findByEnabledTrue(page5)).thenReturn(providers);
            when(paymentProviderRepository.findByEnabledTrue(page20)).thenReturn(providers);
            when(mediaService.getMediaVmMap(providers)).thenReturn(Map.of(1L, mediaVm));

            List<PaymentProviderVm> result1 = paymentProviderService.getEnabledPaymentProviders(page5);
            List<PaymentProviderVm> result2 = paymentProviderService.getEnabledPaymentProviders(page20);

            assertThat(result1).hasSize(1);
            assertThat(result2).hasSize(1);
            verify(paymentProviderRepository).findByEnabledTrue(page5);
            verify(paymentProviderRepository).findByEnabledTrue(page20);
        }
    }

    @Nested
    @DisplayName("Payment Provider Lookup Tests")
    class PaymentProviderLookupTests {
        @Test
        @DisplayName("Should find payment provider by id")
        void getPaymentProvider_byId() {
            when(paymentProviderRepository.findById("PAYPAL")).thenReturn(java.util.Optional.of(paymentProvider));

            com.yas.commonlibrary.exception.NotFoundException exception = null;
            try {
                paymentProviderService.getAdditionalSettingsByPaymentProviderId("PAYPAL");
            } catch (com.yas.commonlibrary.exception.NotFoundException e) {
                exception = e;
            }

            verify(paymentProviderRepository).findById("PAYPAL");
        }

        @Test
        @DisplayName("Should return settings for existing provider")
        void getAdditionalSettings_existingProvider() {
            paymentProvider.setAdditionalSettings("api-key=secret;merchant-id=test123");
            when(paymentProviderRepository.findById("PAYPAL")).thenReturn(java.util.Optional.of(paymentProvider));

            String settings = paymentProviderService.getAdditionalSettingsByPaymentProviderId("PAYPAL");

            assertEquals("api-key=secret;merchant-id=test123", settings);
        }
    }

    @Nested
    @DisplayName("Disable Payment Provider Tests")
    class DisablePaymentProviderTests {
        @Test
        @DisplayName("Should disable enabled payment provider")
        void disablePaymentProvider_fromEnabled() {
            paymentProvider.setEnabled(true);
            UpdatePaymentVm updateVm = getUpdatePaymentVm("PAYPAL");
            updateVm.setEnabled(false);

            when(paymentProviderRepository.findById("PAYPAL")).thenReturn(java.util.Optional.of(paymentProvider));
            when(updatePaymentProviderMapper.toModel(updateVm)).thenReturn(paymentProvider);
            when(paymentProviderRepository.save(paymentProvider)).thenReturn(paymentProvider);

            PaymentProviderVm result = paymentProviderService.update(updateVm);

            assertNotNull(result);
            assertFalse(paymentProvider.isEnabled());
        }

        @Test
        @DisplayName("Should enable disabled payment provider")
        void enablePaymentProvider_fromDisabled() {
            paymentProvider.setEnabled(false);
            UpdatePaymentVm updateVm = getUpdatePaymentVm("STRIPE");
            updateVm.setEnabled(true);

            when(paymentProviderRepository.findById("STRIPE")).thenReturn(java.util.Optional.of(paymentProvider));
            when(updatePaymentProviderMapper.toModel(updateVm)).thenReturn(paymentProvider);
            when(paymentProviderRepository.save(paymentProvider)).thenReturn(paymentProvider);

            PaymentProviderVm result = paymentProviderService.update(updateVm);

            assertNotNull(result);
            assertTrue(paymentProvider.isEnabled());
        }
    }

    @Nested
    @DisplayName("Provider Configuration Tests")
    class ProviderConfigurationTests {
        @Test
        @DisplayName("Should update provider additional settings")
        void updateProvider_newSettings() {
            String newSettings = "new-api-key=updated;new-merchant=updated123";
            UpdatePaymentVm updateVm = getUpdatePaymentVm("PAYPAL");
            updateVm.setAdditionalSettings(newSettings);

            when(paymentProviderRepository.findById("PAYPAL")).thenReturn(java.util.Optional.of(paymentProvider));
            when(updatePaymentProviderMapper.toModel(updateVm)).thenReturn(paymentProvider);
            when(paymentProviderRepository.save(paymentProvider)).thenReturn(paymentProvider);

            PaymentProviderVm result = paymentProviderService.update(updateVm);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should clear provider settings if updated to empty")
        void updateProvider_clearSettings() {
            paymentProvider.setAdditionalSettings("old-settings");
            UpdatePaymentVm updateVm = getUpdatePaymentVm("PAYPAL");
            updateVm.setAdditionalSettings("");

            when(paymentProviderRepository.findById("PAYPAL")).thenReturn(java.util.Optional.of(paymentProvider));
            when(updatePaymentProviderMapper.toModel(updateVm)).thenReturn(paymentProvider);
            when(paymentProviderRepository.save(paymentProvider)).thenReturn(paymentProvider);

            PaymentProviderVm result = paymentProviderService.update(updateVm);

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Multiple Provider Icon Tests")
    class MultipleProviderIconTests {
        @Test
        @DisplayName("Should get enabled providers with different icons")
        void getEnabledProviders_multipleIcons() {
            PaymentProvider provider1 = new PaymentProvider();
            provider1.setId("PAYPAL");
            provider1.setName("PayPal");
            provider1.setEnabled(true);
            provider1.setMediaId(1L);

            PaymentProvider provider2 = new PaymentProvider();
            provider2.setId("STRIPE");
            provider2.setName("Stripe");
            provider2.setEnabled(true);
            provider2.setMediaId(2L);

            List<PaymentProvider> providers = List.of(provider1, provider2);

            MediaVm media1 = MediaVm.builder().id(1L).url("http://paypal-icon.png").build();
            MediaVm media2 = MediaVm.builder().id(2L).url("http://stripe-icon.png").build();

            when(paymentProviderRepository.findByEnabledTrue(defaultPageable))
                    .thenReturn(providers);
            when(mediaService.getMediaVmMap(providers))
                    .thenReturn(Map.of(1L, media1, 2L, media2));

            List<PaymentProviderVm> result = paymentProviderService.getEnabledPaymentProviders(defaultPageable);

            assertThat(result).hasSize(2);
            verify(mediaService).getMediaVmMap(providers);
        }
    }

    private static @NotNull PaymentProvider getPaymentProvider(String randomVal) {
        PaymentProvider provider = new PaymentProvider();
        provider.setId(randomVal);
        provider.setEnabled(true);
        provider.setName(randomVal);
        provider.setConfigureUrl(randomVal);
        provider.setLandingViewComponentName(randomVal);
        return provider;
    }

    private static @NotNull CreatePaymentVm getCreatePaymentVm(String randomVal) {
        CreatePaymentVm createPaymentVm = new CreatePaymentVm();
        createPaymentVm.setId(randomVal);
        createPaymentVm.setEnabled(true);
        createPaymentVm.setName(randomVal);
        createPaymentVm.setConfigureUrl(randomVal);
        createPaymentVm.setLandingViewComponentName(randomVal);
        return createPaymentVm;
    }

    private static @NotNull UpdatePaymentVm getUpdatePaymentVm(String randomVal) {
        UpdatePaymentVm updatePaymentVm = new UpdatePaymentVm();
        updatePaymentVm.setId(randomVal);
        updatePaymentVm.setEnabled(true);
        updatePaymentVm.setName(randomVal);
        updatePaymentVm.setConfigureUrl(randomVal);
        updatePaymentVm.setLandingViewComponentName(randomVal);
        return updatePaymentVm;
    }
}