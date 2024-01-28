package com.kopchak.worldoftoys.exception.exception.image.ext;

import com.kopchak.worldoftoys.exception.exception.image.ImageException;

public class ImageExceedsMaxSizeException extends ImageException {
    public ImageExceedsMaxSizeException(String message) {
        super(message);
    }
}
