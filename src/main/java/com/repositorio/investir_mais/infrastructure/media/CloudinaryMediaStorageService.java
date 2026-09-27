package com.repositorio.investir_mais.infrastructure.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.repositorio.investir_mais.domain.education.service.interfaces.MediaStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * Implementação real de MediaStorageService, usando o SDK do Cloudinary.
 * É essa classe (e não a interface) que o Spring injeta em
 * EducationalContentCommandService — precisa estar anotada com @Service
 * para virar um bean gerenciável.
 */
@Service
public class CloudinaryMediaStorageService implements MediaStorageService {

    private final Cloudinary cloudinary;
    private final String apiKey;
    private final String apiSecret;
    private final String cloudName;
    private final String uploadPreset;

    public CloudinaryMediaStorageService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret,
            @Value("${cloudinary.upload-preset}") String uploadPreset
    ) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.uploadPreset = uploadPreset;
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    /**
     * Gera a assinatura que o front usa para fazer o upload direto pro
     * Cloudinary (signed upload), sem o arquivo passar pelo nosso back.
     * O upload_preset já deve ter o limite de 100MB e os formatos
     * permitidos (MP4, MOV, WebM) configurados no painel do Cloudinary.
     */
    @Override
    public UploadSignature generateUploadSignature() {
        long timestamp = System.currentTimeMillis() / 1000L;

        Map<String, Object> paramsToSign = ObjectUtils.asMap(
                "timestamp", timestamp,
                "upload_preset", uploadPreset
        );

        String signature = cloudinary.apiSignRequest(paramsToSign, apiSecret);

        return new UploadSignature(signature, timestamp, apiKey, cloudName, uploadPreset);
    }

    /**
     * Remove o vídeo do Cloudinary, usado na deleção física do
     * EducationalContent. resource_type "video" é obrigatório aqui —
     * o Cloudinary trata vídeo e imagem como tipos de recurso diferentes.
     */
    @Override
    public void deleteMedia(String mediaUrl) {
        String publicId = extractPublicId(mediaUrl);
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "video"));
        } catch (Exception e) {
            throw new RuntimeException("Falha ao remover arquivo do Cloudinary: " + mediaUrl, e);
        }
    }

    /**
     * Extrai o public_id a partir da URL retornada pelo Cloudinary, ex:
     * https://res.cloudinary.com/{cloud}/video/upload/v1234567/pasta/nome.mp4
     * -> public_id = pasta/nome
     */
    private String extractPublicId(String mediaUrl) {
        String marker = "/upload/";
        String afterUpload = mediaUrl.substring(mediaUrl.indexOf(marker) + marker.length());
        String withoutVersion = afterUpload.replaceFirst("^v\\d+/", "");
        int lastDot = withoutVersion.lastIndexOf('.');
        return lastDot > 0 ? withoutVersion.substring(0, lastDot) : withoutVersion;
    }
}
