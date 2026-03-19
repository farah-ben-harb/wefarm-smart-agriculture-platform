package utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.Transformation;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CloudinaryService {
    private static final String CLOUD_NAME = "dm40etgiq";
    private static final String API_KEY = "386162722595832";
    private static final String API_SECRET = "z7pGxUMBUvJAbxWZH74nf0UvGf4";
    private static final String PROFILE_FOLDER = "profile_pictures";

    private final Cloudinary cloudinary;

    public CloudinaryService() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", CLOUD_NAME,
                "api_key", API_KEY,
                "api_secret", API_SECRET,
                "secure", true
        ));
    }

    /**
     * Upload une image dans le dossier profile_pictures avec optimisation
     * @param imageFile Fichier image à uploader
     * @param userId ID de l'utilisateur pour le nommage
     * @return URL sécurisée de l'image
     */
    public String uploadProfileImage(File imageFile, String userId) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(imageFile,
                ObjectUtils.asMap(
                        "folder", PROFILE_FOLDER,
                        "public_id", "user_" + userId,
                        "transformation", new Transformation()
                                .width(300)
                                .height(300)
                                .crop("fill")
                                .gravity("face")
                                .quality("auto")
                                .fetchFormat("auto"),
                        "overwrite", true,
                        "resource_type", "image"
                ));

        return (String) uploadResult.get("secure_url");
    }

    /**
     * Supprime une image du stockage Cloudinary
     * @param imageUrl URL de l'image à supprimer
     */
    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) return;

        // Extraire le public_id de l'URL
        String[] parts = imageUrl.split("/");
        String publicId = parts[parts.length-1].split("\\.")[0];

        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    /**
     * Met à jour une image existante
     */
    public String updateProfileImage(File newImage, String oldImageUrl, String userId) throws IOException {
        // Supprimer l'ancienne image
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            deleteImage(oldImageUrl);
        }
        // Uploader la nouvelle
        return uploadProfileImage(newImage, userId);
    }
}