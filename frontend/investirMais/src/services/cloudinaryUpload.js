/**
 * Faz o upload direto pro Cloudinary usando a assinatura gerada pelo back
 * (POST /api/education/contents/upload-signature). O arquivo NUNCA passa
 * pelo nosso back — vai direto do navegador pro Cloudinary.
 *
 * Usa XMLHttpRequest (não axios) porque precisamos do evento de progresso
 * nativo do upload (`upload.onprogress`), que é o que alimenta a barra de
 * progresso do modal "Enviar o vídeo".
 */
export function uploadVideoToCloudinary(file, signatureData, onProgress) {
  const { signature, timestamp, apiKey, cloudName, uploadPreset } = signatureData;

  const xhr = new XMLHttpRequest();

  const promise = new Promise((resolve, reject) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("api_key", apiKey);
    formData.append("timestamp", timestamp);
    formData.append("signature", signature);
    formData.append("upload_preset", uploadPreset);

    xhr.open("POST", `https://api.cloudinary.com/v1_1/${cloudName}/video/upload`);

    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable) {
        const percent = Math.round((event.loaded / event.total) * 100);
        onProgress?.(percent);
      }
    };

    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        resolve(JSON.parse(xhr.responseText)); // { secure_url, duration, ... }
      } else {
        reject(new Error(`Upload falhou (status ${xhr.status})`));
      }
    };

    xhr.onerror = () => reject(new Error("Falha de rede durante o upload"));
    xhr.onabort = () => reject(new Error("Upload cancelado"));

    xhr.send(formData);
  });

  // O chamador guarda `abort` e chama se o usuário confirmar "Sair" durante o upload.
  return { promise, abort: () => xhr.abort() };
}

/** Validação client-side, espelhando o preset configurado no Cloudinary. */
export const LIMITE_TAMANHO_MB = 100;
export const FORMATOS_PERMITIDOS = ["video/mp4", "video/quicktime", "video/webm"];

export function validarArquivoVideo(file) {
  if (!FORMATOS_PERMITIDOS.includes(file.type)) {
    return "Formato não suportado. Use MP4, MOV ou WebM.";
  }
  const tamanhoMB = file.size / (1024 * 1024);
  if (tamanhoMB > LIMITE_TAMANHO_MB) {
    return `Arquivo muito grande (${tamanhoMB.toFixed(0)}MB). O limite é ${LIMITE_TAMANHO_MB}MB.`;
  }
  return null;
}
