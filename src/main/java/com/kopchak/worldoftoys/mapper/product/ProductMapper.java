package com.kopchak.worldoftoys.mapper.product;

import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.UpdateProductDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.exception.CategoryNotFoundException;
import com.kopchak.worldoftoys.model.image.Image;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.product.category.AgeCategory;
import com.kopchak.worldoftoys.model.product.category.BrandCategory;
import com.kopchak.worldoftoys.model.product.category.OriginCategory;
import com.kopchak.worldoftoys.repository.product.ProductCategoryRepository;
import com.kopchak.worldoftoys.repository.product.image.ImageRepository;
import com.kopchak.worldoftoys.service.ImageService;
import org.apache.commons.lang3.RandomStringUtils;
import org.mapstruct.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {ImageMapper.class, ProductCategoryMapper.class, OriginCategory.class})
public abstract class ProductMapper {
    public abstract ProductDto toProductDto(Product product);

    public abstract AdminProductDto toAdminProductDto(Product product);

    @Mapping(target = "name", source = "updateProductDto.name")
    @Mapping(target = "originCategory", ignore = true)
    @Mapping(target = "brandCategory", ignore = true)
    @Mapping(target = "ageCategories", ignore = true)
    public abstract Product toProduct(UpdateProductDto updateProductDto,
                                      @Context ProductCategoryRepository categoryRepository, MultipartFile mainImageFile,
                                      List<MultipartFile> imagesFile, @Context ImageRepository imageRepository,
                                      @Context ImageService imageService) throws IOException;

    protected abstract List<FilteredProductDto> toFilteredProductDtoList(List<Product> products);

    protected abstract List<AdminFilteredProductDto> toAdminFilteredProductDtoList(List<Product> products);

    public FilteredProductsPageDto toFilteredProductsPageDto(Page<Product> productPage) {
        return new FilteredProductsPageDto(toFilteredProductDtoList(productPage.getContent()),
                productPage.getTotalElements(), productPage.getTotalPages());
    }

    public AdminFilteredProductsPageDto toAdminFilteredProductsPageDto(Page<Product> productPage) {
        return new AdminFilteredProductsPageDto(toAdminFilteredProductDtoList(productPage.getContent()),
                productPage.getTotalElements(), productPage.getTotalPages());
    }


    @AfterMapping
    protected void afterToProduct(@MappingTarget Product product, UpdateProductDto updateProductDto,
                                  @Context ProductCategoryRepository categoryRepository, MultipartFile mainImageFile,
                                  List<MultipartFile> imagesFile, @Context ImageRepository imageRepository,
                                  @Context ImageService imageService) throws IOException {
        BrandCategory brandCategory = (BrandCategory) categoryRepository.findById(updateProductDto.brandCategory().id(),
                BrandCategory.class).orElseThrow(() -> new CategoryNotFoundException("Brand category not found"));
        product.setBrandCategory(brandCategory);

        OriginCategory originCategory = (OriginCategory) categoryRepository.findById(updateProductDto.originCategory().id(),
                OriginCategory.class).orElseThrow(() -> new CategoryNotFoundException("Origin category not found"));
        product.setOriginCategory(originCategory);

        Set<AgeCategory> ageCategories = updateProductDto.ageCategories().stream()
                .map(category -> (AgeCategory) categoryRepository.findById(category.id(), AgeCategory.class)
                        .orElseThrow(() -> new CategoryNotFoundException("Age category not found")))
                .collect(Collectors.toSet());
        product.setAgeCategories(ageCategories);

        Image productMainImage = toImage(mainImageFile, product, imageRepository, imageService);
        Set<Image> productImages = new HashSet<>();
        for (MultipartFile image : imagesFile) {
            productImages.add(toImage(image, product, imageRepository, imageService));
        }

        product.setMainImage(productMainImage);
        product.setImages(productImages);
    }

    protected Image toImage(MultipartFile multipartFile, Product product,
                            @Context ImageRepository imageRepository,
                            @Context ImageService imageService) throws IOException {
        Image image = imageRepository
                .findByNameAndProduct_Id(multipartFile.getOriginalFilename(), product.getId())
                .orElse(
                        Image
                                .builder()
                                .name(Objects.requireNonNull(multipartFile.getOriginalFilename())
                                        .concat(RandomStringUtils.randomAlphanumeric(4)))
                                .type(multipartFile.getContentType())
                                .product(product)
                                .build()
                );
        image.setImage(imageService.compressImage(multipartFile.getBytes()));
        return image;
    }

}
