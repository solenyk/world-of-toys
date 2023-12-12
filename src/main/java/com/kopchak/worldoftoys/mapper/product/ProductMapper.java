package com.kopchak.worldoftoys.mapper.product;

import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.UpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.category.AdminProductCategoryDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.exception.exception.CategoryNotFoundException;
import com.kopchak.worldoftoys.exception.exception.ImageException;
import com.kopchak.worldoftoys.model.image.Image;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.product.category.AgeCategory;
import com.kopchak.worldoftoys.model.product.category.BrandCategory;
import com.kopchak.worldoftoys.model.product.category.OriginCategory;
import com.kopchak.worldoftoys.repository.product.ProductCategoryRepository;
import com.kopchak.worldoftoys.service.ImageService;
import org.mapstruct.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {ImageMapper.class, ProductCategoryMapper.class, OriginCategory.class})
public abstract class ProductMapper {
    public abstract ProductDto toProductDto(Product product);

    public abstract AdminProductDto toAdminProductDto(Product product);

    @Mapping(target = "id", source = "productId")
    @Mapping(target = "name", source = "updateProductDto.name")
    @Mapping(target = "originCategory", ignore = true)
    @Mapping(target = "brandCategory", ignore = true)
    @Mapping(target = "ageCategories", ignore = true)
    public abstract Product toProduct(UpdateProductDto updateProductDto, Integer productId,
                                      @Context ProductCategoryRepository categoryRepository, MultipartFile mainImageFile,
                                      List<MultipartFile> imageFilesList, @Context ImageService imageService)
            throws ImageException, CategoryNotFoundException;

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
                                  List<MultipartFile> imageFilesList, @Context ImageService imageService)
            throws CategoryNotFoundException, ImageException {
        setProductCategories(product, updateProductDto, categoryRepository);
        setProductImages(product, mainImageFile, imageFilesList, imageService);
    }

    protected void setProductCategories(Product product, UpdateProductDto updateProductDto,
                                        @Context ProductCategoryRepository categoryRepository)
            throws CategoryNotFoundException {
        product.setBrandCategory(categoryRepository.findById(updateProductDto.brandCategory().id(),
                BrandCategory.class));
        product.setOriginCategory(categoryRepository.findById(updateProductDto.originCategory().id(),
                OriginCategory.class));

        Set<AgeCategory> ageCategories = new LinkedHashSet<>();
        for (AdminProductCategoryDto ageCategory : updateProductDto.ageCategories()) {
            ageCategories.add(categoryRepository.findById(ageCategory.id(), AgeCategory.class));
        }
        product.setAgeCategories(ageCategories);
    }

    protected void setProductImages(Product product, MultipartFile mainImageFile, List<MultipartFile> imageFilesList,
                                    @Context ImageService imageService) throws ImageException {
        Image mainImage = imageService.convertMultipartFileToImage(mainImageFile, product);

        Set<Image> imagesSet = new LinkedHashSet<>();
        for (MultipartFile image : imageFilesList) {
            imagesSet.add(imageService.convertMultipartFileToImage(image, product));
        }

        product.setMainImage(mainImage);
        product.setImages(imagesSet);
    }
}
