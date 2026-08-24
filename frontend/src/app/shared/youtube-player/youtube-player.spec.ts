import { ComponentFixture, TestBed } from '@angular/core/testing';

import { YoutubePlayer } from './youtube-player';

describe('YoutubePlayer', () => {
  let fixture: ComponentFixture<YoutubePlayer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [YoutubePlayer]
    }).compileComponents();

    fixture = TestBed.createComponent(YoutubePlayer);
    fixture.componentRef.setInput('titulo', 'Música de teste');
  });

  it('deve montar somente o iframe oficial para um ID válido', () => {
    fixture.componentRef.setInput('videoId', 'dQw4w9WgXcQ');
    fixture.detectChanges();

    const iframe = fixture.nativeElement
      .querySelector('iframe') as HTMLIFrameElement | null;

    expect(iframe).not.toBeNull();
    expect(iframe?.src)
      .toBe('https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ');
    expect(iframe?.title).toBe('Player do YouTube para Música de teste');
    expect(iframe?.getAttribute('referrerpolicy'))
      .toBe('strict-origin-when-cross-origin');
  });

  it('não deve criar iframe para um identificador inválido', () => {
    fixture.componentRef.setInput('videoId', 'javascript:alert(1)');
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('iframe')).toBeNull();
  });
});
