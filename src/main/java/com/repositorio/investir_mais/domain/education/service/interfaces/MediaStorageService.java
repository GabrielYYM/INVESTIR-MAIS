package com.repositorio.investir_mais.domain.education.service.interfaces;
/**
 * Abstração sobre o provedor de storage de mídia (Cloudinary).
 *
 * O domínio de educação não deve depender do SDK do Cloudinary diretamente
 * — a implementação concreta (CloudinaryMediaStorageService, usando o SDK)
 * fica em infrastructure e é injetada aqui via Spring. Isso também facilita
 * trocar de provedor no futuro ou mockar em teste, sem tocar no domínio.
 */
public interface MediaStorageService {

    /**
     * Gera os dados necessários para o front fazer o signed upload direto
     * pro Cloudinary (assinatura, timestamp, api key, upload preset).
     */
    UploadSignature generateUploadSignature();

    /**
     * Remove o arquivo do Cloudinary a partir da URL/public_id armazenado.
     * Chamado na deleção física do EducationalContent.
     */
    void deleteMedia(String mediaUrl);

    record UploadSignature(
            String signature,
            long timestamp,
            String apiKey,
            String cloudName,
            String uploadPreset
    ) {
    }
}
