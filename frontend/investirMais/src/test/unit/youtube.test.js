import { describe, expect, it } from "vitest";
import { externalMediaUrl, youtubeVideo } from "../../services/youtube.js";

describe("endereços de vídeo", () => {
  it.each([
    "https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=12",
    "https://youtube.com/watch?v=dQw4w9WgXcQ",
    "http://m.youtube.com/watch?v=dQw4w9WgXcQ",
    "https://youtu.be/dQw4w9WgXcQ?si=test",
    "https://youtu.be/dQw4w9WgXcQ/",
    "https://www.youtube.com/shorts/dQw4w9WgXcQ",
    "https://www.youtube.com/live/dQw4w9WgXcQ/",
    "https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ",
    "https://youtube-nocookie.com/embed/dQw4w9WgXcQ/",
  ])("normaliza %s para reprodução e miniatura HTTPS", (value) => {
    expect(youtubeVideo(value)).toEqual({
      watchUrl: "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      embedUrl: "https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ",
      thumbnailUrl: "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
    });
  });

  it.each([
    undefined, null, "", "não é uma URL", "/watch?v=dQw4w9WgXcQ",
    "javascript:alert(1)", "data:text/html,test", "ftp://youtube.com/watch?v=dQw4w9WgXcQ",
    "https://youtube.com.evil.test/watch?v=dQw4w9WgXcQ",
    "https://evil.test/youtube.com/watch?v=dQw4w9WgXcQ",
    "https://www.youtube.com:8443/watch?v=dQw4w9WgXcQ",
    "https://user:password@youtube.com/watch?v=dQw4w9WgXcQ",
    "https://user@youtube.com/watch?v=dQw4w9WgXcQ",
    "https://youtube.com/watch", "https://youtube.com/playlist?list=test",
    "https://youtu.be/short", "https://youtu.be/dQw4w9WgXcQ/extra",
    "https://youtube.com/embed/dQw4w9WgXcQ/extra",
    "https://youtube.com/watch?v=abcdefghij!", "https://youtube.com/watch?v=dQw4w9WgXcQQ",
  ])("recusa vídeo inválido ou origem indevida: %s", (value) => {
    expect(youtubeVideo(value)).toBeNull();
  });

  it("preserva links HTTP de registros antigos sem permitir protocolos executáveis", () => {
    expect(externalMediaUrl("https://videos.example.test/aula?a=1")).toBe("https://videos.example.test/aula?a=1");
    expect(externalMediaUrl("http://videos.example.test")).toBe("http://videos.example.test/");
    expect(externalMediaUrl("javascript:alert(1)")).toBeUndefined();
    expect(externalMediaUrl("https://user:secret@videos.example.test")).toBeUndefined();
  });
});
