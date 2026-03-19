package utils;

import com.cloudinary.Cloudinary;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryUtil {
    private static Cloudinary cloudinary;
    private static CloudinaryUtil instance;
    private static final String CLOUD_NAME = "dfn9lk1jj";
    private static final String API_KEY = "138481669269475";
    private static final String API_SECRET = "di9j195pTlL_y6XvkgBhUt8qeaw";

    private CloudinaryUtil() {
        initCloudinary();
    }

    public static CloudinaryUtil getInstance() {
        if (instance == null) {
            instance = new CloudinaryUtil();
        }
        return instance;
    }

    private void initCloudinary() {
        try {
            // Initialize Cloudinary using URL format
            String cloudinaryUrl = "cloudinary://" + API_KEY + ":" + API_SECRET + "@" + CLOUD_NAME;
            System.out.println("Initializing Cloudinary with URL: " + cloudinaryUrl);
            cloudinary = new Cloudinary(cloudinaryUrl);
            
            // Verify the configuration
            @SuppressWarnings("unchecked")
            Map<String, Object> config = cloudinary.config.asMap();
            System.out.println("Cloud name from config: " + config.get("cloud_name"));
            System.out.println("Cloudinary initialization successful");
        } catch (Exception e) {
            System.err.println("Cloudinary initialization error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Cloudinary: " + e.getMessage());
        }
    }

    public String uploadImage(File imageFile) throws IOException {
        if (imageFile == null || !imageFile.exists()) {
            throw new IOException("Image file is null or does not exist");
        }

        try {
            System.out.println("Attempting to upload image: " + imageFile.getAbsolutePath());
            System.out.println("File size: " + imageFile.length() + " bytes");
            
            // Create a more detailed upload request
            Map<String, Object> options = new HashMap<>();
            options.put("resource_type", "image");  // Explicitly set resource type to image
            options.put("folder", "products");  // Optional: organize uploads in a folder
            options.put("use_filename", true);  // Use original filename as basis for public_id
            options.put("unique_filename", true);  // Ensure filename uniqueness

            System.out.println("Upload options: " + options);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(imageFile, options);
            
            System.out.println("Upload response: " + uploadResult);
            
            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null || secureUrl.isEmpty()) {
                throw new IOException("Failed to get secure URL from Cloudinary response");
            }
            
            System.out.println("Upload successful. Secure URL: " + secureUrl);
            return secureUrl;
        } catch (IOException e) {
            System.err.println("Upload error: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage());
        }
    }

    public String uploadImage(File imageFile, String publicId) throws IOException {
        if (imageFile == null || !imageFile.exists()) {
            throw new IOException("Image file is null or does not exist");
        }

        try {
            // Create a basic upload request with public ID
            Map<String, Object> options = new HashMap<>();
            options.put("resource_type", "image");  // Explicitly set resource type to image
            options.put("public_id", publicId);

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(imageFile, options);
            String secureUrl = (String) uploadResult.get("secure_url");
            
            if (secureUrl == null || secureUrl.isEmpty()) {
                throw new IOException("Failed to get secure URL from Cloudinary response");
            }
            
            return secureUrl;
        } catch (IOException e) {
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage());
        }
    }

    public void deleteImage(String publicId) throws IOException {
        if (publicId == null || publicId.trim().isEmpty()) {
            return; // Skip deletion if publicId is null or empty
        }

        try {
            Map<String, Object> options = new HashMap<>();
            options.put("resource_type", "image");  // Explicitly set resource type to image
            options.put("invalidate", true);

            cloudinary.uploader().destroy(publicId, options);
        } catch (IOException e) {
            throw new IOException("Failed to delete image from Cloudinary: " + e.getMessage());
        }
    }

    public String getPublicIdFromUrl(String url) {
        if (url == null || !url.contains("cloudinary.com")) {
            return null;
        }
        try {
            // Extract the path after /upload/
            int uploadIndex = url.indexOf("/upload/") + 8;
            String pathAfterUpload = url.substring(uploadIndex);
            
            // Remove any transformation parameters
            if (pathAfterUpload.contains("/")) {
                pathAfterUpload = pathAfterUpload.substring(pathAfterUpload.indexOf("/") + 1);
            }
            
            // Remove file extension and any query parameters
            int extensionIndex = pathAfterUpload.lastIndexOf(".");
            if (extensionIndex != -1) {
                pathAfterUpload = pathAfterUpload.substring(0, extensionIndex);
            }
            
            // Remove any query parameters
            int queryIndex = pathAfterUpload.indexOf("?");
            if (queryIndex != -1) {
                pathAfterUpload = pathAfterUpload.substring(0, queryIndex);
            }
            
            return pathAfterUpload;
        } catch (Exception e) {
            System.err.println("Failed to extract public ID from URL: " + e.getMessage());
            return null;
        }
    }
} 