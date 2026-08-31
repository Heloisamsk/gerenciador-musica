import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VinylMark } from './vinyl-mark';

describe('VinylMark', () => {
  let fixture: ComponentFixture<VinylMark>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VinylMark]
    }).compileComponents();

    fixture = TestBed.createComponent(VinylMark);
    fixture.detectChanges();
  });

  it('deve desenhar o disco como um svg decorativo', () => {
    const svg = fixture.nativeElement.querySelector('svg');

    expect(svg).not.toBeNull();
    expect(svg?.getAttribute('aria-hidden')).toBe('true');
  });
});
