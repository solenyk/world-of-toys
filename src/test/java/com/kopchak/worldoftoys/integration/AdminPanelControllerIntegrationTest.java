package com.kopchak.worldoftoys.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.kopchak.worldoftoys.domain.order.OrderStatus;
import com.kopchak.worldoftoys.domain.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.domain.product.category.BrandCategory;
import com.kopchak.worldoftoys.dto.admin.category.AdminCategoryDto;
import com.kopchak.worldoftoys.dto.admin.category.CategoryIdDto;
import com.kopchak.worldoftoys.dto.admin.category.CategoryNameDto;
import com.kopchak.worldoftoys.dto.admin.order.FilteredOrdersPageDto;
import com.kopchak.worldoftoys.dto.admin.order.FilteringOrderOptionsDto;
import com.kopchak.worldoftoys.dto.admin.order.StatusDto;
import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductsPageDto;
import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import com.kopchak.worldoftoys.dto.product.category.CategoryDto;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@Slf4j
@ActiveProfiles("integrationtest")
public class AdminPanelControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @RegisterExtension
    public static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("test", "password"))
            .withPerMethodLifecycle(true);

    @Autowired
    private ObjectMapper objectMapper;
    private final static Integer EXISTENT_PRODUCT_ID = 1002;
    private final static Integer NON_EXISTENT_PRODUCT_ID = 5813;
    private final static Integer EXISTENT_CATEGORY_ID = 1001;
    private final static Integer NON_EXISTENT_CATEGORY_ID = 523;
    private final static String ORDER_ID = "4c980930-16eb-41cd-b998-29d03118d67c";
    private final static String PRODUCT_NAME = "лялька";
    private final static String DUPLICATE_PRODUCT_NAME = "Пупсик Оксанка";
    private final static String CATEGORY_NAME = "category-name";
    private final static String DUPLICATE_CATEGORY_NAME = "Devilon";
    private final static String DUPLICATE_PRODUCT_NAME_EXCEPTION_MSG =
            String.format("The product with name: %s is already exist", DUPLICATE_PRODUCT_NAME);
    private final static String DUPLICATE_CATEGORY_NAME_EXCEPTION_MSG =
            String.format("Category with name: %s already exist", DUPLICATE_CATEGORY_NAME);
    private AdminProductDto adminProductDto;
    private AddUpdateProductDto addUpdateProductDto;
    private MockMultipartFile mainImage;
    private CategoryIdDto categoryIdDto;
    private CategoryNameDto categoryNameDto;
    private StatusDto statusDto;
    private ResponseStatusExceptionDto accessDeniedErrorResponse;
    private ResponseStatusExceptionDto unauthorizedErrorResponse;

    @BeforeEach
    void setUp() {
        List<CategoryDto> ageCategories = new ArrayList<>() {{
            add(new CategoryDto("від 1 до 3 років", "vid-1-do-3-rokiv"));
            add(new CategoryDto("від 6 до 9 років", "vid-6-do-9-rokiv"));
        }};
        adminProductDto = AdminProductDto
                .builder()
                .id(EXISTENT_PRODUCT_ID)
                .name("Лялька Даринка")
                .slug("lyalka-darynka")
                .description("Ця іграшка обєднує інноваційний дизайн та розвиваючий функціонал, що сприяє розвитку " +
                        "навичок у дітей. Вона створює захоплюючий світ уяви, розвиваючи логічне мислення та творчість." +
                        " Іграшка безпечна, енергоефективна і сприяє розвитку спостережливості, уваги та винахідливості " +
                        "у дітей, забезпечуючи незабутні враження та навчальний досвід.")
                .price(BigDecimal.valueOf(900))
                .availableQuantity(BigInteger.valueOf(200))
                .isAvailable(true)
                .originCategory(new CategoryDto("Україна", "ukraine"))
                .brandCategory(new CategoryDto("Сurlimals", "сurlimals"))
                .ageCategories(ageCategories)
                .images(new ArrayList<>())
                .build();
        accessDeniedErrorResponse = new ResponseStatusExceptionDto(HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.name(), "Access Denied");
        unauthorizedErrorResponse = new ResponseStatusExceptionDto(HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.name(), "Full authentication is required to access this resource");
        categoryIdDto = new CategoryIdDto(EXISTENT_CATEGORY_ID);
        addUpdateProductDto = new AddUpdateProductDto(PRODUCT_NAME, "description", BigDecimal.TEN,
                BigInteger.ONE, true, categoryIdDto, categoryIdDto, List.of(categoryIdDto));
        mainImage = new MockMultipartFile("image", "filename",
                "image/jpg", "image".getBytes());
        categoryNameDto = new CategoryNameDto(CATEGORY_NAME);
        OrderStatus orderStatus = OrderStatus.AWAITING_PAYMENT;
        statusDto = new StatusDto(orderStatus.name(), orderStatus.getStatus());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void getProductById_ReturnsOkStatus() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/admin/products/{productId}", EXISTENT_PRODUCT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(adminProductDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void getProductById_ThrowImageDecompressionException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        String imageName = "lyalka-klaymber1.png";
        String imageDecompressionExceptionMsg =
                String.format("The image with name: %s cannot be decompressed", imageName);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                imageDecompressionExceptionMsg);

        ResultActions response = mockMvc.perform(get("/api/v1/admin/products/{productId}", 1001)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void getProductById_ThrowProductNotFoundException_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        String productNotFoundExceptionMsg =
                String.format("The product with id: %d is not found.", NON_EXISTENT_PRODUCT_ID);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.NOT_FOUND, productNotFoundExceptionMsg);

        ResultActions response = mockMvc
                .perform(get("/api/v1/admin/products/{productId}", NON_EXISTENT_PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void getProductById_AnonymousUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/admin/products/{productId}", EXISTENT_PRODUCT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void getProductById_AuthUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/admin/products/{productId}", EXISTENT_PRODUCT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void getFilteredProductsPage_ReturnsOkStatus() throws Exception {
        var adminFilteredProductDto = new AdminFilteredProductDto(1002, "Лялька Даринка",
                BigDecimal.valueOf(900), BigInteger.valueOf(200), true, null);
        var adminProductsPageDto = new AdminProductsPageDto(List.of(adminFilteredProductDto), 1L, 1L);

        ResultActions response = mockMvc.perform(get("/api/v1/admin/products")
                .param("page", "0")
                .param("size", "1")
                .param("name", PRODUCT_NAME)
                .param("origin", "china", "ukraine")
                .param("brand", "сurlimals")
                .param("age", "do-1-roku", "vid-1-do-3-rokiv")
                .param("price-sort", "asc")
                .param("availability", "available")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(adminProductsPageDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void getFilteredProductsPage_AnonymousUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/admin/products")
                .param("page", "0")
                .param("size", "1")
                .param("name", DUPLICATE_PRODUCT_NAME)
                .param("origin", "china", "ukraine")
                .param("brand", "сurlimals")
                .param("age", "do-1-roku", "vid-1-do-3-rokiv")
                .param("price-sort", "asc")
                .param("availability", "available")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void getFilteredProductsPage_AuthUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/admin/products")
                .param("page", "0")
                .param("size", "1")
                .param("name", DUPLICATE_PRODUCT_NAME)
                .param("origin", "china", "ukraine")
                .param("brand", "сurlimals")
                .param("age", "do-1-roku", "vid-1-do-3-rokiv")
                .param("price-sort", "asc")
                .param("availability", "available")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void updateProduct_ReturnsNoContentStatus() throws Exception {
        MockMultipartFile addUpdateProductDtoJsonFile = getAddUpdateProductDtoJsonFile(addUpdateProductDto);

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.PUT, "/api/v1/admin/products/{productId}", EXISTENT_PRODUCT_ID)
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void updateProduct_ThrowDuplicateProductNameException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        addUpdateProductDto = new AddUpdateProductDto(DUPLICATE_PRODUCT_NAME, "description", BigDecimal.TEN,
                BigInteger.ONE, true, categoryIdDto, categoryIdDto, List.of(categoryIdDto));
        MockMultipartFile addUpdateProductDtoJsonFile = getAddUpdateProductDtoJsonFile(addUpdateProductDto);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                DUPLICATE_PRODUCT_NAME_EXCEPTION_MSG);

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.PUT, "/api/v1/admin/products/{productId}", EXISTENT_PRODUCT_ID)
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void updateProduct_ThrowProductNotFoundException_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        MockMultipartFile addUpdateProductDtoJsonFile = getAddUpdateProductDtoJsonFile(addUpdateProductDto);
        String productNotFoundExceptionMsg =
                String.format("The product with id: %d is not found.", NON_EXISTENT_PRODUCT_ID);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.NOT_FOUND,
                productNotFoundExceptionMsg);

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.PUT, "/api/v1/admin/products/{productId}", NON_EXISTENT_PRODUCT_ID)
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void updateProduct_AnonymousUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        MockMultipartFile addUpdateProductDtoJsonFile = getAddUpdateProductDtoJsonFile(addUpdateProductDto);

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.PUT, "/api/v1/admin/products/{productId}", NON_EXISTENT_PRODUCT_ID)
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void updateProduct_AuthUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        MockMultipartFile addUpdateProductDtoJsonFile = getAddUpdateProductDtoJsonFile(addUpdateProductDto);

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.PUT, "/api/v1/admin/products/{productId}", NON_EXISTENT_PRODUCT_ID)
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void createProduct_ReturnsCreatedStatus() throws Exception {
        MockMultipartFile addUpdateProductDtoJsonFile = getAddUpdateProductDtoJsonFile(addUpdateProductDto);

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.POST, "/api/v1/admin/products/add")
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void createProduct_ThrowDuplicateProductNameException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        addUpdateProductDto = new AddUpdateProductDto(DUPLICATE_PRODUCT_NAME, "description", BigDecimal.TEN,
                BigInteger.ONE, true, categoryIdDto, categoryIdDto, List.of(categoryIdDto));
        MockMultipartFile addUpdateProductDtoJsonFile = getAddUpdateProductDtoJsonFile(addUpdateProductDto);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                DUPLICATE_PRODUCT_NAME_EXCEPTION_MSG);

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.POST, "/api/v1/admin/products/add")
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void createProduct_ThrowCategoryNotFoundException_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        categoryIdDto = new CategoryIdDto(NON_EXISTENT_CATEGORY_ID);
        addUpdateProductDto = new AddUpdateProductDto(PRODUCT_NAME, "description", BigDecimal.TEN,
                BigInteger.ONE, true, categoryIdDto, categoryIdDto, List.of(categoryIdDto));
        MockMultipartFile addUpdateProductDtoJsonFile = getAddUpdateProductDtoJsonFile(addUpdateProductDto);
        String categoryNotFoundExceptionMsg = String.format("The category type: %s with id: %d is not found",
                BrandCategory.class.getSimpleName(), NON_EXISTENT_CATEGORY_ID);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.NOT_FOUND,
                categoryNotFoundExceptionMsg);

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.POST, "/api/v1/admin/products/add")
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void createProduct_AnonymousUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        MockMultipartFile addUpdateProductDtoJsonFile = getAddUpdateProductDtoJsonFile(addUpdateProductDto);

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.POST, "/api/v1/admin/products/add")
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void createProduct_AuthUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        MockMultipartFile addUpdateProductDtoJsonFile = getAddUpdateProductDtoJsonFile(addUpdateProductDto);

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.POST, "/api/v1/admin/products/add")
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void getProductCategories_ReturnsOkStatusAndAdminCategoryDtoSet() throws Exception {
        Set<AdminCategoryDto> adminCategoryDtoSet = new LinkedHashSet<>() {{
            add(new AdminCategoryDto(1000, "Китай"));
            add(new AdminCategoryDto(1001, "Україна"));
        }};

        ResultActions response = mockMvc
                .perform(get("/api/v1/admin/categories/{categoryType}", "origins")
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(adminCategoryDtoSet)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void getProductCategories_AnonymousUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc
                .perform(get("/api/v1/admin/categories/{categoryType}", "origins")
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void getProductCategories_AuthUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc
                .perform(get("/api/v1/admin/categories/{categoryType}", "origins")
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void deleteCategory_ReturnsNoContentStatus() throws Exception {
        ResultActions response = mockMvc
                .perform(delete("/api/v1/admin/categories/{categoryType}/{categoryId}",
                        "brands", 1004)
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void deleteCategory_ThrowCategoryContainsProductsException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        String categoryContainsProductsExceptionMsg = String.format("It is not possible to delete a category " +
                "with id: %d because there are products in this category.", EXISTENT_CATEGORY_ID);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                categoryContainsProductsExceptionMsg);

        ResultActions response = mockMvc
                .perform(delete("/api/v1/admin/categories/{categoryType}/{categoryId}",
                        "brands", EXISTENT_CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void deleteCategory_AnonymousUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc
                .perform(delete("/api/v1/admin/categories/{categoryType}/{categoryId}",
                        "brands", 1004)
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void deleteCategory_AuthUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc
                .perform(delete("/api/v1/admin/categories/{categoryType}/{categoryId}",
                        "brands", 1004)
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void updateCategory_ReturnsNoContentStatus() throws Exception {
        ResultActions response = mockMvc
                .perform(put("/api/v1/admin/categories/{categoryType}/{categoryId}",
                        "brands", EXISTENT_CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryNameDto)));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void updateCategory_ThrowDuplicateCategoryNameException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        categoryNameDto = new CategoryNameDto(DUPLICATE_CATEGORY_NAME);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                DUPLICATE_CATEGORY_NAME_EXCEPTION_MSG);

        ResultActions response = mockMvc
                .perform(put("/api/v1/admin/categories/{categoryType}/{categoryId}",
                        "brands", EXISTENT_PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryNameDto)));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void updateCategory_AnonymousUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc
                .perform(put("/api/v1/admin/categories/{categoryType}/{categoryId}",
                        "brands", EXISTENT_PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryNameDto)));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void updateCategory_AuthUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc
                .perform(put("/api/v1/admin/categories/{categoryType}/{categoryId}",
                        "brands", EXISTENT_PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryNameDto)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void createCategory_ReturnsCreatedStatus() throws Exception {
        ResultActions response = mockMvc
                .perform(post("/api/v1/admin/categories/{categoryType}/add", "brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryNameDto)));

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void createCategory_ThrowDuplicateCategoryNameException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        categoryNameDto = new CategoryNameDto(DUPLICATE_CATEGORY_NAME);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                DUPLICATE_CATEGORY_NAME_EXCEPTION_MSG);

        ResultActions response = mockMvc
                .perform(post("/api/v1/admin/categories/{categoryType}/add", "brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryNameDto)));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void createCategory_AnonymousUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc
                .perform(post("/api/v1/admin/categories/{categoryType}/add", "brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryNameDto)));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void createCategory_AuthUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc
                .perform(post("/api/v1/admin/categories/{categoryType}/add", "brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryNameDto)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void getOrderFilteringOptions_ReturnsOkStatus() throws Exception {
        var filteringOrderOptionsDto = new FilteringOrderOptionsDto(Set.of(statusDto), new HashSet<>());
        System.out.println(objectMapper.writeValueAsString(filteringOrderOptionsDto));

        ResultActions response = mockMvc.perform(get("/api/v1/admin/orders/filtering-options")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(filteringOrderOptionsDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void getOrderFilteringOptions_AnonymousUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/admin/orders/filtering-options")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void getOrderFilteringOptions_AuthUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/admin/orders/filtering-options")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void filterOrdersByStatusesAndDate_ReturnsOkStatus() throws Exception {
        String dateSortOrder = "asc";
        var filteredOrdersPageDto = new FilteredOrdersPageDto(new HashSet<>(), 0L, 0L);

        ResultActions response = mockMvc.perform(get("/api/v1/admin/orders")
                .param("page", "0")
                .param("size", "1")
                .param("order-status", OrderStatus.CANCELED.name(), OrderStatus.SHIPPED.name())
                .param("payment-status", PaymentStatus.FAILED.name())
                .param("date-sort", dateSortOrder)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(filteredOrdersPageDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void filterOrdersByStatusesAndDate_AnonymousUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/admin/orders")
                .param("page", "0")
                .param("size", "1")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void filterOrdersByStatusesAndDate_AuthUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/admin/orders")
                .param("page", "0")
                .param("size", "1")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void getAllOrderStatuses_ReturnsOkStatus() throws Exception {
        Set<StatusDto> statusDtoSet = Arrays.stream(OrderStatus.values())
                .map(status -> new StatusDto(status.name(), status.getStatus()))
                .collect(Collectors.toSet());

        ResultActions response = mockMvc.perform(get("/api/v1/admin/orders/statuses")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(statusDtoSet)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void getAllOrderStatuses_AnonymousUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/admin/orders/statuses")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void getAllOrderStatuses_AuthUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/admin/orders/statuses")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void updateOrderStatus_ReturnsNoContentStatus() throws Exception {
        String orderStatusSubject = "Order status";
        OrderStatus orderStatus = OrderStatus.CANCELED;
        statusDto = new StatusDto(orderStatus.name(), orderStatus.getStatus());

        ResultActions response = mockMvc.perform(patch("/api/v1/admin/orders/{orderId}", ORDER_ID)
                .content(objectMapper.writeValueAsString(statusDto))
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(1);

        MimeMessage receivedMessage = receivedMessages[0];
        assertThat(receivedMessage.getSubject()).isEqualTo(orderStatusSubject);
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void updateOrderStatus_ThrowInvalidOrderStatusException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        String invalidOrderStatusExceptionMsg = String.format("The status: %s of the order with id: %s " +
                "is the same as the current status", statusDto.status(), ORDER_ID);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                invalidOrderStatusExceptionMsg);

        ResultActions response = mockMvc.perform(patch("/api/v1/admin/orders/{orderId}", ORDER_ID)
                .content(objectMapper.writeValueAsString(statusDto))
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(0);
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void updateOrderStatus_ThrowMessageSendingException_ReturnsServiceUnavailableStatusAndResponseStatusExceptionDto() throws Exception {
        OrderStatus orderStatus = OrderStatus.CANCELED;
        statusDto = new StatusDto(orderStatus.name(), orderStatus.getStatus());
        String messageSendingExceptionMsg =
                """
                        Failed to send the email: Mail server connection failed. Failed messages: com.sun.mail.util.MailConnectException: Couldn't connect to host, port: localhost, 3025; timeout 5000;
                          nested exception is:
                        \tjava.net.ConnectException: Connection refused: no further information""";
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.SERVICE_UNAVAILABLE,
                messageSendingExceptionMsg);

        greenMail.stop();

        ResultActions response = mockMvc.perform(patch("/api/v1/admin/orders/{orderId}", ORDER_ID)
                .content(objectMapper.writeValueAsString(statusDto))
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isServiceUnavailable())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void updateOrderStatus_AnonymousUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        OrderStatus orderStatus = OrderStatus.CANCELED;
        statusDto = new StatusDto(orderStatus.name(), orderStatus.getStatus());

        ResultActions response = mockMvc.perform(patch("/api/v1/admin/orders/{orderId}", ORDER_ID)
                .content(objectMapper.writeValueAsString(statusDto))
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void updateOrderStatus_AuthUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        OrderStatus orderStatus = OrderStatus.CANCELED;
        statusDto = new StatusDto(orderStatus.name(), orderStatus.getStatus());

        ResultActions response = mockMvc.perform(patch("/api/v1/admin/orders/{orderId}", ORDER_ID)
                .content(objectMapper.writeValueAsString(statusDto))
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    private MockMultipartFile getAddUpdateProductDtoJsonFile(AddUpdateProductDto addUpdateProductDto) throws JsonProcessingException {
        String addUpdateProductDtoJson = objectMapper.writeValueAsString(addUpdateProductDto);
        return new MockMultipartFile("product", null,
                "application/json", addUpdateProductDtoJson.getBytes());
    }

    private ResponseStatusExceptionDto getResponseStatusExceptionDto(HttpStatus httpStatus, String msg) {
        return ResponseStatusExceptionDto
                .builder()
                .error(httpStatus.name())
                .status(httpStatus.value())
                .message(msg)
                .build();
    }
}
