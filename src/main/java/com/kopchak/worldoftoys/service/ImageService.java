package com.kopchak.worldoftoys.service;

public interface ImageService {
    byte[] compressImage(byte[] data);
    byte[] decompressImage(byte[] data);
}
