const youtubeHosts = new Set(["youtube.com", "www.youtube.com", "m.youtube.com", "www.youtube-nocookie.com", "youtube-nocookie.com"]);
const videoIdPattern = /^[A-Za-z0-9_-]{11}$/;

function mediaAddress(value) {
  try {
    const url = new URL(value);
    if (["https:", "http:"].includes(url.protocol) && !url.username && !url.password) return url;
  } catch {

  }
  return null;
}

export function youtubeVideo(value) {
  const url = mediaAddress(value);
  if (!url || url.port) return null;

  let id;
  if (url.hostname === "youtu.be") {
    id = /^\/([^/]+)\/?$/.exec(url.pathname)?.[1];
  } else if (youtubeHosts.has(url.hostname)) {
    id = url.pathname === "/watch"
      ? url.searchParams.get("v")
      : /^\/(?:embed|shorts|live)\/([^/]+)\/?$/.exec(url.pathname)?.[1];
  }

  if (!id || !videoIdPattern.test(id)) return null;
  return {
    watchUrl: "https://www.youtube.com/watch?v=" + id,
    embedUrl: "https://www.youtube-nocookie.com/embed/" + id,
    thumbnailUrl: "https://i.ytimg.com/vi/" + id + "/hqdefault.jpg",
  };
}

export function externalMediaUrl(value) {
  return mediaAddress(value)?.href;
}

export function cloudinaryVideo(value) {
  try {
    const url = new URL(value);
    return url.protocol === "https:" && url.hostname === "res.cloudinary.com" && !url.username && !url.password && !url.port && !url.search && !url.hash && /^\/[A-Za-z0-9_-]+\/video\/upload\/[A-Za-z0-9_/.-]+\.(mp4|webm|mov)$/.test(url.pathname);
  } catch { return false; }
}
