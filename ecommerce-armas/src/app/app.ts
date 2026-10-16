import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule, MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { ARSENAL_ICONS } from './icons';

@Component({
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatButtonModule, MatIconModule],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  constructor(icons: MatIconRegistry, sanitizer: DomSanitizer) {
    icons.addSvgIconSetLiteral(sanitizer.bypassSecurityTrustHtml(ARSENAL_ICONS));
  }
  readonly menuAberto = signal(false);
  fecharMenu(): void {
    this.menuAberto.set(false);
  }
}
