/*
 *                       ######
 *                       ######
 * ############    ####( ######  #####. ######  ############   ############
 * #############  #####( ######  #####. ######  #############  #############
 *        ######  #####( ######  #####. ######  #####  ######  #####  ######
 * ###### ######  #####( ######  #####. ######  #####  #####   #####  ######
 * ###### ######  #####( ######  #####. ######  #####          #####  ######
 * #############  #############  #############  #############  #####  ######
 *  ############   ############  #############   ############  #####  ######
 *                                      ######
 *                               #############
 *                               ############
 *
 * Adyen Java API Library
 *
 * Copyright (c) 2018 Adyen B.V.
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more info.
 */
package com.adyen;

import com.adyen.model.Address;
import com.adyen.model.Amount;
import com.adyen.model.checkout.CheckoutPaymentsAction;
import com.adyen.model.checkout.CreatePaymentLinkRequest;
import com.adyen.model.checkout.CreatePaymentLinkResponse;
import com.adyen.model.checkout.DefaultPaymentMethodDetails;
import com.adyen.model.checkout.PaymentMethodDetails;
import com.adyen.model.checkout.PaymentMethodsRequest;
import com.adyen.model.checkout.PaymentMethodsResponse;
import com.adyen.model.checkout.PaymentResultRequest;
import com.adyen.model.checkout.PaymentResultResponse;
import com.adyen.model.checkout.PaymentSessionRequest;
import com.adyen.model.checkout.PaymentSessionResponse;
import com.adyen.model.checkout.PaymentsDetailsRequest;
import com.adyen.model.checkout.PaymentsRequest;
import com.adyen.model.checkout.PaymentsResponse;
import com.adyen.service.Checkout;
import com.adyen.service.exception.ApiException;
import com.google.gson.annotations.SerializedName;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.adyen.Client.LIB_NAME;
import static com.adyen.Client.LIB_VERSION;
import static com.adyen.enums.Environment.LIVE;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for
 * /paymentSession
 * /payments/result
 * /paymentMethods
 * /payments
 * /payments/details
 */
public class CheckoutTest extends BaseTest {
    /**
     * Test success flow for
     * POST /payments
     */
    @Test
    public void TestPaymentsSuccessMocked() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/payments-success.json");
        Checkout checkout = new Checkout(client);
        PaymentsRequest paymentsRequest = createPaymentsCheckoutRequest();
        PaymentsResponse paymentsResponse = checkout.payments(paymentsRequest);
        assertEquals("8535296650153317", paymentsResponse.getPspReference());
    }

    /**
     * Test error flow for
     * POST /payments
     */
    @Test
    public void TestPaymentsErrorMocked() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/payments-error-invalid-data-422.json");
        Checkout checkout = new Checkout(client);
        PaymentsRequest paymentsRequest = createPaymentsCheckoutRequest();
        PaymentsResponse paymentsResponse = checkout.payments(paymentsRequest);
        assertNull(paymentsResponse.getPspReference());
    }

    /**
     * Test success flow for
     * POST /paymentMethods
     */
    @Test
    public void TestPaymentMethodsSuccessMocked() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/paymentmethods-success.json");
        Checkout checkout = new Checkout(client);
        PaymentMethodsRequest paymentMethodsRequest = new PaymentMethodsRequest();
        paymentMethodsRequest.setMerchantAccount("MagentoMerchantTest");
        PaymentMethodsResponse paymentMethodsResponse = checkout.paymentMethods(paymentMethodsRequest);
        assertEquals(26, paymentMethodsResponse.getPaymentMethods().size());
        assertEquals("Credit Card", paymentMethodsResponse.getPaymentMethods().get(0).getName());
        assertEquals(6, paymentMethodsResponse.getPaymentMethods().get(0).getBrands().size());
        assertTrue(paymentMethodsResponse.getPaymentMethods().get(0).getSupportsRecurring());
    }

    /**
     * Test success flow for
     * POST /paymentMethods
     * including one-click methods
     */
    @Test
    public void TestPaymentMethodsOneClickSuccessMocked() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/paymentmethods-oneclick-success.json");
        Checkout checkout = new Checkout(client);
        PaymentMethodsRequest paymentMethodsRequest = new PaymentMethodsRequest();
        paymentMethodsRequest.setMerchantAccount("MagentoMerchantTest");
        PaymentMethodsResponse paymentMethodsResponse = checkout.paymentMethods(paymentMethodsRequest);
        assertNotNull(paymentMethodsResponse.getOneClickPaymentMethods());
        assertEquals(2, paymentMethodsResponse.getOneClickPaymentMethods().size());
        assertEquals("8314960677671745", paymentMethodsResponse.getOneClickPaymentMethods().get(0).getRecurringDetailReference());
    }

    /**
     * Test error flow for
     * POST /paymentMethods
     */
    @Test
    public void TestPaymentMethodsErrorMocked() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/paymentmethods-error-forbidden-403.json");
        Checkout checkout = new Checkout(client);
        PaymentMethodsRequest paymentMethodsRequest = new PaymentMethodsRequest();
        paymentMethodsRequest.setMerchantAccount("MagentoMerchantTest");
        PaymentMethodsResponse paymentMethodsResponse = checkout.paymentMethods(paymentMethodsRequest);
        assertNull(paymentMethodsResponse.getPaymentMethods());
    }

    /**
     * Test success flow for
     * POST /payments/details
     */
    @Test
    public void TestPaymentsDetailsSuccessMocked() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/paymentsdetails-sucess.json");
        Checkout checkout = new Checkout(client);
        PaymentsResponse paymentsResponse = checkout.paymentsDetails(createPaymentsDetailsRequest());
        assertEquals("Authorised", paymentsResponse.getResultCode().toString());
    }

    /**
     * Test error flow for
     * POST /payments/details
     */

    @Test
    public void TestPaymentsDetailsErrorMocked() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/paymentsdetails-error-invalid-data-422.json");
        Checkout checkout = new Checkout(client);
        PaymentsResponse paymentsResponse = checkout.paymentsDetails(createPaymentsDetailsRequest());
        assertNull(paymentsResponse.getResultCode());

    }

    /**
     * Test success flow for
     * POST  /paymentSession
     */

    @Test
    public void TestPaymentSessionSuccessMocked() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/paymentsession-sucess.json");
        Checkout checkout = new Checkout(client);
        PaymentSessionRequest paymentSessionRequest = createPaymentSessionRequest();
        PaymentSessionResponse paymentSessionResponse = checkout.paymentSession(paymentSessionRequest);
        assertNotNull(paymentSessionResponse.getPaymentSession());
    }

    /**
     * Test error flow for
     * POST  /paymentSession
     */

    @Test
    public void TestPaymentSessionErrorMocked() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/paymentsession-error-invalid-data-422.json");
        Checkout checkout = new Checkout(client);
        PaymentSessionRequest paymentSessionRequest = createPaymentSessionRequest();
        PaymentSessionResponse paymentSessionResponse = checkout.paymentSession(paymentSessionRequest);
        assertNull(paymentSessionResponse.getPaymentSession());
    }

    /**
     * Test success flow for
     * POST  /payments/result
     */

    @Test
    public void TestPaymentsResultSuccessMocked() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/paymentsresult-sucess.json");
        Checkout checkout = new Checkout(client);
        PaymentResultRequest paymentResultRequest = new PaymentResultRequest();
        paymentResultRequest.setPayload("This is a test payload");
        PaymentResultResponse paymentResultResponse = checkout.paymentResult(paymentResultRequest);
        assertEquals("Authorised", paymentResultResponse.getResultCode().toString());
    }

    /**
     * Test error flow for
     * POST  /payments/result
     */

    @Test
    public void TestPaymentsResultErrorMocked() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/paymentsresult-error-invalid-data-payload-422.json");
        Checkout checkout = new Checkout(client);
        PaymentResultRequest paymentResultRequest = new PaymentResultRequest();
        paymentResultRequest.setPayload("This is a test payload");
        PaymentResultResponse paymentResultResponse = checkout.paymentResult(paymentResultRequest);
        assertNull(paymentResultResponse.getResultCode());
    }

    /**
     * Test error flow on Checkout creation
     */
    @Test
    public void TestPaymentMethodsFailureMissingIdentifierOnLive() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/paymentsresult-error-invalid-data-payload-422.json");
        client.setEnvironment(LIVE, null);
        try {
            new Checkout(client);
        } catch (IllegalArgumentException e) {
            assertEquals("Please provide your unique live url prefix on the setEnvironment() call on the Client or provide checkoutEndpoint in your config object.", e.getMessage());
        }

    }

    @Test
    public void TestPaymentMethodDetails() {
        PaymentsRequest paymentsRequest = createPaymentsCheckoutRequest();
        String jsonRequest = PRETTY_PRINT_GSON.toJson(paymentsRequest);

        assertEquals("{\n"
                + "  \"amount\": {\n"
                + "    \"value\": 1000,\n"
                + "    \"currency\": \"USD\"\n"
                + "  },\n"
                + "  \"merchantAccount\": \"MagentoMerchantTest\",\n"
                + "  \"paymentMethod\": {\n"
                + "    \"type\": \"scheme\",\n"
                + "    \"number\": \"4111111111111111\",\n"
                + "    \"expiryMonth\": \"10\",\n"
                + "    \"expiryYear\": \"2018\",\n"
                + "    \"holderName\": \"John Smith\",\n"
                + "    \"cvc\": \"737\"\n"
                + "  },\n"
                + "  \"reference\": \"Your order number\",\n"
                + "  \"returnUrl\": \"https://your-company.com/...\",\n"
                + "  \"applicationInfo\": {\n"
                + "    \"adyenLibrary\": {\n"
                + "      \"name\": \"" + LIB_NAME + "\",\n"
                + "      \"version\": \"" + LIB_VERSION + "\"\n"
                + "    }\n"
                + "  }\n"
                + "}", jsonRequest);

        TestPaymentMethodDetails testPaymentMethodDetails = new TestPaymentMethodDetails();
        testPaymentMethodDetails.setType("testType");
        testPaymentMethodDetails.setTestValue("testValue");
        paymentsRequest.setPaymentMethod(testPaymentMethodDetails);

        jsonRequest = PRETTY_PRINT_GSON.toJson(paymentsRequest);
        assertEquals("{\n"
                + "  \"amount\": {\n"
                + "    \"value\": 1000,\n"
                + "    \"currency\": \"USD\"\n"
                + "  },\n"
                + "  \"merchantAccount\": \"MagentoMerchantTest\",\n"
                + "  \"paymentMethod\": {\n"
                + "    \"testKey\": \"testValue\",\n"
                + "    \"type\": \"testType\"\n"
                + "  },\n"
                + "  \"reference\": \"Your order number\",\n"
                + "  \"returnUrl\": \"https://your-company.com/...\",\n"
                + "  \"applicationInfo\": {\n"
                + "    \"adyenLibrary\": {\n"
                + "      \"name\": \"" + LIB_NAME + "\",\n"
                + "      \"version\": \"" + LIB_VERSION + "\"\n"
                + "    }\n"
                + "  }\n"
                + "}", jsonRequest);
    }

    /**
     * Test success flow for
     * POST /payments
     */
    @Test
    public void TestMultibancoPaymentsSuccessMocked() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/paymentsresult-multibanco-succes.json");
        Checkout checkout = new Checkout(client);
        PaymentsRequest paymentsRequest = createPaymentsCheckoutRequest();
        PaymentsResponse paymentsResponse = checkout.payments(paymentsRequest);
        assertEquals(0, new BigDecimal("1000.0").compareTo(BigDecimal.valueOf(paymentsResponse.getAction().getInitialAmount().getValue())));
        assertEquals("2020-01-12T09:37:49",paymentsResponse.getAction().getExpiresAt());
        assertEquals("12101",paymentsResponse.getAction().getEntity());
        assertEquals("501 422 944",paymentsResponse.getAction().getReference());

    }

    @Test
    public void TestSepaPaymentMethodDetails() {
        DefaultPaymentMethodDetails defaultPaymentMethodDetails = new DefaultPaymentMethodDetails();
        defaultPaymentMethodDetails.type("sepadirectdebit");
        defaultPaymentMethodDetails.setSepaOwnerName("A. Schneider");
        defaultPaymentMethodDetails.setSepaIbanNumber("DE87123456781234567890");

        PaymentsRequest paymentsRequest = createPaymentsCheckoutRequest();
        paymentsRequest.setPaymentMethod(defaultPaymentMethodDetails);

        String jsonRequest = PRETTY_PRINT_GSON.toJson(paymentsRequest);

        assertEquals("{\n"
                + "  \"amount\": {\n"
                + "    \"value\": 1000,\n"
                + "    \"currency\": \"USD\"\n"
                + "  },\n"
                + "  \"merchantAccount\": \"MagentoMerchantTest\",\n"
                + "  \"paymentMethod\": {\n"
                + "    \"type\": \"sepadirectdebit\",\n"
                + "    \"sepa.ownerName\": \"A. Schneider\",\n"
                + "    \"sepa.ibanNumber\": \"DE87123456781234567890\"\n"
                + "  },\n"
                + "  \"reference\": \"Your order number\",\n"
                + "  \"returnUrl\": \"https://your-company.com/...\",\n"
                + "  \"applicationInfo\": {\n"
                + "    \"adyenLibrary\": {\n"
                + "      \"name\": \"" + LIB_NAME + "\",\n"
                + "      \"version\": \"" + LIB_VERSION + "\"\n"
                + "    }\n"
                + "  }\n"
                + "}",jsonRequest );
    }

    @Test
    public void TestRecurringPaymentMethodDetails() {
        DefaultPaymentMethodDetails defaultPaymentMethodDetails = new DefaultPaymentMethodDetails();
        defaultPaymentMethodDetails.setStoredPaymentMethodId("testStoredPaymentMethodId");

        PaymentsRequest paymentsRequest = createPaymentsCheckoutRequest();
        paymentsRequest.setPaymentMethod(defaultPaymentMethodDetails);

        String jsonRequest = PRETTY_PRINT_GSON.toJson(paymentsRequest);

        assertEquals("{\n"
                + "  \"amount\": {\n"
                + "    \"value\": 1000,\n"
                + "    \"currency\": \"USD\"\n"
                + "  },\n"
                + "  \"merchantAccount\": \"MagentoMerchantTest\",\n"
                + "  \"paymentMethod\": {\n"
                + "    \"storedPaymentMethodId\": \"testStoredPaymentMethodId\"\n"
                + "  },\n"
                + "  \"reference\": \"Your order number\",\n"
                + "  \"returnUrl\": \"https://your-company.com/...\",\n"
                + "  \"applicationInfo\": {\n"
                + "    \"adyenLibrary\": {\n"
                + "      \"name\": \"" + LIB_NAME + "\",\n"
                + "      \"version\": \"" + LIB_VERSION + "\"\n"
                + "    }\n"
                + "  }\n"
                + "}",jsonRequest );
    }

    @Test
    public void TestDateSerializers() throws ParseException {
        PaymentsRequest paymentsRequest = new PaymentsRequest();

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date d = fmt.parse("2018-10-31");
        paymentsRequest.setDateOfBirth(d);
        paymentsRequest.setDeliveryDate(d);
        String jsonRequest = PRETTY_PRINT_GSON.toJson(paymentsRequest);
        assertEquals("{\n"
                + "  \"dateOfBirth\": \"2018-10-31\",\n"
                + "  \"deliveryDate\": \"2018-10-31T00:00:00.000Z\",\n"
                + "  \"applicationInfo\": {\n"
                + "    \"adyenLibrary\": {\n"
                + "      \"name\": \"" + LIB_NAME + "\",\n"
                + "      \"version\": \"" + LIB_VERSION + "\"\n"
                + "    }\n"
                + "  }\n"
                + "}",jsonRequest );
    }

    @Test
    public void TestRecurringProcessingModels() {
        PaymentsRequest paymentsRequest = new PaymentsRequest();
        paymentsRequest.setRecurringProcessingModel(PaymentsRequest.RecurringProcessingModelEnum.CARD_ON_FILE);

        String jsonRequest = PRETTY_PRINT_GSON.toJson(paymentsRequest);
        assertTrue(jsonRequest.contains("recurringProcessingModel\": \"CardOnFile\"\n"));

        paymentsRequest.setRecurringProcessingModel(PaymentsRequest.RecurringProcessingModelEnum.SUBSCRIPTION);
        jsonRequest = PRETTY_PRINT_GSON.toJson(paymentsRequest);
        assertTrue(jsonRequest.contains("recurringProcessingModel\": \"Subscription\"\n"));

        paymentsRequest.setRecurringProcessingModel(PaymentsRequest.RecurringProcessingModelEnum.UNSCHEDULED_CARD_ON_FILE);
        jsonRequest = PRETTY_PRINT_GSON.toJson(paymentsRequest);
        assertTrue(jsonRequest.contains("recurringProcessingModel\": \"UnscheduledCardOnFile\"\n"));
    }

    @Test
    public void TestPaymentResponseDate() {
        PaymentsResponse paymentsResponse = new PaymentsResponse();
        try {
            Date expiryDate = paymentsResponse.getExpiryDate();
            assertEquals(null, expiryDate);
        } catch (Exception ex) {

            Assert.fail("multibanco date throw Exception");
        }
    }

    @Test
    public void TestPaymentResponseChallengeShopper() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/payments-3ds2-challenge-shopper.json");
        Checkout checkout = new Checkout(client);
        PaymentsRequest paymentsRequest = createPaymentsCheckoutRequest();
        PaymentsResponse paymentsResponse = checkout.payments(paymentsRequest);
        assertEquals(PaymentsResponse.ResultCodeEnum.CHALLENGESHOPPER, paymentsResponse.getResultCode());
        assertNotNull(paymentsResponse.getAction());
        assertNotNull(paymentsResponse.getAction().getData());
        assertEquals(3, paymentsResponse.getAction().getData().size());
        assertEquals("POST", paymentsResponse.getAction().getMethod());
        assertEquals("Ab02b4c0!BQABAgA4e3wGkhVah4CJL19qdegdmm9E...", paymentsResponse.getAction().getPaymentData());
        assertEquals("scheme", paymentsResponse.getAction().getPaymentMethodType());
        assertEquals("https://test.adyen.com/hpp/3d/validate.shtml", paymentsResponse.getAction().getUrl());
        assertEquals(CheckoutPaymentsAction.CheckoutActionType.REDIRECT, paymentsResponse.getAction().getType());
        assertEquals("Ab02b4c0!BQABAgA4e3wGkhVah4CJL19qdegdmm9E...", paymentsResponse.getPaymentData());
        assertNotNull(paymentsResponse.getDetails());
        assertEquals(1, paymentsResponse.getDetails().size());
        assertEquals("threeds2.challengeResult", paymentsResponse.getDetails().get(0).getKey());
        assertEquals("text", paymentsResponse.getDetails().get(0).getType());
        assertNotNull(paymentsResponse.getAuthentication());
        assertEquals(1, paymentsResponse.getAuthentication().size());
        assertEquals("S0zYWQ0MGEwMjU2MjEifQ==", paymentsResponse.getAuthentication().get("threeds2.challengeToken"));
    }

    @Test
    public void TestPaymentResponseAuthenticationFinished() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/payments-3ds2-authentication-finished.json");
        Checkout checkout = new Checkout(client);
        PaymentsRequest paymentsRequest = createPaymentsCheckoutRequest();
        PaymentsResponse paymentsResponse = checkout.payments(paymentsRequest);
        assertEquals(PaymentsResponse.ResultCodeEnum.AUTHENTICATIONFINISHED, paymentsResponse.getResultCode());
        assertNotNull(paymentsResponse.getThreeDS2Result());
        assertNotNull(paymentsResponse.getThreeDS2Result().getAuthenticationValue());
        assertNotNull(paymentsResponse.getThreeDS2Result().getDsTransID());
        assertNotNull(paymentsResponse.getThreeDS2Result().getEci());
        assertNotNull(paymentsResponse.getThreeDS2Result().getMessageVersion());
        assertNotNull(paymentsResponse.getThreeDS2Result().getThreeDSServerTransID());
        assertNotNull(paymentsResponse.getThreeDS2Result().getTransStatus());
    }

    @Test
    public void TestPaymentLinksSuccess() throws Exception {
        Client client = createMockClientFromFile("mocks/checkout/payment-links-success.json");
        Checkout checkout = new Checkout(client);
        CreatePaymentLinkRequest createPaymentLinkRequest = createPaymentLinkRequest();
        CreatePaymentLinkResponse createPaymentLinkResponse = checkout.paymentLinks(createPaymentLinkRequest);
        assertNotNull(createPaymentLinkResponse);
        assertNotNull(createPaymentLinkResponse.getUrl());
        assertNotNull(createPaymentLinkResponse.getExpiresAt());
        assertNotNull(createPaymentLinkResponse.getAmount());
        assertNotNull(createPaymentLinkResponse.getReference());
    }

    @Test
    public void TestPaymentLinksErrorMerchantMissing() throws IOException, ApiException {
        Client client = createMockClientForErrors(403,"mocks/checkout/payment-links-error-merchant.json");
        Checkout checkout = new Checkout(client);
        CreatePaymentLinkRequest createPaymentLinkRequest = createPaymentLinkRequest();
        try {
            checkout.paymentLinks(createPaymentLinkRequest);
            fail("Exception expected");
        } catch (ApiException e) {
            assertNotNull(e.getError());
            assertEquals("901", e.getError().getErrorCode());
            assertEquals(403, e.getError().getStatus());
        }
    }

    @Test
    public void TestPaymentLinksErrorAmountMissing() throws IOException, ApiException {
        Client client = createMockClientForErrors(422,"mocks/checkout/payment-links-error-amount.json");
        Checkout checkout = new Checkout(client);
        CreatePaymentLinkRequest createPaymentLinkRequest = createPaymentLinkRequest();
        try {
            checkout.paymentLinks(createPaymentLinkRequest);
            fail("Exception expected");
        } catch (ApiException e) {
            assertNotNull(e.getError());
            assertEquals("100", e.getError().getErrorCode());
            assertEquals(422, e.getError().getStatus());
        }
    }

    @Test
    public void TestPaymentLinksErrorReferenceMissing() throws IOException, ApiException {
        Client client = createMockClientForErrors(422,"mocks/checkout/payment-links-error-reference.json");
        Checkout checkout = new Checkout(client);
        CreatePaymentLinkRequest createPaymentLinkRequest = createPaymentLinkRequest();
        try {
            checkout.paymentLinks(createPaymentLinkRequest);
            fail("Exception expected");
        } catch (ApiException e) {
            assertNotNull(e.getError());
            assertEquals("130", e.getError().getErrorCode());
            assertEquals(422, e.getError().getStatus());
        }
    }

    /**
     * Test ApiException flow for POST
     */
    @Test
    public void TestApiExceptionErrorMocked() throws Exception {
        Client client = createMockClientForErrors(422, "mocks/checkout/payments-error-invalid-data-422.json");
        Checkout checkout = new Checkout(client);
        PaymentsRequest paymentsRequest = createPaymentsCheckoutRequest();
        try {
            checkout.payments(paymentsRequest);
            fail("Exception expected");
        } catch (ApiException e) {
            assertNotNull(e.getError());
            assertEquals("130", e.getError().getErrorCode());
            assertEquals(422, e.getError().getStatus());
            assertEquals(e.getResponseHeaders().size(), 0);
            assertNotNull(e.getResponseHeaders());
        }
    }

    /**
     * Returns a sample PaymentSessionRequest opbject with test data
     */

    protected PaymentSessionRequest createPaymentSessionRequest() {
        PaymentSessionRequest paymentSessionRequest = new PaymentSessionRequest();
        paymentSessionRequest.setAmount(createAmountObject("USD", 1000L));
        paymentSessionRequest.setReference("Your order number");
        paymentSessionRequest.setCountryCode("NL");
        paymentSessionRequest.setReturnUrl("https://your-company.com/...");
        paymentSessionRequest.setMerchantAccount("MagentoMerchantTest");
        return paymentSessionRequest;

    }

    /**
     * Returns a sample PaymentsRequest opbject with test data
     */
    protected PaymentsRequest createPaymentsCheckoutRequest() {
        PaymentsRequest paymentsRequest = new PaymentsRequest();

        paymentsRequest.setReference("Your order number");
        paymentsRequest.setAmount(createAmountObject("USD", 1000L));
        paymentsRequest.addCardData("4111111111111111", "10", "2018", "737", "John Smith");

        paymentsRequest.setReturnUrl("https://your-company.com/...");
        paymentsRequest.setMerchantAccount("MagentoMerchantTest");

        return paymentsRequest;
    }

    /**
     * Returns a sample PaymentsDetailsRequest opbject with test data
     */

    protected PaymentsDetailsRequest createPaymentsDetailsRequest() {
        PaymentsDetailsRequest paymentsDetailsRequest = new PaymentsDetailsRequest();
        paymentsDetailsRequest.setPaymentData("Ab02b4c0!BQABAgCJN1wRZuGJmq8dMncmypvknj9s7l5Tj...");
        paymentsDetailsRequest.set3DRequestData("mdValue", "paResValue", "paymentDataValue");
        return paymentsDetailsRequest;
    }

    /**
     * Returns a sample Amount opbject with given currency and value
     */
    protected Amount createAmountObject(String currency, Long value) {
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setValue(value);
        return amount;
    }

    /**
     * Returns a sample PaymentsRequest opbject with test data
     */
    protected CreatePaymentLinkRequest createPaymentLinkRequest() {
        CreatePaymentLinkRequest createPaymentLinkRequest = new CreatePaymentLinkRequest();

        createPaymentLinkRequest.setReference("YOUR_ORDER_NUMBER");
        createPaymentLinkRequest.setAmount(createAmountObject("BRL", 1000L));
        createPaymentLinkRequest.setCountryCode("BR");
        createPaymentLinkRequest.setMerchantAccount("MagentoMerchantTest");
        createPaymentLinkRequest.setShopperReference("YOUR_UNIQUE_SHOPPER_ID");
        createPaymentLinkRequest.setShopperEmail("test@email.com");
        createPaymentLinkRequest.setShopperLocale("pt_BR");
        createPaymentLinkRequest.setExpiresAt("2019-12-17T10:05:29Z");
        Address address = new Address();
        address.setStreet("Street");
        address.setPostalCode("59000060");
        address.setCity("City");
        address.setHouseNumberOrName("999");
        address.setCountry("BR");
        address.setStateOrProvince("SP");
        createPaymentLinkRequest.setBillingAddress(address);
        createPaymentLinkRequest.setDeliveryAddress(address);

        return createPaymentLinkRequest;
    }
}

class TestPaymentMethodDetails implements PaymentMethodDetails {
    @SerializedName("testKey")
    private String testValue;

    @SerializedName("type")
    private String type;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    public String getTestValue() {
        return testValue;
    }

    public void setTestValue(String testValue) {
        this.testValue = testValue;
    }
}