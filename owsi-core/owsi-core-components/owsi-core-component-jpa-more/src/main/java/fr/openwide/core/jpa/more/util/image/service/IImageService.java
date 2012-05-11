package fr.openwide.core.jpa.more.util.image.service;

import java.io.File;
import java.io.InputStream;

import fr.openwide.core.jpa.more.util.image.exception.ImageThumbnailGenerationException;
import fr.openwide.core.jpa.more.util.image.model.ImageInformation;
import fr.openwide.core.jpa.more.util.image.model.ImageThumbnailFormat;

public interface IImageService {
	
	void generateThumbnail(File source, File destination, ImageThumbnailFormat thumbnailFormat)
			throws ImageThumbnailGenerationException;

	ImageInformation getImageInformation(InputStream source);

	ImageInformation getImageInformation(File source);

}