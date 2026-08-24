const VIDEO_ID_PATTERN = /^[A-Za-z0-9_-]{11}$/;

const YOUTUBE_HOSTS = new Set([
  'youtube.com',
  'www.youtube.com',
  'm.youtube.com',
  'music.youtube.com',
  'youtube-nocookie.com',
  'www.youtube-nocookie.com'
]);

const SHORT_LINK_HOSTS = new Set([
  'youtu.be',
  'www.youtu.be'
]);

const PATH_PREFIXES = new Set([
  'embed',
  'shorts',
  'live'
]);

export function extrairYoutubeVideoId(
  link: string | null | undefined
): string | null {
  const valor = link?.trim();

  if (!valor) {
    return null;
  }

  try {
    const url = new URL(valor);

    if (url.protocol !== 'https:' && url.protocol !== 'http:') {
      return null;
    }

    const host = url.hostname.toLocaleLowerCase('en-US');
    let videoId: string | null = null;

    if (SHORT_LINK_HOSTS.has(host)) {
      videoId = segmentosDoCaminho(url.pathname)[0] ?? null;
    } else if (YOUTUBE_HOSTS.has(host)) {
      videoId = extrairDoYoutube(url);
    }

    return videoId && VIDEO_ID_PATTERN.test(videoId)
      ? videoId
      : null;
  } catch {
    return null;
  }
}

export function criarLinkYoutube(videoId: string | null | undefined): string {
  return videoId && VIDEO_ID_PATTERN.test(videoId)
    ? `https://www.youtube.com/watch?v=${videoId}`
    : '';
}

export function criarLinkEmbedYoutube(
  videoId: string | null | undefined
): string | null {
  return videoId && VIDEO_ID_PATTERN.test(videoId)
    ? `https://www.youtube-nocookie.com/embed/${videoId}`
    : null;
}

function extrairDoYoutube(url: URL): string | null {
  const segmentos = segmentosDoCaminho(url.pathname);

  if (segmentos.length === 1 && segmentos[0] === 'watch') {
    return url.searchParams.get('v');
  }

  if (segmentos.length === 2 && PATH_PREFIXES.has(segmentos[0])) {
    return segmentos[1];
  }

  return null;
}

function segmentosDoCaminho(caminho: string): string[] {
  return caminho
    .split('/')
    .filter(segmento => segmento.length > 0);
}
