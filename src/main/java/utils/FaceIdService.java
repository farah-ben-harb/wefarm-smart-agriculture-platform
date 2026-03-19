package utils;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.ContentType;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FaceIdService {
    private static final String API_KEY = "aAR6v9WSJmN6641OIzYr904RfsBnwkvp"; // Remplace par ta clé API Face++
    private static final String API_SECRET = "IsGp26fUfkdvEE8_NlFoZ9XdxuZ4g6Aj"; // Remplace par ton secret API Face++
    private static final String FACE_DETECT_URL = "https://api-us.faceplusplus.com/facepp/v3/detect";
    private static final String FACE_COMPARE_URL = "https://api-us.faceplusplus.com/facepp/v3/compare";
    private static final CloudinaryService cloudinaryService = new CloudinaryService();


    public String detectFace(File imageFile) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(FACE_DETECT_URL);

            // Création du corps multipart
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("api_key", API_KEY);
            builder.addTextBody("api_secret", API_SECRET);
            builder.addBinaryBody("image_file", imageFile);

            post.setEntity(builder.build());

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String compareFaces(File image1, File image2) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(FACE_COMPARE_URL);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("api_key", API_KEY);
            builder.addTextBody("api_secret", API_SECRET);
            builder.addBinaryBody("image_file1", image1);
            builder.addBinaryBody("image_file2", image2);

            post.setEntity(builder.build());

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Upload une image dans Cloudinary, puis renvoie l'URL de l'image téléchargée
     * @param imageFile Fichier de l'image à télécharger
     * @param userId ID utilisateur pour nommer l'image
     * @return URL de l'image téléchargée sur Cloudinary
     * @throws IOException
     */
    public String uploadImageToCloudinary(File imageFile, String userId) throws IOException {
        return cloudinaryService.uploadProfileImage(imageFile, userId);
    }

    /**
     * Supprimer une image depuis Cloudinary
     * @param imageUrl URL de l'image à supprimer
     * @throws IOException
     */
    public void deleteImageFromCloudinary(String imageUrl) throws IOException {
        cloudinaryService.deleteImage(imageUrl);
    }
}
