import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  constructor() {
    const hasWindow = typeof window !== 'undefined';
    const hasDocument = typeof document !== 'undefined';
    if (!hasWindow || !hasDocument) {
      return;
    }

    const saved = window.localStorage.getItem('sgi-lib-theme');
    if (saved === 'dark') {
      document.body.classList.add('theme-dark');
    }
  }
}
