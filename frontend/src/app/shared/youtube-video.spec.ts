import {
  criarLinkEmbedYoutube,
  criarLinkYoutube,
  extrairYoutubeVideoId
} from './youtube-video';

describe('youtube-video', () => {
  const videoId = 'dQw4w9WgXcQ';

  it.each([
    `https://www.youtube.com/watch?v=${videoId}`,
    `https://youtu.be/${videoId}?si=teste`,
    `https://youtube.com/embed/${videoId}`,
    `https://youtube.com/shorts/${videoId}`,
    `https://music.youtube.com/watch?list=teste&v=${videoId}`
  ])('deve extrair o ID de %s', link => {
    expect(extrairYoutubeVideoId(link)).toBe(videoId);
  });

  it.each([
    'https://example.com/watch?v=dQw4w9WgXcQ',
    'javascript:alert(1)',
    'https://youtube.com/watch?v=curto',
    'link inválido'
  ])('deve rejeitar %s', link => {
    expect(extrairYoutubeVideoId(link)).toBeNull();
  });

  it('deve criar apenas links a partir de um ID válido', () => {
    expect(criarLinkYoutube(videoId))
      .toBe(`https://www.youtube.com/watch?v=${videoId}`);
    expect(criarLinkEmbedYoutube(videoId))
      .toBe(`https://www.youtube-nocookie.com/embed/${videoId}`);
    expect(criarLinkYoutube('inválido')).toBe('');
    expect(criarLinkEmbedYoutube('inválido')).toBeNull();
  });

  it('deve tratar valor ausente como link opcional', () => {
    expect(extrairYoutubeVideoId(null)).toBeNull();
    expect(extrairYoutubeVideoId('   ')).toBeNull();
  });
});
