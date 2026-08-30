import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { TemaService } from './services/tema';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly temaService = inject(TemaService);
}
