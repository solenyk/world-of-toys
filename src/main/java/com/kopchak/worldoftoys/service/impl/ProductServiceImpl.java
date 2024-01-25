package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.image.Image;
import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.category.AgeCategory;
import com.kopchak.worldoftoys.domain.product.category.BrandCategory;
import com.kopchak.worldoftoys.domain.product.category.OriginCategory;
import com.kopchak.worldoftoys.dto.admin.category.CategoryIdDto;
import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductsPageDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.dto.product.image.ImageDto;
import com.kopchak.worldoftoys.exception.exception.category.CategoryNotFoundException;
import com.kopchak.worldoftoys.exception.exception.image.ImageException;
import com.kopchak.worldoftoys.exception.exception.image.ext.ImageDecompressionException;
import com.kopchak.worldoftoys.exception.exception.product.DuplicateProductNameException;
import com.kopchak.worldoftoys.exception.exception.product.ProductNotFoundException;
import com.kopchak.worldoftoys.mapper.product.ProductMapper;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import com.kopchak.worldoftoys.repository.specifications.impl.ProductSpecificationsImpl;
import com.kopchak.worldoftoys.service.CategoryService;
import com.kopchak.worldoftoys.service.ImageService;
import com.kopchak.worldoftoys.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    //    private final CategoryRepository categoryRepository;
    private final ProductSpecificationsImpl productSpecifications;
    private final ProductMapper productMapper;
    private final ImageService imageService;

    @Override
    public FilteredProductsPageDto getFilteredProductsPage(int page, int size, String productName, BigDecimal minPrice,
                                                           BigDecimal maxPrice, List<String> originCategories,
                                                           List<String> brandCategories, List<String> ageCategories,
                                                           String priceSortOrder) {
        Page<Product> productPage = getFilteredProductPage(page, size, productName, minPrice, maxPrice,
                originCategories, brandCategories, ageCategories, priceSortOrder, null);
        return productMapper.toFilteredProductsPageDto(productPage);
    }

    @Override
    public ProductDto getProductBySlug(String productSlug) throws ProductNotFoundException, ImageDecompressionException {
        Optional<Product> productOptional = productRepository.findBySlug(productSlug);
        if (productOptional.isEmpty()) {
            String errMsg = String.format("The product with slug: %s is not found.", productSlug);
            log.error(errMsg);
            throw new ProductNotFoundException(errMsg);
        }
        Product product = productOptional.get();
        Image mainImage = product.getMainImage();
        Set<Image> images = product.getImages();
        ImageDto mainImageDto = mainImage == null ? null : imageService.generateDecompressedImageDto(mainImage);
        List<ImageDto> imageDtoList = (images == null || images.isEmpty()) ? new ArrayList<>() :
                getDecompressedProductImageDtoList(product.getImages(), mainImage);
        log.info("Fetched product by slug: '{}'", productSlug);
        return productMapper.toProductDto(product, mainImageDto, imageDtoList);
    }

    @Override
    public AdminProductsPageDto getAdminProductsPage(int page, int size, String productName, BigDecimal minPrice,
                                                     BigDecimal maxPrice, List<String> originCategories,
                                                     List<String> brandCategories, List<String> ageCategories,
                                                     String priceSortOrder, String availability) {
        Page<Product> productPage = getFilteredProductPage(page, size, productName, minPrice, maxPrice,
                originCategories, brandCategories, ageCategories, priceSortOrder, availability);
        return productMapper.toAdminFilteredProductsPageDto(productPage);
    }

    @Override
    public AdminProductDto getProductById(Integer productId) throws ProductNotFoundException, ImageDecompressionException {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException(String.format("The product with id: %d is not found.", productId)));
        Image mainImage = product.getMainImage();
        ImageDto mainImageDto = mainImage == null ? null : imageService.generateDecompressedImageDto(mainImage);
        List<ImageDto> imageDtoList = getDecompressedProductImageDtoList(product.getImages(), mainImage);
        log.info("Fetched product by id: '{}'", productId);
        return productMapper.toAdminProductDto(product, mainImageDto, imageDtoList);
    }

    @Override
    @Transactional
    public void updateProduct(Integer productId, AddUpdateProductDto addUpdateProductDto, MultipartFile mainImageFile,
                              List<MultipartFile> imageFilesList) throws ProductNotFoundException, ImageException,
            DuplicateProductNameException, CategoryNotFoundException {
        if (productRepository.findById(productId).isEmpty()) {
            throw new ProductNotFoundException(String.format("The product with id: %d is not found.", productId));
        }
        String productName = addUpdateProductDto.name();
        Optional<Product> productOptional = productRepository.findByName(productName);
        if (productOptional.isPresent() && !productOptional.get().getId().equals(productId)) {
            throw new DuplicateProductNameException(
                    String.format("The product with name: %s is already exist", productName));
        }
        Product product = buildProductFromDtoAndImages(addUpdateProductDto, mainImageFile, imageFilesList);
        product.setId(productId);
        productRepository.save(product);
        log.info("The product with id: {} was successfully updated", productId);
    }

    @Override
    public void createProduct(AddUpdateProductDto addUpdateProductDto, MultipartFile mainImageFile,
                              List<MultipartFile> imageFileList)
            throws DuplicateProductNameException, CategoryNotFoundException, ImageException {
        String productName = addUpdateProductDto.name();
        if (productRepository.findByName(productName).isPresent()) {
            throw new DuplicateProductNameException(String.format("The product with name: %s is already exist", productName));
        }
        Product product = buildProductFromDtoAndImages(addUpdateProductDto, mainImageFile, imageFileList);
        productRepository.save(product);
        log.info("The product with name: {} was successfully saved", product.getName());
    }

    private Page<Product> getFilteredProductPage(int page, int size, String productName, BigDecimal minPrice,
                                                 BigDecimal maxPrice, List<String> originCategories,
                                                 List<String> brandCategories, List<String> ageCategories,
                                                 String priceSortOrder, String availability) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Product> spec = productSpecifications.filterByAllCriteria(productName, minPrice,
                maxPrice, originCategories, brandCategories, ageCategories, priceSortOrder, availability);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        log.info("Fetched filtered products - Page: {}, Size: {}, Product Name: '{}', Min Price: {}, Max Price: {}, " +
                        "Origin Categories: {}, Brand Categories: {}, Age Categories: {}, Availability: {}, " +
                        "Price Sort Order: '{}'", page, size, productName, minPrice, maxPrice, originCategories,
                brandCategories, ageCategories, availability, priceSortOrder);
        return productPage;
    }

    private Product buildProductFromDtoAndImages(AddUpdateProductDto addUpdateProductDto, MultipartFile mainImageFile,
                                                 List<MultipartFile> imageFilesList)
            throws CategoryNotFoundException, ImageException {
        Product product = productMapper.toProduct(addUpdateProductDto);
        setProductCategories(product, addUpdateProductDto);
        setProductImages(product, mainImageFile, imageFilesList);
        return product;
    }

    private void setProductCategories(Product product, AddUpdateProductDto productDto) throws CategoryNotFoundException {
        product.setBrandCategory(categoryService.findCategoryById(productDto.brandCategory().id(), BrandCategory.class));
        product.setOriginCategory(categoryService.findCategoryById(productDto.originCategory().id(), OriginCategory.class));
        Set<AgeCategory> ageCategories = new LinkedHashSet<>();
        for (CategoryIdDto ageCategory : productDto.ageCategories()) {
            ageCategories.add(categoryService.findCategoryById(ageCategory.id(), AgeCategory.class));
        }
        product.setAgeCategories(ageCategories);
    }

    private void setProductImages(Product product, MultipartFile mainImageFile, List<MultipartFile> imageFilesList)
            throws ImageException {
        Image mainImage = mainImageFile == null ? null : imageService.convertMultipartFileToImage(mainImageFile, product);
        Set<Image> imagesSet = new LinkedHashSet<>();
        if (imageFilesList != null) {
            for (MultipartFile image : imageFilesList) {
                imagesSet.add(imageService.convertMultipartFileToImage(image, product));
            }
        }
        product.setMainImage(mainImage);
        product.setImages(imagesSet);
    }

    private List<ImageDto> getDecompressedProductImageDtoList(Set<Image> images, Image mainImage)
            throws ImageDecompressionException {
        List<ImageDto> imageDtoList = new ArrayList<>();
        for (Image image : images) {
            if (!image.equals(mainImage)) {
                imageDtoList.add(imageService.generateDecompressedImageDto(image));
            }
        }
        return imageDtoList;
    }
}
