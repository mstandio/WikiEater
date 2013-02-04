package org.openpano.wikieater.data;

import java.io.File;

/**
 * @author mstandio
 */
public class ImageData {

	private final String imageName;
	private final File imageFile;

	public ImageData(String imageName, File imageFile) {
		this.imageName = imageName;
		this.imageFile = imageFile;
	}

	public String getImageName() {
		return imageName;
	}

	public File getImageFile() {
		return imageFile;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || !(obj instanceof ImageData)) {
			return false;
		}
		return ((ImageData) obj).imageName.equals(imageName);
	};

	@Override
	public int hashCode() {
		return imageName.hashCode();
	}
}
